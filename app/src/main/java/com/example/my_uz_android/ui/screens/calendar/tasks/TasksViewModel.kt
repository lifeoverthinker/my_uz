package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val tasksStream: Flow<List<TaskEntity>> = tasksRepository.getTasksStream()

    // Nowe stany dla funkcji udostępniania
    private val _sharedCode = MutableStateFlow<String?>(null)
    val sharedCode: StateFlow<String?> = _sharedCode

    private val _isSharing = MutableStateFlow(false)
    val isSharing: StateFlow<Boolean> = _isSharing

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError: StateFlow<String?> = _shareError

    fun isPlanSelected(): Boolean {
        return runBlocking {
            val settings = settingsRepository.getSettingsStream().first()
            !settings?.selectedGroupCode.isNullOrBlank()
        }
    }

    // Funkcja wywoływana z UI
    fun shareMyTasks() {
        viewModelScope.launch {
            _isSharing.value = true
            _shareError.value = null
            try {
                // Pobieramy aktualną listę zadań (snapshot)
                val currentTasks = tasksStream.first()

                if (currentTasks.isNotEmpty()) {
                    val code = tasksRepository.shareTasks(currentTasks)
                    _sharedCode.value = code
                } else {
                    _shareError.value = "Brak zadań do udostępnienia"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _shareError.value = "Błąd udostępniania: ${e.message}"
            } finally {
                _isSharing.value = false
            }
        }
    }

    fun clearSharedCode() {
        _sharedCode.value = null
    }

    fun clearError() {
        _shareError.value = null
    }
}