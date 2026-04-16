package com.example.my_uz_android.ui

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
            val app = myUZApplication()
            val container = app.container

            HomeViewModel(
                application = app,
                settingsRepository = container.settingsRepository,
                classRepository = container.classRepository,
                tasksRepository = container.tasksRepository,
                universityRepository = container.universityRepository,
                userCourseRepository = container.userCourseRepository,
                eventRepository = container.eventRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            TasksViewModel(
                tasksRepository = container.tasksRepository
            )
        }

        initializer {
            val app = myUZApplication()
            val container = app.container
            TaskAddEditViewModel(
                application = app,
                savedStateHandle = createSavedStateHandle(),
                tasksRepository = container.tasksRepository,
                classRepository = container.classRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            ClassDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                classRepository = container.classRepository
            )
        }

        initializer {
            EventDetailsViewModel(
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            val container = myUZApplication().container
            TaskDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                tasksRepository = container.tasksRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            GradesViewModel(
                gradesRepository = container.gradesRepository,
                classRepository = container.classRepository,
                settingsRepository = container.settingsRepository,
                userCourseRepository = container.userCourseRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            GradeDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                gradesRepository = container.gradesRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            AddEditGradeViewModel(
                savedStateHandle = createSavedStateHandle(),
                gradesRepository = container.gradesRepository,
                classRepository = container.classRepository,
                settingsRepository = container.settingsRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            AbsencesViewModel(
                absenceRepository = container.absenceRepository,
                classRepository = container.classRepository,
                settingsRepository = container.settingsRepository,
                userCourseRepository = container.userCourseRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            AbsenceDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                absenceRepository = container.absenceRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            AddEditAbsenceViewModel(
                savedStateHandle = createSavedStateHandle(),
                absenceRepository = container.absenceRepository,
                classRepository = container.classRepository,
                settingsRepository = container.settingsRepository,
                userCourseRepository = container.userCourseRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            AccountViewModel(
                settingsRepository = container.settingsRepository,
                universityRepository = container.universityRepository,
                classRepository = container.classRepository,
                userCourseRepository = container.userCourseRepository
            )
        }

        initializer {
            val app = myUZApplication()
            val container = app.container
            SettingsViewModel(
                backupManager = BackupManager(AppDatabase.getDatabase(app)),
                settingsRepository = container.settingsRepository,
                universityRepository = container.universityRepository,
                classRepository = container.classRepository,
                tasksRepository = container.tasksRepository,
                gradesRepository = container.gradesRepository,
                absenceRepository = container.absenceRepository,
                eventRepository = container.eventRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            OnboardingViewModel(
                settingsRepository = container.settingsRepository,
                universityRepository = container.universityRepository,
                classRepository = container.classRepository,
                userCourseRepository = container.userCourseRepository
            )
        }

        initializer {
            val app = myUZApplication()
            val container = app.container
            CalendarViewModel(
                application = app,
                favoritesRepository = container.favoritesRepository,
                classRepository = container.classRepository,
                settingsRepository = container.settingsRepository,
                userCourseRepository = container.userCourseRepository,
                universityRepository = container.universityRepository,
                tasksRepository = container.tasksRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            ScheduleSearchViewModel(
                universityRepository = container.universityRepository,
                favoritesRepository = container.favoritesRepository
            )
        }

        initializer {
            val container = myUZApplication().container
            NotificationsViewModel(container.notificationsRepository)
        }

    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)