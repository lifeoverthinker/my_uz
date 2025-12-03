package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = settingsRepository.getSettingsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _availableGroups = MutableStateFlow<List<String>>(emptyList())
    val availableGroups: StateFlow<List<String>> = _availableGroups.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    init {
        fetchGroups()
    }

    private fun fetchGroups() {
        viewModelScope.launch {
            try {
                val groups = universityRepository.getGroupCodes()
                _availableGroups.value = groups
            } catch (e: Exception) {
                _availableGroups.value = emptyList()
            }
        }
    }

    fun toggleDarkMode(isEnabled: Boolean) {
        updateSettings { it.copy(isDarkMode = isEnabled) }
    }

    fun updateGroup(groupCode: String) {
        updateSettings { it.copy(selectedGroupCode = groupCode) }

        viewModelScope.launch {
            try {
                // 1. Podgrupy
                val subgroups = universityRepository.getSubgroups(groupCode)
                _availableSubgroups.value = subgroups

                // 2. Plan Zajęć (POPRAWKA NAZWY METODY)
                val schedule = universityRepository.getSchedule(groupCode)
                classRepository.refreshSchedule(schedule)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleSubgroup(subgroup: String) {
        val current = settings.value?.selectedSubgroup ?: ""
        val currentList = if (current.isBlank()) mutableListOf() else current.split(",").toMutableList()

        if (currentList.contains(subgroup)) {
            currentList.remove(subgroup)
        } else {
            currentList.add(subgroup)
        }

        val newSubgroups = currentList.joinToString(",")
        updateSettings { it.copy(selectedSubgroup = newSubgroups) }
    }

    private fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val newSettings = if (currentSettings != null) {
                transform(currentSettings).copy(id = 0)
            } else {
                val default = SettingsEntity(
                    id = 0,
                    userName = "Student",
                    isAnonymous = false,
                    isDarkMode = false,
                    isFirstRun = false,
                    notificationsEnabled = true
                )
                transform(default).copy(id = 0)
            }
            settingsRepository.insertSettings(newSettings)
        }
    }
}