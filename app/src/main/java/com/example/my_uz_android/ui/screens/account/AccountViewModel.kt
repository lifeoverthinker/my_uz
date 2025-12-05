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

    // UI State
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    // Flows dla AccountScreen
    val settings: StateFlow<SettingsEntity?> = settingsRepository.getSettingsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
        Log.d("AccountViewModel", "🟢 ViewModel CREATED")

        // Obserwuj ustawienia z bazy
        viewModelScope.launch {
            settingsRepository.getSettingsStream().collect { settings ->
                if (settings != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            // POPRAWKA: Użycie selectedGroupCode i selectedSubgroup
                            selectedGroupCode = settings.selectedGroupCode ?: "",
                            selectedSubgroup = settings.selectedSubgroup ?: "",
                            isDarkMode = settings.isDarkMode
                        )
                    }
                    _draftSelectedGroup.value = settings.selectedGroupCode
                    _draftSubgroups.value = settings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                }
            }
        }

        // Pobierz listę grup
        loadGroupCodes()
    }

    private fun loadGroupCodes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                allGroupCodes = universityRepository.getGroupCodes()
                Log.d("AccountViewModel", "Loaded ${allGroupCodes.size} group codes")
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
        Log.d("AccountViewModel", "🔵 Select group: $groupCode")
        _draftSelectedGroup.value = groupCode
        _groupSearchQuery.value = groupCode
        _filteredGroups.value = emptyList()

        // Pobierz podgrupy dla wybranej grupy
        loadSubgroupsForGroup(groupCode)
    }

    private fun loadSubgroupsForGroup(groupCode: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val subgroups = universityRepository.getSubgroups(groupCode)
                _availableSubgroups.value = subgroups
                Log.d("AccountViewModel", "Loaded subgroups for $groupCode: $subgroups")
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error loading subgroups", e)
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
        Log.d("AccountViewModel", "Toggled subgroup $subgroup, current: $current")
    }

    fun saveChanges() {
        Log.d("AccountViewModel", "🔴 SAVE CHANGES CALLED")
        val groupCode = _draftSelectedGroup.value ?: return
        val subgroups = _draftSubgroups.value

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Zapisz ustawienia
                val settings = SettingsEntity(
                    id = 1,
                    // POPRAWKA: Użycie nazw z SettingsEntity
                    selectedGroupCode = groupCode,
                    selectedSubgroup = subgroups.joinToString(","),
                    isDarkMode = _uiState.value.isDarkMode
                )
                settingsRepository.insertSettings(settings)

                // Pobierz i zapisz plan zajęć
                refreshSchedule(groupCode, subgroups)

                _saveMessage.value = "✅ Zapisano! Plan został zaktualizowany."
                Log.d("AccountViewModel", "✅ Settings saved successfully")
            } catch (e: Exception) {
                Log.e("AccountViewModel", "❌ Error saving settings", e)
                _saveMessage.value = "❌ Błąd zapisu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshSchedule(groupCode: String, subgroups: List<String>) {
        try {
            val schedule = universityRepository.getSchedule(groupCode, subgroups)
            classRepository.deleteAllClasses()
            classRepository.insertClasses(schedule)
            Log.d("AccountViewModel", "✅ Schedule refreshed: ${schedule.size} classes")
        } catch (e: Exception) {
            Log.e("AccountViewModel", "❌ Error refreshing schedule", e)
            throw e
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDarkMode = enabled) }

            // Zapisz do bazy
            val current = settings.value
            if (current != null) {
                settingsRepository.insertSettings(
                    current.copy(isDarkMode = enabled)
                )
            }
        }
    }

    data class AccountUiState(
        val selectedGroupCode: String = "",
        val selectedSubgroup: String = "",
        val isDarkMode: Boolean = false
    )
}