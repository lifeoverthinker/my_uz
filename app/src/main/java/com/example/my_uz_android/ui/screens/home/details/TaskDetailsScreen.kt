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
import androidx.compose.ui.graphics.Color // ✅ Naprawiono brakujący import
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.calendar.TaskDetailsViewModel
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.util.ClassTypeUtils
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
        task = uiState.taskEntity,
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

    // Obsługa kolorów Dark Mode dla statusu
    val taskAccentColor = if (task?.isCompleted == true) Color.Gray else MaterialTheme.extendedColors.classCardBackground

    // Używamy surfaceContainerLowest: Biały w Light Mode, Ciemny w Dark Mode
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 10) onNavigateBack()
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailIconBox(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = stringResource(R.string.btn_close),
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (task != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditTask(task.id) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = stringResource(R.string.btn_edit),
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dots_vertical),
                                    contentDescription = stringResource(R.string.options_menu),
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
                                    text = { Text(stringResource(R.string.btn_duplicate), fontFamily = InterFontFamily, color = textColor) },
                                    onClick = { onDuplicateTask(task); showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.btn_delete), fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.error) },
                                    onClick = { onDeleteTask(); showMenu = false }
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
                    // Tytuł
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
                                    .background(taskAccentColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = if (task.isCompleted) textColor.copy(alpha = 0.6f) else textColor,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            val dateString = if (task.dueDate > 0) {
                                try {
                                    val date = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault())
                                    val timeStr = task.dueTime?.let { ", $it" } ?: ""
                                    date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("pl"))) + timeStr
                                } catch (e: Exception) {
                                    stringResource(R.string.task_no_date)
                                }
                            } else stringResource(R.string.task_no_date)

                            Text(
                                text = dateString,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = subTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (task.subjectName.isNotEmpty()) {
                        DetailSection(
                            label = stringResource(R.string.label_subject),
                            text = task.subjectName,
                            iconRes = R.drawable.ic_book_open,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    if (task.classType.isNotEmpty()) {
                        DetailSection(
                            label = stringResource(R.string.label_type),
                            text = ClassTypeUtils.getFullName(task.classType),
                            iconRes = R.drawable.ic_graduation_hat,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    if (!task.description.isNullOrEmpty()) {
                        DetailSection(
                            label = stringResource(R.string.label_description),
                            text = task.description,
                            iconRes = R.drawable.ic_menu_2,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    DetailSection(
                        label = stringResource(R.string.label_status),
                        text = stringResource(if (task.isCompleted) R.string.task_status_completed else R.string.task_status_in_progress),
                        iconRes = if (task.isCompleted) R.drawable.ic_check_circle_broken else R.drawable.ic_info_circle,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }

                // Przycisk akcji
                Button(
                    onClick = { onToggleCompletion(task) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = stringResource(if (task.isCompleted) R.string.task_mark_incomplete else R.string.task_mark_complete),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.task_not_found), color = textColor)
                }
            }
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
private fun DetailSection(
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
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = labelColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textColor,
                lineHeight = 22.sp
            )
        }
    }
}