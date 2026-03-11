package com.example.my_uz_android.ui.screens.calendar.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false
)

data class SearchResultItem(
    val name: String,
    val type: String,
    val isFavorite: Boolean = false
)

class ScheduleSearchViewModel(
    private val universityRepository: UniversityRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Lokalne "słowniki" do szybkiego przeszukiwania offline
    private var allGroups: List<String> = emptyList()
    private var allTeachers: List<String> = emptyList()

    init {
        // Nasłuchiwanie ulubionych
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

        // Pobranie wszystkich danych przy uruchomieniu ekranu
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val groupsDeferred = async { universityRepository.getGroupCodes() }
            val teachersDeferred = async { universityRepository.getAllTeachers() }

            val groupsRes = groupsDeferred.await()
            val teachersRes = teachersDeferred.await()

            if (groupsRes is NetworkResult.Success) allGroups = groupsRes.data ?: emptyList()
            if (teachersRes is NetworkResult.Success) allTeachers = teachersRes.data ?: emptyList()

            _uiState.update { it.copy(isLoading = false) }

            // Jeśli użytkownik wpisał coś zanim dane się załadowały, ponawiamy wyszukiwanie
            if (_uiState.value.searchQuery.isNotEmpty()) {
                performSearch(_uiState.value.searchQuery)
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    // Funkcja czyszcząca tekst: usuwa polskie znaki, białe znaki i zamienia na małe litery
    private fun String.normalizeForSearch(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex()
            .replace(normalized, "")
            .lowercase()
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            // Rozbijamy wpisane zapytanie na osobne słowa
            val searchWords = query.normalizeForSearch().split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (searchWords.isEmpty()) {
                _uiState.update { it.copy(searchResults = emptyList()) }
                return@launch
            }

            val favorites = favoritesRepository.favoritesStream.first()
            val results = mutableListOf<SearchResultItem>()

            // Szukamy w grupach: każde słowo z zapytania musi znajdować się w nazwie grupy
            val matchedGroups = allGroups.filter { group ->
                val normalizedGroup = group.normalizeForSearch()
                searchWords.all { word -> normalizedGroup.contains(word) }
            }.take(30) // Ograniczamy do 30 wyników, by nie blokować interfejsu przy np. wpisaniu litery "a"

            // Szukamy w nauczycielach: każde słowo z zapytania musi znajdować się w godności nauczyciela
            val matchedTeachers = allTeachers.filter { teacher ->
                val normalizedTeacher = teacher.normalizeForSearch()
                searchWords.all { word -> normalizedTeacher.contains(word) }
            }.take(30)

            results.addAll(matchedGroups.map { name ->
                SearchResultItem(name, "group", favorites.any { it.resourceId == name })
            })
            results.addAll(matchedTeachers.map { name ->
                SearchResultItem(name, "teacher", favorites.any { it.resourceId == name })
            })

            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun toggleFavorite(result: SearchResultItem) {
        viewModelScope.launch {
            if (result.isFavorite) {
                favoritesRepository.deleteFavoriteByResourceId(result.name)
            } else {
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