package com.example.my_uz_android.di

import android.content.Context
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.provideSupabaseClient
import com.example.my_uz_android.data.repositories.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

interface AppContainer {
    val settingsRepository: SettingsRepository
    val universityRepository: UniversityRepository
    val classRepository: ClassRepository
    val tasksRepository: TasksRepository
    val gradesRepository: GradesRepository
    val absenceRepository: AbsenceRepository
    val eventRepository: EventRepository
    val favoritesRepository: FavoritesRepository
    val userCourseRepository: UserCourseRepository // Nasz nowy kierunek
    val notificationDao: com.example.my_uz_android.data.daos.NotificationDao
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    // Inicjalizacja Supabase
    private val supabase: SupabaseClient by lazy {
        provideSupabaseClient()
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    override val universityRepository: UniversityRepository by lazy {
        // UniversityRepository potrzebuje wtyczki postgrest
        UniversityRepository(supabase.postgrest)
    }

    override val classRepository: ClassRepository by lazy {
        ClassRepository(database.classDao())
    }

    override val tasksRepository: TasksRepository by lazy {
        // TasksRepository potrzebuje DAO i całego klienta Supabase do eksportu
        TasksRepository(database.tasksDao(), supabase)
    }

    override val gradesRepository: GradesRepository by lazy {
        GradesRepository(database.gradesDao())
    }

    override val absenceRepository: AbsenceRepository by lazy {
        AbsenceRepository(database.absenceDao())
    }

    override val eventRepository: EventRepository by lazy {
        EventRepository(database.eventDao())
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepository(database.favoritesDao())
    }

    override val userCourseRepository: UserCourseRepository by lazy {
        UserCourseRepository(database.userCourseDao())
    }

    override val notificationDao: com.example.my_uz_android.data.daos.NotificationDao by lazy {
        database.notificationDao()
    }
}