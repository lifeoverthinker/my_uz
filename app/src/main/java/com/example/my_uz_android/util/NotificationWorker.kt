package com.example.my_uz_android.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.models.NotificationEntity
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Instant

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val settings = database.settingsDao().getSettings().first() ?: return Result.success()

        if (!settings.notificationsEnabled) return Result.success()

        if (settings.notificationsTasks) checkTasks(database)
        if (settings.notificationsClasses) checkClasses(database)
        checkScheduleChanges(database)

        return Result.success()
    }

    private suspend fun checkTasks(database: AppDatabase) {
        val now = LocalTime.now()
        if (now.hour != 8) return

        val tasks = database.tasksDao().getAllTasks().first()
        val tomorrow = LocalDate.now().plusDays(1)

        tasks.filter { !it.isCompleted }.forEach { task ->
            val taskDate = Instant.ofEpochMilli(task.dueDate)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            if (taskDate == tomorrow) {
                NotificationHelper.showNotification(
                    applicationContext,
                    "Zadanie na jutro!",
                    "Zbliża się termin: ${task.title}",
                    isTask = true
                )
            }
        }
    }

    private suspend fun checkClasses(database: AppDatabase) {
        val classes = database.classDao().getAllClasses().first()
        val now = LocalTime.now()
        val todayStr = LocalDate.now().toString()

        classes.filter { it.date == todayStr }.forEach { classItem ->
            try {
                val startTime = LocalTime.parse(classItem.startTime)
                val diff = Duration.between(now, startTime).toMinutes()

                if (diff in 14..16) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        "Zajęcia za 15 minut",
                        "${classItem.subjectName} (sala ${classItem.room})",
                        isTask = false
                    )
                }
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Błąd parsowania czasu: ${e.message}")
            }
        }
    }

    // --- NOWA LOGIKA SPRAWDZANIA ZMIAN ---

    private suspend fun checkScheduleChanges(database: AppDatabase) {
        try {
            val settings = database.settingsDao().getSettings().first() ?: return
            val groupCode = settings.selectedGroupCode ?: return
            val subgroups = settings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

            val application = applicationContext as com.example.my_uz_android.MyUZApplication
            val repository = application.container.universityRepository

            val result = repository.getSchedule(groupCode, subgroups)
            val remoteClasses = (result as? NetworkResult.Success)?.data ?: return

            if (remoteClasses.isEmpty()) return

            val localClasses = database.classDao().getAllClasses().first()
            val localMap = localClasses.associateBy { it.supabaseId }
            val remoteMap = remoteClasses.associateBy { it.supabaseId }

            val notificationsToTrigger = mutableListOf<NotificationEntity>()

            // 1. Sprawdzanie NOWYCH i ZMIENIONYCH zajęć
            for (remoteClass in remoteClasses) {
                if (remoteClass.supabaseId == null) continue // Pomijamy błędne dane bez ID

                val localClass = localMap[remoteClass.supabaseId]

                if (localClass == null) {
                    // Wykryto NOWE zajęcia
                    handleNewClass(remoteClass, notificationsToTrigger, database)
                } else {
                    // Wykryto istniejące zajęcia - sprawdzamy ZMIANY
                    handleModifiedClass(localClass, remoteClass, notificationsToTrigger, database)
                }
            }

            // 2. Sprawdzanie ODWOŁANYCH / USUNIĘTYCH zajęć
            for (localClass in localClasses) {
                // Sprawdzamy tylko zajęcia pochodzące z Supabase (mają supabaseId)
                if (localClass.supabaseId != null && !remoteMap.containsKey(localClass.supabaseId)) {
                    handleCanceledClass(localClass, notificationsToTrigger, database)
                }
            }

            // 3. Wysyłanie powiadomień na telefon
            notificationsToTrigger.forEach { notif ->
                NotificationHelper.showNotification(
                    applicationContext,
                    notif.title,
                    notif.message,
                    isTask = false
                )
            }

        } catch (e: Exception) {
            Log.e("NotificationWorker", "Błąd podczas sprawdzania zmian: ${e.message}")
        }
    }

    private suspend fun handleNewClass(
        remoteClass: ClassEntity,
        notifications: MutableList<NotificationEntity>,
        database: AppDatabase
    ) {
        val msg = "Dodano nowe zajęcia: ${remoteClass.subjectName} (${remoteClass.date} o ${remoteClass.startTime})"

        notifications.add(createNotification("Nowe zajęcia!", msg))
        database.notificationDao().insertNotification(createNotification("Nowe zajęcia!", msg))
        database.classDao().insertClass(remoteClass)
    }

    private suspend fun handleModifiedClass(
        localClass: ClassEntity,
        remoteClass: ClassEntity,
        notifications: MutableList<NotificationEntity>,
        database: AppDatabase
    ) {
        val changes = detectDifferences(localClass, remoteClass)

        if (changes.isNotEmpty()) {
            val msg = "Zmiana w zajęciach: ${remoteClass.subjectName}.\n$changes"

            notifications.add(createNotification("Zmiana w planie!", msg))
            database.notificationDao().insertNotification(createNotification("Zmiana w planie!", msg))

            // UWAGA: Musimy zachować lokalne ID, żeby Room zaktualizował ten sam wiersz, zamiast tworzyć duplikat!
            val updatedClass = remoteClass.copy(id = localClass.id)
            database.classDao().update(updatedClass)
        }
    }

    private suspend fun handleCanceledClass(
        localClass: ClassEntity,
        notifications: MutableList<NotificationEntity>,
        database: AppDatabase
    ) {
        val msg = "Odwołano zajęcia: ${localClass.subjectName} w dniu ${localClass.date}"

        notifications.add(createNotification("Zajęcia odwołane!", msg))
        database.notificationDao().insertNotification(createNotification("Zajęcia odwołane!", msg))
        database.classDao().delete(localClass)
    }

    // Funkcja porównująca poszczególne pola
    private fun detectDifferences(local: ClassEntity, remote: ClassEntity): String {
        val changes = mutableListOf<String>()

        if (local.room != remote.room) changes.add("Sala: ${remote.room ?: "Brak"} (było: ${local.room ?: "Brak"})")
        if (local.startTime != remote.startTime || local.endTime != remote.endTime) changes.add("Godziny: ${remote.startTime}-${remote.endTime}")
        if (local.date != remote.date) changes.add("Data: ${remote.date}")
        if (local.teacherName != remote.teacherName) changes.add("Prowadzący: ${remote.teacherName ?: "Brak"}")
        if (local.subjectName != remote.subjectName) changes.add("Przedmiot: ${remote.subjectName}")
        if (local.classType != remote.classType) changes.add("Typ: ${remote.classType}")

        return changes.joinToString("\n")
    }

    private fun createNotification(title: String, message: String): NotificationEntity {
        return NotificationEntity(
            title = title,
            message = message.trim(),
            timestamp = System.currentTimeMillis(),
            type = "schedule_change"
        )
    }
}