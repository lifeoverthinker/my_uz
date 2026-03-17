package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.screens.index.components.AverageCard
import com.example.my_uz_android.ui.screens.index.components.ExpandableSubjectCard
import com.example.my_uz_android.ui.screens.index.components.SubjectTypeState
import com.example.my_uz_android.R

@Composable
fun GradesScreen(
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    onAddGradeClick: (String?, String?) -> Unit,
    viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Lokalne zarządzanie stanem rozwinięcia kart
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (uiState.subjects.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateMessage(
                title = "Czysta karta! \uD83D\uDCD6",
                subtitle = "Startujesz od zera",
                message = "W Twoim indeksie nie ma jeszcze wpisów. Czas zdobyć pierwsze piątki w tym semestrze!",
                iconRes = R.drawable.grades_rafiki,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        // Grupowanie ocen po nazwie kierunku
        val groupedSubjects = uiState.subjects.groupBy { it.courseName }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AverageCard(
                    label = "Średnia z bieżącego semestru",
                    average = uiState.average
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            groupedSubjects.forEach { (courseName, subjectsList) ->

                // Pokazujemy nagłówek TYLKO jeśli jest więcej niż 1 kierunek na liście
                if (groupedSubjects.size > 1) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = if (courseName == groupedSubjects.keys.first()) 0.dp else 16.dp, bottom = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_graduation_hat),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = courseName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(subjectsList) { subject ->
                    val isExpanded = expandedStates[subject.name] ?: false

                    val mappedClassTypes = subject.types.map { type ->
                        SubjectTypeState(
                            typeName = type.name,
                            average = type.average,
                            grades = type.grades
                        )
                    }

                    ExpandableSubjectCard(
                        subjectName = subject.name,
                        subjectCode = subject.code,
                        overallAverage = subject.average,
                        classTypes = mappedClassTypes,
                        isExpanded = isExpanded,
                        onExpandClick = { expandedStates[subject.name] = !isExpanded },
                        onAddGradeClick = { typeName ->
                            onAddGradeClick(subject.name, typeName)
                        },
                        onTypeClick = { typeName ->
                            onNavigateToClassTypeGrades(subject.name, typeName)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GradesScreenEmptyPreview() {
    com.example.my_uz_android.ui.theme.MyUZTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateMessage(
                title = "Brak ocen",
                message = "Nie znaleźliśmy żadnych przedmiotów w indeksie.\nUpewnij się, że poprawnie wybrałeś plan zajęć w ustawieniach.",
                iconRes = R.drawable.grades_rafiki,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}