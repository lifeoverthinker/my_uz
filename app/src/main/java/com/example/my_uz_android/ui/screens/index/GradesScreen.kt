package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.subjects.isEmpty()) {
        EmptyStateMessage(
            message = "Brak przedmiotów w indeksie",
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
                AverageCard(
                    label = "Średnia ogólna",
                    average = if (uiState.average > 0) uiState.average else null
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(uiState.subjects) { subject ->
                val isExpanded = expandedStates[subject.name] ?: false

                val mappedClassTypes = subject.types.map { type ->
                    SubjectTypeState(
                        typeName = type.name,
                        average = if (type.average > 0) type.average else null,
                        grades = type.grades
                    )
                }

                ExpandableSubjectCard(
                    subjectName = subject.name,
                    subjectCode = subject.code,
                    overallAverage = if (subject.average > 0) subject.average else null,
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