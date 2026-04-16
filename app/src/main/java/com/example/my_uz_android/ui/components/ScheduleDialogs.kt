package com.example.my_uz_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.util.ClassTypeUtils
import androidx.compose.ui.res.stringResource
import com.example.my_uz_android.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast

/**
 * Okno dialogowe pokazujące informacje kontaktowe wykładowcy.
 */
@Composable
fun TeacherInfoDialog(
    onDismiss: () -> Unit,
    fullName: String,
    department: String,
    email: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.dialog_teacher_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = department,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (email.isNotBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = stringResource(R.string.dialog_teacher_email_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(email))
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.dialog_teacher_email_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.dialog_teacher_copy_email))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Okno dialogowe pozwalające na wybór/odznaczenie pojedynczych podgrup w planie "SchedulePreview".
 */
@Composable
fun SubgroupFilterDialog(
    subgroups: List<String>,
    selectedSubgroups: Set<String>,
    onDismiss: () -> Unit,
    onSelectionChange: (Set<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_filter_subgroups_title)) },
        text = {
            LazyColumn {
                items(subgroups) { subgroup ->
                    val subgroupLabel = if (subgroup.isBlank()) {
                        stringResource(R.string.label_whole_group)
                    } else {
                        subgroup
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newSelection = if (selectedSubgroups.contains(subgroup)) {
                                    selectedSubgroups - subgroup
                                } else {
                                    selectedSubgroups + subgroup
                                }
                                onSelectionChange(newSelection)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSubgroups.contains(subgroup),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = subgroupLabel)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_done)) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Okno dialogowe filtrowania głównego planu zajęć.
 * Zoptymalizowane - brak niepotrzebnych, podwójnych teł Surface.
 */
@Composable
fun FilterDialog(
    groups: List<String>,
    selectedGroups: Set<String>,
    classTypes: List<String>,
    selectedClassTypes: Set<String>,
    onGroupSelected: (String, Boolean) -> Unit,
    onClassTypeSelected: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_filter_schedule_title)) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (groups.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.label_subgroups),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                        )
                    }
                    items(groups) { group ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onGroupSelected(group, !selectedGroups.contains(group)) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedGroups.contains(group),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = group,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                if (classTypes.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.label_type),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                        )
                    }
                    items(classTypes) { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onClassTypeSelected(type, !selectedClassTypes.contains(type)) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedClassTypes.contains(type),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = ClassTypeUtils.getFullName(type),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_done))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}