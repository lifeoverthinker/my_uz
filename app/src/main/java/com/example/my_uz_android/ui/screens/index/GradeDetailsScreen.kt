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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GradeDetailsScreen(
    onNavigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDuplicateGrade: (String, String, Double, Int, String, String) -> Unit,
    viewModel: GradeDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val grade = uiState.grade

    GradeDetailsContent(
        grade = grade,
        isLoading = grade == null,
        onNavigateBack = onNavigateBack,
        onEditGrade = onEdit,
        onDeleteGrade = {
            viewModel.deleteGrade(onSuccess = onNavigateBack)
        },
        onDuplicateClick = {
            grade?.let {
                // Duplikacja przesyła te same dane (subject, type, grade, weight, desc, comment)
                // Ale id zostanie nadane jako 0 (nowe) w formularzu docelowym
                onDuplicateGrade(
                    it.subjectName,
                    it.classType,
                    it.grade,
                    it.weight.toInt(),
                    it.description ?: "",
                    it.comment ?: ""
                )
            }
        }
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

                if (grade != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconBox(onClick = { onEditGrade(grade.id) }) {
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

                            // Menu ujednolicone z stylem terminarza
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface).width(180.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplikuj", style = MaterialTheme.typography.bodyLarge) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_copy), null, Modifier.size(20.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_trash), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) },
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

            if (grade != null) {
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
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = grade.description ?: "Ocena",
                                style = MaterialTheme.typography.headlineMedium,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Text(
                                text = formatDate(grade.date),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = subTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SZCZEGÓŁY ---
                    DetailSectionGrade(
                        label = "PRZEDMIOT",
                        text = grade.subjectName,
                        iconRes = R.drawable.ic_book_open,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )

                    if (grade.classType.isNotEmpty()) {
                        DetailSectionGrade(
                            label = "RODZAJ ZAJĘĆ",
                            text = ClassTypeUtils.getFullName(grade.classType),
                            iconRes = R.drawable.ic_graduation_hat,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    val gradeText = when {
                        grade.grade == -1.0 -> "Aktywność +"
                        grade.grade % 1.0 == 0.0 -> grade.grade.toInt().toString()
                        else -> grade.grade.toString()
                    }

                    DetailSectionGrade(
                        label = "OCENA",
                        text = gradeText,
                        iconRes = R.drawable.ic_trophy,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )

                    DetailSectionGrade(
                        label = "WAGA",
                        text = grade.weight.toString(),
                        iconRes = R.drawable.ic_scales,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )

                    if (!grade.comment.isNullOrEmpty()) {
                        DetailSectionGrade(
                            label = "OPIS",
                            text = grade.comment,
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
                title = { Text("Usuń ocenę", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)) },
                text = { Text("Czy na pewno chcesz usunąć tę ocenę?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteGrade()
                        showDeleteDialog = false
                    }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Anuluj", style = MaterialTheme.typography.labelLarge)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface
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

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("pl"))
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}