package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

// --- WRAPPER DLA NAWIGACJI ---
@Composable
fun TaskDetailsScreenRoute(
    viewModel: TaskDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDuplicateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task

    if (uiState.isLoading || task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val localTime = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalTime()

    val mockedHasReminder = false
    val mockedReminderDate = System.currentTimeMillis()
    val mockedReminderTime = "08:00"

    TaskDetailsScreen(
        title = task.title,
        subjectName = task.subjectName ?: "",
        classType = ClassTypeUtils.getFullName(task.classType),
        isAllDay = task.isAllDay,
        dateMillis = task.dueDate,
        timeHour = localTime.hour,
        timeMinute = localTime.minute,
        hasReminder = mockedHasReminder,
        reminderDateMillis = mockedReminderDate,
        reminderTimeText = mockedReminderTime,
        description = task.description ?: "",
        priority = task.priority,
        isCompleted = task.isCompleted,
        onCompletionChange = { viewModel.toggleTaskCompletion() },
        onNavigateBack = onNavigateBack,
        onEditClick = { onEditClick(task.id) },
        onDeleteClick = { viewModel.deleteTask(onSuccess = onNavigateBack) },
        onDuplicateClick = onDuplicateClick
    )
}

// --- BEZSTANOWY WIDOK ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    title: String,
    subjectName: String,
    classType: String,
    isAllDay: Boolean,
    dateMillis: Long,
    timeHour: Int,
    timeMinute: Int,
    hasReminder: Boolean,
    reminderDateMillis: Long,
    reminderTimeText: String,
    description: String,
    priority: Int,
    isCompleted: Boolean,
    onCompletionChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "",
                navigationIcon = R.drawable.ic_close,
                isNavigationIconFilled = true,
                onNavigationClick = onNavigateBack,
                actions = {
                    TopBarActionIcon(icon = R.drawable.ic_edit, onClick = onEditClick)
                    Box {
                        TopBarActionIcon(icon = R.drawable.ic_dots_vertical, onClick = { showMenu = true })
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Duplikuj zadanie") },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_copy), contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = { showMenu = false; onDuplicateClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Usuń zadanie", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_trash), contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                                onClick = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Nagłówek z zachowaniem linii wizualnej ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(16.dp).background(Color(0xFF4285F4), RoundedCornerShape(4.dp)))
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val timeString = if (isAllDay) "Cały dzień" else String.format(Locale.getDefault(), "%02d:%02d", timeHour, timeMinute)
                    Text(text = "${formatDate(dateMillis)} • $timeString", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Status zadania (Nowy, klikalny wiersz) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCompletionChange(!isCompleted) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = null // Zdarzenie idzie do modyfikatora Clickable na Row
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isCompleted) "Zaliczone" else "Do zrobienia",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (isCompleted) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // --- 1. Przypomnienie ---
            if (hasReminder) {
                DetailRow(iconRes = R.drawable.ic_bell, label = "Przypomnienie", value = "${formatDate(reminderDateMillis)} • $reminderTimeText")
            }
            // --- 2. Przedmiot ---
            if (subjectName.isNotBlank()) {
                DetailRow(iconRes = R.drawable.ic_graduation_hat, label = "Przedmiot", value = subjectName)
            }
            // --- 3. Rodzaj zajęć ---
            if (classType.isNotBlank() && classType != "Rodzaj zajęć (opcjonalne)") {
                DetailRow(iconRes = R.drawable.ic_stand, label = "Rodzaj zajęć", value = classType)
            }

            // --- 4. Priorytet ---
            val (priorityText, priorityColor) = when (priority) {
                2 -> "Wysoki priorytet" to MaterialTheme.colorScheme.error
                1 -> "Średni priorytet" to Color(0xFFF57C00)
                else -> "Niski priorytet" to Color(0xFF388E3C)
            }
            DetailRow(iconRes = R.drawable.ic_info_circle, label = "Priorytet", value = priorityText, valueColor = priorityColor)

            // --- 5. Opis ---
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(iconRes = R.drawable.ic_menu_2, label = null, value = description, isMultiline = true)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń zadanie") },
                text = { Text("Czy na pewno chcesz usunąć to zadanie? Tej operacji nie można cofnąć.") },
                confirmButton = { TextButton(onClick = { onDeleteClick(); showDeleteDialog = false }) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") } }
            )
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int, label: String?, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = if (isMultiline) 4.dp else 0.dp).size(24.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            if (label != null) Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value.ifEmpty { "-" }, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = valueColor)
        }
    }
}

private fun formatDate(timestamp: Long): String = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL")).format(Date(timestamp)).replaceFirstChar { it.uppercase() }

@Preview(showBackground = true)
@Composable
fun TaskDetailsScreenPreview() {
    MaterialTheme {
        TaskDetailsScreen(
            title = "Napisać esej na zaliczenie", subjectName = "Psychologia Ogólna", classType = "Wykład",
            isAllDay = false, dateMillis = System.currentTimeMillis(), timeHour = 23, timeMinute = 59,
            hasReminder = true, reminderDateMillis = System.currentTimeMillis(), reminderTimeText = "20:00",
            description = "Temat: Wpływ stresu na procesy poznawcze. Minimum 5 stron, formatowanie APA.",
            priority = 2, isCompleted = false, onCompletionChange = {}, onNavigateBack = {},
            onEditClick = {}, onDeleteClick = {}, onDuplicateClick = {}
        )
    }
}