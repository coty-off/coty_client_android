package coty.band.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coty.band.app.domain.AppResult
import coty.band.app.domain.LoginUseCase
import coty.band.app.domain.YandexAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val yandexAuthUseCase: YandexAuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value) }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            when (val result = loginUseCase(state.username, state.password)) {
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
