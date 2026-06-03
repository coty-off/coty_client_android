package coty.band.app.presentation.result

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coty.band.app.domain.Measurement
import coty.band.app.presentation.common.BodyFigureCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementResultScreen(
    measurement: Measurement,
    onSaved: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: MeasurementResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(measurement) { viewModel.setMeasurement(measurement) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    val m = uiState.measurement ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты измерений") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "История")
                    }
                    IconButton(onClick = viewModel::toggleEditing) {
                        Icon(if (uiState.isEditing) Icons.Default.Check else Icons.Default.Edit,
                            if (uiState.isEditing) "Готово" else "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Тип фигуры", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
                    Text(m.bodyType, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Figure + measurements side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Body figure
                BodyFigureCanvas(
                    segments = m.toSegments(),
                    modifier = Modifier.width(160.dp)
                )

                Spacer(Modifier.width(16.dp))

                // Measurements grid
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasurementRow("Грудь",    m.chestCm,    uiState.isEditing) { viewModel.updateField(MeasurementField.CHEST, it) }
                    MeasurementRow("Талия",    m.waistCm,    uiState.isEditing) { viewModel.updateField(MeasurementField.WAIST, it) }
                    MeasurementRow("Бёдра",    m.hipCm,      uiState.isEditing) { viewModel.updateField(MeasurementField.HIP, it) }
                }
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }

            // Confirm button
            Button(
                onClick = viewModel::confirm,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(52.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Подтвердить и сохранить", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MeasurementRow(
    label: String,
    value: Float,
    isEditing: Boolean,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier = Modifier.width(70.dp))

        if (isEditing) {
            var textValue by remember(value) { mutableStateOf("%.2f".format(value)) }
            OutlinedTextField(
                value = textValue,
                onValueChange = { s ->
                    textValue = s
                    s.toFloatOrNull()?.let { onValueChange(it) }
                },
                suffix = { Text("см") },
                singleLine = true,
                modifier = Modifier.width(110.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
        } else {
            Text(
                "%.2f см".format(value),
                fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LocalTextStyle() = MaterialTheme.typography.bodyMedium
