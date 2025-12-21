package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CalendarUiState(
    val favorites: List<FavoriteEntity> = emptyList(),
    val selectedResourceId: String? = null, // null = Mój Plan
    val selectedPlanName: String = "Mój Terminarz"
)

class CalendarViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getAllFavoritesStream().collectLatest { list ->
                _uiState.value = _uiState.value.copy(favorites = list)
            }
        }
    }

    fun selectMyPlan() {
        _uiState.value = _uiState.value.copy(
            selectedResourceId = null,
            selectedPlanName = "Mój Terminarz"
        )
    }

    fun selectFavoritePlan(favorite: FavoriteEntity) {
        _uiState.value = _uiState.value.copy(
            selectedResourceId = favorite.resourceId,
            selectedPlanName = favorite.name
        )
    }
}