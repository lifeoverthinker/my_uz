package com.example.my_uz_android.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider
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

        // HomeViewModel: Usunięto eventRepository (bo konstruktor go nie ma), dodano universityRepository (bo konstruktor go ma)
        initializer {
            HomeViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                classRepository = myUZApplication().container.classRepository,
                tasksRepository = myUZApplication().container.tasksRepository,
                // eventRepository = myUZApplication().container.eventRepository, // Błąd: HomeViewModel nie ma tego parametru
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        // TasksViewModel: Dodano settingsRepository (bo konstruktor go ma)
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

        // AccountViewModel: Usunięto classRepository (bo konstruktor go nie ma)
        initializer {
            AccountViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        // SettingsViewModel: Dodano wszystkie brakujące repozytoria
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

        // CalendarViewModel: OK
        initializer {
            CalendarViewModel(
                application = myUZApplication(),
                favoritesRepository = myUZApplication().container.favoritesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository
            )
        }

        // ScheduleSearchViewModel: Usunięto classRepository (bo konstruktor go nie ma)
        initializer {
            ScheduleSearchViewModel(
                universityRepository = myUZApplication().container.universityRepository,
                // classRepository = myUZApplication().container.classRepository, // Błąd: ScheduleSearchViewModel nie ma tego parametru
                favoritesRepository = myUZApplication().container.favoritesRepository
            )
        }
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)