package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val gson = Gson()

    val classes: StateFlow<List<ClassEntity>> = classRepository.getAllClassesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                favoritesRepository.getAllFavoritesStream(),
                settingsRepository.getSettingsStream()
            ) { favorites, settings ->
                // Parsowanie kolorów z JSONa zapisanego w ustawieniach
                val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
                val classColorMap: Map<String, Int> = try {
                    gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                _uiState.value.copy(
                    favorites = favorites,
                    classColorMap = classColorMap // Aktualizacja mapy w stanie
                )
            }.collectLatest { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectMyPlan() {
        _uiState.value = _uiState.value.copy(
            selectedResourceId = null,
            selectedPlanName = "Mój Plan"
        )
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _uiState.value = _uiState.value.copy(
            selectedResourceId = favorite.resourceId,
            selectedPlanName = favorite.name
        )
    }
}

data class CalendarUiState(
    val favorites: List<FavoriteEntity> = emptyList(),
    val selectedResourceId: String? = null,
    val selectedPlanName: String = "Mój Plan",
    val classColorMap: Map<String, Int> = emptyMap()
)