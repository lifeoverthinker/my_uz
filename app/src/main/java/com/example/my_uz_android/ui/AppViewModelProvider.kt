package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.EventDetailsViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            OnboardingViewModel(
                myUZApplication().container.settingsRepository,
                myUZApplication().container.universityRepository
            )
        }
        initializer {
            HomeViewModel(
                myUZApplication().container.settingsRepository,
                myUZApplication().container.classRepository,
                myUZApplication().container.tasksRepository,
                myUZApplication().container.universityRepository
            )
        }
        initializer {
            ClassDetailsViewModel(
                this.createSavedStateHandle(),
                myUZApplication().container.classRepository
            )
        }
        // NOWY: EventDetailsViewModel
        initializer {
            EventDetailsViewModel(
                this.createSavedStateHandle(),
                myUZApplication().container.eventRepository // Zakładam, że dodasz eventRepository do AppContainer
            )
        }
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)