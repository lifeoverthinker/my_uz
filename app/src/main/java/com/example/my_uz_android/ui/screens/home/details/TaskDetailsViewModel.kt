package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository
) : ViewModel() {
    private val taskId: Int = checkNotNull(savedStateHandle["taskId"])

    val uiState: StateFlow<TaskDetailsUiState> =
        tasksRepository.getTasksStream()
            .map { tasks ->
                val task = tasks.find { it.id == taskId }
                TaskDetailsUiState(
                    taskEntity = task,
                    isLoading = false
                )
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

    fun duplicateTask(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.insertTask(
                task.copy(id = 0, title = "${task.title} (Kopia)")
            )
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            uiState.value.taskEntity?.let {
                tasksRepository.deleteTask(it)
            }
        }
    }
}

data class TaskDetailsUiState(
    val taskEntity: TaskEntity? = null,
    val isLoading: Boolean = false
)
