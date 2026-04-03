package com.example.my_uz_android.ui.components

/**
 * Zestaw dialogów pomocniczych używanych w module kalendarza.
 * Obejmuje filtrowanie podgrup oraz prezentację danych prowadzącego,
 * wraz ze współdzielonym stylem bazowego okna dialogowego.
 */

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
import com.example.my_uz_android.R
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
/**
 * Wyświetla dialog filtrowania podgrup planu zajęć.
 *
 * @param subgroups Lista wszystkich dostępnych podgrup.
 * @param selectedSubgroups Zbiór aktualnie zaznaczonych podgrup.
 * @param onDismiss Callback zamykający dialog.
 * @param onSelectionChange Callback zwracający nowy zestaw zaznaczeń.
 */
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight(0.4f)
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp)
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
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "Gotowe",
        dismissButton = {
            TextButton(onClick = { onSelectionChange(subgroups.toSet()) }) {
                Text("Wszystkie", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
/**
 * Wyświetla dialog ze szczegółowymi informacjami o wykładowcy.
 *
 * @param onDismiss Callback zamykający dialog.
 * @param fullName Imię i nazwisko prowadzącego.
 * @param department Nazwa instytutu lub katedry.
 * @param email Adres e-mail prowadzącego.
 */
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
        title = "Szczegóły wykładowcy",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DialogInfoSection(label = "Dane wykładowcy", value = fullName)

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
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Kopiuj",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_copy),
                                    contentDescription = "Kopiuj",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
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
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = content,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = confirmButtonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        },
        dismissButton = dismissButton,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp
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
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        DialogLabel(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Email", text))
    Toast.makeText(context, "Skopiowano do schowka", Toast.LENGTH_SHORT).show()
}