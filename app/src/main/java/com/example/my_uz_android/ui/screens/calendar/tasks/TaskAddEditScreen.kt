package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAddEditScreen(
    taskId: Int?,
    prefilledTitle: String? = null,
    prefilledDesc: String? = null,
    prefilledSubject: String? = null,
    prefilledType: String? = null,
    prefilledDate: Long? = null,
    prefilledIsAllDay: Boolean = false,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskAddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(taskId, prefilledTitle) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        } else {
            if (prefilledTitle != null) viewModel.updateTitle(prefilledTitle)
            if (prefilledDesc != null) viewModel.updateDescription(prefilledDesc)
            if (prefilledSubject != null) viewModel.updateClassSubject(prefilledSubject)
            if (prefilledType != null) viewModel.updateClassType(prefilledType)
            if (prefilledDate != null) {
                val date = Instant.ofEpochMilli(prefilledDate).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.updateStartDate(date)
                viewModel.updateEndDate(date)
                viewModel.updateIsAllDay(prefilledIsAllDay)
            }
        }
    }

    Dialog(
        onDismissRequest = onNavigateBack,
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.ic_x_close), "Anuluj", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { viewModel.saveTask() },
                        enabled = uiState.title.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        ) { paddingValues ->
            TaskAddEditContent(
                uiState = uiState,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onIsAllDayChange = viewModel::updateIsAllDay,
                onStartDateChange = viewModel::updateStartDate,
                onEndDateChange = viewModel::updateEndDate,
                onStartTimeChange = viewModel::updateStartTime,
                onEndTimeChange = viewModel::updateEndTime,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun TaskAddEditContent(
    uiState: TaskAddEditUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIsAllDayChange: (Boolean) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Nagłówek/Tytuł z Google Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(40.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.title.isEmpty()) {
                    Text(
                        stringResource(R.string.task_title_placeholder),
                        style = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                BasicTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    textStyle = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))

        // Czas i Data
        FormRow(iconRes = R.drawable.ic_clock) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.task_all_day), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.isAllDay, onCheckedChange = onIsAllDayChange)
                }
                SimpleDateTimeRow(
                    date = uiState.startDate,
                    time = if (uiState.isAllDay) null else uiState.startTime,
                    onDateClick = { showDatePickerStart = true },
                    onTimeClick = { showTimePickerStart = true }
                )
                SimpleDateTimeRow(
                    date = uiState.endDate,
                    time = if (uiState.isAllDay) null else uiState.endTime,
                    onDateClick = { showDatePickerEnd = true },
                    onTimeClick = { showTimePickerEnd = true }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Opis
        FormRow(iconRes = R.drawable.ic_menu_2) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (uiState.description.isEmpty()) {
                    Text(stringResource(R.string.task_description_placeholder), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BasicTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Pickery dostosowane do Twoich parametrów
    if (showDatePickerStart) {
        DatePicker(
            date = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            onDateSelected = { onStartDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()); showDatePickerStart = false },
            onDismiss = { showDatePickerStart = false }
        )
    }
    if (showDatePickerEnd) {
        DatePicker(
            date = uiState.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            onDateSelected = { onEndDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()); showDatePickerEnd = false },
            onDismiss = { showDatePickerEnd = false }
        )
    }
    if (showTimePickerStart) {
        TimePicker(
            time = String.format("%02d:%02d", uiState.startTime.hour, uiState.startTime.minute),
            onTimeSelected = { h, m -> onStartTimeChange(LocalTime.of(h, m)); showTimePickerStart = false },
            onDismiss = { showTimePickerStart = false }
        )
    }
    if (showTimePickerEnd) {
        TimePicker(
            time = String.format("%02d:%02d", uiState.endTime.hour, uiState.endTime.minute),
            onTimeSelected = { h, m -> onEndTimeChange(LocalTime.of(h, m)); showTimePickerEnd = false },
            onDismiss = { showTimePickerEnd = false }
        )
    }
}

@Composable
private fun SimpleDateTimeRow(date: LocalDate, time: LocalTime?, onDateClick: () -> Unit, onTimeClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = date.format(formatter),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).clickable { onDateClick() }.padding(vertical = 8.dp)
        )
        if (time != null) {
            Text(
                text = String.format("%02d:%02d", time.hour, time.minute),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { onTimeClick() }.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FormRow(
    iconRes: Int?,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.CenterStart) {
            if (iconRes != null) Icon(painterResource(iconRes), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}