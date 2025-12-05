package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import com.example.my_uz_android.ui.screens.calendar.TaskAddEditViewModel
import com.example.my_uz_android.ui.screens.calendar.TaskDetailsViewModel
import com.example.my_uz_android.ui.screens.calendar.TasksViewModel
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.EventDetailsViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // OnboardingViewModel
        initializer {
            OnboardingViewModel(
                settingsRepository = myUzApp().container.settingsRepository,
                universityRepository = myUzApp().container.universityRepository,
                classRepository = myUzApp().container.classRepository
            )
        }

        // HomeViewModel
        initializer {
            HomeViewModel(
                settingsRepository = myUzApp().container.settingsRepository,
                classRepository = myUzApp().container.classRepository,
                tasksRepository = myUzApp().container.tasksRepository,
                universityRepository = myUzApp().container.universityRepository
            )
        }

        // TasksViewModel
        initializer {
            TasksViewModel(
                tasksRepository = myUzApp().container.tasksRepository
            )
        }

        // TaskDetailsViewModel
        initializer {
            TaskDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUzApp().container.tasksRepository
            )
        }

        // TaskAddEditViewModel
        initializer {
            TaskAddEditViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUzApp().container.tasksRepository,
                classRepository = myUzApp().container.classRepository // ✅ Dodano
            )
        }


        // ClassDetailsViewModel
        initializer {
            ClassDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                classRepository = myUzApp().container.classRepository
            )
        }

        // EventDetailsViewModel
        initializer {
            EventDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                eventRepository = myUzApp().container.eventRepository
            )
        }

        // AccountViewModel
        initializer {
            AccountViewModel(
                settingsRepository = myUzApp().container.settingsRepository,
                universityRepository = myUzApp().container.universityRepository,
                classRepository = myUzApp().container.classRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [MyUZApplication].
 */
fun CreationExtras.myUzApp(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)
