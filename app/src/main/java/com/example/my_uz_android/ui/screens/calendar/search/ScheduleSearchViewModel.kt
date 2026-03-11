package com.example.my_uz_android.ui.screens.calendar.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.TeacherDetailsDto
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
    val isFavorite: Boolean = false,
    val email: String? = null,
    val institute: String? = null
)

class ScheduleSearchViewModel(
    private val universityRepository: UniversityRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allGroups: List<String> = emptyList()
    private var allTeachers: List<TeacherDetailsDto> = emptyList()

    init {
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val groupsDeferred = async { universityRepository.getGroupCodes() }
            val teachersDeferred = async { universityRepository.getAllTeachersWithDetails() }

            val groupsRes = groupsDeferred.await()
            val teachersRes = teachersDeferred.await()

            if (groupsRes is NetworkResult.Success) allGroups = groupsRes.data ?: emptyList()
            if (teachersRes is NetworkResult.Success) allTeachers = teachersRes.data ?: emptyList()

            _uiState.update { it.copy(isLoading = false) }

            if (_uiState.value.searchQuery.isNotEmpty()) {
                performSearch(_uiState.value.searchQuery)
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        performSearch(query) // Zmiana: Szukamy zawsze, nawet dla pustego stringa (choć w performSearch to obsłużymy)
    }

    private fun String.normalizeForSearch(): String {
        // Ręczna zamiana polskich znaków dla pewności, szczególnie dla ł/Ł
        val manual = this.lowercase()
            .replace("ł", "l")
            .replace("ą", "a")
            .replace("ć", "c")
            .replace("ę", "e")
            .replace("ń", "n")
            .replace("ó", "o")
            .replace("ś", "s")
            .replace("ź", "z")
            .replace("ż", "z")

        val normalized = Normalizer.normalize(manual, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex()
            .replace(normalized, "")
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            val searchWords = query.normalizeForSearch().split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (searchWords.isEmpty()) {
                _uiState.update { it.copy(searchResults = emptyList()) }
                return@launch
            }

            val favorites = favoritesRepository.favoritesStream.first()
            val results = mutableListOf<SearchResultItem>()

            // Szukanie w grupach od pierwszego znaku
            val matchedGroups = allGroups.filter { group ->
                val normalizedGroup = group.normalizeForSearch()
                searchWords.all { word -> normalizedGroup.contains(word) }
            }.take(30)

            // Szukanie w nauczycielach
            val matchedTeachers = allTeachers.filter { teacher ->
                val normalizedTeacher = teacher.name.normalizeForSearch()
                searchWords.all { word -> normalizedTeacher.contains(word) }
            }.take(30)

            results.addAll(matchedGroups.map { name ->
                SearchResultItem(name, "group", favorites.any { it.resourceId == name })
            })

            results.addAll(matchedTeachers.map { t ->
                SearchResultItem(
                    name = t.name,
                    type = "teacher",
                    isFavorite = favorites.any { it.resourceId == t.name },
                    email = t.email,
                    institute = t.institute
                )
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