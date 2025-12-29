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
import com.example.my_uz_android.ui.theme.extendedColors // IMPORT KONIECZNY
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val task = (uiState as? TaskDetailsUiState.Success)?.task

    TaskDetailsContent(
        task = task,
        isLoading = uiState is TaskDetailsUiState.Loading,
        onNavigateBack = onNavigateBack,
        onEditTask = onEditTask,
        onDeleteTask = { viewModel.deleteTask(onSuccess = onNavigateBack) },
        onDuplicateTask = { viewModel.duplicateTask(onSuccess = onNavigateBack) },
        onToggleCompletion = { viewModel.toggleTaskCompletion() }
    )
}

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
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (task != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditTask(task.id) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edytuj",
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dots_vertical),
                                    contentDescription = "Opcje",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplikuj", color = textColor) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateTask()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Usuń", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (task != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- TYTUŁ i DATA ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DetailIconBox {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    // ZMIANA: Używamy taskCardBackground (pastelowy niebieski), zamiast primaryContainer
                                    .background(MaterialTheme.extendedColors.taskCardBackground)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 4.dp),
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            )

                            Text(
                                text = formatTaskDate(task.dueDate),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = subTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- STATUS ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCompletion() }
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DetailIconBox {
                            Icon(
                                painter = painterResource(
                                    if (task.isCompleted) R.drawable.ic_check_square_broken else R.drawable.ic_square
                                ),
                                contentDescription = null,
                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = subTextColor,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            Text(
                                text = if (task.isCompleted) "Ukończone" else "Do zrobienia",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = if (task.isCompleted) MaterialTheme.colorScheme.primary else textColor
                            )
                        }
                    }

                    if (!task.subjectName.isNullOrEmpty()) {
                        DetailSectionTask(
                            label = "PRZEDMIOT",
                            text = task.subjectName,
                            iconRes = R.drawable.ic_book_open,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    if (!task.classType.isNullOrEmpty()) {
                        DetailSectionTask(
                            label = "RODZAJ ZAJĘĆ",
                            text = ClassTypeUtils.getFullName(task.classType),
                            iconRes = R.drawable.ic_graduation_hat,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    val timeString = if (task.isAllDay) "Cały dzień" else {
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                            .withZone(ZoneId.systemDefault())
                        timeFormatter.format(Instant.ofEpochMilli(task.dueDate))
                    }

                    DetailSectionTask(
                        label = "GODZINA",
                        text = timeString,
                        iconRes = R.drawable.ic_clock,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )

                    if (!task.description.isNullOrEmpty()) {
                        DetailSectionTask(
                            label = "OPIS",
                            text = task.description,
                            iconRes = R.drawable.ic_menu_2,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń zadanie", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)) },
                text = { Text("Czy na pewno chcesz usunąć to zadanie?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteTask()
                        showDeleteDialog = false
                    }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun DetailSectionTask(
    label: String,
    text: String,
    iconRes: Int,
    iconColor: Color,
    textColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        DetailIconBox {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        }
    }
}

private fun formatTaskDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("pl"))
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}