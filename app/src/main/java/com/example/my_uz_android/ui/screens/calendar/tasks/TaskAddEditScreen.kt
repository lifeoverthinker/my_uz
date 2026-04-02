package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.components.TimePicker
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@Composable
fun TaskAddEditScreenRoute(
    viewModel: TaskAddEditViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    LaunchedEffect(isSaved) {
        if (isSaved) onNavigateBack()
    }

    val dateMillis = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val reminderDateMillis = uiState.reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    var selectedQuickType by remember { mutableStateOf("") }

    val subjectsList = uiState.availableSubjects.map { it.first }
    val classTypesList = uiState.availableSubjects.find { it.first == uiState.classSubject }?.second ?: emptyList()

    TaskAddEditScreen(
        isEditMode = uiState.taskId > 0,
        selectedQuickType = selectedQuickType,
        onQuickTypeSelect = { type ->
            selectedQuickType = type
            viewModel.updateTitle(type)
        },
        title = uiState.title,
        onTitleChange = { viewModel.updateTitle(it) },

        isAllDay = uiState.isAllDay,
        onAllDayChange = { viewModel.updateIsAllDay(it) },

        selectedDateMillis = dateMillis,
        onDateChange = { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.updateStartDate(date)
            viewModel.updateEndDate(date)
        },

        selectedHour = uiState.startTime.hour,
        selectedMinute = uiState.startTime.minute,
        onTimeChange = { h, m ->
            viewModel.updateStartTime(LocalTime.of(h, m))
        },

        hasReminder = uiState.hasReminder,
        onHasReminderChange = { viewModel.updateHasReminder(it) },
        reminderDateMillis = reminderDateMillis,
        onReminderDateChange = { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.updateReminderDate(date)
        },
        reminderHour = uiState.reminderTime.hour,
        reminderMinute = uiState.reminderTime.minute,
        onReminderTimeChange = { h, m ->
            viewModel.updateReminderTime(LocalTime.of(h, m))
        },

        selectedSubject = uiState.classSubject ?: "",
        onSubjectChange = { newSubject ->
            viewModel.updateSubjectName(newSubject)
            viewModel.updateClassType("")
        },
        subjectsList = subjectsList,

        selectedClassType = uiState.classType ?: "",
        onClassTypeChange = { viewModel.updateClassType(it) },
        classTypesList = classTypesList,

        priority = uiState.priority,
        onPriorityChange = { viewModel.updatePriority(it) },
        description = uiState.description,
        onDescriptionChange = { viewModel.updateDescription(it) },

        onSaveClick = { viewModel.saveTask() },
        onBackClick = onNavigateBack
    )
}

@Composable
private fun appOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary
)

private val AppFieldShape = RoundedCornerShape(16.dp)

