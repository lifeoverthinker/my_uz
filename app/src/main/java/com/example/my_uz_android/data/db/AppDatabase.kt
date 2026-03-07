package com.example.my_uz_android.data.db

import com.example.my_uz_android.data.models.UserCourseEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.my_uz_android.data.daos.AbsenceDao
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.daos.EventDao
import com.example.my_uz_android.data.daos.FavoritesDao
import com.example.my_uz_android.data.daos.GradesDao
import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.TaskEntity

@Database(
    entities = [
        ClassEntity::class,
        TaskEntity::class,
        GradeEntity::class,
        AbsenceEntity::class,
        EventEntity::class,
        SettingsEntity::class,
        FavoriteEntity::class,
        UserCourseEntity::class // <-- TO DODAJESZ
    ],
    version = 6, // <-- ZMIEŃ Z 5 NA 6
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grades ADD COLUMN isPoints INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN activeDirectionCode TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tworzymy nową tabelę dla wielu kierunków
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_courses` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`groupCode` TEXT NOT NULL, " +
                            "`selectedSubgroup` TEXT, " +
                            "`colorHex` TEXT, " +
                            "`fieldOfStudy` TEXT, " +
                            "`faculty` TEXT, " +
                            "`studyMode` TEXT, " +
                            "`semester` INTEGER)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
