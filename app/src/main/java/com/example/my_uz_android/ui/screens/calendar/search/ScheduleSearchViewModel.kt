package com.example.my_uz_android.ui.screens.calendar.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class SearchResultItem(
    val name: String,
    val type: String, // "group" lub "teacher"
    val isFavorite: Boolean = false
)

data class ScheduleSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScheduleSearchViewModel(
    private val universityRepository: UniversityRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleSearchUiState())
    val uiState: StateFlow<ScheduleSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /**
     * Wywoływane przy każdej zmianie tekstu w wyszukiwarce
     */
    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }

        searchJob?.cancel()
        if (newQuery.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300) // Czekamy chwilę, aby nie wysyłać zapytań przy każdym kliknięciu klawisza
                performSearch(newQuery)
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }

    /**
     * Główna logika wyszukiwania korzystająca z ILIKE w bazie danych
     */
    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true) }

        // Wykonujemy zapytania do bazy danych (Supabase)
        val groupsResult = universityRepository.searchGroups(query)
        val teachersResult = universityRepository.searchTeachers(query)

        // Pobieramy aktualne ulubione z bazy lokalnej (Room)
        val favorites = favoritesRepository.getAllFavoritesStream().firstOrNull() ?: emptyList()

        // Mapujemy wyniki grup
        val groupItems = if (groupsResult is NetworkResult.Success) {
            groupsResult.data?.map { name ->
                SearchResultItem(
                    name = name,
                    type = "group",
                    isFavorite = favorites.any { it.name == name && it.type == "group" }
                )
            } ?: emptyList()
        } else emptyList()

        // Mapujemy wyniki nauczycieli
        val teacherItems = if (teachersResult is NetworkResult.Success) {
            teachersResult.data?.map { name ->
                SearchResultItem(
                    name = name,
                    type = "teacher",
                    isFavorite = favorites.any { it.name == name && it.type == "teacher" }
                )
            } ?: emptyList()
        } else emptyList()

        // Łączymy wyniki i sortujemy alfabetycznie
        val combinedResults = (groupItems + teacherItems).sortedBy { it.name }

        _uiState.update { it.copy(
            searchResults = combinedResults,
            isLoading = false
        ) }
    }

    /**
     * Dodawanie/Usuwanie z ulubionych
     */
    fun toggleFavorite(item: SearchResultItem) {
        viewModelScope.launch {
            val favorites = favoritesRepository.getAllFavoritesStream().firstOrNull() ?: emptyList()
            val existing = favorites.find { it.name == item.name && it.type == item.type }

            if (existing != null) {
                favoritesRepository.delete(existing)
            } else {
                val favorite = FavoriteEntity(
                    name = item.name,
                    type = item.type,
                    resourceId = item.name // resourceId używane do pobierania planu
                )
                favoritesRepository.insert(favorite)
            }

            // Odświeżamy listę wyników, aby zaktualizować stan ikonek serca
            val currentQuery = _uiState.value.searchQuery
            if (currentQuery.length >= 2) {
                performSearch(currentQuery)
            }
        }
    }

    /**
     * Pomocnicza funkcja do normalizacji tekstu (opcjonalnie używana lokalnie)
     */
    private fun normalizeText(input: String): String {
        val original = charArrayOf('ą', 'ć', 'ę', 'ł', 'ń', 'ó', 'ś', 'ź', 'ż', 'Ą', 'Ć', 'Ę', 'Ł', 'Ń', 'Ó', 'Ś', 'Ź', 'Ż')
        val normalized = charArrayOf('a', 'c', 'e', 'l', 'n', 'o', 's', 'z', 'z', 'a', 'c', 'e', 'l', 'n', 'o', 's', 'z', 'z')
        var result = input
        for (i in original.indices) {
            result = result.replace(original[i], normalized[i])
        }
        return result.lowercase(Locale.getDefault())
    }
}