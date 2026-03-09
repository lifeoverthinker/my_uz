package com.example.my_uz_android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
        FavoriteEntity::class,
        UserCourseEntity::class // Nasza nowa tabela
    ],
    version = 7, // Podbita wersja
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
    abstract fun userCourseDao(): UserCourseDao

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
                    // Czyści bazę przy zmianie wersji (idealne na fazę testów)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}