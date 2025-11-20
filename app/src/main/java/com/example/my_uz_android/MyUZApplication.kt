package com.example.my_uz_android

import android.app.Application
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.repositories.AbsenceRepository
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository

class MyUZApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val tasksRepository by lazy { TasksRepository(database.tasksDao()) }
    val gradesRepository by lazy { GradesRepository(database.gradesDao()) }
    val classRepository by lazy { ClassRepository(database.classDao()) }
    val absenceRepository by lazy { AbsenceRepository(database.absenceDao()) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}