package com.example.my_uz_android.ui.screens.index

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AbsenceDetailsScreen(
    absenceId: Int,
    onNavigateBack: () -> Unit,
    onEditAbsence: (Int) -> Unit,
    onDuplicateClick: (String?, String?) -> Unit,
    viewModel: AbsenceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AbsenceDetailsContent(
        absence = uiState.absence,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onEditAbsence = onEditAbsence,
        onDeleteAbsence = { viewModel.deleteAbsence(onSuccess = onNavigateBack) },
        onDuplicateClick = {
            uiState.absence?.let { onDuplicateClick(it.subjectName, it.classType) }
        }
    )
}

@Composable
fun AbsenceDetailsContent(
    absence: AbsenceEntity?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEditAbsence: (Int) -> Unit,
    onDeleteAbsence: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

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
                    Icon(
                        painterResource(id = R.drawable.ic_x_close),
                        "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (absence != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditAbsence(absence.id) }) {
                            Icon(
                                painterResource(id = R.drawable.ic_edit),
                                "Edytuj",
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_dots_vertical),
                                    "Opcje",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(surfaceColor)
                                    .width(180.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplikuj", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_copy), null, Modifier.size(20.dp)) },
                                    onClick = { showMenu = false; onDuplicateClick() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_trash), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) },
                                    onClick = { showMenu = false; showDeleteDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            // --- STREFA SCROLLOWANIA ---
            if (absence != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DetailIconBox {
                            Box(
                                Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nieobecność",
                                style = MaterialTheme.typography.titleLarge,
                                color = textColor
                            )
                            Text(
                                text = formatDate(absence.date),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = subTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSectionAbsence("PRZEDMIOT", absence.subjectName ?: "", R.drawable.ic_book_open, textColor, subTextColor)
                    DetailSectionAbsence("RODZAJ ZAJĘĆ", ClassTypeUtils.getFullName(absence.classType), R.drawable.ic_graduation_hat, textColor, subTextColor)

                    if (!absence.description.isNullOrEmpty()) {
                        DetailSectionAbsence("OPIS", absence.description, R.drawable.ic_menu_2, textColor, subTextColor)
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń wpis", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
                text = { Text("Czy na pewno chcesz usunąć tę nieobecność?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = { onDeleteAbsence(); showDeleteDialog = false }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Anuluj", style = MaterialTheme.typography.labelLarge)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = surfaceColor
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
private fun DetailSectionAbsence(
    label: String,
    text: String,
    iconRes: Int,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = text.ifEmpty { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl"))
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}