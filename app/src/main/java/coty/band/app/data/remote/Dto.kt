package coty.band.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class YandexAuthRequest(
    @SerializedName("yandex_token") val yandexToken: String
)

data class AuthResponse(
    @SerializedName("access_token") val token: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?
)

data class AnalyzeTaskResponse(
    @SerializedName("id") val taskId: String,
    @SerializedName("status") val status: String,
    @SerializedName("result") val result: String?
)

data class TaskStatusResponse(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("status") val status: String,
    @SerializedName("result") val result: AnalyzeResult?
)

data class AnalyzeResult(
    @SerializedName("chest_cm") val chestCm: Float,
    @SerializedName("waist_cm") val waistCm: Float,
    @SerializedName("hips_cm") val hipsCm: Float,
    @SerializedName("height_cm") val heightCm: Float,
    @SerializedName("body_type") val bodyType: String,
)

data class SaveMeasurementRequest(
    @SerializedName("chest_cm")  val chestCm: Float,
    @SerializedName("waist_cm")  val waistCm: Float,
    @SerializedName("hips_cm")   val hipsCm: Float,
    @SerializedName("height_cm") val heightCm: Float,
    @SerializedName("body_type") val bodyType: String,
    @SerializedName("notes")     val notes: String? = null
)

data class MeasurementResponse(
    @SerializedName("id")         val id: String,
    @SerializedName("chest_cm")   val chestCm: Float,
    @SerializedName("waist_cm")   val waistCm: Float,
    @SerializedName("hips_cm")    val hipsCm: Float,
    @SerializedName("height_cm")  val heightCm: Float,
    @SerializedName("body_type")  val bodyType: String,
    @SerializedName("source")     val source: String?,
    @SerializedName("notes")      val notes: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
