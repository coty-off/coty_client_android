package coty.band.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coty.band.app.domain.GetMeasurementHistoryUseCase
import coty.band.app.domain.LogoutUseCase
import coty.band.app.domain.Measurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val measurements: List<Measurement> = emptyList(),
    val isLoading: Boolean = true,
    val loggedOut: Boolean = false
)

@HiltViewModel
class MeasurementHistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetMeasurementHistoryUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getHistoryUseCase().collect { list ->
                _uiState.value = HistoryUiState(measurements = list, isLoading = false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
