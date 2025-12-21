package com.example.my_uz_android.ui.screens.calendar.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SearchResultItem(
    val name: String,
    val type: String,
    val isFavorite: Boolean = false
)

class ScheduleSearchViewModel(
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleSearchUiState())
    val uiState: StateFlow<ScheduleSearchUiState> = _uiState.asStateFlow()

    private var allGroups: List<String> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val groupsResult = universityRepository.getGroupCodes()
            if (groupsResult is NetworkResult.Success) {
                allGroups = groupsResult.data ?: emptyList()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        if (newQuery.length >= 2) {
            search(newQuery)
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            // Pobierz aktualne ulubione, żeby oznaczyć wyniki
            val favorites = favoritesRepository.getAllFavoritesStream().first().map { it.name }

            val filtered = allGroups.filter { it.contains(query, ignoreCase = true) }
                .map { name ->
                    SearchResultItem(
                        name = name,
                        type = "Group",
                        isFavorite = favorites.contains(name)
                    )
                }

            _uiState.update { it.copy(searchResults = filtered) }
        }
    }

    fun toggleFavorite(item: SearchResultItem) {
        viewModelScope.launch {
            if (item.isFavorite) {
                // Usuwanie
                val favorites = favoritesRepository.getAllFavoritesStream().first()
                val toDelete = favorites.find { it.name == item.name }
                if (toDelete != null) {
                    favoritesRepository.delete(toDelete) // ✅ FIX: Poprawna nazwa metody
                }
            } else {
                // Dodawanie
                val favorite = FavoriteEntity(
                    name = item.name,
                    type = item.type,
                    resourceId = item.name
                )
                favoritesRepository.insert(favorite) // ✅ FIX: Poprawna nazwa metody
            }
            // Odśwież wyniki
            search(_uiState.value.searchQuery)
        }
    }
}