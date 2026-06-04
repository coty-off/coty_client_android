package coty.band.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coty.band.app.domain.AppResult
import coty.band.app.domain.RegisterUseCase
import coty.band.app.domain.YandexAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val captchaToken: String = "",
    val captchaPassed: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val yandexAuthUseCase: YandexAuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(v: String)        { _uiState.value = _uiState.value.copy(username = v) }
    fun onPasswordChange(v: String)        { _uiState.value = _uiState.value.copy(password = v) }
    fun onConfirmPasswordChange(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v) }

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }

    fun onCaptchaToken(token: String) {
        _uiState.value = _uiState.value.copy(captchaToken = token, captchaPassed = true)
    }

    fun register() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            when (val result = registerUseCase(s.username, s.email, s.password, s.confirmPassword)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is AppResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> Unit
            }
        }
    }

    fun loginWithYandex(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = yandexAuthUseCase(token)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is AppResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> Unit
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
