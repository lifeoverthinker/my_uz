package com.example.my_uz_android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val availableGroups: List<String> = emptyList(),
    val availableSubgroups: List<String> = emptyList()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Pobierz listę grup
            val groups = universityRepository.getGroupCodes()
            _uiState.value = _uiState.value.copy(availableGroups = groups)

            // 2. Obserwuj ustawienia
            settingsRepository.getSettingsStream().collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)

                // ✅ NAPRAWA: Bezpieczne pobieranie wartości (obsługa nulla)
                val groupCode = settings?.selectedGroupCode ?: ""
                if (groupCode.isNotBlank()) {
                    val subgroups = universityRepository.getSubgroups(groupCode)
                    _uiState.value = _uiState.value.copy(availableSubgroups = subgroups)
                }
            }
        }
    }

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun onGroupSelected(groupCode: String) {
        val current = _uiState.value.settings ?: return
        updateSettings(current.copy(selectedGroupCode = groupCode, selectedSubgroup = ""))
    }

    fun onSubgroupSelected(subgroup: String) {
        val current = _uiState.value.settings ?: return
        updateSettings(current.copy(selectedSubgroup = subgroup))
    }

    // ✅ NAPRAWA DARK MODE:
    // Upewniamy się, że jeśli settings jest null, tworzymy nowy z ID=1.
    // Dzięki temu Room nadpisze istniejący wiersz, zamiast tworzyć nowy.
    fun toggleDarkMode() {
        val current = _uiState.value.settings ?: SettingsEntity(id = 1)
        updateSettings(current.copy(isDarkMode = !current.isDarkMode))
    }
}
