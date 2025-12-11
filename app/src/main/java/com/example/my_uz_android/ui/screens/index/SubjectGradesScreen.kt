package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.index.components.AverageCard
import com.example.my_uz_android.ui.screens.index.components.GradeListItem
import com.example.my_uz_android.ui.screens.index.components.SubjectTypeAppBar
import com.example.my_uz_android.util.ClassTypeUtils

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

    // Filtrowanie
    val filteredGrades = uiState.allGrades.filter {
        it.subjectName == subjectName && it.classType == classType
    }

    // Obliczanie średniej (pomijamy aktywność -1.0)
    val gradesForAvg = filteredGrades.filter { it.grade != -1.0 }

    val average = if (gradesForAvg.isNotEmpty()) {
        val sum = gradesForAvg.sumOf { it.grade * it.weight }
        val weightSum = gradesForAvg.sumOf { it.weight }
        if (weightSum > 0) sum / weightSum else 0.0
    } else null

    Scaffold(
        topBar = {
            SubjectTypeAppBar(
                subjectName = subjectName,
                className = ClassTypeUtils.getFullName(classType),
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            UniversalFab(
                onMainFabClick = onAddGradeClick,
                iconRes = R.drawable.ic_plus,
                isExpandable = false,
                isExpanded = false,
                options = emptyList()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AverageCard(
                    label = "Średnia ocen",
                    average = average
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "Oceny",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredGrades.isEmpty()) {
                item {
                    Text(
                        text = "Brak ocen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredGrades.sortedByDescending { it.date }) { grade ->
                    GradeListItem(
                        grade = grade,
                        onClick = { onGradeClick(grade.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}