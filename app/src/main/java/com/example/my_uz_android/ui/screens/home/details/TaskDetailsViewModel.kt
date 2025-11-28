package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskDetailsUiState(
    val task: TaskEntity? = null,
    val isLoading: Boolean = true
)

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle["taskId"])

    // Obserwowanie zadania
    val uiState: StateFlow<TaskDetailsUiState> = tasksRepository.getTasksStream()
        .map { tasks ->
            val task = tasks.find { it.id == taskId }
            TaskDetailsUiState(task = task, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskDetailsUiState(isLoading = true)
        )

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask() {
        val currentTask = uiState.value.task
        if (currentTask != null) {
            viewModelScope.launch {
                tasksRepository.deleteTask(currentTask)
            }
        }
    }

    fun duplicateTask(task: TaskEntity) {
        viewModelScope.launch {
            val newTask = task.copy(
                id = 0, // Reset ID, aby utworzyć nowe
                title = "${task.title} (Kopia)"
            )
            tasksRepository.insertTask(newTask)
        }
    }
}