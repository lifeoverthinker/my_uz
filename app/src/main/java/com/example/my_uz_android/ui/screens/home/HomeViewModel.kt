package com.example.my_uz_android.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.NotificationsRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.util.SubgroupMatcher
import com.example.my_uz_android.widget.triggerWidgetUpdate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Witaj w MyUZ!",
    val userInitials: String = "",
    val departmentInfo: String = "",
    val upcomingClasses: List<ClassEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val upcomingEvents: List<EventEntity> = emptyList(),
    val isLoading: Boolean = true,
    val currentDate: String = "",
    val tasksMessage: String? = null,
    val classesMessage: String? = null,
    val classesDayLabel: String? = null,
    val isPlanSelected: Boolean = false,
    val classColorMap: Map<String, Int> = emptyMap(),
    val themeMode: String = "SYSTEM",
    val hasUnreadNotifications: Boolean = false
)

class HomeViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val classRepository: ClassRepository,
    private val tasksRepository: TasksRepository,
    private val universityRepository: UniversityRepository,
    private val notificationsRepository: NotificationsRepository,
    private val userCourseRepository: UserCourseRepository
) : AndroidViewModel(application) {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
    private val gson = Gson()

    init {
        viewModelScope.launch {
            combine(
                classRepository.getUpcomingClasses(),
                settingsRepository.getSettingsStream()
            ) { classes, settings ->
                Pair(classes, settings)
            }.collectLatest {
                triggerWidgetUpdate(getApplication())
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.getSettingsStream(),
        classRepository.getUpcomingClasses(),
        userCourseRepository.getAllUserCoursesStream(),
        tasksRepository.getAllTasks(),
        notificationsRepository.getUnreadCount()
    ) { settings: SettingsEntity?, upcomingClasses: List<ClassEntity>, courses: List<UserCourseEntity>, tasks: List<TaskEntity>, unreadCount: Int ->

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()

        val isAnonymous = settings?.isAnonymous == true
        val hasGroup = !settings?.selectedGroupCode.isNullOrBlank()
        val gender = settings?.gender

        val (greeting, initials) = when {
            isAnonymous && !hasGroup -> "Witaj w MyUZ!" to ""
            isAnonymous && hasGroup -> {
                val suffix = if (gender == "STUDENTKA") "Studentko" else "Studencie"
                "Cześć, $suffix 👋" to ""
            }
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

        val departmentInfo = when {
            !settings?.faculty.isNullOrBlank() -> settings?.faculty!!
            else -> "Uniwersytet Zielonogórski"
        }

        val isPlanSelected = hasGroup

        // Zunifikowana regula intersection dla podgrup (wspolna z Calendar/Grades)
        val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, courses)
        val visibleClasses = upcomingClasses.filter { classItem ->
            SubgroupMatcher.isClassVisible(
                classItem.groupCode,
                classItem.classType,
                classItem.subgroup,
                userEnrollments
            )
        }

        val classesForToday = visibleClasses.filter {
            it.date == today.toString() && runCatching { LocalTime.parse(it.endTime).isAfter(nowTime) }.getOrDefault(true)
        }

        val tomorrow = today.plusDays(1)
        val classesForTomorrow = visibleClasses.filter {
            it.date == tomorrow.toString()
        }

        val (displayedClasses, dayLabel, emptyMessage) = when {
            !isPlanSelected -> Triple(emptyList(), null, "Wybierz plan zajęć w profilu")
            classesForToday.isNotEmpty() -> Triple(classesForToday, "Dzisiaj", null)
            classesForTomorrow.isNotEmpty() -> Triple(classesForTomorrow, "Jutro", null)
            else -> Triple(emptyList(), "Dzisiaj", "Brak zajęć na dziś i jutro")
        }

        val finalTasks = tasks
            .filter { !it.isCompleted }
            .sortedBy { it.dueDate }
            .take(5)

        val mockEvents = listOf(
            EventEntity(id = 1, title = "Juwenalia 2026", description = "Największa impreza...", date = "Piątek, 20 maja 2026", location = "Kampus B", timeRange = "18:00 - 02:00"),
            EventEntity(id = 2, title = "Dzień Otwarty UZ", description = "Poznaj ofertę edukacyjną...", date = "Środa, 15 kwietnia 2026", location = "Budynek Główny", timeRange = "10:00 - 15:00"),
            EventEntity(id = 3, title = "Hackathon MyUZ", description = "24 godziny programowania...", date = "Sobota, 10 maja 2026", location = "Centrum Komputerowe", timeRange = "09:00 - 09:00 (+1 dzień)")
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
            hasUnreadNotifications = unreadCount > 0,
            themeMode = settings?.themeMode ?: "SYSTEM"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )
}