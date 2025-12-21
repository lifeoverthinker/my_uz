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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Witaj w MyUZ!",
    val userInitials: String = "",
    val departmentInfo: String = "",
    val upcomingClasses: List<ClassEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val upcomingEvents: List<EventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val currentDate: String = "",
    val tasksMessage: String? = null,
    val classesMessage: String? = null,
    val classesDayLabel: String? = null,
    val isPlanSelected: Boolean = false,
    val classColorMap: Map<String, Int> = emptyMap(),
    val isDarkMode: Boolean = false
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val classRepository: ClassRepository,
    private val tasksRepository: TasksRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
    private val gson = Gson()

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.getSettingsStream(),
        classRepository.getAllClassesStream(),
        tasksRepository.getAllTasks()
    ) { settings: SettingsEntity?, classes: List<ClassEntity>, tasks: List<TaskEntity> ->

        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // --- NOWA LOGIKA POWITAŃ ---
        val isAnonymous = settings?.isAnonymous == true
        val hasGroup = !settings?.selectedGroupCode.isNullOrBlank()
        val gender = settings?.gender // "STUDENT" lub "STUDENTKA"

        // 1. Ustalanie Powitania i Inicjałów
        val (greeting, initials) = when {
            // SCENARIUSZ 1: TRYB GOŚCIA (Pomiń - brak grupy)
            isAnonymous && !hasGroup -> {
                "Witaj w MyUZ!" to ""
            }
            // SCENARIUSZ 2: TRYB ANONIMOWY (Anonimowy, ale z grupą)
            isAnonymous && hasGroup -> {
                val suffix = if (gender == "STUDENTKA") "studentko" else "studencie"
                "Cześć $suffix 👋" to "" // Inicjały puste, bo to anonim
            }
            // SCENARIUSZ 3: TRYB PEŁNY (Z imieniem)
            else -> {
                val rawName = settings?.userName ?: ""
                val parts = rawName.trim().split(" ").filter { it.isNotBlank() }
                val firstName = parts.firstOrNull() ?: ""
                val initialsStr = if (parts.size >= 2) {
                    "${parts.first().take(1)}${parts.last().take(1)}"
                } else {
                    firstName.take(2)
                }.uppercase()
                "Cześć, $firstName 👋" to initialsStr
            }
        }

        // 2. Ustalanie Podtytułu (Wydział lub Uczelnia)
        val faculty = settings?.faculty
        val departmentInfo = when {
            // Dla Gościa zawsze Uczelnia
            isAnonymous && !hasGroup -> "Uniwersytet Zielonogórski"
            // Dla reszty (Anonim i Student) - nazwa wydziału jeśli jest, w przeciwnym razie Uczelnia
            !faculty.isNullOrBlank() -> faculty
            else -> "Uniwersytet Zielonogórski"
        }

        val isPlanSelected = hasGroup // Używamy tej samej flagi co wyżej

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
            Triple(emptyList(), null, "Wybierz plan zajęć w profilu")
        }

        val finalTasks = tasks
            .filter { !it.isCompleted }
            .sortedBy { it.dueDate }
            .take(5)

        val mockEvents = listOf(
            EventEntity(
                id = 1,
                title = "Juwenalia 2025",
                description = "Największa impreza roku!",
                date = "Piątek, 20 maja 2025",
                location = "Kampus A",
                timeRange = "18:00 - 02:00"
            )
        )

        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val classColorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        HomeUiState(
            greeting = greeting,
            userInitials = initials,
            departmentInfo = departmentInfo,
            upcomingClasses = displayedClasses,
            upcomingTasks = finalTasks,
            upcomingEvents = mockEvents,
            currentDate = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
            classesMessage = emptyMessage,
            classesDayLabel = dayLabel,
            tasksMessage = if (finalTasks.isEmpty()) "Brak zadań" else "Najbliższe zadania",
            isLoading = false,
            isPlanSelected = isPlanSelected,
            classColorMap = classColorMap,
            isDarkMode = settings?.isDarkMode ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}