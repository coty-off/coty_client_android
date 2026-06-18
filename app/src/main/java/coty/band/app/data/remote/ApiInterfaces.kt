package coty.band.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @POST("auth/yandex")
    suspend fun yandexLogin(@Body request: YandexAuthRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponse>
}

interface AnalyzeApi {
    /** Отправить два фото + рост. Фото сразу удаляются на клиенте после отправки */
    @Multipart
    @POST("analyze")
    suspend fun analyze(
        @Part frontImage: MultipartBody.Part,
        @Part sideImage: MultipartBody.Part,
        @Part("height_cm") heightCm: RequestBody
    ): Response<AnalyzeTaskResponse>

    /** Поллинг — пока status != "done" или "error" */
    @GET("analyze/{task_id}")
    suspend fun getTaskStatus(
        @Path("task_id") taskId: String
    ): Response<TaskStatusResponse>
}

interface MeasurementApi {
    @GET("me/measurements")
    suspend fun getMeasurements(): Response<List<MeasurementResponse>>

    @POST("me/measurements")
    suspend fun saveMeasurement(
        @Body request: SaveMeasurementRequest
    ): Response<MeasurementResponse>

    @PUT("me/measurements/{measurement_id}")
    suspend fun updateMeasurement(
        @Path("measurement_id") id: String,
        @Body request: SaveMeasurementRequest
    ): Response<MeasurementResponse>

    @DELETE("me/measurements/{measurement_id}")
    suspend fun deleteMeasurement(
        @Path("measurement_id") id: String
    ): Response<Unit>
}
