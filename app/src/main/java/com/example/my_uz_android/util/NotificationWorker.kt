package com.example.my_uz_android.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.data.db.AppDatabase
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

        // Jeśli powiadomienia są całkowicie wyłączone w ustawieniach, kończymy pracę
        if (!settings.notificationsEnabled) return Result.success()

        // 1. Sprawdzanie zadań
        if (settings.notificationsTasks) {
            checkTasks(database)
        }

        // 2. Sprawdzanie zajęć
        if (settings.notificationsClasses) {
            checkClasses(database)
        }

        return Result.success()
    }

    private suspend fun checkTasks(database: AppDatabase) {
        val now = LocalTime.now()

        // Wysyłamy powiadomienie tylko raz dziennie w oknie godziny 8:00 - 9:00
        // WorkManager i tak budzi się co 15 min, więc trafi tu przynajmniej raz w tym oknie
        if (now.hour != 8) return

        val tasks = database.tasksDao().getAllTasks().first()
        val tomorrow = LocalDate.now().plusDays(1)

        tasks.forEach { task ->
            // Usunięto task.dueDate != null, jeśli Twoja encja ma Long (nie Long?)
            // Jeśli jednak pole MOŻE być nullem, użyj: task.dueDate?.let { ... }
            if (!task.isCompleted) {
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
    }

    private suspend fun checkClasses(database: AppDatabase) {
        val classes = database.classDao().getAllClasses().first()
        val now = LocalTime.now()
        val todayStr = LocalDate.now().toString()

        classes.forEach { classItem ->
            if (classItem.date == todayStr) {
                try {
                    val startTime = LocalTime.parse(classItem.startTime)
                    val diff = Duration.between(now, startTime).toMinutes()

                    // Jeśli do zajęć zostało od 14 do 16 minut
                    if (diff in 14..16) {
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Zajęcia za 15 minut",
                            "${classItem.subjectName} (sala ${classItem.room})",
                            isTask = false
                        )
                    }
                } catch (e: Exception) {
                    // Ignorujemy błędy parsowania czasu dla niepoprawnych danych
                }
            }
        }
    }
}