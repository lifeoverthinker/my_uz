package com.example.my_uz_android.di

import android.content.Context
import androidx.room.Room
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.repositories.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

interface AppContainer {
    val universityRepository: UniversityRepository
    val classRepository: ClassRepository
    val tasksRepository: TasksRepository
    val gradesRepository: GradesRepository
    val absenceRepository: AbsenceRepository
    val eventRepository: EventRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    // Konfiguracja bazy danych Room
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_uz_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // Konfiguracja Supabase
    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://aovlvwjbnjsfplpgqzjv.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFvdmx2d2pibmpzZnBscGdxemp2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE5ODY5OTEsImV4cCI6MjA1NzU2Mjk5MX0.TYvFUUhrksgleb-jiLDa-TxdItWuEO_CqIClPYyHdN0"
        ) {
            install(Postgrest)
        }
    }

    // Repozytoria
    override val universityRepository: UniversityRepository by lazy {
        SupabaseUniversityRepository(supabase.postgrest)
    }

    override val classRepository: ClassRepository by lazy {
        ClassRepository(database.classDao())
    }

    override val tasksRepository: TasksRepository by lazy {
        TasksRepository(database.tasksDao())
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

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }
}