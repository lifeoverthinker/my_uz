package com.example.my_uz_android.ui.screens.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
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

    // ✅ Flaga: true oznacza, że próba odczytu się zakończyła (niezależnie czy znaleziono dane)
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
                            // ✅ Fix: Bezpieczne rozpakowanie nulla
                            selectedGroupCode = retrievedSettings.selectedGroupCode ?: "",
                            selectedSubgroup = retrievedSettings.selectedSubgroup ?: "",
                            isDarkMode = retrievedSettings.isDarkMode
                        )
                    }
                    _draftSelectedGroup.value = retrievedSettings.selectedGroupCode
                    _draftSubgroups.value = retrievedSettings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                }

                // ✅ Ważne: Odznaczamy flagę ładowania nawet jak settings == null (nowy user)
                _isSettingsLoaded.value = true
            }
        }
    }

    private fun loadGroupCodes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                allGroupCodes = universityRepository.getGroupCodes()
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
                val subgroups = universityRepository.getSubgroups(groupCode)
                _availableSubgroups.value = subgroups
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

    fun saveChanges() {
        val groupCode = _draftSelectedGroup.value ?: return
        val subgroups = _draftSubgroups.value

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = _settings.value ?: SettingsEntity()

                val newSettings = currentSettings.copy(
                    selectedGroupCode = groupCode,
                    selectedSubgroup = subgroups.joinToString(","),
                    isDarkMode = _uiState.value.isDarkMode
                )

                settingsRepository.insertSettings(newSettings)
                refreshSchedule(groupCode, subgroups)
                _saveMessage.value = "✅ Zapisano! Plan został zaktualizowany."
            } catch (e: Exception) {
                _saveMessage.value = "❌ Błąd zapisu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshSchedule(groupCode: String, subgroups: List<String>) {
        val schedule = universityRepository.getSchedule(groupCode, subgroups)
        classRepository.deleteAllClasses()
        classRepository.insertClasses(schedule)
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
        val isDarkMode: Boolean = false
    )
}