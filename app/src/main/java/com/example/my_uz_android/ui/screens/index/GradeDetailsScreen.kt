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
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GradeDetailsScreen(
    gradeId: Int,
    onNavigateBack: () -> Unit,
    onEditGrade: (Int) -> Unit,
    onDuplicateClick: () -> Unit,
    viewModel: GradeDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GradeDetailsContent(
        grade = uiState.grade,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onEditGrade = onEditGrade,
        onDeleteGrade = { viewModel.deleteGrade(onSuccess = onNavigateBack) },
        onDuplicateClick = onDuplicateClick
    )
}

@Composable
fun GradeDetailsContent(
    grade: GradeEntity?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEditGrade: (Int) -> Unit,
    onDeleteGrade: () -> Unit,
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
                    Icon(painterResource(id = R.drawable.ic_x_close), "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                if (grade != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditGrade(grade.id) }) {
                            Icon(painterResource(id = R.drawable.ic_edit), "Edytuj", tint = textColor, modifier = Modifier.size(24.dp))
                        }

                        Box {
                            DetailIconBox(onClick = { showMenu = true }) {
                                Icon(painterResource(id = R.drawable.ic_dots_vertical), "Opcje", tint = textColor, modifier = Modifier.size(24.dp))
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(surfaceColor).width(180.dp)
                            ) {
                                DropdownMenuItem(text = { Text("Duplikuj", style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(painterResource(R.drawable.ic_copy), null, Modifier.size(20.dp)) }, onClick = { showMenu = false; onDuplicateClick() })
                                DropdownMenuItem(text = { Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(painterResource(R.drawable.ic_trash), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; showDeleteDialog = true })
                            }
                        }
                    }
                }
            }

            // --- STREFA SCROLLOWANIA ---
            if (grade != null) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                        DetailIconBox { Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primaryContainer)) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = grade.description ?: "Ocena", style = MaterialTheme.typography.titleLarge, color = textColor)
                            Text(text = formatDate(grade.date), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = subTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSectionGrade("PRZEDMIOT", grade.subjectName, R.drawable.ic_book_open, iconTint, textColor, subTextColor)

                    if (grade.classType.isNotEmpty()) {
                        DetailSectionGrade("RODZAJ ZAJĘĆ", ClassTypeUtils.getFullName(grade.classType), R.drawable.ic_graduation_hat, iconTint, textColor, subTextColor)
                    }

                    val gradeText = when {
                        grade.isPoints -> if (grade.grade % 1.0 == 0.0) "${grade.grade.toInt()} pkt" else "${grade.grade} pkt"
                        grade.grade == -1.0 -> "Aktywność +"
                        grade.grade % 1.0 == 0.0 -> grade.grade.toInt().toString()
                        else -> grade.grade.toString()
                    }

                    DetailSectionGrade(if (grade.isPoints) "PUNKTY" else "OCENA", gradeText, R.drawable.ic_trophy, iconTint, textColor, subTextColor)

                    if (!grade.isPoints) {
                        DetailSectionGrade("WAGA", grade.weight.toString(), R.drawable.ic_scales, iconTint, textColor, subTextColor)
                    }

                    if (!grade.comment.isNullOrEmpty()) {
                        DetailSectionGrade("OPIS", grade.comment, R.drawable.ic_menu_2, iconTint, textColor, subTextColor)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń ocenę", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
                text = { Text("Czy na pewno chcesz usunąć tę ocenę?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = { TextButton(onClick = { onDeleteGrade(); showDeleteDialog = false }) { Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge) } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj", style = MaterialTheme.typography.labelLarge) } },
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
private fun DetailSectionGrade(
    label: String,
    text: String,
    iconRes: Int,
    iconTint: Color,
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
                tint = iconTint,
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