package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.ScheduleType
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Źródło danych: Mój Plan, Ulubione, Udostępnione
sealed class ScheduleSource {
    data object MyPlan : ScheduleSource()
    data class Favorite(val entity: FavoriteEntity) : ScheduleSource()
    data object Shared : ScheduleSource()
}

data class CalendarUiState(
    val selectedSource: ScheduleSource = ScheduleSource.MyPlan,
    val favorites: List<FavoriteEntity> = emptyList(),
    val drawerOpen: Boolean = false
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
            favoritesRepository.getAllFavorites().collectLatest { list ->
                _uiState.value = _uiState.value.copy(favorites = list)
            }
        }
    }

    fun selectSchedule(source: ScheduleSource) {
        _uiState.value = _uiState.value.copy(
            selectedSource = source
        )
        // TODO: Tutaj w przyszłości dodamy logikę przeładowania danych w kalendarzu
    }

    fun getCurrentTitle(): String {
        return when (val source = _uiState.value.selectedSource) {
            is ScheduleSource.MyPlan -> "Mój Plan"
            is ScheduleSource.Favorite -> source.entity.name
            is ScheduleSource.Shared -> "Udostępniony"
        }
    }
}