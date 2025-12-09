package com.example.my_uz_android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.EventEntity
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Cześć, Gościu 👋",
    val departmentInfo: String = "",
    val upcomingClasses: List<ClassEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val upcomingEvents: List<EventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val currentDate: String = "",
    val tasksMessage: String? = null,
    val classesMessage: String? = null,
    val classesDayLabel: String? = null
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

        // ========== PERSONALIZACJA ==========
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

        // ========== ZAJĘCIA ==========
        val effectiveClasses = if (classes.isEmpty()) {
            listOf(
                ClassEntity(
                    id = 999,
                    subjectName = "Przykładowe Zajęcia",
                    classType = "Wykład",
                    startTime = "10:00",
                    endTime = "11:30",
                    dayOfWeek = today.dayOfWeek.value,
                    date = today.toString(), // ← DODAJ TO
                    groupCode = settings?.selectedGroupCode ?: "GRUPA",
                    subgroup = null,
                    room = "Sala 101",
                    teacherName = "Dr Jan Testowy"
                )
            )
        } else {
            classes
        }


        val todayString = today.toString() // "2025-12-09"
        val todaysClasses = effectiveClasses
            .filter { it.date == todayString } // ← ZMIANA
            .sortedBy { it.startTime }

        // Ustal label i komunikat
        val dayLabel = if (todaysClasses.isNotEmpty()) "Dzisiaj" else null
        val emptyMessage = if (todaysClasses.isEmpty()) "Brak zajęć na dzisiaj" else null

        // ========== ZADANIA ==========
        val finalTasks = tasks
            .sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenBy { it.dueDate })
            .take(10)

        // ========== WYDARZENIA (mock) ==========
        val mockEvents = listOf(
            EventEntity(
                title = "Juwenalia 2025",
                description = "Największa impreza roku!",
                date = "2025-05-20",
                location = "Kampus A",
                timeRange = "18:00 - 02:00"
            ),
            EventEntity(
                title = "Targi Pracy",
                description = "Oferty staży IT",
                date = "2025-06-01",
                location = "Aula C",
                timeRange = "10:00 - 15:00"
            )
        )

        // ========== ZWRÓĆ UI STATE ==========
        HomeUiState(
            greeting = greeting,
            departmentInfo = departmentInfo,
            upcomingClasses = todaysClasses, // ← ZMIANA: tylko dzisiejsze
            upcomingTasks = finalTasks,
            upcomingEvents = mockEvents,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = emptyMessage,
            classesDayLabel = dayLabel,
            tasksMessage = "Zadania",
            isLoading = false
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
                        description = "Dokończyć implementację aplikacji.",
                        dueDate = System.currentTimeMillis() + 172800000L,
                        isCompleted = false,
                        subjectId = null
                    )
                )
            }
        }
    }
}
