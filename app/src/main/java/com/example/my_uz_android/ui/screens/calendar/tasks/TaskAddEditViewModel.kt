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
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(8, 0),
    val endDate: LocalDate = LocalDate.now(),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val isAllDay: Boolean = false,
    val priority: Int = 1,
    val isSaved: Boolean = false,
    val availableSubjects: List<Pair<String, List<String>>> = emptyList()
)

class TaskAddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskAddEditUiState())
    val uiState: StateFlow<TaskAddEditUiState> = _uiState.asStateFlow()

    // Flaga dla nawigacji wstecz po zapisaniu/usunięciu
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var loadedTaskId: Int? = null

    init {
        loadAvailableSubjects()
        val taskId = savedStateHandle.get<Int>("taskId")

        if (taskId != null && taskId != -1 && taskId != 0) {
            loadTask(taskId)
        } else {
            // Logika duplikacji: wypełnij pola z parametrów nawigacji
            _uiState.update { it.copy(
                title = savedStateHandle.get<String>("title") ?: "",
                description = savedStateHandle.get<String>("description") ?: "",
                classSubject = savedStateHandle.get<String>("subject"),
                classType = savedStateHandle.get<String>("classType"),
                isAllDay = savedStateHandle.get<Boolean>("isAllDay") ?: false,
                startDate = savedStateHandle.get<Long>("date")?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                } ?: LocalDate.now()
            )}
        }
    }

    fun loadTask(taskId: Int) {
        if (loadedTaskId == taskId) return
        loadedTaskId = taskId
        viewModelScope.launch {
            tasksRepository.getTaskById(taskId).filterNotNull().collect { task ->
                val startZone = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault())
                val endZone = Instant.ofEpochMilli(task.endDate).atZone(ZoneId.systemDefault())

                _uiState.update { it.copy(
                    taskId = task.id,
                    title = task.title,
                    description = task.description ?: "",
                    classSubject = task.subjectName,
                    classType = task.classType,
                    startDate = startZone.toLocalDate(),
                    startTime = startZone.toLocalTime(),
                    endDate = endZone.toLocalDate(),
                    endTime = endZone.toLocalTime(),
                    isAllDay = task.isAllDay,
                    priority = task.priority
                )}
            }
        }
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            classRepository.getAllClassesStream().collect { classes ->
                val subjectsMap = classes.groupBy { it.subjectName }
                    .mapValues { (_, list) -> list.map { it.classType }.distinct().sorted() }
                    .toList().sortedBy { it.first }
                _uiState.update { it.copy(availableSubjects = subjectsMap) }
            }
        }
    }

    fun updateTitle(v: String) = _uiState.update { it.copy(title = v) }
    fun updateClassSubject(v: String?) = _uiState.update { it.copy(classSubject = v) }
    fun updateClassType(v: String?) = _uiState.update { it.copy(classType = v) }
    fun updateIsAllDay(v: Boolean) = _uiState.update { it.copy(isAllDay = v) }
    fun updateStartDate(v: LocalDate) = _uiState.update { it.copy(startDate = v) }
    fun updateEndDate(v: LocalDate) = _uiState.update { it.copy(endDate = v) }
    fun updateStartTime(v: LocalTime) = _uiState.update { it.copy(startTime = v) }
    fun updateEndTime(v: LocalTime) = _uiState.update { it.copy(endTime = v) }
    fun updateDescription(v: String) = _uiState.update { it.copy(description = v) }
    fun updatePriority(v: Int) = _uiState.update { it.copy(priority = v) }

    fun saveTask() {
        viewModelScope.launch {
            val s = _uiState.value
            val due = s.startDate.atTime(if (s.isAllDay) LocalTime.MIN else s.startTime)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = s.endDate.atTime(if (s.isAllDay) LocalTime.MAX else s.endTime)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val task = TaskEntity(
                id = s.taskId,
                title = s.title,
                description = s.description.ifBlank { null },
                subjectName = s.classSubject ?: "",
                classType = s.classType ?: "",
                priority = s.priority,
                isAllDay = s.isAllDay,
                dueDate = due,
                endDate = end,
                isCompleted = false
            )
            if (task.id == 0) tasksRepository.insertTask(task) else tasksRepository.updateTask(task)
            _isSaved.value = true
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            val id = _uiState.value.taskId
            if (id != 0) {
                val task = tasksRepository.getTaskById(id).first()
                if (task != null) {
                    tasksRepository.deleteTask(task)
                    _isSaved.value = true
                }
            }
        }
    }
}