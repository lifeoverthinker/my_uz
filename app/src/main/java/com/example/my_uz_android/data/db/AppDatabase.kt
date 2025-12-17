package com.example.my_uz_android.data.db

import androidx.room.Database
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
        FavoriteEntity::class // Upewnij się, że ta klasa istnieje
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun tasksDao(): TasksDao
    abstract fun gradesDao(): GradesDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun eventDao(): EventDao
    abstract fun settingsDao(): SettingsDao
    // ✅ DODANO BRAKUJĄCE DAO:
    abstract fun favoritesDao(): FavoritesDao
}