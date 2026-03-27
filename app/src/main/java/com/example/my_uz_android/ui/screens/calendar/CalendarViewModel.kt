package com.example.my_uz_android.ui.screens.calendar

import android.app.Application
import android.util.Log
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
    val tasks: List<TaskEntity> = emptyList(),
    val selectedPlanName: String = "Mój Plan",
    val selectedResourceId: String? = null,
    val classColorMap: Map<String, Int> = emptyMap(),
    val currentSource: ScheduleSource = ScheduleSource.MyPlan,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("Europe/Warsaw")),
    val isMonthView: Boolean = false,
    val temporaryClassForDetails: ClassEntity? = null
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
    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.of("Europe/Warsaw")))
    private val _isMonthView = MutableStateFlow(false)
    private val _isLoadingNetwork = MutableStateFlow(false)
    private val _temporaryClassForDetails = MutableStateFlow<ClassEntity?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        userCourseRepository.getAllUserCoursesStream()
            .onEach { Log.d("CalendarVM", "userCourses stream: emituje ${it.size} elementów") },
        classRepository.getAllClassesStream()
            .onEach { Log.d("CalendarVM", "myClasses stream: emituje ${it.size} elementów") },
        favoritesRepository.favoritesStream.onEach {
            Log.d(
                "CalendarVM",
                "favorites stream: emituje ${it.size} elementów"
            )
        },
        settingsRepository.getSettingsStream().onEach {
            Log.d(
                "CalendarVM",
                "settings stream: wyemitowano wartość (null=${it == null})"
            )
        },
        _selectedGroups.onEach {
            Log.d(
                "CalendarVM",
                "selectedGroups stream: emituje ${it.size} elementów"
            )
        },
        _currentSource.onEach { Log.d("CalendarVM", "currentSource stream: $it") },
        _previewState.onEach {
            Log.d(
                "CalendarVM",
                "previewState stream: emituje ${it.size} elementów"
            )
        },
        _selectedDate.onEach { Log.d("CalendarVM", "selectedDate stream: $it") },
        _isMonthView.onEach { Log.d("CalendarVM", "isMonthView stream: $it") },
        _isLoadingNetwork.onEach { Log.d("CalendarVM", "isLoadingNetwork stream: $it") },
        tasksRepository.getAllTasks()
            .onEach { Log.d("CalendarVM", "tasks stream: emituje ${it.size} elementów") },
        _temporaryClassForDetails.onEach {
            Log.d(
                "CalendarVM",
                "temporaryClass stream: (null=${it == null})"
            )
        }
    ) { args: Array<Any?> ->
        try {
            val courses = args[0] as List<UserCourseEntity>
            val myClasses = args[1] as List<ClassEntity>
            val favorites = args[2] as List<FavoriteEntity>
            val settings = args[3] as SettingsEntity?
            val selectedCodes = args[4] as Set<String>
            val source = args[5] as ScheduleSource
            val previewClasses = args[6] as List<ClassEntity>
            val selectedDate = args[7] as LocalDate
            val isMonthView = args[8] as Boolean
            val isLoadingNet = args[9] as Boolean
            val tasks = args[10] as List<TaskEntity>
            val tempClass = args[11] as ClassEntity?

            Log.d("CalendarVM", "Combine odpalił! Budowanie stanu UI...")

            val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
            val colorMap: Map<String, Int> = try {
                gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
            } catch (e: Exception) {
                Log.e("CalendarVM", "Błąd parsowania kolorów JSON", e)
                emptyMap()
            }

            // BUDOWANIE KURSÓW
            val allCoursesForUi = mutableListOf<UserCourseEntity>()
            settings?.selectedGroupCode?.let { mainCode ->
                allCoursesForUi.add(
                    UserCourseEntity(
                        id = -1,
                        groupCode = mainCode,
                        fieldOfStudy = settings.fieldOfStudy ?: mainCode,
                        semester = settings.currentSemester
                    )
                )
            }
            allCoursesForUi.addAll(courses)

            // BUDOWANIE AKTYWNYCH KODÓW
            val activeCodes = if (selectedCodes.isEmpty()) {
                allCoursesForUi.map { it.groupCode }.toMutableSet()
            } else {
                selectedCodes
            }

            // Filtrowanie
            val classesToShow = when (source) {
                is ScheduleSource.MyPlan -> {
                    // Jeśli NIE mamy żadnych zdefiniowanych kodów (bo np. dane z bazy
                    // jeszcze nie doszły lub użytkownik nie wybrał żadnej grupy z ustawień)
                    // ZAWSZE wyświetl wszystkie dostępne w bazie zajęcia jako zabezpieczenie.
                    if (activeCodes.isEmpty() && settings?.selectedGroupCode == null) {
                        Log.w(
                            "CalendarVM",
                            "Brak ustawień i aktywnych kodów! Awaryjnie pokazuję wszystkie zajęcia."
                        )
                        myClasses
                    } else if (activeCodes.isEmpty()) {
                        // Jeśli są ustawienia, ale activeCodes chwilowo puste
                        myClasses
                    } else {
                        myClasses.filter { activeCodes.contains(it.groupCode) }
                    }
                }

                else -> previewClasses
            }

            val resourceId = when (source) {
                is ScheduleSource.Favorite -> source.resourceId
                is ScheduleSource.Preview -> source.resourceId
                else -> null
            }

            CalendarUiState(
                userCourses = allCoursesForUi,
                selectedGroupCodes = activeCodes,
                favorites = favorites,
                visibleClasses = classesToShow,
                tasks = tasks,
                classColorMap = colorMap,
                currentSource = source,
                selectedResourceId = resourceId,
                selectedPlanName = when (source) {
                    is ScheduleSource.MyPlan -> "Mój Plan"
                    is ScheduleSource.Favorite -> source.name
                    is ScheduleSource.Preview -> source.name
                },
                isLoading = isLoadingNet,
                selectedDate = selectedDate,
                isMonthView = isMonthView,
                temporaryClassForDetails = tempClass
            )
        } catch (e: Exception) {
            Log.e("CalendarVM", "Błąd w trakcie mapowania stanu UI w combine!", e)
            CalendarUiState(error = e.localizedMessage, isLoading = false)
        }
    }.catch { e ->
        Log.e("CalendarVM", "Krytyczny błąd! Złapano wyjątek na poziomie całego potoku Flow.", e)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CalendarUiState(isLoading = false) // Start without loading to avoid initial splash
    )

    fun toggleGroupVisibility(groupCode: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(groupCode)) current.remove(groupCode) else current.add(groupCode)
        _selectedGroups.value = current
    }

    fun selectMyPlan() {
        _currentSource.value = ScheduleSource.MyPlan
    }

    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            try {
                val existing = uiState.value.favorites.find { it.name == name }
                if (existing != null) favoritesRepository.deleteFavorite(existing)
                else favoritesRepository.insertFavorite(
                    FavoriteEntity(
                        resourceId = name,
                        name = name,
                        type = type
                    )
                )
            } catch (e: Exception) {
                Log.e("CalendarVM", "Błąd przy zmianie statusu ulubionych", e)
            }
        }
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _previewState.value = emptyList()
        _currentSource.value =
            ScheduleSource.Favorite(favorite.resourceId, favorite.name, favorite.type)
        loadNetworkSchedule(favorite.name, favorite.type)
    }

    fun selectPreviewPlan(name: String, type: String) {
        _previewState.value = emptyList()
        _currentSource.value = ScheduleSource.Preview(name, name, type)
        loadNetworkSchedule(name, type)
    }

    private fun loadNetworkSchedule(name: String, type: String) {
        viewModelScope.launch {
            _isLoadingNetwork.value = true
            try {
                val result = if (type == "teacher") {
                    universityRepository.getScheduleForTeacher(name)
                } else {
                    universityRepository.getSchedule(name, emptyList())
                }

                if (result is NetworkResult.Success) {
                    Log.d("CalendarVM", "Pobrano plan z sieci: ${result.data?.size} zajęć")
                    _previewState.value = result.data ?: emptyList()
                } else {
                    Log.e("CalendarVM", "Błąd sieci podczas pobierania planu")
                }
            } catch (e: Exception) {
                Log.e("CalendarVM", "Wyjątek podczas pobierania planu sieciowego", e)
            } finally {
                _isLoadingNetwork.value = false
            }
        }
    }

    /**
     * Odświeża plan z Supabase na żądanie.
     * Wywołuj to z CalendarScreen przy wejściu użytkownika na ekran.
     */
    fun refreshMyPlan() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsStream().firstOrNull() ?: return@launch
            val groupCode = settings.selectedGroupCode ?: return@launch

            _isLoadingNetwork.value = true
            try {
                val result = universityRepository.refreshSchedule(
                    groupCode = groupCode,
                    subgroup = settings.selectedSubgroup,  // pojedynczy String?
                    classRepository = classRepository
                )
                if (result is NetworkResult.Error) {
                    Log.e("CalendarVM", "Błąd odświeżania planu: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("CalendarVM", "Wyjątek podczas odświeżania planu", e)
            } finally {
                _isLoadingNetwork.value = false
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setMonthView(isMonth: Boolean) {
        _isMonthView.value = isMonth
    }

    fun setTemporaryClassForDetails(classEntity: ClassEntity?) {
        _temporaryClassForDetails.value = classEntity
    }
}