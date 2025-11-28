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

    // Pobieramy ustawienia. Jeśli baza jest pusta, wartość będzie null.
    val settings: StateFlow<SettingsEntity?> = settingsRepository.getSettingsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settings.value

            if (currentSettings != null) {
                // Sytuacja standardowa: aktualizujemy istniejące ustawienia
                settingsRepository.insertSettings(currentSettings.copy(isDarkMode = isEnabled))
            } else {
                // NAPRAWA: Jeśli ustawień nie ma (np. pominięto onboarding), tworzymy nowe domyślne
                val newSettings = SettingsEntity(
                    userName = "Student",
                    isAnonymous = false,
                    isDarkMode = isEnabled, // Ustawiamy wartość z przełącznika
                    isFirstRun = false,
                    notificationsEnabled = true,
                    gender = null,
                    selectedGroupCode = null,
                    selectedSubgroup = null,
                    faculty = null,
                    fieldOfStudy = null,
                    studyMode = null
                )
                settingsRepository.insertSettings(newSettings)
            }
        }
    }
}