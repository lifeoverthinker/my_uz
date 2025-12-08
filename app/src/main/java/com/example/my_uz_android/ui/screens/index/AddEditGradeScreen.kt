package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGradeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditGradeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edytuj ocenę" else "Dodaj ocenę") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pole: Nazwa przedmiotu
            OutlinedTextField(
                value = uiState.subjectName,
                onValueChange = viewModel::updateSubjectName,
                label = { Text("Nazwa przedmiotu") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pole: Ocena
            OutlinedTextField(
                value = uiState.grade,
                onValueChange = viewModel::updateGradeValue,
                label = { Text("Ocena") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pole: Waga
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = viewModel::updateWeight,
                label = { Text("Waga") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pole: Opis (opcjonalny)
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Opis (opcjonalny)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            // Przycisk Zapisz
            Button(
                onClick = { viewModel.saveGrade(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isEditing) "Zapisz zmiany" else "Dodaj ocenę")
            }
        }
    }
}
