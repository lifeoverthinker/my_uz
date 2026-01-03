package com.example.my_uz_android.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.my_uz_android.data.daos.*
import com.example.my_uz_android.data.models.*

@Database(
    entities = [
        ClassEntity::class,
        TaskEntity::class,
        GradeEntity::class,
        AbsenceEntity::class,
        EventEntity::class,
        SettingsEntity::class,
        FavoriteEntity::class
    ],
    version = 3, // Podniesiono na 3, by obsłużyć zmiany w Settings
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun tasksDao(): TasksDao
    abstract fun gradesDao(): GradesDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun eventDao(): EventDao
    abstract fun settingsDao(): SettingsDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Resetuje bazę przy zmianie struktury - używaj Eksportu do JSON przed aktualizacją!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}