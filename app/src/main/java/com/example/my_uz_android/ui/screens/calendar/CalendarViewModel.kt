package com.example.my_uz_android.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import com.example.my_uz_android.widget.WidgetWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScheduleSource {
    data object MyPlan : ScheduleSource
    data class Favorite(val resourceId: String, val name: String, val type: String) : ScheduleSource
    data class Preview(val resourceId: String, val name: String, val type: String) : ScheduleSource
}

data class CalendarUiState(
    val favorites: List<FavoriteEntity> = emptyList(),
    val visibleClasses: List<ClassEntity> = emptyList(),
    val selectedResourceId: String? = null,
    val selectedPlanName: String = "M�j Plan",
    val classColorMap: Map<String, Int> = emptyMap(),
    val currentSource: ScheduleSource = ScheduleSource.MyPlan,
    val availableDirections: List<String> = emptyList(),
    val activeDirectionCode: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isShareLoading: Boolean = false,
    val sharedCode: String? = null,
    val temporaryClassDetails: ClassEntity? = null
)

class CalendarViewModel(
    private val application: Application,
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val tasksRepository: TasksRepository
) : AndroidViewModel(application) {

    private val gson = Gson()

    private val _userSelection = MutableStateFlow(
        CalendarUiState(isLoading = true)
    )

    val uiState: StateFlow<CalendarUiState> = combine(
        _userSelection,
        classRepository.getAllClassesStream(),
        favoritesRepository.getAllFavoritesStream(),
        settingsRepository.getSettingsStream()
    ) { selection, myClasses, favorites, settings ->

        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val classColorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }

        val availableDirections = myClasses
            .map { it.groupCode.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val fallbackDirection = settings?.selectedGroupCode
            ?.takeIf { it.isNotBlank() && availableDirections.contains(it) }

        val resolvedActiveDirection = settings?.activeDirectionCode
            ?.takeIf { it.isNotBlank() && availableDirections.contains(it) }
            ?: fallbackDirection
            ?: availableDirections.firstOrNull()

        val classesToShow = if (selection.currentSource is ScheduleSource.MyPlan) {
            myClasses
        } else {
            selection.visibleClasses
        }

        selection.copy(
            favorites = favorites,
            classColorMap = classColorMap,
            visibleClasses = classesToShow,
            availableDirections = if (selection.currentSource is ScheduleSource.MyPlan) availableDirections else emptyList(),
            activeDirectionCode = if (selection.currentSource is ScheduleSource.MyPlan) resolvedActiveDirection else null,
            isLoading = if (selection.currentSource is ScheduleSource.MyPlan) false else selection.isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(isLoading = true)
    )

    val tasks: Flow<List<TaskEntity>> = tasksRepository.getAllTasks()

    init {
        observeSettingsAndRefresh()
    }

    private fun observeSettingsAndRefresh() {
        viewModelScope.launch {
            settingsRepository.getSettingsStream()
                .map { settings ->
                    val groupCode = settings?.selectedGroupCode?.trim().orEmpty()
                    val subgroups = parseSubgroups(settings?.selectedSubgroup)
                    if (groupCode.isBlank()) {
                        null
                    } else {
                        groupCode to subgroups
                    }
                }
                .distinctUntilChanged()
                .collect { config ->
                    if (config != null) {
                        refreshScheduleFromServer(config.first, config.second)
                    }
                }
        }
    }

    private fun parseSubgroups(rawSubgroups: String?): List<String> {
        return rawSubgroups
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    fun refreshScheduleFromServer(groupCode: String, subgroups: List<String>) {
        viewModelScope.launch {
            val result = universityRepository.getSchedule(groupCode, subgroups)
            if (result is NetworkResult.Success) {
                classRepository.deleteAllClasses()
                classRepository.insertClasses(result.data ?: emptyList())
                val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
                WorkManager.getInstance(application).enqueue(request)
            }
        }
    }

    fun setActiveDirection(directionCode: String) {
        val normalizedDirection = directionCode.trim()
        if (normalizedDirection.isEmpty()) return

        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsStream().firstOrNull() ?: SettingsEntity(id = 1)
            if (currentSettings.activeDirectionCode == normalizedDirection) return@launch

            settingsRepository.insertOrUpdate(
                currentSettings.copy(activeDirectionCode = normalizedDirection)
            )
        }
    }

    fun setTemporaryClassForDetails(classEntity: ClassEntity) {
        _userSelection.update { it.copy(temporaryClassDetails = classEntity) }
    }

    fun selectMyPlan() {
        _userSelection.update {
            it.copy(
                currentSource = ScheduleSource.MyPlan,
                selectedResourceId = null,
                selectedPlanName = "M�j Plan",
                error = null
            )
        }
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _userSelection.update {
            it.copy(
                currentSource = ScheduleSource.Favorite(favorite.resourceId, favorite.name, favorite.type),
                selectedResourceId = favorite.resourceId,
                selectedPlanName = favorite.name,
                isLoading = true,
                error = null
            )
        }
        loadNetworkSchedule(favorite.name, favorite.type)
    }

    fun selectPreviewPlan(name: String, type: String) {
        _userSelection.update {
            it.copy(
                currentSource = ScheduleSource.Preview(name, name, type),
                selectedResourceId = name,
                selectedPlanName = name,
                isLoading = true,
                error = null
            )
        }
        loadNetworkSchedule(name, type)
    }

    private fun loadNetworkSchedule(name: String, type: String) {
        viewModelScope.launch {
            val result = if (type.equals("teacher", ignoreCase = true)) {
                universityRepository.getScheduleForTeacher(name)
            } else {
                universityRepository.getSchedule(name, emptyList())
            }

            _userSelection.update { current ->
                when (result) {
                    is NetworkResult.Success -> current.copy(
                        visibleClasses = result.data ?: emptyList(),
                        isLoading = false
                    )

                    is NetworkResult.Error -> current.copy(
                        error = result.message,
                        isLoading = false,
                        visibleClasses = emptyList()
                    )

                    else -> current.copy(isLoading = false)
                }
            }
        }
    }

    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            val currentFavorites = uiState.value.favorites
            val existing = currentFavorites.find { it.name == name }

            if (existing != null) {
                favoritesRepository.delete(existing)
            } else {
                val newFav = FavoriteEntity(
                    resourceId = name,
                    name = name,
                    type = type
                )
                favoritesRepository.insert(newFav)
            }
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task)
        }
    }

    fun shareCalendarTasks() {
        viewModelScope.launch {
            _userSelection.update { it.copy(isShareLoading = true, error = null) }
            try {
                val tasksList = tasksRepository.getAllTasks().firstOrNull().orEmpty()
                if (tasksList.isNotEmpty()) {
                    val code = tasksRepository.shareTasks(tasksList)
                    _userSelection.update { it.copy(isShareLoading = false, sharedCode = code) }
                } else {
                    _userSelection.update { it.copy(isShareLoading = false, error = "Brak zada�") }
                }
            } catch (e: Exception) {
                _userSelection.update { it.copy(isShareLoading = false, error = e.message) }
            }
        }
    }

    fun clearSharedCode() {
        _userSelection.update { it.copy(sharedCode = null) }
    }
}


