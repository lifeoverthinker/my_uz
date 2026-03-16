package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.components.TimePicker
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

// --- WRAPPER DLA NAWIGACJI ---
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

    val dateMillis = uiState.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // Lokalne stany (np. dla przypomnienia, które nie istnieje jeszcze w encji)
    var selectedQuickType by remember { mutableStateOf("") }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var reminderHour by remember { mutableStateOf(8) }
    var reminderMinute by remember { mutableStateOf(0) }

    val subjectsList = uiState.availableSubjects.map { it.first }
    val classTypesList = uiState.availableSubjects.find { it.first == uiState.classSubject }?.second ?: emptyList()

    TaskAddEditScreen(
        isEditMode = uiState.taskId > 0,
        selectedQuickType = selectedQuickType,
        onQuickTypeSelect = { type ->
            selectedQuickType = type
            viewModel.updateTitle(type) // Odblokowany szybki wybór!
        },
        title = uiState.title,
        onTitleChange = { viewModel.updateTitle(it) }, // Odblokowana edycja tytułu!

        isAllDay = uiState.isAllDay,
        onAllDayChange = { viewModel.updateIsAllDay(it) }, // Odblokowany switch całego dnia!

        selectedDateMillis = dateMillis,
        onDateChange = { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            viewModel.updateStartDate(date)
            viewModel.updateEndDate(date) // Bezpieczeństwo - zadanie 1-dniowe
        },

        selectedHour = uiState.startTime.hour,
        selectedMinute = uiState.startTime.minute,
        onTimeChange = { h, m ->
            viewModel.updateStartTime(LocalTime.of(h, m))
        },

        hasReminder = hasReminder,
        onHasReminderChange = { hasReminder = it },
        reminderDateMillis = reminderDateMillis,
        onReminderDateChange = { reminderDateMillis = it },
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        onReminderTimeChange = { h, m -> reminderHour = h; reminderMinute = m },

        selectedSubject = uiState.classSubject ?: "",
        onSubjectChange = { newSubject ->
            viewModel.updateClassSubject(newSubject)
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
        onNavigateBack = onNavigateBack
    )
}

// --- BEZSTANOWY WIDOK (STATELESS UI) ---
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
    onNavigateBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderDatePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (isEditMode) "Edytuj zadanie" else "Dodaj zadanie",
                navigationIcon = R.drawable.ic_x_close,
                isNavigationIconFilled = true,
                onNavigationClick = onNavigateBack,
                actions = {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.padding(end = 8.dp),
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SZYBKI WYBÓR
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SZYBKI WYBÓR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val quickTypes = listOf("Zadanie domowe", "Projekt", "Egzamin", "Kolokwium", "Prezentacja", "Raport")
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

            // 2. TYTUŁ ZADANIA
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Tytuł zadania") },
                placeholder = { Text("np. Przeczytać rozdział 4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            // 5. CAŁY DZIEŃ PRZEŁĄCZNIK (przeniesiony nad datę)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAllDayChange(!isAllDay) }.padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Zadanie całodniowe", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isAllDay, onCheckedChange = { onAllDayChange(it) })
            }

            // 3. DATA WYKONANIA
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatDate(selectedDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data wykonania") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }

            // 4. CZAS WYKONANIA
            AnimatedVisibility(visible = !isAllDay, enter = expandVertically(), exit = shrinkVertically()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Godzina wykonania") },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_clock), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                }
            }


            // 6. PRZYPOMNIENIE
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onHasReminderChange(!hasReminder) }.padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_bell), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Ustaw przypomnienie", style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(checked = hasReminder, onCheckedChange = { onHasReminderChange(it) })
                }

                AnimatedVisibility(visible = hasReminder, enter = expandVertically(), exit = shrinkVertically()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = formatDateShort(reminderDateMillis),
                                onValueChange = {}, readOnly = true, label = { Text("Data") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.matchParentSize().clickable { showReminderDatePicker = true })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute),
                                onValueChange = {}, readOnly = true, label = { Text("Godzina") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.matchParentSize().clickable { showReminderTimePicker = true })
                        }
                    }
                }
            }

            // 7. PRZEDMIOT
            AppDropdownMenu(
                label = "Przedmiot (opcjonalnie)",
                selectedOption = selectedSubject,
                options = subjectsList,
                onOptionSelected = onSubjectChange
            )

            // 8. RODZAJ ZAJĘĆ
            AnimatedVisibility(visible = selectedSubject.isNotEmpty() && classTypesList.isNotEmpty(), enter = expandVertically(), exit = shrinkVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RODZAJ ZAJĘĆ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        classTypesList.forEach { type ->
                            FilterChip(
                                selected = selectedClassType == type,
                                onClick = { onClassTypeChange(type) },
                                label = { Text(ClassTypeUtils.getFullName(type)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                    }
                }
            }

            // 9. PRIORYTET
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIORYTET", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = priority == 0, onClick = { onPriorityChange(0) }, shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)) { Text("Niski") }
                    SegmentedButton(selected = priority == 1, onClick = { onPriorityChange(1) }, shape = RoundedCornerShape(0.dp)) { Text("Średni") }
                    SegmentedButton(selected = priority == 2, onClick = { onPriorityChange(2) }, shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)) { Text("Wysoki") }
                }
            }

            // 10. OPIS
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis (opcjonalnie)") },
                placeholder = { Text("Dodatkowe szczegóły, materiały...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Okna Dialogowe
    if (showDatePicker) DatePicker(date = selectedDateMillis, onDateSelected = { onDateChange(it); showDatePicker = false }, onDismiss = { showDatePicker = false })
    if (showReminderDatePicker) DatePicker(date = reminderDateMillis, onDateSelected = { onReminderDateChange(it); showReminderDatePicker = false }, onDismiss = { showReminderDatePicker = false })

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
fun AppDropdownMenu(label: String, selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}

private fun formatDate(timestamp: Long): String = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL")).format(Date(timestamp)).replaceFirstChar { it.uppercase() }
private fun formatDateShort(timestamp: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(Date(timestamp))

@Preview(showBackground = true)
@Composable
fun TaskAddEditScreenPreview() {
    MaterialTheme {
        TaskAddEditScreen(
            isEditMode = false, selectedQuickType = "Projekt", onQuickTypeSelect = {},
            title = "Projekt Zaliczeniowy", onTitleChange = {},
            isAllDay = false, onAllDayChange = {},
            selectedDateMillis = System.currentTimeMillis(), onDateChange = {},
            selectedHour = 14, selectedMinute = 30, onTimeChange = { _, _ -> },
            hasReminder = true, onHasReminderChange = {},
            reminderDateMillis = System.currentTimeMillis(), onReminderDateChange = {},
            reminderHour = 12, reminderMinute = 0, onReminderTimeChange = { _, _ -> },
            selectedSubject = "Programowanie", onSubjectChange = {}, subjectsList = listOf("Programowanie"),
            selectedClassType = "Laboratorium", onClassTypeChange = {}, classTypesList = listOf("Laboratorium"),
            priority = 2, onPriorityChange = {},
            description = "Przygotować bazę danych.", onDescriptionChange = {},
            onSaveClick = {}, onNavigateBack = {}
        )
    }
}