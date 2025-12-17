package com.example.my_uz_android.di

import android.content.Context
import androidx.room.Room
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.provideSupabaseClient // ✅ Importujemy Twoją funkcję
import com.example.my_uz_android.data.repositories.*
import io.github.jan.supabase.postgrest.postgrest

interface AppContainer {
    val classRepository: ClassRepository
    val tasksRepository: TasksRepository
    val settingsRepository: SettingsRepository
    val gradesRepository: GradesRepository
    val absenceRepository: AbsenceRepository
    val eventRepository: EventRepository
    val universityRepository: UniversityRepository
    val favoritesRepository: FavoritesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    // ✅ Tworzymy instancję klienta Supabase używając Twojej funkcji
    private val supabase by lazy { provideSupabaseClient() }

    override val classRepository: ClassRepository by lazy {
        ClassRepository(database.classDao())
    }

    override val tasksRepository: TasksRepository by lazy {
        TasksRepository(database.tasksDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
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

    override val universityRepository: UniversityRepository by lazy {
        // ✅ Przekazujemy moduł Postgrest z utworzonej instancji
        UniversityRepository(supabase.postgrest)
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepository(database.favoritesDao())
    }
}