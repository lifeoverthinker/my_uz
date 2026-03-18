package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.screens.index.components.GradeListItem
import com.example.my_uz_android.util.ClassTypeUtils

// Importy dla FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubjectGradesScreen(
    subjectName: String,
    classType: String,
    onNavigateBack: () -> Unit,
    onGradeClick: (Int) -> Unit,
    onAddGradeClick: () -> Unit,
    viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val filteredGrades = uiState.allGrades.filter {
        it.subjectName == subjectName && it.classType == classType
    }

    val gradesForAvg = filteredGrades.filter { !it.isPoints && it.grade != -1.0 && it.weight > 0 }
    val pointsSum = filteredGrades.filter { it.isPoints }.sumOf { it.grade }
    val plusCount = filteredGrades.count { it.grade == -1.0 }
    val finalGrade = filteredGrades.find { it.description?.contains("końcow", ignoreCase = true) == true }

    val average = if (gradesForAvg.isNotEmpty()) {
        val sum = gradesForAvg.sumOf { it.grade * it.weight }
        val weightSum = gradesForAvg.sumOf { it.weight }
        if (weightSum > 0) sum / weightSum else 0.0
    } else null

    // Stan modala
    var showFinalGradeDialog by remember { mutableStateOf(false) }

    if (showFinalGradeDialog) {
        FinalGradeDialog(
            currentGrade = finalGrade?.grade,
            onDismiss = { showFinalGradeDialog = false },
            onConfirm = { newGrade ->
                val semester = filteredGrades.firstOrNull()?.semester ?: 1
                val entity = finalGrade?.copy(
                    grade = newGrade,
                    date = System.currentTimeMillis()
                ) ?: GradeEntity(
                    id = 0,
                    subjectName = subjectName,
                    classType = classType,
                    grade = newGrade,
                    weight = 0, // Waga 0 zapobiega zaburzaniu średniej z innych ocen
                    description = "Ocena końcowa",
                    comment = null,
                    date = System.currentTimeMillis(),
                    semester = semester,
                    isPoints = false
                )
                viewModel.saveGrade(entity)
                showFinalGradeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = ClassTypeUtils.getFullName(classType),
                subtitle = subjectName,
                onNavigationClick = onNavigateBack,
                navigationIcon = R.drawable.ic_chevron_left,
                actions = {
                    TopBarActionIcon(
                        icon = R.drawable.ic_plus,
                        onClick = onAddGradeClick
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {

            item {
                SubjectStatsCard(average = average, points = pointsSum, pluses = plusCount)
            }

            item {
                FinalGradeMinimalButton(
                    finalGrade = finalGrade?.grade,
                    onClick = { showFinalGradeDialog = true }
                )
            }

            item {
                Text(
                    text = "Historia wpisów",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (filteredGrades.isEmpty()) {
                item {
                    Text(
                        text = "Brak wpisów",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredGrades.sortedByDescending { it.date }, key = { it.id }) { grade ->
                    GradeListItem(
                        grade = grade,
                        onClick = { onGradeClick(grade.id) },
                        onDelete = { viewModel.deleteGrade(it) },
                        onDuplicate = { viewModel.duplicateGrade(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectStatsCard(average: Double?, points: Double, pluses: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = average?.let { String.format("%.2f", it) } ?: "-",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Średnia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (points % 1.0 == 0.0) "${points.toInt()}" else "$points",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Punkty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$pluses",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Aktywności", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FinalGradeMinimalButton(finalGrade: Double?, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Średnia końcowaPROMPT K: Nowoczesny Ekran Konta (Styl Google)\n" +
                        "Ten prompt odświeży profil użytkownika, robiąc z niego czytelne centrum dowodzenia.\n" +
                        "\n" +
                        "Pliki do załączenia: AccountScreen.kt, SettingsEntity.kt, PersonalDataScreen.kt.\n" +
                        "\n" +
                        "Zadanie: Działaj jako UI Designer i Senior Android Developer. Chcę całkowicie odświeżyć wygląd AccountScreen.kt, aby pasował do estetyki Google Account / Material Design 3.\n" +
                        "\n" +
                        "Wymagania wizualne:\n" +
                        "\n" +
                        "Nagłówek: Na górze duże, okrągłe zdjęcie profilowe (lub inicjały na kolorowym tle), pod nim Imię i Nazwisko (Bold) oraz adres e-mail/numer indeksu (Medium, Grey).\n" +
                        "\n" +
                        "Sekcje (Karty): Zamiast luźnej listy, pogrupuj opcje w eleganckie karty (ElevatedCard) z zaokrąglonymi rogami:\n" +
                        "\n" +
                        "Dane osobiste: Wydział, kierunek, grupa.\n" +
                        "\n" +
                        "Ustawienia aplikacji: Powiadomienia, motyw, backup.\n" +
                        "\n" +
                        "Informacje: O aplikacji, wyloguj.\n" +
                        "\n" +
                        "Interakcja: Każdy wiersz w karcie ma mieć ikonę po lewej, tytuł i strzałkę chevron_right po prawej.\n" +
                        "\n" +
                        "Spójność: Użyj kolorów z mojego motywu MaterialTheme.colorScheme.primary dla ikon.\n" +
                        "\n" +
                        "Zwróć tylko zaktualizowany kod Compose dla AccountScreen.kt.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (finalGrade != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = finalGrade?.toString() ?: "Wpisz",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (finalGrade != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// NOWY MODAL
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FinalGradeDialog(
    currentGrade: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val grades = listOf(2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)
    var selectedGrade by remember { mutableStateOf(currentGrade ?: 5.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Średnia końcowa", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Wybierz średnią z przedmiotu:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grades.forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.toString()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedGrade) }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}