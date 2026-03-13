package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.calendar.tasks.FormRow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGradeScreen(
    onDismiss: () -> Unit,
    viewModel: AddEditGradeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    var showTypeModal by remember { mutableStateOf(false) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onDismiss()
        }
    }

    val gradeInput = when (uiState.gradeType) {
        GradeType.STANDARD -> if (uiState.customGradeValue.isNotBlank()) {
            uiState.customGradeValue
        } else {
            formatGradeValue(uiState.gradeValue)
        }
        GradeType.ACTIVITY -> "+"
        GradeType.CUSTOM,
        GradeType.POINTS -> uiState.customGradeValue
    }

    val canSave = when (uiState.gradeType) {
        GradeType.ACTIVITY -> true
        GradeType.STANDARD -> uiState.gradeValue != null
        GradeType.CUSTOM,
        GradeType.POINTS -> uiState.customGradeValue.isNotBlank()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.ic_x_close), "Anuluj")
                    }
                    Button(
                        onClick = { viewModel.saveGrade(); onDismiss() },
                        enabled = canSave,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(40.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (gradeInput.isEmpty()) {
                            Text(
                                text = "Ocena (np. 5.0, Zal, 12pkt)",
                                style = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        BasicTextField(
                            value = gradeInput,
                            onValueChange = { value ->
                                when (uiState.gradeType) {
                                    GradeType.STANDARD -> {
                                        viewModel.updateCustomGradeValue(value)
                                        viewModel.updateGradeValue(value.replace(",", ".").toDoubleOrNull())
                                    }
                                    GradeType.CUSTOM,
                                    GradeType.POINTS -> viewModel.updateCustomGradeValue(value)
                                    GradeType.ACTIVITY -> Unit
                                }
                            },
                            textStyle = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            readOnly = uiState.gradeType == GradeType.ACTIVITY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                FormRow(iconRes = R.drawable.ic_info_circle, onClick = { showTypeModal = true }) {
                    Text(
                        text = gradeTypeLabel(uiState.gradeType),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FormRow(iconRes = R.drawable.ic_menu) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.description.isEmpty()) {
                            Text(text = "Tytuł / Opis (np. Kolokwium)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = viewModel::updateDescription,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FormRow(iconRes = R.drawable.ic_scales) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.weight.isEmpty()) {
                            Text(text = "Waga (opcjonalnie)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        BasicTextField(
                            value = uiState.weight,
                            onValueChange = viewModel::updateWeight,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showTypeModal) {
            AlertDialog(
                onDismissRequest = { showTypeModal = false },
                title = { Text("Wybierz typ", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        val availableTypes = listOf(GradeType.STANDARD, GradeType.CUSTOM, GradeType.POINTS)
                        availableTypes.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateGradeType(type); showTypeModal = false }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = uiState.gradeType == type, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(gradeTypeLabel(type), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateGradeType(GradeType.ACTIVITY)
                                    viewModel.updateCustomGradeValue("+")
                                    viewModel.updateDescription("Aktywność")
                                    showTypeModal = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.gradeType == GradeType.ACTIVITY, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Aktywność +", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showTypeModal = false }) { Text("Anuluj") } }
            )
        }
    }
}

private fun gradeTypeLabel(type: GradeType): String = when (type) {
    GradeType.STANDARD -> "Ocena"
    GradeType.CUSTOM -> "Ocena niestandardowa"
    GradeType.POINTS -> "Punkty"
    GradeType.ACTIVITY -> "Aktywność +"
}

private fun formatGradeValue(value: Double?): String {
    if (value == null) return ""
    return if (value % 1.0 == 0.0) value.roundToInt().toString() else value.toString()
}
