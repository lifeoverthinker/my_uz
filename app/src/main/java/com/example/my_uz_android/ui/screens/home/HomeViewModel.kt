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
import com.example.my_uz_android.ui.screens.onboarding.UserGender
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
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

        val userName = settings?.userName ?: "Student"
        val isAnonymous = settings?.isAnonymous == true
        val genderStr = settings?.gender

        val greeting = if (isAnonymous) {
            if (genderStr == UserGender.STUDENTKA.name) "Cześć, Studentko 👋" else "Cześć, Studencie 👋"
        } else {
            "Cześć, $userName 👋"
        }

        val faculty = settings?.faculty
        val departmentInfo = if (!faculty.isNullOrBlank()) {
            faculty
        } else {
            "Uniwersytet Zielonogórski"
        }

        val effectiveClasses = if (classes.isEmpty()) {
            listOf(
                ClassEntity(
                    id = 999,
                    subjectName = "Przykładowe Zajęcia",
                    classType = "Wykład",
                    startTime = "10:00",
                    endTime = "11:30",
                    dayOfWeek = today.dayOfWeek.value,
                    groupCode = settings?.selectedGroupCode ?: "GRUPA",
                    subgroup = null,
                    room = "Sala 101",
                    teacherName = "Dr Jan Testowy"
                )
            )
        } else {
            classes
        }

        val todaysClasses = effectiveClasses.filter { classEntity ->
            classEntity.dayOfWeek == today.dayOfWeek.value
        }.sortedBy { it.startTime }

        val classesMsg = if (todaysClasses.isEmpty()) "Brak zajęć na dzisiaj" else null

        // Sortowanie: Najpierw nieukończone, potem po dacie
        val finalTasks = tasks
            .sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenBy { it.dueDate })
            .take(10)

        HomeUiState(
            greeting = greeting,
            departmentInfo = departmentInfo,
            upcomingClasses = todaysClasses,
            upcomingTasks = finalTasks,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = classesMsg,
            // POPRAWKA: Usunięto licznik, tylko tekst "Zadania"
            tasksMessage = "Zadania"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            val tasks = tasksRepository.getTasksStream().first()
            if (tasks.isEmpty()) {
                tasksRepository.insertTask(
                    TaskEntity(
                        title = "Projekt Zaliczeniowy",
                        description = "Dokończyć implementację ekranu szczegółów w aplikacji mobilnej.",
                        dueDate = System.currentTimeMillis() + 172800000L,
                        isCompleted = false,
                        subjectId = null
                    )
                )
            }
        }
    }

    fun toggleTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(isCompleted = isCompleted))
        }
    }
}