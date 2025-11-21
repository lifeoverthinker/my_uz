package com.example.my_uz_android.di

import android.content.Context
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.SupabaseUniversityRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
// Ten import jest kluczowy:
import io.github.jan.supabase.postgrest.postgrest

interface AppContainer {
    val settingsRepository: SettingsRepository
    val universityRepository: UniversityRepository
    val classRepository: ClassRepository
    val tasksRepository: TasksRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    override val universityRepository: UniversityRepository by lazy {
        // Przekazujemy moduł .postgrest, a nie cały obiekt klienta
        SupabaseUniversityRepository(AppModule.supabase.postgrest)
    }

    override val classRepository: ClassRepository by lazy {
        ClassRepository(database.classDao())
    }

    override val tasksRepository: TasksRepository by lazy {
        TasksRepository(database.tasksDao())
    }
}