package com.example.my_uz_android.ui.screens.home

/**
 * ViewModel zarządzający Ekranem Głównym (Pulpitem) aplikacji.
 * Odpowiada za serwowanie dzisiejszego planu zajęć, powitania studenta,
 * zadań na dziś oraz statystyk postępu semestru.
 * Obsługuje inteligentne łączenie głównego kierunku studiów (z Settings) z dodatkowymi (UserCourse).
 */

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.EventRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.util.classesStillRemainingToday
import com.example.my_uz_android.util.SubgroupMatcher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Stan danych (Data Class) przekazywany do interfejsu ekranu Home.
 */
data class HomeUiState(
    val userName: String = "Student",
    // 1. ZMIANA: Zamiast jednego Stringa, używamy listy stringów reprezentujących kierunki
    val studyFields: List<String> = emptyList(),
    // Nowe pole: lista wydziałów do powitania
    val faculties: List<String> = emptyList(),
    val semester: Int? = null,
    val isLoading: Boolean = false,
    val todaysClasses: List<ClassEntity> = emptyList(),
    val tomorrowClasses: List<ClassEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val todaysEvents: List<EventEntity> = emptyList(),
    val semesterProgress: Float = 0f,
    val daysLeftInSemester: Int = 0,
    val error: String? = null,
    val classColorMap: Map<String, Int> = emptyMap(),
    // 2. ZMIANA: Flaga potrzebna, aby wymusić odświeżenie UI po Północy ("Klątwa Północy")
    val currentDateReference: LocalDate = LocalDate.now(ZoneId.of("Europe/Warsaw"))
)

