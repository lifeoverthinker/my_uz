package com.example.my_uz_android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.my_uz_android.data.daos.AbsenceDao
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.daos.GradesDao
import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity

@Database(
    entities = [
        ClassEntity::class,
        TaskEntity::class,
        GradeEntity::class,
        AbsenceEntity::class,
        SettingsEntity::class
    ],
    version = 2, // ZMIANA: Wersja 2 wymusi przebudowanie bazy i naprawi crash
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao
    abstract fun tasksDao(): TasksDao
    abstract fun gradesDao(): GradesDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration() // Ważne: czyści starą bazę przy zmianie wersji
                    .build()
                    .also { Instance = it }
            }
        }
    }
}