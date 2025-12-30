package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class TaskAddEditUiState(
    val taskId: Int = 0,
    val title: String = "",
    val classSubject: String? = null,
    val classType: String? = null,
    val description: String = "",
    val startDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val isAllDay: Boolean = false,
    val priority: Int = 1,
    val availableSubjects: List<Pair<String, List<String>>> = emptyList()
)

class TaskAddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskAddEditUiState())
    val uiState: StateFlow<TaskAddEditUiState> = _uiState.asStateFlow()

    // ✅ Flaga do nawigacji
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var loadedTaskId: Int? = null

    init {
        loadAvailableSubjects()
        val taskId = savedStateHandle.get<Int>("taskId")
        if (taskId != null && taskId != -1 && taskId != 0) {
            loadTask(taskId)
        }
    }

    fun loadTask(taskId: Int) {
        if (loadedTaskId == taskId) return
        loadedTaskId = taskId
        viewModelScope.launch {
            val task = tasksRepository.getTaskById(taskId).first()
            if (task != null) {
                val startZone = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault())
                val endZone = Instant.ofEpochMilli(task.endDate).atZone(ZoneId.systemDefault())

                // Parsowanie czasu, jeśli istnieje
                val sTime = task.dueTime?.let {
                    try {
                        val parts = it.split(":")
                        LocalTime.of(parts[0].toInt(), parts[1].toInt())
                    } catch (e: Exception) { null }
                }

                _uiState.update {
                    it.copy(
                        taskId = task.id,
                        title = task.title,
                        description = task.description ?: "",
                        classSubject = task.subjectName, // ✅ Poprawiono nazwę pola z encji
                        classType = task.classType,
                        startDate = startZone.toLocalDate(),
                        startTime = sTime,
                        endDate = endZone.toLocalDate(),
                        isAllDay = task.isAllDay,
                        priority = task.priority
                    )
                }
            }
        }
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            classRepository.getAllClassesStream().collect { classes ->
                val subjectsMap = classes.groupBy { it.subjectName }
                    .mapValues { (_, classList) ->
                        classList.map { it.classType }.distinct().sorted()
                    }
                    .toList()
                    .sortedBy { it.first }
                _uiState.update { it.copy(availableSubjects = subjectsMap) }
            }
        }
    }

    fun updateTitle(v: String) { _uiState.update { it.copy(title = v) } }
    fun updateClassSubject(v: String?) { _uiState.update { it.copy(classSubject = v) } }
    fun updateClassType(v: String?) { _uiState.update { it.copy(classType = v) } }
    fun updatePriority(v: Int) { _uiState.update { it.copy(priority = v) } }
    fun updateIsAllDay(v: Boolean) { _uiState.update { it.copy(isAllDay = v) } }
    fun updateStartDate(v: LocalDate) { _uiState.update { it.copy(startDate = v) } }
    fun updateEndDate(v: LocalDate) { _uiState.update { it.copy(endDate = v) } }
    fun updateStartTime(v: LocalTime) { _uiState.update { it.copy(startTime = v) } }
    fun updateEndTime(v: LocalTime) { _uiState.update { it.copy(endTime = v) } }
    fun updateDescription(v: String) { _uiState.update { it.copy(description = v) } }

    fun saveTask() {
        viewModelScope.launch {
            val s = _uiState.value
            // Konwersja daty do long
            val due = s.startDate?.atTime(s.startTime ?: LocalTime.MIN)?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
            val end = s.endDate?.atTime(s.endTime ?: LocalTime.MAX)?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: due

            val timeStr = s.startTime?.let { String.format("%02d:%02d", it.hour, it.minute) }

            val task = TaskEntity(
                id = loadedTaskId ?: 0,
                title = s.title,
                description = s.description.ifBlank { null },
                subjectName = s.classSubject ?: "",
                classType = s.classType ?: "",
                priority = s.priority,
                isAllDay = s.isAllDay,
                dueDate = due,
                endDate = end,
                dueTime = timeStr,
                isCompleted = false
            )

            if (loadedTaskId != null && loadedTaskId != 0) {
                tasksRepository.updateTask(task) // ✅ Poprawna nazwa metody
            } else {
                tasksRepository.insertTask(task) // ✅ Poprawna nazwa metody
            }
            _isSaved.value = true
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            if (loadedTaskId != null && loadedTaskId != 0) {
                val task = tasksRepository.getTaskById(loadedTaskId!!).first()
                if (task != null) {
                    tasksRepository.deleteTask(task) // ✅ Poprawna nazwa metody
                    _isSaved.value = true
                }
            }
        }
    }
}