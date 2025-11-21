package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer dla OnboardingViewModel
        initializer {
            OnboardingViewModel(
                settingsRepository = myUzApplication().container.settingsRepository,
                universityRepository = myUzApplication().container.universityRepository
            )
        }

        // Initializer dla HomeViewModel
        initializer {
            val container = myUzApplication().container
            HomeViewModel(
                settingsRepository = container.settingsRepository,
                classRepository = container.classRepository, // Upewnij się, że jest w AppContainer
                tasksRepository = container.tasksRepository, // Upewnij się, że jest w AppContainer
                universityRepository = container.universityRepository
            )
        }
    }
}

fun CreationExtras.myUzApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)