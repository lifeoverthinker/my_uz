package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import com.example.my_uz_android.ui.screens.account.SettingsViewModel
import com.example.my_uz_android.ui.screens.calendar.TaskAddEditViewModel
import com.example.my_uz_android.ui.screens.calendar.TasksViewModel
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.EventDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsViewModel
import com.example.my_uz_android.ui.screens.index.AbsencesViewModel
import com.example.my_uz_android.ui.screens.index.AddEditAbsenceViewModel
import com.example.my_uz_android.ui.screens.index.AddEditGradeViewModel
import com.example.my_uz_android.ui.screens.index.GradeDetailsViewModel
import com.example.my_uz_android.ui.screens.index.GradesViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // HomeViewModel
        initializer {
            HomeViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                classRepository = myUZApplication().container.classRepository,
                tasksRepository = myUZApplication().container.tasksRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        // TasksViewModel (Calendar)
        initializer {
            TasksViewModel(
                tasksRepository = myUZApplication().container.tasksRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // TaskAddEditViewModel
        initializer {
            TaskAddEditViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // ClassDetailsViewModel
        initializer {
            ClassDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                classRepository = myUZApplication().container.classRepository
            )
        }

        // EventDetailsViewModel
        initializer {
            EventDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                eventRepository = myUZApplication().container.eventRepository
            )
        }

        // TaskDetailsViewModel
        initializer {
            TaskDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository
            )
        }

        // GradesViewModel (Indeks - lista ocen)
        initializer {
            GradesViewModel(
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // GradeDetailsViewModel
        initializer {
            GradeDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                gradesRepository = myUZApplication().container.gradesRepository
            )
        }

        // AddEditGradeViewModel
        initializer {
            AddEditGradeViewModel(
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        // AbsencesViewModel (Indeks - lista nieobecności)
        initializer {
            AbsencesViewModel(
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // AddEditAbsenceViewModel
        initializer {
            AddEditAbsenceViewModel(
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // AccountViewModel
        initializer {
            AccountViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        // SettingsViewModel
        initializer {
            SettingsViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository // Dodano
            )
        }

        // OnboardingViewModel
        initializer {
            OnboardingViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }
    }
}

/**
 * Funkcja rozszerzająca do pobierania obiektu [MyUZApplication] z [CreationExtras].
 */
fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)