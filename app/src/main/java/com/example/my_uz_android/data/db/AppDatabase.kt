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
    version = 13,
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

        // Kompletna, bezpieczna migracja z wersji 12 do 13
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Tworzymy tabelę z dokładną strukturą oczekiwaną przez nową wersję Room
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS settings_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        userName TEXT,
                        firstName TEXT,
                        lastName TEXT,
                        gender TEXT,
                        faculty TEXT,
                        department TEXT,
                        fieldOfStudy TEXT,
                        currentSemester INTEGER NOT NULL,
                        studyMode TEXT,
                        selectedGroupCode TEXT,
                        selectedGroupName TEXT,
                        selectedSubgroup TEXT,
                        additionalGroupCodes TEXT NOT NULL,
                        isFirstRun INTEGER NOT NULL,
                        isAnonymous INTEGER NOT NULL,
                        isDarkMode INTEGER NOT NULL,
                        themeMode TEXT NOT NULL,
                        notificationsEnabled INTEGER NOT NULL,
                        notificationsClasses INTEGER NOT NULL,
                        notificationClassTimeBefore INTEGER NOT NULL DEFAULT 15,
                        autoSyncEnabled INTEGER NOT NULL DEFAULT 1,
                        syncIntervalHours INTEGER NOT NULL DEFAULT 4,
                        offlineModeEnabled INTEGER NOT NULL,
                        classColorsJson TEXT NOT NULL,
                        activeDirectionCode TEXT,
                        activeIndexDirectionCode TEXT,
                        appLanguage TEXT NOT NULL DEFAULT 'system',
                        lastSyncedSemesterId TEXT
                    )
                """.trimIndent())

                // 2. Kopiujemy dane użytkownika ze starej tabeli do nowej
                db.execSQL("""
                    INSERT INTO settings_new (
                        id, userName, firstName, lastName, gender, faculty, department, fieldOfStudy,
                        currentSemester, studyMode, selectedGroupCode, selectedGroupName, selectedSubgroup,
                        additionalGroupCodes, isFirstRun, isAnonymous, isDarkMode, themeMode,
                        notificationsEnabled, notificationsClasses, notificationClassTimeBefore,
                        autoSyncEnabled, syncIntervalHours, offlineModeEnabled, classColorsJson,
                        activeDirectionCode, activeIndexDirectionCode, appLanguage, lastSyncedSemesterId
                    )
                    SELECT 
                        id, userName, firstName, lastName, gender, faculty, department, fieldOfStudy,
                        currentSemester, studyMode, selectedGroupCode, selectedGroupName, selectedSubgroup,
                        additionalGroupCodes, isFirstRun, isAnonymous, isDarkMode, themeMode,
                        notificationsEnabled, notificationsClasses, 15,
                        1, 4, offlineModeEnabled, classColorsJson,
                        activeDirectionCode, activeIndexDirectionCode, appLanguage, NULL
                    FROM settings
                """.trimIndent())

                // 3. Zastępujemy starą tabelę nową
                db.execSQL("DROP TABLE settings")
                db.execSQL("ALTER TABLE settings_new RENAME TO settings")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}