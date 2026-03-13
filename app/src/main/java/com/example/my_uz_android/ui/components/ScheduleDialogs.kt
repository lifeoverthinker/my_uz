package com.example.my_uz_android.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R

@Composable
fun SubgroupFilterDialog(
    subgroups: List<String>,
    selectedSubgroups: Set<String>,
    onDismiss: () -> Unit,
    onSelectionChange: (Set<String>) -> Unit
) {
    BaseScheduleDialog(
        onDismiss = onDismiss,
        icon = R.drawable.ic_users,
        title = "Widoczne podgrupy",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Wybierz podgrupy do wyświetlenia na planie.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (subgroups.isEmpty()) {
                    Text(
                        text = "Brak zdefiniowanych podgrup w pobranym planie.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight(0.4f)
                            .fillMaxWidth()
                    ) {
                        items(subgroups) { subgroup ->
                            val isSelected = selectedSubgroups.contains(subgroup)
                            val displayName = if (subgroup.isBlank()) "Cała grupa" else subgroup

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val new = if (isSelected) selectedSubgroups - subgroup else selectedSubgroups + subgroup
                                        onSelectionChange(new)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "Gotowe",
        dismissButton = {
            TextButton(onClick = { onSelectionChange(subgroups.toSet()) }) {
                Text("Wszystkie", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary))
            }
        }
    )
}

@Composable
fun TeacherInfoDialog(
    onDismiss: () -> Unit,
    fullName: String,
    department: String,
    email: String
) {
    val context = LocalContext.current
    BaseScheduleDialog(
        onDismiss = onDismiss,
        icon = R.drawable.ic_user,
        title = "Szczegóły nauczyciela",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DialogInfoSection(label = "Dane nauczyciela", value = fullName)

                val deptDisplay = if (department.isNotBlank()) department else "Brak informacji o jednostce"
                DialogInfoSection(label = "Instytut / Katedra", value = deptDisplay)

                val emailDisplay = if (email.isNotBlank()) email else "Brak adresu e-mail"

                Column {
                    DialogLabel(text = "Adres e-mail")
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        onClick = {
                            if (email.isNotBlank() && email != "Brak adresu e-mail") {
                                copyToClipboard(context, email)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Subtelniejsze tło
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mail),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = emailDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButtonText = "Zamknij"
    )
}

@Composable
private fun BaseScheduleDialog(
    onDismiss: () -> Unit,
    icon: Int,
    title: String,
    content: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp // Google Calendar style size
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = content,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = confirmButtonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = dismissButton,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp // Kluczowe dla płaskiego wyglądu
    )
}

@Composable
private fun DialogLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun DialogInfoSection(label: String, value: String) {
    Column {
        DialogLabel(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Email", text))
    Toast.makeText(context, "Skopiowano do schowka", Toast.LENGTH_SHORT).show()
}