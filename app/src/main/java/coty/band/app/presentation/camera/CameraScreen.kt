package coty.band.app.presentation.camera

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import coty.band.app.domain.Measurement
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFlowScreen(
    onAnalysisComplete: (Measurement) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    LaunchedEffect(state) {
        if (state is CameraFlowState.Success) {
            onAnalysisComplete((state as CameraFlowState.Success).measurement)
        }
    }

    when (val s = state) {
        is CameraFlowState.Camera  -> CameraCapture(step = s.step, viewModel = viewModel)
        is CameraFlowState.Preview -> PhotoPreviewScreen(step = s.step, file = s.file, viewModel = viewModel)
        is CameraFlowState.Analyzing -> AnalyzingScreen()
        is CameraFlowState.Error   -> ErrorScreen(message = s.message, onRetry = viewModel::retry)
        is CameraFlowState.Success -> { /* handled above */ }
    }
}

@Composable
private fun CameraCapture(step: CameraStep, viewModel: CameraViewModel) {
    val context      = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val (tempFile, tempUri) = remember(step) { viewModel.createTempImageFile(step) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Bind failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (step == CameraStep.FRONT) "Сфотографируйтесь СПЕРЕДИ"
                else "Сфотографируйтесь СБОКУ",
                color = Color.White, fontWeight = FontWeight.Bold
            )
            Text(
                text = "Встаньте прямо, камера должна видеть вас полностью",
                color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp
            )
        }

        // Shutter button
        FloatingActionButton(
            onClick = {
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                imageCapture?.takePicture(outputOptions, executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            viewModel.onPhotoCaptured(step, tempFile)
                        }
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("CameraCapture", "Capture failed", exc)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.Camera, contentDescription = "Сделать фото",
                modifier = Modifier.size(36.dp), tint = Color.White)
        }
    }
}

@Composable
private fun PhotoPreviewScreen(step: CameraStep, file: File, viewModel: CameraViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = file,
            contentDescription = "Превью фото",
            contentScale = ContentScale.Fit,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        Text(
            text = if (step == CameraStep.FRONT) "Фото СПЕРЕДИ — хорошо получилось?"
            else "Фото СБОКУ — хорошо получилось?",
            color = Color.White, modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onRetake(step) },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Переснять", color = Color.White)
            }
            Button(
                onClick = { viewModel.onPreviewAccepted(step) },
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(if (step == CameraStep.FRONT) "Далее" else "Анализировать")
            }
        }
    }
}

@Composable
private fun AnalyzingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary, strokeWidth = 5.dp)
        Spacer(Modifier.height(24.dp))
        Text("Анализируем фигуру...", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text("Это займёт несколько секунд", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ошибка", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Попробовать снова") }
    }
}
