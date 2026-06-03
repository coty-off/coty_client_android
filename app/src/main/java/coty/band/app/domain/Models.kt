package coty.band.app.domain

import java.io.Serializable
import java.time.LocalDateTime

data class User(
    val id: String,
    val username: String,
    val token: String
)

data class Measurement(
    val id: String = "",
    val chestCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val heightCm: Float,
    val bodyType: String,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Serializable {
    fun toSegments(): List<BodySegment> = listOf(
        BodySegment("Грудь", chestCm,  BodyArea.CHEST),
        BodySegment("Талия", waistCm,  BodyArea.WAIST),
        BodySegment("Бёдра", hipCm,    BodyArea.HIP),
        BodySegment("Рост",  heightCm, BodyArea.HEIGHT),
    )
}

data class BodySegment(
    val label: String,
    val valueCm: Float,
    val area: BodyArea
)

enum class BodyArea { CHEST, WAIST, HIP, HEIGHT }

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()
}
