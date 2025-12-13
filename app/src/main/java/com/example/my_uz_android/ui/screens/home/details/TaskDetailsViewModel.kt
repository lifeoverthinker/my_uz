package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface TaskDetailsUiState {
    data object Loading : TaskDetailsUiState
    data class Success(val task: TaskEntity) : TaskDetailsUiState
    data class Error(val message: String) : TaskDetailsUiState
}

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow<TaskDetailsUiState>(TaskDetailsUiState.Loading)
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            tasksRepository.getTaskByIdStream(taskId)
                .catch { e ->
                    _uiState.value = TaskDetailsUiState.Error(e.message ?: "Błąd ładowania zadania")
                }
                .collect { task ->
                    if (task != null) {
                        _uiState.value = TaskDetailsUiState.Success(task)
                    } else {
                        _uiState.value = TaskDetailsUiState.Error("Nie znaleziono zadania")
                    }
                }
        }
    }

    fun deleteTask(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is TaskDetailsUiState.Success) {
            viewModelScope.launch {
                tasksRepository.deleteTask(currentState.task)
                onSuccess()
            }
        }
    }

    fun duplicateTask(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is TaskDetailsUiState.Success) {
            viewModelScope.launch {
                val currentTask = currentState.task
                val newTask = currentTask.copy(
                    id = 0, // 0 oznacza, że Room wygeneruje nowe ID
                    title = "${currentTask.title} (Kopia)"
                )
                tasksRepository.insertTask(newTask)
                onSuccess()
            }
        }
    }

    fun toggleTaskCompletion() {
        val currentState = _uiState.value
        if (currentState is TaskDetailsUiState.Success) {
            viewModelScope.launch {
                val updatedTask = currentState.task.copy(isCompleted = !currentState.task.isCompleted)
                tasksRepository.updateTask(updatedTask)
                // UI zaktualizuje się samo dzięki Flow w loadTask
            }
        }
    }
}