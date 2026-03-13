package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    onDuplicateTask: (String, String, String, String, Long, Boolean) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val task = uiState.task

    TaskDetailsContent(
        task = task,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onEditTask = onEditTask,
        onDeleteTask = { viewModel.deleteTask(onSuccess = onNavigateBack) },
        onDuplicateTask = {
            task?.let {
                onDuplicateTask(it.title, it.description ?: "", it.subjectName ?: "", it.classType ?: "", it.dueDate, it.isAllDay)
            }
        },
        onToggleCompletion = { viewModel.toggleTaskCompletion() }
    )
}

// Podmień całą funkcję TaskDetailsContent
@Composable
fun TaskDetailsContent(
    task: TaskEntity?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: () -> Unit,
    onDuplicateTask: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailIconBox(onClick = onNavigateBack) {
                    Icon(painterResource(R.drawable.ic_x_close), "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                if (task != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditTask(task.id) }) {
                            Icon(painterResource(R.drawable.ic_edit), "Edytuj", tint = textColor, modifier = Modifier.size(24.dp))
                        }
                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(painterResource(R.drawable.ic_dots_vertical), "Opcje", tint = textColor, modifier = Modifier.size(24.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(surfaceColor).width(180.dp)) {
                                DropdownMenuItem(text = { Text("Duplikuj", style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(painterResource(R.drawable.ic_copy), null, Modifier.size(20.dp)) }, onClick = { showMenu = false; onDuplicateTask() })
                                DropdownMenuItem(text = { Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(painterResource(R.drawable.ic_trash), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; showDeleteDialog = true })
                            }
                        }
                    }
                }
            }

            // --- STREFA SCROLLOWANIA ---
            if (task != null) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                        DetailIconBox { Box(Modifier.size(18.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(MaterialTheme.extendedColors.taskCardBackground)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = task.title, style = MaterialTheme.typography.titleLarge, color = textColor, textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                            Text(text = formatTaskDate(task.dueDate), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = subTextColor)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    DetailSectionTask("STATUS", if (task.isCompleted) "Ukończone" else "Do zrobienia", if (task.isCompleted) R.drawable.ic_check_square_broken else R.drawable.ic_square, if (task.isCompleted) MaterialTheme.colorScheme.primary else iconTint, if (task.isCompleted) MaterialTheme.colorScheme.primary else textColor, subTextColor, modifier = Modifier.clickable { onToggleCompletion() })
                    if (!task.subjectName.isNullOrEmpty()) DetailSectionTask("PRZEDMIOT", task.subjectName, R.drawable.ic_book_open, iconTint, textColor, subTextColor)
                    if (!task.classType.isNullOrEmpty()) DetailSectionTask("RODZAJ ZAJĘĆ", ClassTypeUtils.getFullName(task.classType), R.drawable.ic_graduation_hat, iconTint, textColor, subTextColor)
                    val timeString = if (task.isAllDay) "Cały dzień" else java.time.format.DateTimeFormatter.ofPattern("HH:mm").withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(task.dueDate))
                    DetailSectionTask("GODZINA", timeString, R.drawable.ic_clock, iconTint, textColor, subTextColor)
                    if (!task.description.isNullOrEmpty()) DetailSectionTask("OPIS", task.description, R.drawable.ic_menu_2, iconTint, textColor, subTextColor)
                    Spacer(Modifier.height(100.dp))
                }
            } else if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("Usuń zadanie", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) }, text = { Text("Czy na pewno chcesz usunąć to zadanie?", style = MaterialTheme.typography.bodyMedium) }, confirmButton = { TextButton(onClick = { onDeleteTask(); showDeleteDialog = false }) { Text("Usuń", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") } }, containerColor = surfaceColor)
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center, content = content
    )
}

@Composable
private fun DetailSectionTask(
    label: String, text: String, iconRes: Int, iconColor: Color, textColor: Color, labelColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalAlignment = Alignment.Top) {
        DetailIconBox { Icon(painterResource(iconRes), null, tint = iconColor, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(top = 4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor, modifier = Modifier.padding(bottom = 2.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = textColor)
        }
    }
}

private fun formatTaskDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("pl"))
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}