package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val availableGroups: List<String> = emptyList(),
    val availableSubgroups: List<String> = emptyList(),
    val uniqueClassTypes: List<String> = emptyList() // ✅ Lista typów z planu
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository // ✅ Nowa zależność
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 1. Pobierz listę grup (jednorazowo)
            val groupsResult = universityRepository.getGroupCodes()
            val groups = if (groupsResult is NetworkResult.Success) {
                groupsResult.data ?: emptyList()
            } else {
                emptyList()
            }

            // 2. Obserwuj ustawienia ORAZ plan zajęć
            combine(
                settingsRepository.getSettingsStream(),
                classRepository.getAllClassesStream()
            ) { settings, classes ->
                val uniqueTypes = classes
                    .map { it.classType }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                Triple(settings, uniqueTypes, groups)
            }.collect { (settings, uniqueTypes, loadedGroups) ->

                var subgroups = emptyList<String>()
                val groupCode = settings?.selectedGroupCode ?: ""

                // Pobieramy podgrupy tylko jeśli zmieniła się grupa lub jest to pierwsze ładowanie
                if (groupCode.isNotBlank()) {
                    // Uwaga: To jest operacja sieciowa wewnątrz collect, w idealnym świecie powinna być oddzielona,
                    // ale dla uproszczenia przy tym flow zostawiamy tutaj, ew. można cache'ować.
                    // Tutaj zakładamy optymistycznie, że lista podgrup w settings view nie musi się odświeżać live z sieci przy każdej zmianie DB.
                    // Dla płynności UI pobierzemy je tylko raz lub przy zmianie grupy w osobnej metodzie.
                }

                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    availableGroups = loadedGroups,
                    uniqueClassTypes = uniqueTypes
                )
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
        // Tutaj można dodać logikę pobierania podgrup dla nowej grupy
    }

    fun toggleDarkMode() {
        val current = _uiState.value.settings ?: SettingsEntity(id = 1)
        updateSettings(current.copy(isDarkMode = !current.isDarkMode))
    }
}