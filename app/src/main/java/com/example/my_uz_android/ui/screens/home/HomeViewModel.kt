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
    val classesDayLabel: String? = null,
    val isPlanSelected: Boolean = false
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
        tasksRepository.getAllTasks()
    ) { settings: SettingsEntity?, classes: List<ClassEntity>, tasks: List<TaskEntity> ->

        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // PERSONALIZACJA
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

        // SPRAWDZENIE CZY PLAN WYBRANY
        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank() && classes.isNotEmpty()

        // ZAJĘCIA - tylko jeśli plan wybrany
        val todayString = today.toString()
        val tomorrowString = today.plusDays(1).toString()

        val (displayedClasses, dayLabel, emptyMessage) = if (isPlanSelected) {
            val todaysClasses = classes
                .filter { it.date == todayString }
                .sortedBy { it.startTime }

            val tomorrowsClasses = classes
                .filter { it.date == tomorrowString }
                .sortedBy { it.startTime }

            when {
                todaysClasses.isNotEmpty() -> Triple(todaysClasses, "Dzisiaj", null)
                tomorrowsClasses.isNotEmpty() -> Triple(tomorrowsClasses, "Jutro", null)
                else -> Triple(emptyList(), null, "Brak zajęć w najbliższych dniach")
            }
        } else {
            Triple(emptyList(), null, "Wybierz plan zajęć w ustawieniach")
        }

        // ZADANIA - zawsze wyświetlamy (mogą być dodane ręcznie)
        val finalTasks = tasks
            .sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenBy { it.dueDate })
            .take(10)

        // WYDARZENIA - mock data
        val mockEvents = listOf(
            EventEntity(
                id = 1,
                title = "Juwenalia 2025",
                description = "Największa impreza roku!",
                date = "Piątek, 20 maja 2025",
                location = "Kampus A",
                timeRange = "18:00 - 02:00"
            ),
            EventEntity(
                id = 2,
                title = "Targi Pracy IT",
                description = "Oferty staży i pracy",
                date = "Niedziela, 1 czerwca 2025",
                location = "Aula C",
                timeRange = "10:00 - 15:00"
            )
        )

        HomeUiState(
            greeting = greeting,
            departmentInfo = departmentInfo,
            upcomingClasses = displayedClasses,
            upcomingTasks = finalTasks,
            upcomingEvents = mockEvents,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = emptyMessage,
            classesDayLabel = dayLabel,
            tasksMessage = if (finalTasks.isEmpty()) "Brak zadań" else "Zadania",
            isLoading = false,
            isPlanSelected = isPlanSelected
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
