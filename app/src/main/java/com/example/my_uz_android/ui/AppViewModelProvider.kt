package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer dla OnboardingViewModel
        initializer {
            val application = inventoryApplication()
            OnboardingViewModel(
                settingsRepository = application.container.settingsRepository,
                universityRepository = application.container.universityRepository
            )
        }

        // Tutaj dodasz inne ViewModele w przyszłości, np. HomeViewModel
    }
}

// Funkcja pomocnicza do pobierania instancji aplikacji
fun CreationExtras.inventoryApplication(): MyUZApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)