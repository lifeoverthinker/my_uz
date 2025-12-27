package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import com.example.my_uz_android.ui.screens.account.SettingsViewModel
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchViewModel
import com.example.my_uz_android.ui.screens.calendar.tasks.TaskAddEditViewModel
import com.example.my_uz_android.ui.screens.calendar.tasks.TasksViewModel
import com.example.my_uz_android.ui.screens.home.HomeViewModel
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.EventDetailsViewModel
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsViewModel
import com.example.my_uz_android.ui.screens.index.*
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            HomeViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                classRepository = myUZApplication().container.classRepository,
                tasksRepository = myUZApplication().container.tasksRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        initializer {
            TasksViewModel(
                tasksRepository = myUZApplication().container.tasksRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        initializer {
            TaskAddEditViewModel(
                savedStateHandle = createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        initializer {
            ClassDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                classRepository = myUZApplication().container.classRepository
            )
        }

        initializer {
            EventDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                eventRepository = myUZApplication().container.eventRepository
            )
        }

        initializer {
            TaskDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                tasksRepository = myUZApplication().container.tasksRepository
            )
        }

        initializer {
            GradesViewModel(
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        initializer {
            GradeDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                gradesRepository = myUZApplication().container.gradesRepository
            )
        }

        initializer {
            AddEditGradeViewModel(
                savedStateHandle = createSavedStateHandle(),
                gradesRepository = myUZApplication().container.gradesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository
            )
        }

        initializer {
            AbsencesViewModel(
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        initializer {
            AddEditAbsenceViewModel(
                savedStateHandle = createSavedStateHandle(),
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        initializer {
            AccountViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository
                // Usunięto classRepository, którego brakowało w konstruktorze AccountViewModel
            )
        }

        initializer {
            SettingsViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository,
                tasksRepository = myUZApplication().container.tasksRepository,
                gradesRepository = myUZApplication().container.gradesRepository,
                absenceRepository = myUZApplication().container.absenceRepository,
                eventRepository = myUZApplication().container.eventRepository
            )
        }

        initializer {
            OnboardingViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository
            )
        }

        initializer {
            CalendarViewModel(
                application = myUZApplication(), // DODAJ TO
                favoritesRepository = myUZApplication().container.favoritesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        initializer {
            ScheduleSearchViewModel(
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository,
                favoritesRepository = myUZApplication().container.favoritesRepository
            )
        }
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)