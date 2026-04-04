package com.example.my_uz_android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        UserCourseEntity::class,
        NotificationEntity::class
    ],
    version = 12,
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
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN appLanguage TEXT NOT NULL DEFAULT 'system'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}