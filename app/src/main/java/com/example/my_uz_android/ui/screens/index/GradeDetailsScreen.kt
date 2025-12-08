package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailsScreen(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: GradeDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val grade = uiState.grade

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły oceny") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            )
        }
    ) { padding ->
        if (grade == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = grade.subjectName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Ocena: ${grade.grade}",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Waga", grade.weight.toString())
                        if (!grade.description.isNullOrBlank()) {
                            DetailRow("Opis", grade.description)
                        }
                        DetailRow("Data", formatDate(grade.date))
                        DetailRow("Semestr", grade.semester.toString())
                    }
                }
            }
        }
    }

    // Dialog usuwania
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń ocenę") },
            text = { Text("Czy na pewno chcesz usunąć tę ocenę?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrade(onSuccess = onNavigateBack)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("pl"))
    return sdf.format(Date(timestamp))
}
