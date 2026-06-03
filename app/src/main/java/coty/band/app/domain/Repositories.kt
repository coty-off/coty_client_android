package coty.band.app.domain

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AuthRepository {
    suspend fun login(username: String, password: String): AppResult<User>
    suspend fun register(username: String, password: String): AppResult<User>
    suspend fun loginWithYandex(yandexToken: String): AppResult<User>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun logout()
}

interface MeasurementRepository {
    /** Отправляет фото, поллит статус, возвращает результат. Фото удаляются сразу после отправки */
    suspend fun analyze(frontPhoto: File, sidePhoto: File, heightCm: Float): AppResult<Measurement>

    suspend fun saveMeasurement(measurement: Measurement): AppResult<Measurement>
    fun getMeasurementHistory(): Flow<List<Measurement>>
}
