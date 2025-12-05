package com.example.my_uz_android.ui.screens.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.components.TimePicker
import com.example.my_uz_android.ui.theme.InterFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskAddEditScreen(
    taskId: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskAddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load task if editing
    LaunchedEffect(taskId) {
        if (taskId != null && taskId != -1) {
            viewModel.loadTask(taskId)
        }
    }

    TaskAddEditContent(
        uiState = uiState,
        taskId = taskId,
        onNavigateBack = onNavigateBack,
        onSaveTask = {
            viewModel.saveTask()
            Toast.makeText(context, "Zadanie zapisane", Toast.LENGTH_SHORT).show()
            onNavigateBack()
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
    taskId: Int?,
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
    // ✅ BIAŁA bottom section
    val surfaceColor = Color.White
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    // Quick titles
    val quickTitles = listOf("Zadanie domowe", "Kolokwium", "Wejściówka", "Egzamin", "Projekt", "Prezentacja")

    // Modal states
    var showSubjectModal by remember { mutableStateOf(false) }
    var showTypeModal by remember { mutableStateOf(false) }
    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Default values
    val startDate = uiState.startDate ?: LocalDate.now()
    val endDate = uiState.endDate ?: startDate
    val startTime = uiState.startTime ?: LocalTime.of(8, 0)
    val endTime = uiState.endTime ?: LocalTime.of(10, 0)

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========== HEADER ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp) // ✅ 24dp jak w Details
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ✅ Menu z opcją DELETE (zamiast przycisku na górze)
                    if (taskId != null && taskId != -1) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dots_vertical),
                                    contentDescription = "Opcje",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp) // ✅ 24dp
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Usuń", fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.error) },
                                    onClick = { onDeleteTask(); showMenu = false }
                                )
                            }
                        }
                    }

                    // Save button
                    Button(
                        onClick = onSaveTask,
                        enabled = uiState.title.isNotBlank(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Zapisz", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ========== 1. TYTUŁ ==========
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            textStyle = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = textColor
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "Dodaj tytuł",
                                        style = TextStyle(
                                            fontFamily = InterFontFamily,
                                            fontSize = 28.sp,
                                            color = textColor.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // ========== QUICK CHIPS ==========
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 76.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTitles) { title ->
                        val isSelected = uiState.title == title
                        Surface(
                            onClick = { onTitleChange(title) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primaryColor else Color.Transparent,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, dividerColor) else null,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = TextStyle(
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else textColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor)

                // ========== 2. CAŁY DZIEŃ + DATA/CZAS ==========
                CommonRow(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Switch "Cały dzień"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cały dzień",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = textColor
                            )

                            Switch(
                                checked = uiState.isAllDay,
                                onCheckedChange = onIsAllDayChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Divider tylko gdy NIE cały dzień
                        if (!uiState.isAllDay) {
                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                        }

                        // LOGIKA JAK W GOOGLE CALENDAR
                        if (uiState.isAllDay) {
                            // Tryb "Cały dzień" - DWA RZĘDY DAT
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Data początku
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePickerStart = true }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = formatDateLong(startDate),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                        color = textColor
                                    )
                                }

                                // Data końca
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePickerEnd = true }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = formatDateLong(endDate),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                        color = textColor
                                    )
                                }
                            }
                        } else {
                            // Tryb zwykły - Data (lewa) + Czas (prawa) - JEDEN RZĄD
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // DATA (lewa)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showDatePickerStart = true }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = formatDateLong(startDate),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                        color = textColor
                                    )
                                }

                                // CZAS (prawa)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showTimePickerStart = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = formatTime(startTime),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerColor)

                // ========== 3. PRZEDMIOT ==========
                CommonRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSubjectModal = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.classSubject ?: "Przedmiot (opcjonalnie)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (uiState.classSubject == null) subTextColor else textColor
                        )

                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_down),
                            contentDescription = null,
                            tint = subTextColor,
                            modifier = Modifier.size(24.dp) // ✅ 24dp
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // ========== 4. RODZAJ ==========
                CommonRow(iconRes = R.drawable.ic_graduation_hat, iconTint = iconTint) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (uiState.classSubject != null) showTypeModal = true
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.classType ?: "Rodzaj (opcjonalnie)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (uiState.classType == null) subTextColor else textColor
                        )

                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_down),
                            contentDescription = null,
                            tint = subTextColor,
                            modifier = Modifier.size(24.dp) // ✅ 24dp
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // ========== 5. OPIS ==========
                CommonRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = InterFontFamily,
                                color = textColor,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.description.isEmpty()) {
                                    Text(
                                        "Dodaj opis",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = InterFontFamily,
                                            color = subTextColor
                                        )
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // ========== DIALOGI ==========
        // Date Picker - Start
        if (showDatePickerStart) {
            DatePicker(
                date = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                onDateSelected = { millis ->
                    onStartDateChange(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    showDatePickerStart = false
                },
                onDismiss = { showDatePickerStart = false }
            )
        }

        // Date Picker - End
        if (showDatePickerEnd) {
            DatePicker(
                date = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                onDateSelected = { millis ->
                    onEndDateChange(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    showDatePickerEnd = false
                },
                onDismiss = { showDatePickerEnd = false }
            )
        }

        // Time Picker - Start
        if (showTimePickerStart) {
            TimePicker(
                time = formatTime(startTime),
                onTimeSelected = { hour, minute ->
                    onStartTimeChange(LocalTime.of(hour, minute))
                    showTimePickerStart = false
                },
                onDismiss = { showTimePickerStart = false }
            )
        }

        // Time Picker - End
        if (showTimePickerEnd) {
            TimePicker(
                time = formatTime(endTime),
                onTimeSelected = { hour, minute ->
                    onEndTimeChange(LocalTime.of(hour, minute))
                    showTimePickerEnd = false
                },
                onDismiss = { showTimePickerEnd = false }
            )
        }

        // Subject Modal
        if (showSubjectModal) {
            Dialog(onDismissRequest = { showSubjectModal = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(vertical = 24.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.classSubject == null,
                                        onClick = {
                                            onSubjectChange(null)
                                            onClassTypeChange(null)
                                            showSubjectModal = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.classSubject == null,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Brak",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                    color = textColor
                                )
                            }
                        }
                        items(uiState.availableSubjects) { (subject, _) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.classSubject == subject,
                                        onClick = {
                                            onSubjectChange(subject)
                                            onClassTypeChange(null)
                                            showSubjectModal = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.classSubject == subject,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Type Modal
        if (showTypeModal) {
            val selectedSubject = uiState.availableSubjects.find { it.first == uiState.classSubject }
            val availableTypes = selectedSubject?.second ?: emptyList()

            Dialog(onDismissRequest = { showTypeModal = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(vertical = 24.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.classType == null,
                                        onClick = {
                                            onClassTypeChange(null)
                                            showTypeModal = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.classType == null,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Brak",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                    color = textColor
                                )
                            }
                        }
                        items(availableTypes) { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.classType == type,
                                        onClick = {
                                            onClassTypeChange(type)
                                            showTypeModal = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.classType == type,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== HELPER COMPONENTS ==========
@Composable
fun CommonRow(
    iconRes: Int,
    iconTint: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp) // ✅ 24dp jak w Details
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}

// ========== HELPER FUNCTIONS ==========
private fun formatDateLong(date: LocalDate): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))
        date.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) }
    } catch (e: Exception) {
        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}

private fun formatTime(time: LocalTime): String {
    return String.format("%02d:%02d", time.hour, time.minute)
}

// ========== COMPOSE PREVIEW ==========
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TaskAddEditScreenPreview() {
    com.example.my_uz_android.ui.theme.MyUZTheme {
        TaskAddEditScreen(
            taskId = null,
            onNavigateBack = { }
        )
    }
}
