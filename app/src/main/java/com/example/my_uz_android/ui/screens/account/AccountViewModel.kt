package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = settingsRepository.getSettingsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleDarkMode(isEnabled: Boolean) {
        updateSettings { it.copy(isDarkMode = isEnabled) }
    }

    fun updateGroup(groupCode: String) {
        updateSettings { it.copy(selectedGroupCode = groupCode) }
    }

    fun toggleSubgroup(subgroup: String) {
        updateSettings { it.copy(selectedSubgroup = subgroup) }
    }

    private fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch {
            val currentSettings = settings.value
            if (currentSettings != null) {
                settingsRepository.insertSettings(transform(currentSettings))
            } else {
                val default = SettingsEntity(
                    userName = "Student",
                    isAnonymous = false,
                    isDarkMode = false,
                    isFirstRun = false,
                    notificationsEnabled = true
                )
                settingsRepository.insertSettings(transform(default))
            }
        }
    }
}