package com.example.my_uz_android.ui.screens.calendar.tasks

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.components.TimePicker
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun TaskAddEditScreen(
    taskId: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskAddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        if (taskId != null && taskId != -1) {
            viewModel.loadTask(taskId)
        }
    }

    TaskAddEditContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSaveTask = {
            viewModel.saveTask()
            if (uiState.title.isNotBlank()) {
                Toast.makeText(context, "Zadanie zapisane", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } else {
                Toast.makeText(context, "Wpisz tytuł zadania", Toast.LENGTH_SHORT).show()
            }
        },
        onDeleteTask = {
            viewModel.deleteTask()
            Toast.makeText(context, "Zadanie usunięte", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        },
        onTitleChange = viewModel::updateTitle,
        onSubjectChange = viewModel::updateClassSubject,
        onClassTypeChange = viewModel::updateClassType,
        onPriorityChange = viewModel::updatePriority,
        onIsAllDayChange = viewModel::updateIsAllDay,
        onStartDateChange = viewModel::updateStartDate,
        onEndDateChange = viewModel::updateEndDate,
        onStartTimeChange = viewModel::updateStartTime,
        onEndTimeChange = viewModel::updateEndTime,
        onDescriptionChange = viewModel::updateDescription,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAddEditContent(
    uiState: TaskAddEditUiState,
    onNavigateBack: () -> Unit,
    onSaveTask: () -> Unit,
    onDeleteTask: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSubjectChange: (String?) -> Unit,
    onClassTypeChange: (String?) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onIsAllDayChange: (Boolean) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val quickTitles = listOf("Zadanie domowe", "Kolokwium", "Wejściówka", "Egzamin", "Projekt", "Prezentacja")

    var showSubjectModal by remember { mutableStateOf(false) }
    var showTypeModal by remember { mutableStateOf(false) }
    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }

    val startDate = uiState.startDate ?: LocalDate.now()
    val endDate = uiState.endDate ?: startDate
    val startTime = uiState.startTime ?: LocalTime.of(8, 0)
    val endTime = uiState.endTime ?: LocalTime.of(10, 0)

    val isTypeSelectionEnabled = !uiState.classSubject.isNullOrEmpty()

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.fillMaxSize().statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_x_close), contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
                }
                Button(
                    onClick = onSaveTask,
                    enabled = uiState.title.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    // labelLarge (14sp Medium)
                    Text(stringResource(R.string.btn_save), style = MaterialTheme.typography.labelLarge)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Box(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        // ZMIANA: Użycie headlineMedium (28sp Normal) z Type.kt
                        val titleStyle = MaterialTheme.typography.headlineMedium.copy(color = textColor)

                        BasicTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            textStyle = titleStyle,
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        stringResource(R.string.task_title_placeholder),
                                        style = titleStyle.copy(color = textColor.copy(0.4f))
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(contentPadding = PaddingValues(horizontal = 76.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTitles) { title ->
                        val isSelected = uiState.title == title
                        Surface(
                            onClick = { onTitleChange(title) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primaryColor else Color.Transparent,
                            border = if (!isSelected) BorderStroke(1.dp, dividerColor) else null,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(
                                    title,
                                    // labelMedium (12sp Medium), używamy onPrimary gdy zaznaczone
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor)

                CommonRow(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.task_all_day), style = MaterialTheme.typography.bodyLarge, color = textColor)
                            Switch(checked = uiState.isAllDay, onCheckedChange = onIsAllDayChange)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SimpleDateTimeRow(startDate, if (uiState.isAllDay) null else startTime, { showDatePickerStart = true }, { showTimePickerStart = true }, textColor)
                            SimpleDateTimeRow(endDate, if (uiState.isAllDay) null else endTime, { showDatePickerEnd = true }, { showTimePickerEnd = true }, textColor)
                        }
                    }
                }

                HorizontalDivider(color = dividerColor)

                CommonRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.classSubject ?: stringResource(R.string.task_subject_label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (uiState.classSubject == null) subTextColor else textColor
                            )
                            Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = subTextColor, modifier = Modifier.size(24.dp))
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showSubjectModal = true }
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                CommonRow(iconRes = R.drawable.ic_graduation_hat, iconTint = if (isTypeSelectionEnabled) iconTint else iconTint.copy(0.4f)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val typeText = uiState.classType?.let { ClassTypeUtils.getFullName(it) } ?: stringResource(R.string.task_type_label)
                            Text(
                                text = typeText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isTypeSelectionEnabled) subTextColor.copy(0.4f) else if (uiState.classType == null) subTextColor else textColor
                            )
                            Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = if (isTypeSelectionEnabled) subTextColor else subTextColor.copy(0.4f), modifier = Modifier.size(24.dp))
                        }
                        if (isTypeSelectionEnabled) {
                            Box(modifier = Modifier.matchParentSize().clickable { showTypeModal = true })
                        }
                    }
                }

                HorizontalDivider(color = dividerColor)

                CommonRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { inner ->
                                if (uiState.description.isEmpty()) Text(stringResource(R.string.task_description_placeholder), style = MaterialTheme.typography.bodyLarge.copy(color = subTextColor))
                                inner()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showDatePickerStart) DatePicker(date = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), onDateSelected = { showDatePickerStart = false; onStartDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()) }, onDismiss = { showDatePickerStart = false })
    if (showDatePickerEnd) DatePicker(date = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), onDateSelected = { showDatePickerEnd = false; onEndDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()) }, onDismiss = { showDatePickerEnd = false })
    if (showTimePickerStart) TimePicker(time = String.format("%02d:%02d", startTime.hour, startTime.minute), onTimeSelected = { h, m -> showTimePickerStart = false; onStartTimeChange(LocalTime.of(h, m)) }, onDismiss = { showTimePickerStart = false })
    if (showTimePickerEnd) TimePicker(time = String.format("%02d:%02d", endTime.hour, endTime.minute), onTimeSelected = { h, m -> showTimePickerEnd = false; onEndTimeChange(LocalTime.of(h, m)) }, onDismiss = { showTimePickerEnd = false })

    if (showSubjectModal) {
        AlertDialog(
            onDismissRequest = { showSubjectModal = false },
            title = { Text("Wybierz przedmiot") },
            text = {
                LazyColumn {
                    item { TextButton(onClick = { onSubjectChange(null); onClassTypeChange(null); showSubjectModal = false }) { Text("Brak") } }
                    items(uiState.availableSubjects) { (sub, _) -> TextButton(onClick = { onSubjectChange(sub); onClassTypeChange(null); showSubjectModal = false }) { Text(sub) } }
                }
            },
            confirmButton = {}
        )
    }
    if (showTypeModal) {
        val types = uiState.availableSubjects.find { it.first == uiState.classSubject }?.second ?: emptyList()
        AlertDialog(
            onDismissRequest = { showTypeModal = false },
            title = { Text("Wybierz rodzaj") },
            text = { LazyColumn { items(types) { type -> TextButton(onClick = { onClassTypeChange(type); showTypeModal = false }) { Text(ClassTypeUtils.getFullName(type)) } } } },
            confirmButton = {}
        )
    }
}

@Composable
private fun SimpleDateTimeRow(date: LocalDate, time: LocalTime?, onDateClick: () -> Unit, onTimeClick: () -> Unit, textColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.weight(1f).clickable(onClick = onDateClick).padding(vertical = 12.dp)) {
            Text(text = date.toString(), style = MaterialTheme.typography.bodyLarge, color = textColor)
        }
        if (time != null) {
            Box(modifier = Modifier.weight(0.6f).clickable(onClick = onTimeClick).padding(vertical = 12.dp), contentAlignment = Alignment.CenterEnd) {
                Text(text = String.format("%02d:%02d", time.hour, time.minute), style = MaterialTheme.typography.bodyLarge, color = textColor)
            }
        }
    }
}

@Composable
fun CommonRow(iconRes: Int, iconTint: Color, content: @Composable BoxScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) { Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) { content() }
    }
}