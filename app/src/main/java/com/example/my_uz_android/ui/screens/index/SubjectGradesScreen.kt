package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubjectGradesScreen(
    subjectName: String,
    onBackClick: () -> Unit,
    onGradeClick: (Int) -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subject = uiState.subjects.find { it.name == subjectName }
    var showFinalGradeDialog by remember { mutableStateOf(false) }
    var gradeToDelete by remember { mutableStateOf<GradeEntity?>(null) }
    
    val isDark = isSystemInDarkTheme()

    if (showFinalGradeDialog) {
        FinalGradeDialog(
            onConfirm = { showFinalGradeDialog = false },
            onDismiss = { showFinalGradeDialog = false }
        )
    }

    if (gradeToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteGrade(gradeToDelete!!)
                gradeToDelete = null
            },
            onDismiss = { gradeToDelete = null },
            itemType = "ocenę"
        )
    }

    val classTypeSubtitle = subject?.types?.joinToString(", ") {
        ClassTypeUtils.getFullName(it.name)
    } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = subjectName,
                subtitle = classTypeSubtitle,
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onBackClick,
                isNavigationIconFilled = true,
                actions = {
                    TopBarActionIcon(
                        icon = R.drawable.ic_plus,
                        isFilled = true,
                        onClick = {
                            val preferredType = subject?.types?.firstOrNull()?.name.orEmpty()
                            onAddGradeClick(subjectName, preferredType)
                        }
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (subject == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Brak danych", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val totalPoints = subject.types.flatMap { it.grades }.filter { it.isPoints }.sumOf { it.grade }
            val activitiesCount = subject.types.flatMap { it.grades }.size
            val overallAverage = subject.average?.let { String.format("%.1f", it) } ?: "-"
            val pointsText = if (totalPoints % 1.0 == 0.0) totalPoints.toInt().toString() else totalPoints.toString()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailSummaryItem(
                            value = overallAverage,
                            label = "Średnia",
                            modifier = Modifier.weight(1f),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)))
                        DetailSummaryItem(
                            value = if (totalPoints > 0) pointsText else "-",
                            label = "Punkty",
                            modifier = Modifier.weight(1f),
                            valueColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)))
                        DetailSummaryItem(
                            value = activitiesCount.toString(),
                            label = "Aktywności",
                            modifier = Modifier.weight(1f),
                            valueColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Oceny",
                                style = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+ Dodaj ocenę końcową",
                                style = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showFinalGradeDialog = true }
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            val allGrades = subject.types.flatMap { type -> type.grades.map { it to type.name } }
                                .sortedByDescending { it.first.date }

                            allGrades.forEachIndexed { index, (grade, typeName) ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            gradeToDelete = grade
                                            false
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.error),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Usuń",
                                                tint = Color.White,
                                                modifier = Modifier.padding(horizontal = 24.dp)
                                            )
                                        }
                                    },
                                    content = {
                                        GradeCardRow(grade = grade, onClick = { onGradeClick(grade.id) })
                                    }
                                )

                                if (index < allGrades.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinalGradeDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val grades = listOf(2.0, 3.0, 3.5, 4.0, 4.5, 5.0)
    var selectedGrade by remember { mutableStateOf(grades.last()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Dodaj ocenę końcową", style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Wybierz ocenę końcową z przedmiotu:", style = TextStyle(fontFamily = InterFontFamily))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grades.forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.toString()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedGrade) }) { Text("Zatwierdź") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
private fun DetailSummaryItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
            color = valueColor
        )
        Text(
            text = label,
            style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GradeCardRow(grade: GradeEntity, onClick: () -> Unit) {
    val dateStr = Instant.ofEpochMilli(grade.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("pl")))
    
    val displayValue = when {
        grade.grade == -1.0 -> "+"
        grade.grade % 1.0 == 0.0 -> grade.grade.toInt().toString()
        else -> grade.grade.toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ocena Circle (Box)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }

        // Tytuł i Data
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = grade.description.takeIf { !it.isNullOrBlank() } ?: (if (grade.isPoints) "Punkty" else "Aktywność"),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateStr,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Waga (opcjonalnie)
        if (!grade.isPoints && grade.weight > 0 && grade.grade != -1.0) {
            Text(
                text = "Waga: ${if (grade.weight % 1.0 == 0.0) grade.weight.toInt() else grade.weight}",
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}