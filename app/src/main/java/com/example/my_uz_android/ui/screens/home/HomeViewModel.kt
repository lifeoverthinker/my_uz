package com.example.my_uz_android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Cześć, Gościu 👋",
    val departmentInfo: String = "",
    val upcomingClasses: List<ClassEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val currentDate: String = "",
    val tasksMessage: String? = null,
    val classesMessage: String? = null
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val classRepository: ClassRepository,
    private val tasksRepository: TasksRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.getSettingsStream(),
        classRepository.getAllClassesStream(),
        tasksRepository.getTasksStream()
    ) { settings: SettingsEntity?, classes: List<ClassEntity>, tasks: List<TaskEntity> ->

        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // 1. POWITANIE
        val userName = settings?.userName ?: "Student"
        val isAnonymous = settings?.isAnonymous == true

        val greeting = if (isAnonymous) {
            "Cześć, Studencie 👋"
        } else {
            "Cześć, $userName 👋"
        }

        // 2. WYDZIAŁ (Bez kodu grupy)
        val faculty = settings?.faculty

        val departmentInfo = if (!faculty.isNullOrBlank()) {
            faculty
        } else {
            "Uniwersytet Zielonogórski"
        }

        // 3. ZAJĘCIA
        val todaysClasses = classes.filter { classEntity ->
            classEntity.dayOfWeek == today.dayOfWeek.value
        }.sortedBy { it.startTime }

        val classesMsg = if (todaysClasses.isEmpty()) "Brak zajęć na dzisiaj" else null

        // 4. ZADANIA
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val endOfNextWeek = endOfWeek.plusWeeks(1)

        val currentWeekTasks = tasks.filter { task ->
            val taskDate = try { LocalDate.parse(task.dueDate) } catch (e: Exception) { null }
            if (taskDate != null) {
                !task.isCompleted &&
                        (taskDate.isEqual(startOfWeek) || taskDate.isAfter(startOfWeek)) &&
                        (taskDate.isEqual(endOfWeek) || taskDate.isBefore(endOfWeek))
            } else false
        }.sortedBy { it.dueDate }

        val nextWeekTasks = tasks.filter { task ->
            val taskDate = try { LocalDate.parse(task.dueDate) } catch (e: Exception) { null }
            if (taskDate != null) {
                !task.isCompleted &&
                        taskDate.isAfter(endOfWeek) &&
                        (taskDate.isEqual(endOfNextWeek) || taskDate.isBefore(endOfNextWeek))
            } else false
        }.sortedBy { it.dueDate }

        // Wybierz listę, ale nazwa sekcji zawsze "Zadania"
        val finalTasks = if (currentWeekTasks.isNotEmpty()) currentWeekTasks else if (nextWeekTasks.isNotEmpty()) nextWeekTasks else emptyList()
        val tasksMsg = "Zadania"

        HomeUiState(
            greeting = greeting,
            departmentInfo = departmentInfo,
            upcomingClasses = todaysClasses,
            upcomingTasks = finalTasks,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = classesMsg,
            tasksMessage = tasksMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}