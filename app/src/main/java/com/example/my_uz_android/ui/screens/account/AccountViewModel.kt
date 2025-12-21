package com.example.my_uz_android.ui.screens.account

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

    // --- DANE EDYCJI ---
    private val _draftName = MutableStateFlow("")
    val draftName: StateFlow<String> = _draftName.asStateFlow()

    private val _draftSurname = MutableStateFlow("")
    val draftSurname: StateFlow<String> = _draftSurname.asStateFlow()

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
                    // Aktualizuj pola edycji tylko przy pierwszym załadowaniu
                    if (!_isSettingsLoaded.value) {
                        _draftSelectedGroup.value = retrievedSettings.selectedGroupCode
                        _groupSearchQuery.value = retrievedSettings.selectedGroupCode ?: ""

                        val fullName = retrievedSettings.userName.trim()
                        val parts = fullName.split(" ").filter { it.isNotBlank() }
                        if (parts.isNotEmpty()) {
                            _draftName.value = parts[0]
                            _draftSurname.value = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
                        } else {
                            _draftName.value = ""
                            _draftSurname.value = ""
                        }

                        _draftSubgroups.value = retrievedSettings.selectedSubgroup
                            ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

                        if (!retrievedSettings.selectedGroupCode.isNullOrBlank()) {
                            loadSubgroupsForGroup(retrievedSettings.selectedGroupCode)
                        }
                    }
                }
                _isSettingsLoaded.value = true
            }
        }
    }

    private fun loadGroupCodes() {
        viewModelScope.launch {
            val result = universityRepository.getGroupCodes()
            if (result is NetworkResult.Success) {
                allGroupCodes = result.data ?: emptyList()
            }
        }
    }

    fun updateDraftName(name: String) { _draftName.value = name }
    fun updateDraftSurname(surname: String) { _draftSurname.value = surname }

    fun onSearchQueryChange(query: String) {
        _groupSearchQuery.value = query
        // Reset wyboru jeśli zmieniono tekst
        if (query != _draftSelectedGroup.value) {
            _draftSelectedGroup.value = null
            _availableSubgroups.value = emptyList()
            _draftSubgroups.value = emptyList()
        }

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
        _draftSubgroups.value = emptyList()
    }

    private fun loadSubgroupsForGroup(groupCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = universityRepository.getSubgroups(groupCode)
            if (result is NetworkResult.Success) {
                _availableSubgroups.value = result.data ?: emptyList()
            } else {
                _availableSubgroups.value = emptyList()
            }
            _isLoading.value = false
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
        val groupCode = if(!_draftSelectedGroup.value.isNullOrBlank()) _draftSelectedGroup.value else _groupSearchQuery.value.takeIf { it.isNotBlank() }
        val subgroups = _draftSubgroups.value
        val newName = _draftName.value.trim()
        val newSurname = _draftSurname.value.trim()

        val combinedName = if (newSurname.isNotBlank()) "$newName $newSurname" else newName

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var faculty: String? = _settings.value?.faculty
                var fieldOfStudy: String? = _settings.value?.fieldOfStudy
                var studyMode: String? = _settings.value?.studyMode

                if (!groupCode.isNullOrBlank()) {
                    val detailsResult = universityRepository.getGroupDetails(groupCode)
                    if (detailsResult is NetworkResult.Success) {
                        detailsResult.data?.let {
                            studyMode = it.studyMode
                            it.fieldInfo?.let { info ->
                                faculty = info.faculty
                                fieldOfStudy = info.name
                            }
                        }
                    }

                    val planResult = universityRepository.getSchedule(groupCode, subgroups)
                    if (planResult is NetworkResult.Success && planResult.data != null) {
                        classRepository.deleteAllClasses()
                        classRepository.insertClasses(planResult.data)
                    }
                }

                val current = _settings.value ?: SettingsEntity()
                val newSettings = current.copy(
                    userName = combinedName,
                    selectedGroupCode = groupCode,
                    selectedSubgroup = subgroups.joinToString(","),
                    faculty = faculty,
                    fieldOfStudy = fieldOfStudy,
                    studyMode = studyMode,
                    isAnonymous = false
                )

                settingsRepository.insertSettings(newSettings)
                _saveMessage.value = "✅ Zapisano pomyślnie!"
            } catch (e: Exception) {
                _saveMessage.value = "❌ Błąd: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSaveMessage() { _saveMessage.value = null }
    data class AccountUiState(val dummy: Boolean = false)
}