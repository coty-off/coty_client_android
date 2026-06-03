package coty.band.app.data

import coty.band.app.data.local.MeasurementDao
import coty.band.app.data.local.MeasurementEntity
import coty.band.app.data.remote.AnalyzeApi
import coty.band.app.data.remote.AnalyzeResult
import coty.band.app.data.remote.AuthApi
import coty.band.app.data.remote.MeasurementApi
import coty.band.app.data.remote.MeasurementResponse
import coty.band.app.data.remote.RegisterRequest
import coty.band.app.data.remote.SaveMeasurementRequest
import coty.band.app.domain.AppResult
import coty.band.app.domain.AuthRepository
import coty.band.app.domain.Measurement
import coty.band.app.domain.MeasurementRepository
import coty.band.app.domain.User
import coty.band.app.presentation.util.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): AppResult<User> =
        safeCall {
            val response = authApi.login(username, password)
            if (response.isSuccessful) {
                val token = response.body()!!.token
                val meResp = authApi.getMe()
                val me = meResp.body()
                val user = User(id = me?.id?.toString()?: "", username = me?.username ?: username, token = token)
                dataStoreManager.saveUser(token, user.id, user.username)
                AppResult.Success(user)
            } else {
                AppResult.Error(parseError(response.code()))
            }
        }

    override suspend fun register(username: String, password: String): AppResult<User> =
        safeCall {
            val response = authApi.register(RegisterRequest(username, password))
            if (response.isSuccessful) login(username, password)
            else AppResult.Error(parseError(response.code()))
        }

    override suspend fun loginWithYandex(yandexToken: String): AppResult<User> =
        AppResult.Error("Яндекс авторизация пока не поддерживается сервером")

    override fun isLoggedIn(): Flow<Boolean> = dataStoreManager.isLoggedIn
    override suspend fun logout() = dataStoreManager.clear()
}

@Singleton
class MeasurementRepositoryImpl @Inject constructor(
    private val analyzeApi: AnalyzeApi,
    private val measurementApi: MeasurementApi,
    private val measurementDao: MeasurementDao
) : MeasurementRepository {

    override suspend fun analyze(frontPhoto: File, sidePhoto: File, heightCm: Float): AppResult<Measurement> =
        safeCall {
            val frontPart  = frontPhoto.toMultipart("front_image")
            val sidePart   = sidePhoto.toMultipart("side_image")
            val heightBody = heightCm.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val taskResp = analyzeApi.analyze(frontPart, sidePart, heightBody)
            frontPhoto.delete()
            sidePhoto.delete()

            if (!taskResp.isSuccessful) return@safeCall AppResult.Error(parseError(taskResp.code()))

            val taskId = taskResp.body()!!.taskId

            repeat(30) {
                delay(2000)
                val statusResp = analyzeApi.getTaskStatus(taskId)
                if (statusResp.isSuccessful) {
                    val status = statusResp.body()!!
                    when (status.status) {
                        "done"  -> return@safeCall AppResult.Success(status.result!!.toDomain())
                        "error" -> return@safeCall AppResult.Error("Ошибка обработки на сервере")
                    }
                }
            }
            AppResult.Error("Превышено время ожидания")
        }

    override suspend fun saveMeasurement(measurement: Measurement): AppResult<Measurement> =
        safeCall {
            val response = measurementApi.saveMeasurement(
                SaveMeasurementRequest(
                    chestCm = measurement.chestCm,
                    waistCm = measurement.waistCm,
                    hipsCm = measurement.hipCm,
                    heightCm = measurement.heightCm,
                    bodyType = measurement.bodyType,
                    notes = measurement.notes
                )
            )
            if (response.isSuccessful) {
                val saved = response.body()!!.toDomain()
                measurementDao.insert(saved.toEntity())
                AppResult.Success(saved)
            } else {
                AppResult.Error(parseError(response.code()))
            }
        }

    override fun getMeasurementHistory(): Flow<List<Measurement>> =
        measurementDao.getAll().map { list -> list.map { it.toDomain() } }
}

private val fmt = DateTimeFormatter.ISO_DATE_TIME

private fun File.toMultipart(field: String): MultipartBody.Part =
    MultipartBody.Part.createFormData(field, name, asRequestBody("image/jpeg".toMediaTypeOrNull()))

private fun AnalyzeResult.toDomain() = Measurement(
    chestCm  = chestCm,
    waistCm  = waistCm,
    hipCm    = hipsCm,
    heightCm = heightCm,
    bodyType = bodyType
)

private fun MeasurementResponse.toDomain() = Measurement(
    id       = id,
    chestCm  = chestCm,
    waistCm  = waistCm,
    hipCm    = hipsCm,
    heightCm = heightCm,
    bodyType = bodyType,
    notes    = notes,
    createdAt = try { LocalDateTime.parse(createdAt, fmt) } catch (e: Exception) { LocalDateTime.now() }
)

private fun Measurement.toEntity() = MeasurementEntity(
    remoteId = id,
    chestCm  = chestCm,
    waistCm  = waistCm,
    hipCm    = hipCm,
    heightCm = heightCm,
    bodyType = bodyType,
    notes    = notes,
    createdAt = createdAt.format(fmt)
)

private fun MeasurementEntity.toDomain() = Measurement(
    id       = remoteId,
    chestCm  = chestCm,
    waistCm  = waistCm,
    hipCm    = hipCm,
    heightCm = heightCm,
    bodyType = bodyType,
    notes    = notes,
    createdAt = try { LocalDateTime.parse(createdAt, fmt) } catch (e: Exception) { LocalDateTime.now() }
)

private fun parseError(code: Int) = when (code) {
    401  -> "Неверный логин или пароль"
    403  -> "Доступ запрещён"
    422  -> "Некорректные данные"
    500  -> "Ошибка сервера"
    else -> "Ошибка: $code"
}

private suspend fun <T> safeCall(call: suspend () -> AppResult<T>): AppResult<T> =
    try { call() } catch (e: Exception) { AppResult.Error(e.localizedMessage ?: "Неизвестная ошибка") }