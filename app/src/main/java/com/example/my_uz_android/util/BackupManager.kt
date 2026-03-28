package com.example.my_uz_android.util

import androidx.room.withTransaction
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.models.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Model przechowujący całą strukturę bazy w jednym obiekcie JSON
@Serializable
data class BackupData(
    val classes: List<ClassEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val absences: List<AbsenceEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val settings: SettingsEntity? = null,
    val favorites: List<FavoriteEntity> = emptyList(),
    val userCourses: List<UserCourseEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList()
)

class BackupManager(private val db: AppDatabase) {

    // Konfiguracja JSON: ignorujemy nieznane klucze (przydatne przy migracjach w przyszłości)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Zbiera dane ze wszystkich DAO i zwraca je jako jeden string JSON.
     */
    suspend fun exportData(): String {
        val data = BackupData(
            classes = db.classDao().getAllClasses().first(),
            tasks = db.tasksDao().getAllTasks().first(),
            grades = db.gradesDao().getAllGrades().first(),
            absences = db.absenceDao().getAllAbsences().first(),
            events = db.eventDao().getAllEvents().first(),
            settings = db.settingsDao().getSettingsStream().first(),
            favorites = db.favoritesDao().getAllFavoritesStream().first(),
            userCourses = db.userCourseDao().getAllUserCoursesStream().first(),
            notifications = db.notificationDao().getAllNotifications().first()
        )
        return json.encodeToString(data)
    }

    /**
     * Czyści obecną bazę i ładuje dane z JSONa.
     * Używamy withTransaction, by upewnić się, że w razie błędu nic się nie zepsuje.
     */
    suspend fun importData(jsonString: String) {
        val data = json.decodeFromString<BackupData>(jsonString)

        db.withTransaction {
            // 1. Czyszczenie starych danych
            db.classDao().deleteAll()
            db.tasksDao().deleteAll()
            db.gradesDao().deleteAll()
            db.absenceDao().deleteAll()
            db.eventDao().deleteAll()
            db.settingsDao().clearAll()
            db.favoritesDao().deleteAll()
            db.userCourseDao().deleteAll()
            db.notificationDao().clearAll()

            // 2. Wstawianie nowych danych
            // W przypadku braku insertAll, pętla wewnątrz withTransaction jest równie wydajna
            data.classes.forEach { db.classDao().insertClass(it) }
            data.tasks.forEach { db.tasksDao().insert(it) }
            data.grades.forEach { db.gradesDao().insertGrade(it) }
            data.absences.forEach { db.absenceDao().insertAbsence(it) }
            data.events.forEach { db.eventDao().insert(it) }
            data.settings?.let { db.settingsDao().insertOrUpdate(it) }
            data.favorites.forEach { db.favoritesDao().insertFavorite(it) }
            data.userCourses.forEach { db.userCourseDao().insertUserCourse(it) }
            data.notifications.forEach { db.notificationDao().insertNotification(it) }
        }
    }
}