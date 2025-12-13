package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły zadania") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_chevron_left), contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (uiState is TaskDetailsUiState.Success) {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opcje")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edytuj") },
                                onClick = {
                                    showMenu = false
                                    onEditTask((uiState as TaskDetailsUiState.Success).task.id)
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplikuj") },
                                onClick = {
                                    showMenu = false
                                    viewModel.duplicateTask { onNavigateBack() }
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) } // Używam Add jako ikony duplikacji
                            )
                            DropdownMenuItem(
                                text = { Text("Usuń") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteTask { onNavigateBack() }
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is TaskDetailsUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is TaskDetailsUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
                is TaskDetailsUiState.Success -> {
                    val task = state.task
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tytuł
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Checkbox ukończenia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion() }
                            )
                            Text(
                                text = if (task.isCompleted) "Ukończone" else "Do zrobienia",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Divider()

                        // Szczegóły
                        if (!task.subjectName.isNullOrEmpty()) {
                            DetailRow(label = "Przedmiot", value = task.subjectName)
                        }

                        if (!task.classType.isNullOrEmpty()) {
                            DetailRow(label = "Rodzaj", value = ClassTypeUtils.getFullName(task.classType))
                        }

                        // Data
                        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            .withZone(ZoneId.systemDefault())
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                            .withZone(ZoneId.systemDefault())

                        val dateStr = dateFormatter.format(Instant.ofEpochMilli(task.dueDate))
                        val timeStr = if (!task.isAllDay) ", ${timeFormatter.format(Instant.ofEpochMilli(task.dueDate))}" else ""

                        DetailRow(label = "Termin", value = "$dateStr$timeStr")

                        // Opis
                        if (!task.description.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Opis", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}