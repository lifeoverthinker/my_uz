package com.example.my_uz_android.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.*
import com.example.my_uz_android.data.repositories.*
import com.example.my_uz_android.util.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

sealed interface ScheduleSource {
    data object MyPlan : ScheduleSource
    data class Favorite(val resourceId: String, val name: String, val type: String) : ScheduleSource
    data class Preview(val resourceId: String, val name: String, val type: String) : ScheduleSource
}

data class CalendarUiState(
    val userCourses: List<UserCourseEntity> = emptyList(),
    val selectedGroupCodes: Set<String> = emptySet(),
    val favorites: List<FavoriteEntity> = emptyList(),
    val visibleClasses: List<ClassEntity> = emptyList(),
    val selectedPlanName: String = "Mój Plan",
    val selectedResourceId: String? = null,
    val classColorMap: Map<String, Int> = emptyMap(),
    val currentSource: ScheduleSource = ScheduleSource.MyPlan,
    val isLoading: Boolean = false,
    val error: String? = null,
    // DODANE: Stan kalendarza przeniesiony z UI
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("Europe/Warsaw")),
    val isMonthView: Boolean = false
)

class CalendarViewModel(
    private val application: Application,
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository,
    private val universityRepository: UniversityRepository,
    private val tasksRepository: TasksRepository
) : AndroidViewModel(application) {

    private val gson = Gson()
    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    private val _previewState = MutableStateFlow<List<ClassEntity>>(emptyList())
    private val _currentSource = MutableStateFlow<ScheduleSource>(ScheduleSource.MyPlan)

    // DODANE: Strumienie trzymające wybrany dzień i widok
    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.of("Europe/Warsaw")))
    private val _isMonthView = MutableStateFlow(false)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CalendarUiState> = combine(
        userCourseRepository.getAllUserCoursesStream(),
        classRepository.getAllClassesStream(),
        favoritesRepository.favoritesStream,
        settingsRepository.getSettingsStream(),
        _selectedGroups,
        _currentSource,
        _previewState,
        _selectedDate,
        _isMonthView
    ) { args: Array<Any?> ->
        val courses = args[0] as List<UserCourseEntity>
        val myClasses = args[1] as List<ClassEntity>
        val favorites = args[2] as List<FavoriteEntity>
        val settings = args[3] as SettingsEntity?
        val selectedCodes = args[4] as Set<String>
        val source = args[5] as ScheduleSource
        val previewClasses = args[6] as List<ClassEntity>
        val selectedDate = args[7] as LocalDate
        val isMonthView = args[8] as Boolean

        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val colorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (_: Exception) { emptyMap() }

        val activeCodes = if (selectedCodes.isEmpty()) {
            val codes = courses.map { it.groupCode }.toMutableSet()
            if (!settings?.selectedGroupCode.isNullOrBlank()) codes.add(settings!!.selectedGroupCode!!)
            codes
        } else {
            selectedCodes
        }

        val classesToShow = when (source) {
            is ScheduleSource.MyPlan -> myClasses.filter { activeCodes.contains(it.groupCode) }
            else -> previewClasses
        }

        val resourceId = when (source) {
            is ScheduleSource.Favorite -> source.resourceId
            is ScheduleSource.Preview -> source.resourceId
            else -> null
        }

        CalendarUiState(
            userCourses = courses,
            selectedGroupCodes = activeCodes,
            favorites = favorites,
            visibleClasses = classesToShow,
            classColorMap = colorMap,
            currentSource = source,
            selectedResourceId = resourceId,
            selectedPlanName = when (source) {
                is ScheduleSource.MyPlan -> "Mój Plan"
                is ScheduleSource.Favorite -> source.name
                is ScheduleSource.Preview -> source.name
            },
            isLoading = false,
            selectedDate = selectedDate,
            isMonthView = isMonthView
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState(isLoading = true))

    fun toggleGroupVisibility(groupCode: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(groupCode)) current.remove(groupCode) else current.add(groupCode)
        _selectedGroups.value = current
    }

    fun setTemporaryClassForDetails(classEntity: ClassEntity) {
        classRepository.setTemporaryClass(classEntity)
    }

    fun selectMyPlan() { _currentSource.value = ScheduleSource.MyPlan }

    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            val existing = uiState.value.favorites.find { it.name == name }
            if (existing != null) favoritesRepository.deleteFavorite(existing)
            else favoritesRepository.insertFavorite(FavoriteEntity(resourceId = name, name = name, type = type))
        }
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _currentSource.value = ScheduleSource.Favorite(favorite.resourceId, favorite.name, favorite.type)
        loadNetworkSchedule(favorite.name, favorite.type)
    }

    fun selectPreviewPlan(name: String, type: String) {
        _currentSource.value = ScheduleSource.Preview(name, name, type)
        loadNetworkSchedule(name, type)
    }

    private fun loadNetworkSchedule(name: String, type: String) {
        viewModelScope.launch {
            val result = if (type == "teacher") universityRepository.getScheduleForTeacher(name)
            else universityRepository.getSchedule(name, emptyList())
            if (result is NetworkResult.Success) _previewState.value = result.data ?: emptyList()
        }
    }

    // DODANE: Metody do zarządzania stanem kalendarza
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setMonthView(isMonth: Boolean) {
        _isMonthView.value = isMonth
    }
}