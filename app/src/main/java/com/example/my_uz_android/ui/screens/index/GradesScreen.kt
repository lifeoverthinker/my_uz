package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.screens.index.components.AverageCard
import com.example.my_uz_android.ui.screens.index.components.ExpandableSubjectCard
import com.example.my_uz_android.ui.screens.index.components.SubjectTypeState

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
        EmptyStateMessage(
            message = "Brak przedmiotów w indeksie.\nUpewnij się, że wybrałeś plan zajęć w ustawieniach.",
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Tutaj wyświetla się główna średnia (już bez punktów, bo przeliczona w VM)
                AverageCard(
                    label = "Średnia z bieżącego semestru",
                    average = uiState.average
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(uiState.subjects) { subject ->
                val isExpanded = expandedStates[subject.name] ?: false

                // Mapowanie modelu z ViewModel na model komponentu UI
                // Uwaga: subject.types.grades to teraz List<GradeEntity>
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