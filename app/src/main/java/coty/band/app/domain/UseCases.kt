package coty.band.app.domain

import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): AppResult<User> {
        if (username.isBlank()) return AppResult.Error("Логин не может быть пустым")
        if (password.length < 6) return AppResult.Error("Пароль должен быть не менее 6 символов")
        return repo.login(username.trim(), password)
    }
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        username: String, email: String, password: String, confirmPassword: String
    ): AppResult<User> {
        if (username.isBlank()) return AppResult.Error("Логин не может быть пустым")
        if (email.isBlank() || !email.contains("@"))
            return AppResult.Error("Введите корректный email")
        if (password.length < 6) return AppResult.Error("Пароль не менее 6 символов")
        if (password != confirmPassword) return AppResult.Error("Пароли не совпадают")
        return repo.register(
            username.trim(), email.trim(), password,
            password1 = password
        )
    }
}

class YandexAuthUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(token: String): AppResult<User> = repo.loginWithYandex(token)
}

class AnalyzeMeasurementsUseCase @Inject constructor(private val repo: MeasurementRepository) {
    suspend operator fun invoke(front: File, side: File, heightCm: Float): AppResult<Measurement> {
        if (!front.exists()) return AppResult.Error("Нет фото спереди")
        if (!side.exists()) return AppResult.Error("Нет фото сбоку")
        if (heightCm < 50f || heightCm > 250f) return AppResult.Error("Некорректный рост")
        return repo.analyze(front, side, heightCm)
    }
}

class SaveMeasurementUseCase @Inject constructor(private val repo: MeasurementRepository) {
    suspend operator fun invoke(measurement: Measurement): AppResult<Measurement> =
        repo.saveMeasurement(measurement)
}

class GetMeasurementHistoryUseCase @Inject constructor(private val repo: MeasurementRepository) {
    operator fun invoke(): Flow<List<Measurement>> = repo.getMeasurementHistory()
}

class IsLoggedInUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn()
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}
