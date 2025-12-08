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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
    val classesDayLabel: String? = null // "Dzisiaj" lub "Jutro"
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
        val tomorrow = today.plusDays(1)

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

        // ========== ZAJĘCIA: DZISIAJ vs JUTRO ==========

        // Zajęcia z bazy danych lub przykładowe (jeśli puste)
        val effectiveClasses = if (classes.isEmpty()) {
            listOf(
                ClassEntity(
                    id = 999,
                    subjectName = "Przykładowe Zajęcia",
                    classType = "Wykład",
                    startTime = "10:00",
                    endTime = "11:30",
                    dayOfWeek = today.dayOfWeek.value, // ISO 8601: 1=PON, 7=NIEDZ
                    groupCode = settings?.selectedGroupCode ?: "GRUPA",
                    subgroup = null,
                    room = "Sala 101",
                    teacherName = "Dr Jan Testowy"
                )
            )
        } else {
            classes
        }

        // ✅ KROK 1: Filtruj zajęcia na DZISIAJ (dayOfWeek == today.dayOfWeek.value)
        val todaysClasses = effectiveClasses
            .filter { it.dayOfWeek == today.dayOfWeek.value }
            .sortedBy { it.startTime }

        // ✅ KROK 2: Filtruj zajęcia na JUTRO (dayOfWeek == tomorrow.dayOfWeek.value)
        val tomorrowsClasses = effectiveClasses
            .filter { it.dayOfWeek == tomorrow.dayOfWeek.value }
            .sortedBy { it.startTime }

        // ✅ KROK 3: Wybierz które zajęcia pokazać + ustal label
        val (displayedClasses, dayLabel, emptyMessage) = when {
            todaysClasses.isNotEmpty() -> Triple(todaysClasses, "Dzisiaj", null)
            tomorrowsClasses.isNotEmpty() -> Triple(tomorrowsClasses, "Jutro", null)
            else -> Triple(emptyList(), null, "Brak zajęć na dzisiaj")
        }

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
            upcomingClasses = displayedClasses,
            upcomingTasks = finalTasks,
            upcomingEvents = mockEvents,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = emptyMessage,
            classesDayLabel = dayLabel, // "Dzisiaj" lub "Jutro"
            tasksMessage = "Zadania",
            isLoading = false
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    // ========== MOCK DATA (pierwsze uruchomienie) ==========
    init {
        viewModelScope.launch {
            val tasks = tasksRepository.getTasksStream().first()
            if (tasks.isEmpty()) {
                tasksRepository.insertTask(
                    TaskEntity(
                        title = "Projekt Zaliczeniowy",
                        description = "Dokończyć implementację aplikacji.",
                        dueDate = System.currentTimeMillis() + 172800000L, // +2 dni
                        isCompleted = false,
                        subjectId = null
                    )
                )
            }
        }
    }
}
