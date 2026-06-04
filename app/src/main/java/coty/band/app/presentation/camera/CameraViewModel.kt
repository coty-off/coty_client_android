package coty.band.app.presentation.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coty.band.app.domain.AnalyzeMeasurementsUseCase
import coty.band.app.domain.AppResult
import coty.band.app.domain.Measurement
import coty.band.app.presentation.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class CameraStep { FRONT, SIDE }

sealed class CameraFlowState {
    data class Camera(val step: CameraStep) : CameraFlowState()
    data class Preview(val step: CameraStep, val file: File) : CameraFlowState()
    object Analyzing : CameraFlowState()
    data class Success(val measurement: Measurement) : CameraFlowState()
    data class Error(val message: String) : CameraFlowState()
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzeMeasurementsUseCase: AnalyzeMeasurementsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CameraFlowState>(CameraFlowState.Camera(CameraStep.FRONT))
    val state: StateFlow<CameraFlowState> = _state.asStateFlow()

    private var frontPhotoFile: File? = null
    private var sidePhotoFile: File? = null
    private var userHeightCm: Float = 170f

    fun createTempImageFile(step: CameraStep): Pair<File, android.net.Uri> {
        val dir = File(context.cacheDir, "camera_images").also { it.mkdirs() }
        val file = File.createTempFile(
            if (step == CameraStep.FRONT) "front_" else "side_",
            ".jpg", dir
        )
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return file to uri
    }

    fun onPhotoCaptured(step: CameraStep, file: File) {
        when (step) {
            CameraStep.FRONT -> frontPhotoFile = file
            CameraStep.SIDE  -> sidePhotoFile  = file
        }
        _state.value = CameraFlowState.Preview(step, file)
    }

    fun onPhotoPicked(step: CameraStep, uri: Uri) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { copyUriToTempFile(step, uri) }
            if (file != null) {
                onPhotoCaptured(step, file)
            } else {
                _state.value = CameraFlowState.Error("Не удалось открыть выбранное фото")
            }
        }
    }

    private fun copyUriToTempFile(step: CameraStep, uri: Uri): File? = try {
        val dir = File(context.cacheDir, "camera_images").also { it.mkdirs() }
        val file = File.createTempFile(
            if (step == CameraStep.FRONT) "front_" else "side_",
            ".jpg", dir
        )
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file
    } catch (e: Exception) {
        null
    }

    fun onPreviewAccepted(step: CameraStep) {
        _state.value = when (step) {
            CameraStep.FRONT -> CameraFlowState.Camera(CameraStep.SIDE)
            CameraStep.SIDE  -> {
                analyzePhotos()
                CameraFlowState.Analyzing
            }
        }
    }
    fun onRetake(step: CameraStep) {
        _state.value = CameraFlowState.Camera(step)
    }

    private fun analyzePhotos() {
        viewModelScope.launch {
            val front = frontPhotoFile
            val side  = sidePhotoFile
            if (front == null || side == null) {
                _state.value = CameraFlowState.Error("Ошибка: нет фотографий")
                return@launch
            }
            _state.value = CameraFlowState.Analyzing

            val result = try {
                val (frontCompressed, sideCompressed) = withContext(Dispatchers.IO) {
                    ImageCompressor.compress(front) to ImageCompressor.compress(side)
                }
                front.delete()
                side.delete()

                analyzeMeasurementsUseCase(frontCompressed, sideCompressed, userHeightCm)
            } catch (e: Exception) {
                AppResult.Error("Не удалось подготовить фото: ${e.localizedMessage ?: "ошибка обработки"}")
            }

            when (result) {
                is AppResult.Success -> _state.value = CameraFlowState.Success(result.data)
                is AppResult.Error   -> _state.value = CameraFlowState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun retry() {
        frontPhotoFile = null
        sidePhotoFile  = null
        _state.value = CameraFlowState.Camera(CameraStep.FRONT)
    }
}