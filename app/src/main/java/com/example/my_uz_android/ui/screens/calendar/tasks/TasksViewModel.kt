package com.example.my_uz_android.ui.screens.calendar.tasks

import android.util.Log
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

    fun shareMyTasks() {
        viewModelScope.launch {
            _isSharing.value = true
            _shareError.value = null
            Log.d("TasksDebug", "Rozpoczynam udostępnianie...")
            try {
                val tasks = tasksStream.first()
                if (tasks.isNotEmpty()) {
                    val code = tasksRepository.shareTasks(tasks)
                    _sharedCode.value = code
                    Log.d("TasksDebug", "Udostępniono kod: $code")
                } else {
                    val msg = "Brak zadań do udostępnienia"
                    _shareError.value = msg
                    Log.w("TasksDebug", msg)
                }
            } catch (e: Exception) {
                val msg = "Błąd udostępniania: ${e.message}"
                _shareError.value = msg
                Log.e("TasksDebug", msg, e)
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
            Log.d("TasksDebug", "Rozpoczynam import kodu: $code")

            try {
                val result = tasksRepository.importTasks(code.trim().uppercase())
                when (result) {
                    is NetworkResult.Success -> {
                        val count = result.data?.size ?: 0
                        val msg = "Pomyślnie zaimportowano $count zadań!"
                        _importStatus.value = msg
                        Log.d("TasksDebug", msg)
                    }
                    is NetworkResult.Error -> {
                        _importStatus.value = result.message
                        Log.e("TasksDebug", "Błąd importu (wynik): ${result.message}")
                    }
                }
            } catch (e: Exception) {
                val msg = "Błąd importu (wyjątek): ${e.message}"
                _importStatus.value = msg
                Log.e("TasksDebug", msg, e)
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun clearSharedCode() {
        _sharedCode.value = null
    }

    fun clearError() {
        _shareError.value = null
        _importStatus.value = null
    }
}