@Composable
private fun ClickableFieldRow(
    label: String,
    value: String,
    iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppFieldShape)
            .clickable(onClick = onClick),
        shape = AppFieldShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskAddEditScreen(
    isEditMode: Boolean,
    selectedQuickType: String,
    onQuickTypeSelect: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    isAllDay: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    selectedDateMillis: Long,
    onDateChange: (Long) -> Unit,
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChange: (Int, Int) -> Unit,
    hasReminder: Boolean,
    onHasReminderChange: (Boolean) -> Unit,
    reminderDateMillis: Long,
    onReminderDateChange: (Long) -> Unit,
    reminderHour: Int,
    reminderMinute: Int,
    onReminderTimeChange: (Int, Int) -> Unit,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    subjectsList: List<String>,
    selectedClassType: String,
    onClassTypeChange: (String) -> Unit,
    classTypesList: List<String>,
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderDatePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = if (isEditMode) "Edytuj zadanie" else "Dodaj zadanie",
                navigationIcon = R.drawable.ic_close,
                isNavigationIconFilled = true,
                onNavigationClick = onBackClick,
                actions = {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .heightIn(min = 48.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Zapisz", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SZYBKI WYBÓR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val quickTypes = listOf(
                    "Kolokwium",
                    "Egzamin",
                    "Wejściówka",
                    "Projekt",
                    "Prezentacja",
                    "Zadanie domowe",
                    "Odpowiedź ustna"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTypes) { type ->
                        FilterChip(
                            selected = selectedQuickType == type,
                            onClick = { onQuickTypeSelect(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Tytuł zadania") },
                placeholder = { Text("np. Przeczytać rozdział 4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appOutlinedTextFieldColors(),
                shape = AppFieldShape
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAllDayChange(!isAllDay) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Zadanie całodniowe", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isAllDay, onCheckedChange = { onAllDayChange(it) })
                }
            }

            ClickableFieldRow(
                label = "Data wykonania",
                value = formatDate(selectedDateMillis),
                iconRes = R.drawable.ic_calendar,
                onClick = { showDatePicker = true }
            )

            AnimatedVisibility(visible = !isAllDay, enter = expandVertically(), exit = shrinkVertically()) {
                ClickableFieldRow(
                    label = "Godzina wykonania",
                    value = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                    iconRes = R.drawable.ic_clock,
                    onClick = { showTimePicker = true }
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHasReminderChange(!hasReminder) }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_bell),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Ustaw przypomnienie", style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(checked = hasReminder, onCheckedChange = { onHasReminderChange(it) })
                    }

                    AnimatedVisibility(visible = hasReminder, enter = expandVertically(), exit = shrinkVertically()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ClickableFieldRow(
                                label = "Data",
                                value = formatDateShort(reminderDateMillis),
                                iconRes = null,
                                onClick = { showReminderDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            ClickableFieldRow(
                                label = "Godzina",
                                value = String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute),
                                iconRes = null,
                                onClick = { showReminderTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            AppDropdownMenu(
                label = "Przedmiot (opcjonalnie)",
                selectedOption = selectedSubject,
                options = subjectsList,
                onOptionSelected = onSubjectChange
            )

            AnimatedVisibility(
                visible = selectedSubject.isNotEmpty() && classTypesList.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "RODZAJ ZAJĘĆ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        classTypesList.forEach { type ->
                            FilterChip(
                                selected = selectedClassType == type,
                                onClick = { onClassTypeChange(type) },
                                label = { Text(ClassTypeUtils.getFullName(type)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "PRIORYTET",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = priority == 0,
                        onClick = { onPriorityChange(0) },
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ) { Text("Niski") }

                    SegmentedButton(
                        selected = priority == 1,
                        onClick = { onPriorityChange(1) },
                        shape = RoundedCornerShape(0.dp)
                    ) { Text("Średni") }

                    SegmentedButton(
                        selected = priority == 2,
                        onClick = { onPriorityChange(2) },
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    ) { Text("Wysoki") }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis (opcjonalnie)") },
                placeholder = { Text("Dodatkowe szczegóły, materiały...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                colors = appOutlinedTextFieldColors(),
                shape = AppFieldShape
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) DatePicker(
        date = selectedDateMillis,
        onDateSelected = { onDateChange(it); showDatePicker = false },
        onDismiss = { showDatePicker = false }
    )
    if (showReminderDatePicker) DatePicker(
        date = reminderDateMillis,
        onDateSelected = { onReminderDateChange(it); showReminderDatePicker = false },
        onDismiss = { showReminderDatePicker = false }
    )

    if (showTimePicker) {
        TimePicker(
            time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
            onTimeSelected = { h, m -> onTimeChange(h, m); showTimePicker = false },
            onDismiss = { showTimePicker = false }
        )
    }
    if (showReminderTimePicker) {
        TimePicker(
            time = String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute),
            onTimeSelected = { h, m -> onReminderTimeChange(h, m); showReminderTimePicker = false },
            onDismiss = { showReminderTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownMenu(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = appOutlinedTextFieldColors(),
            shape = AppFieldShape
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false }
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL"))
        .format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }

private fun formatDateShort(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(Date(timestamp))

@Preview(showBackground = true)
@Composable
private fun TaskAddEditScreenPreview() {
    MyUZTheme {
        TaskAddEditScreen(
            isEditMode = false,
            selectedQuickType = "Projekt",
            onQuickTypeSelect = {},
            title = "Przeczytać rozdział 4",
            onTitleChange = {},
            isAllDay = false,
            onAllDayChange = {},
            selectedDateMillis = System.currentTimeMillis(),
            onDateChange = {},
            selectedHour = 14,
            selectedMinute = 30,
            onTimeChange = { _, _ -> },
            hasReminder = true,
            onHasReminderChange = {},
            reminderDateMillis = System.currentTimeMillis(),
            onReminderDateChange = {},
            reminderHour = 12,
            reminderMinute = 0,
            onReminderTimeChange = { _, _ -> },
            selectedSubject = "Programowanie",
            onSubjectChange = {},
            subjectsList = listOf("Programowanie", "Matematyka", "Fizyka"),
            selectedClassType = "L",
            onClassTypeChange = {},
            classTypesList = listOf("W", "L", "C"),
            priority = 1,
            onPriorityChange = {},
            description = "Powtórzyć materiał i przygotować notatki.",
            onDescriptionChange = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}