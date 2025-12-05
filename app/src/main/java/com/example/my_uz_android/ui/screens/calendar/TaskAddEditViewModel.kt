package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class TaskAddEditUiState(
    val title: String = "",
    val description: String = "",
    val classSubject: String? = null,
    val classType: String? = null,
    val priority: Int = 1,
    val isAllDay: Boolean = false,
    val startDate: LocalDate? = LocalDate.now(),
    val endDate: LocalDate? = LocalDate.now(),
    val startTime: LocalTime? = LocalTime.of(8, 0),
    val endTime: LocalTime? = LocalTime.of(10, 0),
    val availableSubjects: List<Pair<String, List<String>>> = emptyList()
)

class TaskAddEditViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskAddEditUiState())
    val uiState: StateFlow<TaskAddEditUiState> = _uiState.asStateFlow()

    private var currentTaskId: Int? = null

    init {
        loadAvailableSubjects()
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            classRepository.getAllClassesStream().collect { classes ->
                val subjectsMap = classes
                    .groupBy { it.subjectName }
                    .mapValues { entry ->
                        entry.value
                            .map { it.classType }
                            .distinct()
                            .toList()
                    }

                _uiState.update {
                    it.copy(availableSubjects = subjectsMap.toList())
                }
            }
        }
    }

    fun loadTask(taskId: Int) {
        currentTaskId = taskId
        viewModelScope.launch {
            tasksRepository.getTaskById(taskId)?.let { task ->
                _uiState.value = TaskAddEditUiState(
                    title = task.title,
                    description = task.description ?: "",
                    classSubject = task.subjectName,
                    classType = task.classType,
                    priority = 1,
                    isAllDay = task.dueTime == null,
                    startDate = java.time.Instant.ofEpochMilli(task.dueDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                    endDate = java.time.Instant.ofEpochMilli(task.dueDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                    startTime = task.dueTime?.let {
                        val parts = it.split(":")
                        LocalTime.of(parts[0].toInt(), parts[1].toInt())
                    },
                    endTime = task.dueTime?.let {
                        val parts = it.split(":")
                        LocalTime.of(parts[0].toInt(), parts[1].toInt()).plusHours(2)
                    },
                    availableSubjects = _uiState.value.availableSubjects
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateClassSubject(subject: String?) {
        _uiState.value = _uiState.value.copy(classSubject = subject)
    }

    fun updateClassType(type: String?) {
        _uiState.value = _uiState.value.copy(classType = type)
    }

    fun updatePriority(priority: Int) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun updateIsAllDay(isAllDay: Boolean) {
        _uiState.value = _uiState.value.copy(isAllDay = isAllDay)
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(startTime = time)
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = time)
    }

    fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank() || state.startDate == null || state.endDate == null) return
        if (!state.isAllDay && (state.startTime == null || state.endTime == null)) return

        viewModelScope.launch {
            val dueDate = state.startDate!!
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val dueTime = if (!state.isAllDay) {
                state.startTime?.let {
                    String.format("%02d:%02d", it.hour, it.minute)
                }
            } else {
                null
            }

            val task = TaskEntity(
                id = currentTaskId ?: 0,
                title = state.title,
                description = state.description.ifBlank { null },
                subjectName = state.classSubject?.ifBlank { null } ?: "",  // ✅ POPRAWIONE
                classType = state.classType?.ifBlank { null } ?: "",        // ✅ POPRAWIONE
                dueDate = dueDate,
                dueTime = dueTime,
                isCompleted = false,
                subjectId = null
            )

            if (currentTaskId != null && currentTaskId!! > 0) {
                tasksRepository.updateTask(task)
            } else {
                tasksRepository.insertTask(task)
            }
        }
    }

    fun deleteTask() {
        currentTaskId?.let { id ->
            viewModelScope.launch {
                tasksRepository.getTaskById(id)?.let { task ->
                    tasksRepository.deleteTask(task)
                }
            }
        }
    }
}
