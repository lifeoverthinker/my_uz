package com.example.my_uz_android.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import com.example.my_uz_android.widget.WidgetWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ScheduleSource {
    data object MyPlan : ScheduleSource
    data class Favorite(val resourceId: String, val name: String, val type: String) : ScheduleSource
    data class Preview(val resourceId: String, val name: String, val type: String) : ScheduleSource
}

data class CalendarUiState(
    val favorites: List<FavoriteEntity> = emptyList(),
    val networkClasses: List<ClassEntity> = emptyList(),
    val selectedResourceId: String? = null,
    val selectedPlanName: String = "Mój Plan",
    val classColorMap: Map<String, Int> = emptyMap(),
    val currentSource: ScheduleSource = ScheduleSource.MyPlan,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CalendarViewModel(
    private val application: Application,
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository
) : AndroidViewModel(application) {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Strumień TYLKO dla mojego planu (z bazy danych)
    val myPlanClasses: StateFlow<List<ClassEntity>> = classRepository.getAllClassesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Strumień dla podglądu (z sieci)
    val networkClasses: StateFlow<List<ClassEntity>> = _uiState
        .map { it.networkClasses }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
        observeSettingsAndRefresh()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                favoritesRepository.getAllFavoritesStream(),
                settingsRepository.getSettingsStream()
            ) { favs, settings ->
                val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
                val classColorMap: Map<String, Int> = try {
                    gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
                } catch (e: Exception) { emptyMap() }

                _uiState.update { it.copy(
                    favorites = favs,
                    classColorMap = classColorMap
                ) }
            }.collect()
        }
    }

    private fun observeSettingsAndRefresh() {
        viewModelScope.launch {
            settingsRepository.getSettingsStream().collect { settings ->
                val groupCode = settings?.selectedGroupCode
                val subgroups = settings?.selectedSubgroup?.split(",")?.map { it.trim() } ?: emptyList()

                if (!groupCode.isNullOrBlank()) {
                    refreshScheduleFromServer(groupCode, subgroups)
                }
            }
        }
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

    fun selectMyPlan() {
        _uiState.update { it.copy(
            currentSource = ScheduleSource.MyPlan,
            selectedResourceId = null,
            selectedPlanName = "Mój Plan",
            networkClasses = emptyList()
        ) }
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _uiState.update { it.copy(
            currentSource = ScheduleSource.Favorite(favorite.resourceId, favorite.name, favorite.type),
            selectedResourceId = favorite.resourceId,
            selectedPlanName = favorite.name
        ) }
        loadNetworkSchedule(favorite.name, favorite.type)
    }

    fun selectPreviewPlan(name: String, type: String) {
        _uiState.update { it.copy(
            currentSource = ScheduleSource.Preview(name, name, type),
            selectedResourceId = name,
            selectedPlanName = name
        ) }
        loadNetworkSchedule(name, type)
    }

    private fun loadNetworkSchedule(name: String, type: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (type.equals("teacher", ignoreCase = true)) {
                universityRepository.getScheduleForTeacher(name)
            } else {
                universityRepository.getSchedule(name, emptyList())
            }
            when (result) {
                is NetworkResult.Success -> _uiState.update { it.copy(networkClasses = result.data ?: emptyList(), isLoading = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }

    fun setTemporaryClassForDetails(classEntity: ClassEntity) {
        classRepository.setTemporaryClass(classEntity)
    }

    // POPRAWKA: Używamy metod .delete() i .insert() zamiast .deleteFavorite() i .insertFavorite()
    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favorites
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
}