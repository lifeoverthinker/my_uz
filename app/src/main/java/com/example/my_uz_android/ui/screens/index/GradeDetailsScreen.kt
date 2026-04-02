package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.getAppAccentColor
import com.example.my_uz_android.ui.theme.getClassColorIndex
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GradeDetailsScreenRoute(
    viewModel: GradeDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditGrade: (Int) -> Unit,
    onDuplicateGrade: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val grade = uiState.grade

    if (uiState.isLoading || grade == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val gradeText = when {
        grade.isPoints -> "${if (grade.grade % 1.0 == 0.0) grade.grade.toInt() else grade.grade} pkt"
        grade.grade == -1.0 -> "Aktywność (+)"
        else -> grade.grade.toString()
    }

    GradeDetailsScreen(
        title = grade.description ?: if (grade.isPoints) "Punkty" else "Ocena",
        dateMillis = grade.date,
        subjectName = grade.subjectName,
        classType = ClassTypeUtils.getFullName(grade.classType),
        gradeText = gradeText,
        isPointsOrActivity = grade.isPoints || grade.grade == -1.0,
        weightText = grade.weight.toString(),
        comment = grade.comment,
        onNavigateBack = onNavigateBack,
        onEditGrade = { onEditGrade(grade.id) },
        onDeleteGrade = { viewModel.deleteGrade(onSuccess = onNavigateBack) },
        onDuplicateClick = onDuplicateGrade
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailsScreen(
    title: String,
    dateMillis: Long,
    subjectName: String,
    classType: String,
    gradeText: String,
    isPointsOrActivity: Boolean,
    weightText: String?,
    comment: String?,
    onNavigateBack: () -> Unit,
    onEditGrade: () -> Unit,
    onDeleteGrade: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val colorIndex = getClassColorIndex(subjectName)
    val accentColor = getAppAccentColor(colorIndex, isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = "",
                navigationIcon = R.drawable.ic_close,
                isNavigationIconFilled = true,
                onNavigationClick = onNavigateBack,
                containerColor = Color.Transparent,
                actions = {
                    TopBarActionIcon(
                        icon = R.drawable.ic_edit,
                        isFilled = true,
                        onClick = onEditGrade
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
                                text = { Text("Duplikuj") },
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
                                text = { Text("Usuń", color = MaterialTheme.colorScheme.error) },
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
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = accentColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatGradeDetailsDate(dateMillis),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val gradeLabel = if (isPointsOrActivity) {
                "Punkty / Aktywność"
            } else {
                if (!weightText.isNullOrEmpty()) "Ocena • Waga: $weightText" else "Ocena"
            }

            DetailRow(
                iconRes = R.drawable.ic_trophy,
                label = gradeLabel,
                value = gradeText,
                isValueHighlight = true
            )

            DetailRow(
                iconRes = R.drawable.ic_graduation_hat,
                label = "Przedmiot",
                value = subjectName
            )

            DetailRow(
                iconRes = R.drawable.ic_stand,
                label = "Rodzaj zajęć",
                value = classType
            )

            if (!comment.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    iconRes = R.drawable.ic_menu_2,
                    label = null,
                    value = comment,
                    isMultiline = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    onDeleteGrade()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false },
                itemType = "ocenę"
            )
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int,
    label: String?,
    value: String,
    isValueHighlight: Boolean = false,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = if (isMultiline) 4.dp else 0.dp)
                .size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Column {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value.ifEmpty { "-" },
                style = if (isValueHighlight) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isValueHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatGradeDetailsDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL"))
        .format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

@Preview(showBackground = true)
@Composable
fun GradeDetailsScreenPreview() {
    MyUZTheme {
        GradeDetailsScreen(
            title = "Kolokwium 1",
            dateMillis = System.currentTimeMillis(),
            subjectName = "Matematyka Dyskretna",
            classType = "Wykład",
            gradeText = "4.5",
            isPointsOrActivity = false,
            weightText = "3",
            comment = "Zadania otwarte poszły świetnie, ale brakło czasu na ostatnie zadanie.",
            onNavigateBack = {},
            onEditGrade = {},
            onDeleteGrade = {},
            onDuplicateClick = {}
        )
    }
}