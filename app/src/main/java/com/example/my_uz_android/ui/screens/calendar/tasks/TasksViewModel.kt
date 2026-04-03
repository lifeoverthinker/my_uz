package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    val tasksStream: Flow<List<TaskEntity>> = tasksRepository.getAllTasks()

    private val _sharedCode = MutableStateFlow<String?>(null)
    val sharedCode = _sharedCode.asStateFlow()

    private val _isSharing = MutableStateFlow(false)
    val isSharing = _isSharing.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError = _shareError.asStateFlow()

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus = _importStatus.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task)
        }
    }

    fun shareMyTasks(selectedTaskIds: Set<Int>? = null) {
        viewModelScope.launch {
            _isSharing.value = true
            _shareError.value = null
            try {
                val allTasks = tasksStream.first()

                val tasksToShare = if (!selectedTaskIds.isNullOrEmpty()) {
                    allTasks.filter { selectedTaskIds.contains(it.id) }
                } else {
                    allTasks
                }

                if (tasksToShare.isNotEmpty()) {
                    when (val result = tasksRepository.shareTasks(tasksToShare)) {
                        is NetworkResult.Success -> _sharedCode.value = result.data
                        is NetworkResult.Error -> _shareError.value = result.message
                    }
                } else {
                    _shareError.value = "Brak zadań do udostępnienia."
                }
            } catch (e: Exception) {
                _shareError.value = "Błąd: ${e.message}"
            } finally {
                _isSharing.value = false
            }
        }
    }

    fun importTasks(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _isImporting.value = true
            _importStatus.value = null
            try {
                when (val result = tasksRepository.importTasks(code.trim().uppercase())) {
                    is NetworkResult.Success -> {
                        val count = result.data?.size ?: 0
                        _importStatus.value = "Pomyślnie zaimportowano $count zadań!"
                    }

                    is NetworkResult.Error -> {
                        _importStatus.value = result.message
                    }
                }
            } catch (e: Exception) {
                _importStatus.value = "Błąd krytyczny: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun clearSharedCode() = _sharedCode.update { null }

    fun clearShareError() {
        _shareError.value = null
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }

    /**
     * Zachowane dla kompatybilności starszego kodu.
     */
    fun clearError() {
        clearShareError()
        clearImportStatus()
    }
}