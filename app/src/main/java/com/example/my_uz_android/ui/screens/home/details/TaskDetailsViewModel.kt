package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Zmieniono strukturę UiState na prostszą klasę danych,
// aby uniknąć błędów Unresolved reference 'task' w TaskDetailsScreen.
data class TaskDetailsUiState(
    val task: TaskEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            tasksRepository.getTaskById(taskId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { task ->
                    _uiState.update { it.copy(task = task, isLoading = false) }
                }
        }
    }

    fun deleteTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState.value.task?.let {
                tasksRepository.deleteTask(it)
                onSuccess()
            }
        }
    }

    // Metoda toggle pozostaje dla szybkiej edycji z poziomu szczegółów
    fun toggleTaskCompletion() {
        viewModelScope.launch {
            uiState.value.task?.let {
                val updatedTask = it.copy(isCompleted = !it.isCompleted)
                tasksRepository.updateTask(updatedTask)
            }
        }
    }
}