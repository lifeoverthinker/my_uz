package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.index.components.GradeListItem

@Composable
fun GradesScreen(
    onGradeClick: (Int) -> Unit,
    viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val grades = uiState.grades

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (grades.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Brak ocen do wyświetlenia")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades) { grade ->
                    GradeListItem(
                        grade = grade,
                        onClick = { onGradeClick(grade.id) }
                    )
                }
            }
        }
    }
}
