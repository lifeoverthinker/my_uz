package com.example.my_uz_android.ui.screens.calendar

/**
 * ViewModel obsługujący główne funkcje ekranu kalendarza (CalendarScreen).
 *
 * Zarządza stanem:
 * - Filtrowania planu (widoczność danych kierunków/podgrup)
 * - Pamięci wybranej daty i wybranego widoku (Miesiąc/Tydzień)
 * - Przełączaniem źródeł (Mój plan vs. Ulubione vs. Wyszukiwarka)
 */

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.util.NetworkResult
import com.example.my_uz_android.util.SubgroupMatcher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.time.LocalDate
import java.time.ZoneId

/**
 * Definicje źródła, z którego aktualnie pobierane są dane do kalendarza.
 */
sealed interface ScheduleSource {
    data object MyPlan : ScheduleSource
    data class Favorite(val resourceId: String, val name: String, val type: String) : ScheduleSource
    data class Preview(val resourceId: String, val name: String, val type: String) : ScheduleSource
}

/**
 * Struktura danych reprezentująca stan UI (widoku) Kalendarza.
 */
data class CalendarUiState(
    val userCourses: List<UserCourseEntity> = emptyList(),
    val selectedGroupCodes: Set<String> = emptySet(),
    val favorites: List<FavoriteEntity> = emptyList(),
    val visibleClasses: List<ClassEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val selectedPlanName: String = "Mój Plan",
    val selectedResourceId: String? = null,
    val classColorMap: Map<String, Int> = emptyMap(),
    val currentSource: ScheduleSource = ScheduleSource.MyPlan,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("Europe/Warsaw")),
    val isMonthView: Boolean = false,
    val temporaryClassForDetails: ClassEntity? = null,
    val themeMode: String = "SYSTEM"
)

