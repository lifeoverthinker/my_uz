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
import com.example.my_uz_android.ui.screens.index.AbsencesViewModel // ✅ DODANY IMPORT
import com.example.my_uz_android.ui.screens.index.AddEditGradeViewModel
import com.example.my_uz_android.ui.screens.index.GradeDetailsViewModel
import com.example.my_uz_android.ui.screens.index.GradesViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Home
        initializer {
            HomeViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                classRepository = myUZApplication().container.classRepository,
                tasksRepository = myUZApplication().container.tasksRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        // Class Details
        initializer {
            ClassDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                classRepository = myUZApplication().container.classRepository
            )
        }

        // Event Details
        initializer {
            EventDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                eventRepository = myUZApplication().container.eventRepository
            )
        }

        // Task Details
        initializer {
            TaskDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository
            )
        }

        // Tasks (Calendar)
        initializer {
            TasksViewModel(
                tasksRepository = myUZApplication().container.tasksRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // Task Add/Edit
        initializer {
            TaskAddEditViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // Grades
        initializer {
            GradesViewModel(
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // Grade Details
        initializer {
            GradeDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                gradesRepository = myUZApplication().container.gradesRepository
            )
        }

        // Add/Edit Grade
        initializer {
            AddEditGradeViewModel(
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // ✅ NOWOŚĆ: Absences (Nieobecności)
        initializer {
            AbsencesViewModel(
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // Account
        initializer {
            AccountViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // Onboarding
        initializer {
            OnboardingViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)