class HomeViewModel(
    application: Application,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val tasksRepository: TasksRepository,
    private val universityRepository: UniversityRepository,
    private val userCourseRepository: UserCourseRepository,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val gson = Gson()
    private val _isLoadingNetwork = MutableStateFlow(false)
    private val isRefreshInProgress = AtomicBoolean(false)

    // 3. ZMIANA: Strumień "tikający" na bieżąco, wymuszający przeliczanie planu, jak wybije północ
    private val _currentTimeReference = MutableStateFlow(LocalDateTime.now(ZoneId.of("Europe/Warsaw")))
    private var timeTickerJob: Job? = null

    // Rozpoczynamy "Zegarek", gdy ViewModel zostanie stworzony
    init {
        startTimeTicker()
    }

    /**
     * Strumień serwujący połączony stan UI (Baza + Opcje) z wykorzystaniem combine.
     * Dodaliśmy w arg[6] zmienną czasu, by "odtykała" Flow, kiedy zmienia się dzień.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.getSettingsStream(),
        classRepository.getAllClassesStream(),
        tasksRepository.getAllTasks(),
        userCourseRepository.getAllUserCoursesStream(),
        eventRepository.getAllEventsStream(),
        _isLoadingNetwork,
        _currentTimeReference // Reakcja na upływający czas!
    ) { args: Array<Any?> ->
        try {
            val settings = args[0] as SettingsEntity?
            @Suppress("UNCHECKED_CAST") val myClasses = args[1] as List<ClassEntity>
            @Suppress("UNCHECKED_CAST") val tasks = args[2] as List<TaskEntity>
            @Suppress("UNCHECKED_CAST") val courses = args[3] as List<UserCourseEntity>
            @Suppress("UNCHECKED_CAST") val events = args[4] as List<EventEntity>
            val isLoadingNet = args[5] as Boolean
            val nowReference = args[6] as LocalDateTime

            val today = nowReference.toLocalDate()
            val tomorrow = today.plusDays(1)

            // Wyciągamy kolory z formatu JSON
            val colorMap = try {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson<Map<String, Int>>(settings?.classColorsJson ?: "{}", type) ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }


            // --- 4.1: BUDOWANIE LISTY KIERUNKÓW (jak dotychczas)
            val allStudyFields = mutableListOf<String>()
            settings?.selectedGroupCode?.let { mainGroup ->
                val fieldStr = if (!settings.fieldOfStudy.isNullOrBlank()) {
                    "${settings.fieldOfStudy} ($mainGroup)"
                } else {
                    mainGroup
                }
                allStudyFields.add(fieldStr)
            }
            courses.forEach { course ->
                val fieldStr = if (!course.fieldOfStudy.isNullOrBlank()) {
                    "${course.fieldOfStudy} (${course.groupCode})"
                } else {
                    course.groupCode
                }
                if (!allStudyFields.contains(fieldStr)) {
                    allStudyFields.add(fieldStr)
                }
            }

            // --- 4.2: BUDOWANIE LISTY WYDZIAŁÓW ---
            val facultiesSet = mutableSetOf<String>()
            settings?.faculty?.let { if (!it.isNullOrBlank()) facultiesSet.add(it) }
            courses.forEach { course ->
                course.faculty?.let { if (!it.isNullOrBlank()) facultiesSet.add(it) }
            }
            val facultiesList = facultiesSet.toList().sorted()

            // Tworzymy gigantyczną listę wszystkich uprawnień (Enrollments) ze wszystkich dodanych kierunków.
            // null z boku oznacza, że nie mamy tu wykluczających filtrów, więc algorytm założy "Bierz wszystko".
            val allUserCodesLower = mutableSetOf<String>()
            settings?.selectedGroupCode?.let { allUserCodesLower.add(it.trim().lowercase()) }
            courses.forEach { allUserCodesLower.add(it.groupCode.trim().lowercase()) }

            val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, courses, allUserCodesLower)

            // Filtrujemy wszystkie klasy dla użytkownika. Mamy "wszystkie kierunki = włączone domyślnie"
            val visibleClasses = myClasses.filter { classItem ->
                SubgroupMatcher.isClassVisible(
                    classItem.groupCode,
                    classItem.classType,
                    classItem.subgroup,
                    userEnrollments
                )
            }

            val finalVisibleClasses = if (visibleClasses.isEmpty() && myClasses.isNotEmpty() && userEnrollments.isEmpty()) {
                myClasses
            } else {
                visibleClasses
            }

            val classesForToday = classesStillRemainingToday(
                classes = finalVisibleClasses,
                today = today,
                nowTime = nowReference.toLocalTime()
            )
            val classesForTomorrow = finalVisibleClasses.filter { it.date == tomorrow.toString() }
            val upcomingTasks = tasks.filter { !it.isCompleted }.sortedBy { it.dueDate }
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale("pl", "PL"))
            val todayStr = today.format(formatter).replaceFirstChar { it.uppercase() }
            val todaysEvents = events.filter { it.date == todayStr }

            val totalDays = ChronoUnit.DAYS.between(LocalDate.of(2025, 2, 24), LocalDate.of(2025, 6, 20)).toFloat()
            val daysPassed = ChronoUnit.DAYS.between(LocalDate.of(2025, 2, 24), today).coerceAtLeast(0L).toFloat()
            val progress = (daysPassed / totalDays).coerceIn(0f, 1f)
            val left = ChronoUnit.DAYS.between(today, LocalDate.of(2025, 6, 20)).coerceAtLeast(0).toInt()

            // 5. ZMIANA: Tymczasowe opóźnienie, by uniknąć migania "Pustego ekranu"
            val showLoading = isLoadingNet || (settings == null)

            HomeUiState(
                userName = settings?.userName ?: "Student",
                studyFields = allStudyFields, // Przekazujemy nową listę kierunków do UI!
                faculties = facultiesList, // Przekazujemy listę wydziałów do UI!
                semester = settings?.currentSemester,
                isLoading = showLoading,
                todaysClasses = classesForToday,
                tomorrowClasses = classesForTomorrow,
                upcomingTasks = upcomingTasks,
                todaysEvents = todaysEvents,
                semesterProgress = progress,
                daysLeftInSemester = left,
                classColorMap = colorMap,
                currentDateReference = today
            )
        } catch (e: Exception) {
            Log.e("HomeVM", "Błąd generowania stanu UI", e)
            HomeUiState(error = e.localizedMessage, isLoading = false)
        }
    }.catch { e -> Log.e("HomeVM", "Flow error", e) }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeUiState(isLoading = true) // Ważne by startować z "true" - naprawa Migania
        )

    /**
     * Wymusza twardą synchronizację planu w tle, uderzając do API serwera.
     */
    fun refreshSchedule() {
        viewModelScope.launch {
            if (!isRefreshInProgress.compareAndSet(false, true)) return@launch
            try {
                val settings = settingsRepository.getSettingsStream().firstOrNull() ?: return@launch
                val groupCodes = mutableListOf<Pair<String, String?>>()

                settings.selectedGroupCode?.let { groupCodes.add(it to settings.selectedSubgroup) }

                val extraCourses = userCourseRepository.getAllUserCoursesStream().firstOrNull().orEmpty()
                extraCourses.forEach { groupCodes.add(it.groupCode to it.selectedSubgroup) }

                if (groupCodes.isEmpty()) return@launch

                _isLoadingNetwork.value = true
                // Skrypt Supabase do odświeżania na podstawie danych UZ
                groupCodes
                    .distinctBy { (code, subgroup) -> code.trim().lowercase() to subgroup?.trim()?.lowercase() }
                    .forEach { (code, subgroup) ->
                    universityRepository.refreshSchedule(code, subgroup, classRepository)
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Błąd odświeżania: ${e.message}")
            } finally {
                _isLoadingNetwork.value = false
                isRefreshInProgress.set(false)
            }
        }
    }

    /**
     * Zegarek systemowy chroniący przed błędem "Klątwy Północy".
     * Co minutę odświeża bieżący czas. Jeśli minie 00:00, Flow samoistnie przeliczy `today` i `tomorrow`
     * bez konieczności restartowania aplikacji.
     */
    private fun startTimeTicker() {
        timeTickerJob?.cancel()
        timeTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L) // Czekaj minutę
                val newTime = LocalDateTime.now(ZoneId.of("Europe/Warsaw"))
                _currentTimeReference.value = newTime
            }
        }
    }
}