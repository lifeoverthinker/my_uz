package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.getAppBackgroundColor
import com.example.my_uz_android.ui.theme.taskPriorityDotColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskDetailsScreenRoute(
    viewModel: TaskDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDuplicateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task

    if (uiState.isLoading) {
        DetailsLoadingState()
        return
    }

    if (task == null) {
        DetailsEmptyState(
            title = stringResource(R.string.task_details_empty_title),
            description = stringResource(R.string.task_details_empty_message)
        )
        return
    }

    TaskDetailsScreen(
        task = task,
        onBackClick = onNavigateBack,
        onEditClick = { onEditClick(task.id) },
        onDeleteClick = { viewModel.deleteTask(onSuccess = onNavigateBack) },
        onDuplicateClick = onDuplicateClick,
        onToggleCompletionClick = viewModel::toggleTaskCompletion
    )
}

@Composable
fun TaskDetailsScreen(
    task: TaskEntity,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onToggleCompletionClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    DetailsScaffold(
        onBackClick = onBackClick,
        actions = {
            TopBarActionIcon(
                icon = R.drawable.ic_edit,
                isFilled = true,
                onClick = onEditClick
            )
            Box {
                TopBarActionIcon(
                    icon = R.drawable.ic_dots_vertical,
                    isFilled = true,
                    onClick = { showMenu = true }
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.btn_duplicate)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_copy),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDuplicateClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_trash),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    ) {
        DetailsHeader(
            title = task.title,
            subtitle = formatDate(task.dueDate),
            squareColor = getAppBackgroundColor(1)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(taskPriorityDotColor(task.priority), CircleShape)
                )
                Text(
                    text = stringResource(taskPriorityLabelResTaskDetails(task.priority)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TaskCompletionButton(
            isCompleted = task.isCompleted,
            onClick = onToggleCompletionClick
        )

        if (!task.subjectName.isNullOrBlank()) {
            DetailsInfoRow(
                iconRes = R.drawable.ic_graduation_hat,
                label = stringResource(R.string.label_subject_details),
                value = formatSentenceCaseTaskDetails(task.subjectName ?: "")
            )
        }

        if (task.hasReminder && task.reminderTime != null) {
            DetailsInfoRow(
                iconRes = R.drawable.ic_bell,
                label = stringResource(R.string.label_set_reminder),
                value = formatDateTime(task.reminderTime)
            )
        }

        task.description?.let {
            if (it.isNotBlank()) {
                DetailsInfoRow(
                    iconRes = R.drawable.ic_menu_2,
                    label = stringResource(R.string.label_description),
                    value = formatSentenceCaseTaskDetails(it),
                    isMultiline = true
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            itemType = stringResource(R.string.item_type_task)
        )
    }
}

@Composable
private fun TaskCompletionButton(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .heightIn(min = 48.dp)
    val buttonShape = RoundedCornerShape(100.dp)
    val buttonContent: @Composable RowScope.() -> Unit = {
        Icon(
            imageVector = if (isCompleted) Icons.Default.Refresh else Icons.Default.Done,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                if (isCompleted) R.string.task_details_btn_undo_complete else R.string.task_details_btn_complete
            ),
            style = MaterialTheme.typography.labelLarge
        )
    }

    if (isCompleted) {
        FilledTonalButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = buttonShape,
            content = buttonContent
        )
    } else {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = buttonShape,
            content = buttonContent
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    itemType: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_confirmation_title)) },
        text = { Text(stringResource(R.string.delete_confirmation_message, itemType)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        .format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

private fun taskPriorityLabelResTaskDetails(priority: Int): Int {
    return when (priority) {
        0 -> R.string.priority_low
        2 -> R.string.priority_high
        else -> R.string.priority_medium
    }
}

private fun formatSentenceCaseTaskDetails(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return trimmed
    val lowercased = trimmed.lowercase(Locale.getDefault())
    return lowercased.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailsScreenPreview() {
    MyUZTheme {
        TaskDetailsScreen(
            task = TaskEntity(
                id = 1,
                title = "Przygotować prezentację",
                description = "Dodać slajdy o architekturze Compose.",
                dueDate = System.currentTimeMillis(),
                isCompleted = false,
                subjectName = "Programowanie",
                classType = "L",
                hasReminder = true,
                reminderTime = System.currentTimeMillis() + 3600_000,
                priority = 1
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDuplicateClick = {},
            onToggleCompletionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailsScreenCompletedPreview() {
    MyUZTheme {
        TaskDetailsScreen(
            task = TaskEntity(
                id = 2,
                title = "Oddać sprawozdanie z laboratorium",
                description = "",
                dueDate = System.currentTimeMillis(),
                isCompleted = true,
                subjectName = "Fizyka",
                classType = "L",
                hasReminder = false,
                reminderTime = null,
                priority = 2
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDuplicateClick = {},
            onToggleCompletionClick = {}
        )
    }
}