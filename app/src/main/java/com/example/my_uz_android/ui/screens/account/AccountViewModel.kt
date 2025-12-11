package com.example.my_uz_android.ui.screens.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow<SettingsEntity?>(null)
    val settings: StateFlow<SettingsEntity?> = _settings.asStateFlow()

    private val _isSettingsLoaded = MutableStateFlow(false)
    val isSettingsLoaded: StateFlow<Boolean> = _isSettingsLoaded.asStateFlow()

    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _filteredGroups = MutableStateFlow<List<String>>(emptyList())
    val filteredGroups: StateFlow<List<String>> = _filteredGroups.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    private val _draftSubgroups = MutableStateFlow<List<String>>(emptyList())
    val draftSubgroups: StateFlow<List<String>> = _draftSubgroups.asStateFlow()

    private val _draftSelectedGroup = MutableStateFlow<String?>(null)
    val draftSelectedGroup: StateFlow<String?> = _draftSelectedGroup.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private var allGroupCodes: List<String> = emptyList()

    init {
        loadSettings()
        loadGroupCodes()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettingsStream().collect { retrievedSettings ->
                _settings.value = retrievedSettings

                if (retrievedSettings != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedGroupCode = retrievedSettings.selectedGroupCode ?: "",
                            selectedSubgroup = retrievedSettings.selectedSubgroup ?: "",
                            isDarkMode = retrievedSettings.isDarkMode,
                            currentSemester = retrievedSettings.currentSemester
                        )
                    }
                    _draftSelectedGroup.value = retrievedSettings.selectedGroupCode
                    _draftSubgroups.value = retrievedSettings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                }
                _isSettingsLoaded.value = true
            }
        }
    }

    private fun loadGroupCodes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                when (val result = universityRepository.getGroupCodes()) {
                    is NetworkResult.Success -> {
                        allGroupCodes = result.data ?: emptyList()
                    }
                    is NetworkResult.Error -> {
                        Log.e("AccountViewModel", "Błąd pobierania grup: ${result.message}")
                        allGroupCodes = emptyList()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error loading group codes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _groupSearchQuery.value = query
        if (query.isBlank()) {
            _filteredGroups.value = emptyList()
        } else {
            _filteredGroups.value = allGroupCodes
                .filter { it.contains(query, ignoreCase = true) }
                .take(10)
        }
    }

    fun selectGroup(groupCode: String) {
        _draftSelectedGroup.value = groupCode
        _groupSearchQuery.value = groupCode
        _filteredGroups.value = emptyList()
        loadSubgroupsForGroup(groupCode)
    }

    private fun loadSubgroupsForGroup(groupCode: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                when (val result = universityRepository.getSubgroups(groupCode)) {
                    is NetworkResult.Success -> {
                        _availableSubgroups.value = result.data ?: emptyList()
                    }
                    is NetworkResult.Error -> {
                        _availableSubgroups.value = emptyList()
                        Log.e("AccountViewModel", "Błąd podgrup: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _availableSubgroups.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSubgroup(subgroup: String) {
        val current = _draftSubgroups.value.toMutableList()
        if (current.contains(subgroup)) {
            current.remove(subgroup)
        } else {
            current.add(subgroup)
        }
        _draftSubgroups.value = current
    }

    fun updateSemester(newSemester: Int) {
        viewModelScope.launch {
            val current = _settings.value ?: return@launch
            val updated = current.copy(currentSemester = newSemester)
            settingsRepository.insertSettings(updated)
        }
    }

    fun saveChanges() {
        val groupCode = _draftSelectedGroup.value ?: return
        val subgroups = _draftSubgroups.value

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Pobierz Plan
                refreshSchedule(groupCode, subgroups)

                // 2. Pobierz Szczegóły Grupy (Wydział, Kierunek) - ✅ DODANE
                var faculty: String? = null
                var fieldOfStudy: String? = null
                var studyMode: String? = null

                when (val detailsResult = universityRepository.getGroupDetails(groupCode)) {
                    is NetworkResult.Success -> {
                        detailsResult.data?.let { details ->
                            studyMode = details.studyMode
                            details.fieldInfo?.let { info ->
                                faculty = info.faculty
                                fieldOfStudy = info.name
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e("AccountViewModel", "Błąd pobierania szczegółów: ${detailsResult.message}")
                    }
                    else -> {}
                }

                // 3. Zapisz wszystko w ustawieniach
                val currentSettings = _settings.value ?: SettingsEntity()
                val newSettings = currentSettings.copy(
                    selectedGroupCode = groupCode,
                    selectedSubgroup = subgroups.joinToString(","),
                    isDarkMode = _uiState.value.isDarkMode,
                    currentSemester = _settings.value?.currentSemester ?: 1,
                    // ✅ Aktualizacja danych uczelnianych
                    faculty = faculty,
                    fieldOfStudy = fieldOfStudy,
                    studyMode = studyMode
                )

                settingsRepository.insertSettings(newSettings)
                _saveMessage.value = "✅ Zapisano! Plan i dane zostały zaktualizowane."
            } catch (e: Exception) {
                _saveMessage.value = "❌ Błąd: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshSchedule(groupCode: String, subgroups: List<String>) {
        val result = universityRepository.getSchedule(groupCode, subgroups)

        when (result) {
            is NetworkResult.Success -> {
                val schedule = result.data ?: emptyList()
                classRepository.deleteAllClasses()
                classRepository.insertClasses(schedule)
            }
            is NetworkResult.Error -> {
                throw Exception(result.message ?: "Nie udało się pobrać planu")
            }
            else -> {}
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDarkMode = enabled) }
            val current = settings.value
            if (current != null) {
                settingsRepository.insertSettings(current.copy(isDarkMode = enabled))
            }
        }
    }

    data class AccountUiState(
        val selectedGroupCode: String = "",
        val selectedSubgroup: String = "",
        val isDarkMode: Boolean = false,
        val currentSemester: Int = 1
    )
}