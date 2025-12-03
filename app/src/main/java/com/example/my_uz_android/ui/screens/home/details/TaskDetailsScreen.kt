package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    TaskDetailsContent(
        task = uiState.task,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onEditTask = onEditTask,
        onDeleteTask = {
            viewModel.deleteTask()
            onNavigateBack()
        },
        onDuplicateTask = { task ->
            viewModel.duplicateTask(task)
            onNavigateBack()
        },
        onToggleCompletion = { task ->
            viewModel.toggleTaskCompletion(task)
        }
    )
}

@Composable
fun TaskDetailsContent(
    task: TaskEntity?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: () -> Unit,
    onDuplicateTask: (TaskEntity) -> Unit,
    onToggleCompletion: (TaskEntity) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val taskAccentColor = MaterialTheme.extendedColors.classCardBackground
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header (X, Edytuj, Opcje) - Padding zgodny z AddEdit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 10) onNavigateBack() }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailIconBox(onClick = onNavigateBack) {
                    Icon(painter = painterResource(id = R.drawable.ic_x_close), contentDescription = "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                if (task != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditTask(task.id) }) {
                            Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "Edytuj", tint = textColor, modifier = Modifier.size(24.dp))
                        }

                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(painter = painterResource(id = R.drawable.ic_dots_vertical), contentDescription = "Opcje", tint = textColor, modifier = Modifier.size(24.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                                DropdownMenuItem(text = { Text("Duplikuj", fontFamily = InterFontFamily, color = textColor) }, onClick = { onDuplicateTask(task); showMenu = false })
                                DropdownMenuItem(text = { Text("Usuń", fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.error) }, onClick = { onDeleteTask(); showMenu = false })
                            }
                        }
                    }
                }
            }

            if (task != null) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {

                    // 1. TYTUŁ SEKCJA (Pixel Perfect z AddEdit)
                    // Padding horizontal 16dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DetailIconBox {
                            Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(taskAccentColor))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            val dateString = if (task.dueDate > 0) {
                                try {
                                    val date = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault())
                                    val timeStr = task.dueTime?.let { ", $it" } ?: ""
                                    date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("pl"))) + timeStr
                                } catch (e: Exception) { "Brak terminu" }
                            } else "Brak terminu"

                            Text(text = dateString, fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = subTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (task.subjectName.isNotEmpty()) {
                        DetailSection(label = "PRZEDMIOT", text = task.subjectName, iconRes = R.drawable.ic_book_open, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }

                    if (task.classType.isNotEmpty()) {
                        DetailSection(label = "RODZAJ ZAJĘĆ", text = task.classType, iconRes = R.drawable.ic_graduation_hat, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }

                    if (!task.description.isNullOrEmpty()) {
                        DetailSection(label = "OPIS", text = task.description, iconRes = R.drawable.ic_menu_2, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }

                    DetailSection(label = "STATUS", text = if (task.isCompleted) "Zakończone" else "W toku", iconRes = if (task.isCompleted) R.drawable.ic_check_circle_broken else R.drawable.ic_info_circle, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                }

                // Padding buttona
                Button(
                    onClick = { onToggleCompletion(task) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary, contentColor = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(text = if (task.isCompleted) "Oznacz jako nieukończone" else "Oznacz jako ukończone", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nie znaleziono zadania", color = textColor) }
            }
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).clickable(enabled = onClick != null) { onClick?.invoke() }, contentAlignment = Alignment.Center, content = content)
}

@Composable
private fun DetailSection(label: String, text: String, iconRes: Int, iconColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color, labelColor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalAlignment = Alignment.Top) {
        DetailIconBox { Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(text = label, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = labelColor, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 2.dp))
            Text(text = text, fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = textColor, lineHeight = 22.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailsPreview() {
    TaskDetailsContent(
        task = TaskEntity(
            id = 1,
            title = "Projekt Zespołowy",
            description = "Przygotować prezentację z postępów prac.",
            dueDate = System.currentTimeMillis(),
            dueTime = "14:30",
            subjectName = "Inżynieria Oprogramowania",
            classType = "Laboratorium",
            isCompleted = false
        ),
        isLoading = false,
        onNavigateBack = {}, onEditTask = {}, onDeleteTask = {}, onDuplicateTask = {}, onToggleCompletion = {}
    )
}