package com.example.my_uz_android.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val settings = database.settingsDao().getSettingsStream().first()
            ?: return Result.success()

        if (!settings.notificationsEnabled) return Result.success()

        if (settings.notificationsTasks) checkTasks(database)
        if (settings.notificationsClasses) checkClasses(database)

        return Result.success()
    }

    private suspend fun checkTasks(database: AppDatabase) {
        val hour = LocalTime.now().hour

        // ZMIANA: zakres 7..8 zamiast != 8, żeby nie przegapić okna przy opóźnionym Workerze
        if (hour !in 7..8) return

        val tomorrow = LocalDate.now().plusDays(1)
        val tasks = database.tasksDao().getAllTasks().first()

        tasks.filter { !it.isCompleted }.forEach { task ->
            val taskDate = Instant.ofEpochMilli(task.dueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            if (taskDate == tomorrow) {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "Zadanie na jutro!",
                    message = "Termin: ${task.title}",
                    isTask = true,
                    notificationId = task.id.hashCode()  // ZMIANA: stabilne, unikalne ID
                )
            }
        }
    }

    private suspend fun checkClasses(database: AppDatabase) {
        val now = LocalTime.now()
        val todayStr = LocalDate.now().toString()
        val classes = database.classDao().getAllClasses().first()

        classes.filter { it.date == todayStr }.forEach { classItem ->
            try {
                val startTime = LocalTime.parse(classItem.startTime)
                val minutesUntil = Duration.between(now, startTime).toMinutes()

                // ZMIANA: węższe okno 13..17 minut.
                // Przy interwale 15 min Worker może odpalić ±2 min od planowanego czasu.
                // Okno 13..17 daje margines, ale nie pozwala na podwójne powiadomienie.
                if (minutesUntil in 13..17) {
                    NotificationHelper.showNotification(
                        context = applicationContext,
                        title = "Zajęcia za 15 minut",
                        message = "${classItem.subjectName} · sala ${classItem.room}",
                        isTask = false,
                        notificationId = classItem.id.hashCode()  // ZMIANA: stabilne ID po klasie
                    )
                }
            } catch (e: Exception) {
                // Błąd parsowania czasu — ignorujemy konkretny wpis
            }
        }
    }
}