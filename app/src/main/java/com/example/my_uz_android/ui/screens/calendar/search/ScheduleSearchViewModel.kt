package com.example.my_uz_android.ui.screens.calendar.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 1. Zdefiniowanie struktury, której dokładnie oczekuje ekran UI
data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchType: SearchType = SearchType.GROUP
)

// 2. Zmiana nazwy na SearchResultItem (tego szuka plik ScheduleSearchScreen.kt)
data class SearchResultItem(
    val name: String,
    val type: String, // Ustawione jako String, bo ekran sprawdza: item.type == "group"
    val isFavorite: Boolean = false
)

enum class SearchType { GROUP, TEACHER }

class ScheduleSearchViewModel(
    private val universityRepository: UniversityRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Nasłuchiwanie zmian w ulubionych, aby na bieżąco odświeżać ikonki serduszek
        viewModelScope.launch {
            favoritesRepository.favoritesStream.collect { favorites ->
                _uiState.update { state ->
                    state.copy(
                        searchResults = state.searchResults.map { res ->
                            res.copy(isFavorite = favorites.any { it.resourceId == res.name })
                        }
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length >= 2) performSearch(query) else _uiState.update { it.copy(searchResults = emptyList()) }
    }

    fun onTypeChange(type: SearchType) {
        _uiState.update { it.copy(searchType = type, searchResults = emptyList()) }
        if (_uiState.value.searchQuery.length >= 2) performSearch(_uiState.value.searchQuery)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val typeEnum = _uiState.value.searchType
            val typeString = if (typeEnum == SearchType.GROUP) "group" else "teacher"

            val result = if (typeEnum == SearchType.GROUP) {
                universityRepository.searchGroups(query)
            } else {
                universityRepository.searchTeachers(query)
            }

            if (result is NetworkResult.Success) {
                val favorites = favoritesRepository.favoritesStream.first()
                val results = (result.data ?: emptyList()).map { name ->
                    SearchResultItem(
                        name = name,
                        type = typeString,
                        isFavorite = favorites.any { it.resourceId == name }
                    )
                }
                _uiState.update { it.copy(searchResults = results, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, searchResults = emptyList()) }
            }
        }
    }

    fun toggleFavorite(result: SearchResultItem) {
        viewModelScope.launch {
            if (result.isFavorite) {
                // Usuwamy za pomocą dodanej w kroku 1 metody
                favoritesRepository.deleteFavoriteByResourceId(result.name)
            } else {
                // FIX: Dodaliśmy brakujące resourceId, o które krzyczał Android Studio
                favoritesRepository.insertFavorite(
                    FavoriteEntity(
                        name = result.name,
                        type = result.type,
                        resourceId = result.name
                    )
                )
            }
        }
    }
}