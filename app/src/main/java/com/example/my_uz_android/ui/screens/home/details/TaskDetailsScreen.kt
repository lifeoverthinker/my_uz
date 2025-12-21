package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.MyUZTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ FIX: Wyciągamy taska tylko jeśli stan to Success
    val currentTask = (uiState as? TaskDetailsUiState.Success)?.task

    Scaffold(
        topBar = {
            MyUZTopAppBar(
                title = "Szczegóły zadania",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onNavigateBack,
                actions = {
                    if (currentTask != null) {
                        IconButton(onClick = { onEditTask(currentTask.id) }) {
                            Icon(painterResource(R.drawable.ic_edit), null)
                        }
                        IconButton(onClick = {
                            // ✅ FIX: Przekazujemy callback onSuccess
                            viewModel.deleteTask { onNavigateBack() }
                        }) {
                            Icon(painterResource(R.drawable.ic_trash), null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is TaskDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TaskDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is TaskDetailsUiState.Success -> {
                    val task = state.task
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(task.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!task.description.isNullOrBlank()) {
                            Text(task.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        // Możesz dodać więcej szczegółów
                    }
                }
            }
        }
    }
}