class CalendarViewModel(
    application: Application,
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository,
    private val universityRepository: UniversityRepository,
    private val tasksRepository: TasksRepository
) : AndroidViewModel(application) {

    private val gson = Gson()

    // 1. Zmienne stanowe przetrzymujące "pamięć" kalendarza wewnątrz pamięci RAM urządzenia
    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    private val _previewState = MutableStateFlow<List<ClassEntity>>(emptyList())
    private val _currentSource = MutableStateFlow<ScheduleSource>(ScheduleSource.MyPlan)

    // Kluczowa zmiana UX: Pamiętamy datę, dopóki użytkownik nie zamknie całkowicie aplikacji!
    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.of("Europe/Warsaw")))
    private val _isMonthView = MutableStateFlow(false)

    private val _isLoadingNetwork = MutableStateFlow(false)
    private val _temporaryClassForDetails = MutableStateFlow<ClassEntity?>(null)
    private val isRefreshInProgress = AtomicBoolean(false)
    private var previewRequestVersion = 0L

    // Flaga powstrzymująca resetowanie wybranych kierunków
    private var isGroupsInitialized = false

    // Flaga powstrzymująca resetowanie planu po wejściu w Details
    private var hasLoadedInitialPlan = false

    // Reaktywny strumień główny aplikacji
    val uiState: StateFlow<CalendarUiState> = combine(
        userCourseRepository.getAllUserCoursesStream(),
        classRepository.getAllClassesStream(),
        favoritesRepository.favoritesStream,
        settingsRepository.getSettingsStream(),
        _selectedGroups,
        _currentSource,
        _previewState,
        _selectedDate,
        _isMonthView,
        _isLoadingNetwork,
        tasksRepository.getAllTasks(),
        _temporaryClassForDetails
    ) { args: Array<Any?> ->
        try {
            @Suppress("UNCHECKED_CAST") val courses = args[0] as List<UserCourseEntity>
            @Suppress("UNCHECKED_CAST") val myClasses = args[1] as List<ClassEntity>
            @Suppress("UNCHECKED_CAST") val favorites = args[2] as List<FavoriteEntity>
            val settings = args[3] as SettingsEntity?
            @Suppress("UNCHECKED_CAST") val selectedCodesRaw = args[4] as Set<String>
            val source = args[5] as ScheduleSource
            @Suppress("UNCHECKED_CAST") val previewClasses = args[6] as List<ClassEntity>
            val selectedDate = args[7] as LocalDate
            val isMonthView = args[8] as Boolean
            val isLoadingNet = args[9] as Boolean
            @Suppress("UNCHECKED_CAST") val tasks = args[10] as List<TaskEntity>
            val tempClass = args[11] as ClassEntity?

            // Tworzenie kolorów i połączenie wszystkich kierunków (Główny + Dodatkowe)
            val colorMap = parseColorMapCalendarVM(settings?.classColorsJson)
            val allCoursesForUi = buildAllCoursesForUiCalendarVM(settings, courses)
            val allUserCodes = allCoursesForUi.map { it.groupCode.trim().lowercase() }.toSet()

            // Pobranie aktywnych (zaznaczonych ptaszkiem) grup na podstawie pamięci
            val selectedCodes = initializeGroupsIfNeededCalendarVM(allUserCodes, selectedCodesRaw)
            val activeCodesLower = selectedCodes
                .map { it.trim().lowercase() }
                .filter { it in allUserCodes }
                .toSet()

            // Zaawansowane filtry - budowanie praw dostępu do konkretnych zajęć (z uwzględnieniem np. tylko Lab1, ale Wykład cały)
            val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, courses, activeCodesLower)

            // Odfiltrowanie klas na podstawie zaznaczonych kierunków
            val classesToShow = when (source) {
                is ScheduleSource.MyPlan -> {
                    val filtered = myClasses.filter { classItem ->
                        SubgroupMatcher.isClassVisible(
                            classItem.groupCode,
                            classItem.classType,
                            classItem.subgroup,
                            userEnrollments
                        )
                    }
                    // Zabezpieczenie: jeśli algorytm coś źle policzył, by nie wyczyścić komuś planu
                    if (filtered.isEmpty() && myClasses.isNotEmpty() && userEnrollments.isEmpty()) {
                        myClasses
                    } else {
                        filtered
                    }
                }
                is ScheduleSource.Favorite,
                is ScheduleSource.Preview -> previewClasses
            }

            val selectedResourceId = when (source) {
                is ScheduleSource.Favorite -> source.resourceId
                is ScheduleSource.Preview -> source.resourceId
                is ScheduleSource.MyPlan -> null
            }

            CalendarUiState(
                userCourses = allCoursesForUi,
                selectedGroupCodes = activeCodesLower,
                favorites = favorites,
                visibleClasses = classesToShow,
                tasks = tasks,
                classColorMap = colorMap,
                currentSource = source,
                selectedResourceId = selectedResourceId,
                selectedPlanName = when (source) {
                    is ScheduleSource.MyPlan -> "Mój Plan"
                    is ScheduleSource.Favorite -> source.name
                    is ScheduleSource.Preview -> source.name
                },
                isLoading = isLoadingNet,
                selectedDate = selectedDate,
                isMonthView = isMonthView,
                temporaryClassForDetails = tempClass,
                themeMode = settings?.themeMode ?: "SYSTEM"
            )
        } catch (e: Exception) {
            Log.e("CalendarVM", "Błąd mapowania stanu UI", e)
            CalendarUiState(error = e.localizedMessage, isLoading = false)
        }
    }.catch { e -> Log.e("CalendarVM", "Błąd Flow uiState", e) }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CalendarUiState(isLoading = false)
        )

    /**
     * Włącza lub wyłącza (Checkbox) widoczność danego kierunku studiów na głównym planie.
     */
    fun toggleGroupVisibility(groupCode: String) {
        val normalized = groupCode.trim().lowercase()
        val current = _selectedGroups.value.toMutableSet()

        // Nie pozwalamy odznaczyc ostatniego aktywnego kierunku, bo zostanie pusty ekran
        if (current.contains(normalized) && current.size <= 1) return

        if (current.contains(normalized)) current.remove(normalized) else current.add(normalized)
        _selectedGroups.value = current
    }

    /**
     * Zmienia źródło danych na domyślny plan zalogowanego studenta.
     * Dodano tu zabezpieczenie przed resetem przy nawigacji!
     */
    fun selectMyPlan(forceRefresh: Boolean = false) {
        previewRequestVersion++
        _isLoadingNetwork.value = false
        if (forceRefresh || !hasLoadedInitialPlan) {
            _currentSource.value = ScheduleSource.MyPlan
            refreshMyPlan()
            hasLoadedInitialPlan = true
        } else {
            // Jeśli już kiedyś ładowaliśmy Mój Plan, tylko podmieniamy Source.
            // Omijamy fetchowanie z serwera by zapobiec ładowaniu (isLoading = true)
            // za każdym razem jak cofamy się ze szczegółów.
            _currentSource.value = ScheduleSource.MyPlan
        }
    }

    /**
     * Dodaje lub usuwa plan z zakładki Ulubionych (ikona Serca).
     */
    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            runCatching {
                val resourceId = name
                val exists = favoritesRepository.existsByResourceIdAndType(resourceId, type)
                if (exists) {
                    favoritesRepository.deleteFavoriteByResourceIdAndType(resourceId, type)
                } else {
                    favoritesRepository.insertFavoriteIfAbsent(
                        FavoriteEntity(resourceId = resourceId, name = name, type = type)
                    )
                }
            }.onFailure { Log.e("CalendarVM", "Błąd zapisu ulubionych", it) }
        }
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _previewState.value = emptyList()
        _currentSource.value = ScheduleSource.Favorite(favorite.resourceId, favorite.name, favorite.type)
        val requestVersion = ++previewRequestVersion
        loadNetworkScheduleCalendarVM(favorite.name, favorite.type, requestVersion)
    }

    fun selectPreviewPlan(name: String, type: String) {
        _previewState.value = emptyList()
        _currentSource.value = ScheduleSource.Preview(name, name, type)
        val requestVersion = ++previewRequestVersion
        loadNetworkScheduleCalendarVM(name, type, requestVersion)
    }

    /**
     * Wewnętrzna funkcja uderzająca do API uniwersytetu (Scraper) pobierająca plany "Ulubionych".
     */
    private fun loadNetworkScheduleCalendarVM(name: String, type: String, requestVersion: Long) {
        viewModelScope.launch {
            _isLoadingNetwork.value = true
            try {
                val result = if (type == "teacher") universityRepository.getScheduleForTeacher(name)
                else universityRepository.getSchedule(name, emptyList())

                val resolvedPreview = if (result is NetworkResult.Success) result.data ?: emptyList() else emptyList()
                if (requestVersion == previewRequestVersion) {
                    _previewState.value = resolvedPreview
                }
            } catch (e: Exception) {
                Log.e("CalendarVM", "Błąd pobierania planu", e)
                if (requestVersion == previewRequestVersion) {
                    _previewState.value = emptyList()
                }
            } finally {
                if (requestVersion == previewRequestVersion) {
                    _isLoadingNetwork.value = false
                }
            }
        }
    }

    /**
     * Główna funkcja synchronizacyjna uderzająca do bazy Room i API uniwersytetu.
     */
    fun refreshMyPlan() {
        viewModelScope.launch {
            if (!isRefreshInProgress.compareAndSet(false, true)) return@launch
            try {
                val settings = settingsRepository.getSettingsStream().firstOrNull() ?: return@launch
                val groupCodes = mutableListOf<Pair<String, String?>>()

                settings.selectedGroupCode?.let { groupCodes.add(it to settings.selectedSubgroup) }
                val extraCourses = userCourseRepository.getAllUserCoursesStream().firstOrNull().orEmpty()
                extraCourses.forEach { groupCodes.add(it.groupCode to it.selectedSubgroup) }

                if (groupCodes.isEmpty()) return@launch

                // Tu możemy dać UI feedback, że odświeża plan w tle, jeśli ma internet
                _isLoadingNetwork.value = true
                groupCodes
                    .distinctBy { (code, subgroup) -> code.trim().lowercase() to subgroup?.trim()?.lowercase() }
                    .forEach { (code, subgroup) ->
                        universityRepository.refreshSchedule(code, subgroup, classRepository)
                    }
            } catch (e: Exception) {
                Log.e("CalendarVM", "Błąd odświeżania planu", e)
            } finally {
                _isLoadingNetwork.value = false
                isRefreshInProgress.set(false)
            }
        }
    }

    fun setSelectedDate(date: LocalDate) { _selectedDate.value = date }
    fun setMonthView(isMonth: Boolean) { _isMonthView.value = isMonth }
    fun setTemporaryClassForDetails(classEntity: ClassEntity?) { _temporaryClassForDetails.value = classEntity }

    /**
     * Mapuje losowe kolory zajęć z formatu tekstowego (JSON) na stałe numery int (0,1,2..).
     */
    private fun parseColorMapCalendarVM(rawJson: String?): Map<String, Int> {
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson<Map<String, Int>>(rawJson ?: "{}", type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun buildAllCoursesForUiCalendarVM(settings: SettingsEntity?, courses: List<UserCourseEntity>): List<UserCourseEntity> {
        val result = mutableListOf<UserCourseEntity>()
        settings?.selectedGroupCode?.let { mainCode ->
            result.add(UserCourseEntity(id = -1, groupCode = mainCode, fieldOfStudy = settings.fieldOfStudy ?: mainCode, semester = settings.currentSemester, selectedSubgroup = settings.selectedSubgroup))
        }
        courses.forEach { course ->
            if (result.none { it.groupCode.trim().lowercase() == course.groupCode.trim().lowercase() }) result.add(course)
        }
        return result
    }

    /**
     * Pamięć zaznaczonych przez użytkownika filtrów i kierunków.
     * Nie resetuje się w trakcie przeglądania!
     */
    private fun initializeGroupsIfNeededCalendarVM(allUserCodes: Set<String>, selectedCodesRaw: Set<String>): Set<String> {
        val normalizedSelected = selectedCodesRaw
            .map { it.trim().lowercase() }
            .filter { it in allUserCodes }
            .toSet()

        if (!isGroupsInitialized && allUserCodes.isNotEmpty()) {
            _selectedGroups.value = allUserCodes
            isGroupsInitialized = true
            return allUserCodes
        }

        if (normalizedSelected.isNotEmpty()) return normalizedSelected

        val current = _selectedGroups.value
            .map { it.trim().lowercase() }
            .filter { it in allUserCodes }
            .toSet()

        return if (current.isNotEmpty()) current else allUserCodes
    }
}