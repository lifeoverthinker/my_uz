package com.example.my_uz_android.ui

import android.app.Application
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.util.BackupManager
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
import com.example.my_uz_android.ui.screens.notifications.NotificationsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            // 1. Pobieramy instancję Application z Extras
            val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as Application

            // 2. Pobieramy aplikację do wstrzyknięcia zależności
            val myUzApplication = application as MyUZApplication

            HomeViewModel(
                application = application,
                settingsRepository = myUzApplication.container.settingsRepository,
                classRepository = myUzApplication.container.classRepository,
                tasksRepository = myUzApplication.container.tasksRepository,
                universityRepository = myUzApplication.container.universityRepository,
                notificationsRepository = myUzApplication.container.notificationsRepository,
                userCourseRepository = myUzApplication.container.userCourseRepository // DODANE
            )
        }

        initializer {
            TasksViewModel(
                tasksRepository = myUZApplication().container.tasksRepository
            )
        }

        initializer {
            TaskAddEditViewModel(
                // DODANO PRZEKAZANIE APPLICATION:
                application = myUZApplication(),
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
                savedStateHandle = createSavedStateHandle()
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
                settingsRepository = myUZApplication().container.settingsRepository,
                userCourseRepository = myUZApplication().container.userCourseRepository
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
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository,
                userCourseRepository = myUZApplication().container.userCourseRepository
            )
        }

        initializer {
            AbsenceDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                absenceRepository = myUZApplication().container.absenceRepository
            )
        }

        initializer {
            AddEditAbsenceViewModel(
                savedStateHandle = createSavedStateHandle(),
                absenceRepository = myUZApplication().container.absenceRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository,
                userCourseRepository = myUZApplication().container.userCourseRepository
            )
        }

        initializer {
            AccountViewModel(
                settingsRepository = myUZApplication().container.settingsRepository,
                universityRepository = myUZApplication().container.universityRepository,
                classRepository = myUZApplication().container.classRepository,
                userCourseRepository = myUZApplication().container.userCourseRepository
            )
        }

        initializer {
            SettingsViewModel(
                backupManager = BackupManager(AppDatabase.getDatabase(myUZApplication())),
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
                application = myUZApplication(),
                favoritesRepository = myUZApplication().container.favoritesRepository,
                classRepository = myUZApplication().container.classRepository,
                settingsRepository = myUZApplication().container.settingsRepository,
                userCourseRepository = myUZApplication().container.userCourseRepository,
                universityRepository = myUZApplication().container.universityRepository,
                tasksRepository = myUZApplication().container.tasksRepository
            )
        }

        initializer {
            ScheduleSearchViewModel(
                universityRepository = myUZApplication().container.universityRepository,
                favoritesRepository = myUZApplication().container.favoritesRepository
            )
        }

        initializer {
            NotificationsViewModel(myUZApplication().container.notificationsRepository)
        }

    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)