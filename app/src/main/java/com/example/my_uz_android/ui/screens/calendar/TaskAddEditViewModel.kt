package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class TaskAddEditUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime? = null, // null oznacza całodniowe
    val isAllDay: Boolean = true,
    val selectedSubject: String = "",
    val selectedType: String = "",
    val description: String = "",
    val availableSubjects: List<String> = emptyList(), // Lista unikalnych przedmiotów z planu
    val availableTypes: List<String> = listOf("Wykład", "Laboratorium", "Ćwiczenia", "Projekt", "Seminarium", "Inne"),
    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false,
    val isSubjectModalVisible: Boolean = false
)

class TaskAddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val taskId: Int? = savedStateHandle.get<Int>("taskId")?.let { if (it != -1) it else null }

    private val _uiState = MutableStateFlow(TaskAddEditUiState())
    val uiState: StateFlow<TaskAddEditUiState> = _uiState.asStateFlow()

    // Pobieramy przedmioty z bazy (z planu zajęć) jako podpowiedzi
    val subjectsStream: StateFlow<List<String>> = classRepository.getAllClassesStream()
        .combine(_uiState) { classes, _ ->
            classes.map { it.subjectName }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Jeśli edytujemy zadanie, pobierz jego dane
        if (taskId != null) {
            viewModelScope.launch {
                val task = tasksRepository.getTask(taskId)
                task?.let { t ->
                    val date = Instant.ofEpochMilli(t.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = t.dueTime?.let { LocalTime.parse(it) }

                    _uiState.update { state ->
                        state.copy(
                            title = t.title,
                            date = date,
                            time = time,
                            isAllDay = time == null,
                            selectedSubject = t.subjectName,
                            selectedType = t.classType,
                            description = t.description ?: ""
                        )
                    }
                }
            }
        }

        // Aktualizacja listy dostępnych przedmiotów w UI State
        viewModelScope.launch {
            subjectsStream.collect { subjects ->
                _uiState.update { it.copy(availableSubjects = subjects) }
            }
        }
    }

    fun updateTitle(newTitle: String) { _uiState.update { it.copy(title = newTitle) } }
    fun updateDate(newDate: LocalDate) { _uiState.update { it.copy(date = newDate) } }
    fun updateTime(newTime: LocalTime?) { _uiState.update { it.copy(time = newTime, isAllDay = newTime == null) } }
    fun updateSubject(newSubject: String) { _uiState.update { it.copy(selectedSubject = newSubject, isSubjectModalVisible = false) } }
    fun updateType(newType: String) { _uiState.update { it.copy(selectedType = newType) } }
    fun updateDescription(newDescription: String) { _uiState.update { it.copy(description = newDescription) } }

    fun toggleSubjectModal(show: Boolean) {
        _uiState.update { it.copy(isSubjectModalVisible = show) }
    }

    fun saveTask() {
        viewModelScope.launch {
            val state = uiState.value
            val dueDateMillis = state.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dueTimeString = if (!state.isAllDay) state.time.toString() else null

            val task = TaskEntity(
                id = taskId ?: 0, // 0 dla nowego zadania (auto-generowanie)
                title = state.title,
                description = state.description,
                dueDate = dueDateMillis,
                dueTime = dueTimeString,
                subjectName = state.selectedSubject,
                classType = state.selectedType,
                isCompleted = false
            )

            if (taskId == null) {
                tasksRepository.insertTask(task)
            } else {
                tasksRepository.updateTask(task)
            }
            _uiState.update { it.copy(isTaskSaved = true) }
        }
    }
}