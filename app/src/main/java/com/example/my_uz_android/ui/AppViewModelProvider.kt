package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import com.example.my_uz_android.ui.screens.calendar.TaskAddEditViewModel
import com.example.my_uz_android.ui.screens.calendar.TasksViewModel
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.EventDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsViewModel
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
        initializer {
            EventDetailsViewModel(
                this.createSavedStateHandle(),
                myUZApplication().container.eventRepository
            )
        }
        initializer {
            TaskDetailsViewModel(
                this.createSavedStateHandle(),
                myUZApplication().container.tasksRepository
            )
        }
        // ViewModel Listy Zadań
        initializer {
            TasksViewModel(
                myUZApplication().container.tasksRepository
            )
        }
        // NOWY ViewModel Dodawania/Edycji
        initializer {
            TaskAddEditViewModel(
                this.createSavedStateHandle(),
                myUZApplication().container.tasksRepository,
                myUZApplication().container.classRepository
            )
        }
        initializer {
            AccountViewModel(
                myUZApplication().container.settingsRepository
            )
        }
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)