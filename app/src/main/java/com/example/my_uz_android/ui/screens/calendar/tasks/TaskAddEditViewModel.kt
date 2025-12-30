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
    val availableSubjects: List<Pair<String, List<String>>> = emptyList(),
    val isTitleValid: Boolean = true,
    val isSubjectValid: Boolean = true
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
            // POPRAWKA: Pobieramy Flow i zbieramy go, aby uzyskać obiekt TaskEntity
            tasksRepository.getTaskById(taskId).collect { task ->
                if (task != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            title = task.title,
                            description = task.description ?: "",
                            classSubject = task.subjectName,
                            classType = task.classType,
                            priority = task.priority,
                            isAllDay = task.isAllDay,
                            startDate = Instant.ofEpochMilli(task.dueDate)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                            endDate = Instant.ofEpochMilli(task.endDate)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                            startTime = task.dueTime?.let {
                                try {
                                    val parts = it.split(":")
                                    LocalTime.of(parts[0].toInt(), parts[1].toInt())
                                } catch (e: Exception) { LocalTime.of(8,0) }
                            },
                            endTime = LocalTime.of(10, 0),
                            isTitleValid = true,
                            isSubjectValid = true
                        )
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, isTitleValid = title.isNotBlank()) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateClassSubject(subject: String?) {
        _uiState.update { it.copy(classSubject = subject, isSubjectValid = true) }
    }

    fun updateClassType(type: String?) {
        _uiState.update { it.copy(classType = type) }
    }

    fun updatePriority(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun updateIsAllDay(isAllDay: Boolean) {
        _uiState.update { it.copy(isAllDay = isAllDay) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun saveTask() {
        val state = _uiState.value
        val isTitleValid = state.title.isNotBlank()

        if (!isTitleValid) {
            _uiState.update { it.copy(isTitleValid = false) }
            return
        }

        if (state.startDate == null || state.endDate == null) return

        viewModelScope.launch {
            val dueDate = state.startDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val endDate = state.endDate
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
                subjectName = state.classSubject?.ifBlank { "" } ?: "",
                classType = state.classType?.ifBlank { "" } ?: "",
                priority = state.priority,
                isAllDay = state.isAllDay,
                dueDate = dueDate,
                endDate = endDate,
                dueTime = dueTime,
                isCompleted = false
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
                tasksRepository.getTaskById(id).first()?.let { task ->
                    tasksRepository.deleteTask(task)
                }
            }
        }
    }
}