package coty.band.app.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coty.band.app.domain.AppResult
import coty.band.app.domain.Measurement
import coty.band.app.domain.SaveMeasurementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val measurement: Measurement? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MeasurementResultViewModel @Inject constructor(
    private val saveMeasurementUseCase: SaveMeasurementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun setMeasurement(measurement: Measurement) {
        _uiState.value = _uiState.value.copy(measurement = measurement)
    }

    fun updateField(field: MeasurementField, value: Float) {
        val m = _uiState.value.measurement ?: return
        val updated = when (field) {
            MeasurementField.CHEST    -> m.copy(chestCm    = value)
            MeasurementField.WAIST    -> m.copy(waistCm    = value)
            MeasurementField.HIP      -> m.copy(hipCm      = value)
        }
        _uiState.value = _uiState.value.copy(measurement = updated)
    }

    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun confirm() {
        val m = _uiState.value.measurement ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            when (val result = saveMeasurementUseCase(m)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                is AppResult.Error   -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                else -> Unit
            }
        }
    }
}

enum class MeasurementField {
    CHEST, WAIST, HIP
}
