package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class TasksUiState(
    val groupedTasks: Map<YearMonth, Map<LocalDate, List<TaskEntity>>> = emptyMap(),
    val isLoading: Boolean = true,
    val availableSubjects: List<SubjectOption> = emptyList()
)

data class SubjectOption(
    val name: String,
    val types: List<String>
)

class TasksViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val mockSubjects = listOf(
        SubjectOption("Systemy Wbudowane", listOf("Wykład", "Laboratorium", "Projekt")),
        SubjectOption("Język Angielski", listOf("Ćwiczenia")),
        SubjectOption("Analiza Matematyczna", listOf("Wykład", "Ćwiczenia")),
        SubjectOption("Programowanie Obiektowe", listOf("Laboratorium", "Wykład"))
    )

    val uiState: StateFlow<TasksUiState> = tasksRepository.getTasksStream()
        .map { tasks ->
            val sortedTasks = tasks.sortedBy { it.dueDate }
            val groupedByMonth = sortedTasks.groupBy { task ->
                val date = if (task.dueDate > 0) {
                    Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                } else LocalDate.now()
                YearMonth.from(date)
            }.toSortedMap()

            val finalMap = groupedByMonth.mapValues { entry ->
                entry.value.groupBy { task ->
                    if (task.dueDate > 0) {
                        Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    } else LocalDate.now()
                }.toSortedMap()
            }

            TasksUiState(
                groupedTasks = finalMap,
                isLoading = false,
                availableSubjects = mockSubjects
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TasksUiState()
        )

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

    fun addTask(title: String, date: Long, subject: String?, type: String?) {
        viewModelScope.launch {
            val description = if (subject != null) "Przedmiot: $subject ($type)" else ""
            val newTask = TaskEntity(
                title = title,
                dueDate = date,
                description = description,
                isCompleted = false
            )
            tasksRepository.insertTask(newTask)
        }
    }
}