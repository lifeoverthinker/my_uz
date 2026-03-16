package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.*

// --- WRAPPER DLA NAWIGACJI ---
@Composable
fun GradeDetailsScreenRoute(
    viewModel: GradeDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditGrade: (Int) -> Unit,
    onDuplicateGrade: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val grade = uiState.grade

    if (uiState.isLoading || grade == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Formatowanie wyświetlania oceny
    val gradeText = if (grade.isPoints) {
        "${if (grade.grade % 1.0 == 0.0) grade.grade.toInt() else grade.grade} pkt"
    } else if (grade.grade == -1.0) {
        "Aktywność (+)"
    } else {
        grade.grade.toString()
    }

    GradeDetailsScreen(
        title = grade.description ?: (if (grade.isPoints) "Punkty" else "Ocena"),
        dateMillis = grade.date,
        subjectName = grade.subjectName,
        classType = ClassTypeUtils.getFullName(grade.classType),
        gradeText = gradeText,
        isPointsOrActivity = grade.isPoints || grade.grade == -1.0,
        weightText = grade.weight.toString(),
        comment = grade.comment,
        onNavigateBack = onNavigateBack,
        onEditGrade = { onEditGrade(grade.id) },
        onDeleteGrade = {
            viewModel.deleteGrade(onSuccess = { onNavigateBack() })
        },
        onDuplicateClick = onDuplicateGrade
    )
}

// --- BEZSTANOWY WIDOK (STATELESS UI) ---
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = "",
                navigationIcon = R.drawable.ic_x_close,
                isNavigationIconFilled = true,
                onNavigationClick = onNavigateBack,
                actions = {
                    TopBarActionIcon(
                        icon = R.drawable.ic_edit,
                        onClick = onEditGrade
                    )

                    Box {
                        TopBarActionIcon(
                            icon = R.drawable.ic_dots_vertical,
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
                                        painterResource(R.drawable.ic_copy),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = { showMenu = false; onDuplicateClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Usuń", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.ic_trash),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Nagłówek (Zrównany w osi z detalami) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Miejsce na ikonę (zajmuje 24dp szerokości, tak jak ikony poniżej)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp) // Wyrównanie do pierwszej linii tekstu
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color(0xFFF57C00), // Pełna ścieżka do Color
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                // Odstęp (24dp, tak jak poniżej)
                Spacer(modifier = Modifier.width(24.dp))

                // Tytuł i data (Złączone wg wytycznych Google Calendar)
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(dateMillis),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 1. Wartość oceny i Waga ---
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

            // --- 2. Przedmiot ---
            DetailRow(
                iconRes = R.drawable.ic_graduation_hat, // Zmiana ikony na czepek absolwenta
                label = "Przedmiot",
                value = subjectName
            )

            // --- 3. Rodzaj zajęć ---
            DetailRow(
                iconRes = R.drawable.ic_stand, // Zmiana ikony na stojak
                label = "Rodzaj zajęć",
                value = classType
            )

            // --- 4. Opis / Notatka (Tylko tekst, bez małej etykiety) ---
            if (!comment.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    iconRes = R.drawable.ic_menu_2,
                    label = null, // Przekazujemy null, aby ukryć podpis "Opis"
                    value = comment,
                    isMultiline = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- Dialog Usuwania ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń ocenę") },
                text = { Text("Czy na pewno chcesz usunąć tę ocenę? Tej operacji nie można cofnąć.") },
                confirmButton = {
                    TextButton(onClick = { onDeleteGrade(); showDeleteDialog = false }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
                }
            )
        }
    }
}

// --- Komponent Pomocniczy dla Wierszy ---
@Composable
private fun DetailRow(
    iconRes: Int,
    label: String?, // Jeśli null, etykieta się nie wyświetli (dla opisu)
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
        // Zawsze 24dp rozmiaru
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = if (isMultiline) 4.dp else 0.dp)
                .size(24.dp)
        )

        // Zawsze 24dp odstępu - gwarantuje to linię prostą
        Spacer(modifier = Modifier.width(24.dp))

        Column {
            // Etykieta (np. "Rodzaj zajęć") - Wyświetla się na górze, jeśli jest podana
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Wartość (np. "Laboratorium") - Wyświetla się na dole
            if (isValueHighlight) {
                Text(
                    text = value.ifEmpty { "-" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = value.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL")).format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

@Preview(showBackground = true)
@Composable
fun GradeDetailsScreenPreview() {
    MaterialTheme {
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