package com.example.my_uz_android.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.models.NotificationEntity
import com.example.my_uz_android.data.models.ClassEntity // Upewnij się, że ten import się zgadza
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

        // 2. Sprawdzanie zajęć (15 min przed)
        if (settings.notificationsClasses) {
            checkClasses(database)
        }

        // 3. NOWE: Sprawdzanie zmian w planie względem bazy danych (np. Supabase)
        checkScheduleChanges(database)

        return Result.success()
    }

    private suspend fun checkTasks(database: AppDatabase) {
        val now = LocalTime.now()

        // Wysyłamy powiadomienie tylko raz dziennie w oknie godziny 8:00 - 9:00
        if (now.hour != 8) return

        val tasks = database.tasksDao().getAllTasks().first()
        val tomorrow = LocalDate.now().plusDays(1)

        tasks.forEach { task ->
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

    private suspend fun checkScheduleChanges(database: AppDatabase) {
        try {
            val localClasses = database.classDao().getAllClasses().first()

            // TODO: W tym miejscu musisz odpytać Firebase / Supabase o aktualny plan dla grupy usera.
            // Poniższa zmienna symuluje pobraną z internetu listę zajęć.
            // Zastąp to właściwym kodem pobierającym dane (np. z Repozytorium lub SupabaseClient)
            val remoteClasses = emptyList<ClassEntity>()

            if (remoteClasses.isEmpty()) return

            val localClassMap = localClasses.associateBy { it.id }

            for (remoteClass in remoteClasses) {
                val localClass = localClassMap[remoteClass.id]
                if (localClass != null) {
                    var isChanged = false
                    var message = "Zmiana w zajęciach: ${remoteClass.subjectName}.\n"

                    // Porównanie Sali
                    if (localClass.room != remoteClass.room) {
                        message += "Nowa sala: ${remoteClass.room} (poprzednio: ${localClass.room}). "
                        isChanged = true
                    }

                    // Porównanie Godzin
                    if (localClass.startTime != remoteClass.startTime || localClass.endTime != remoteClass.endTime) {
                        message += "Nowe godziny: ${remoteClass.startTime}-${remoteClass.endTime}. "
                        isChanged = true
                    }

                    if (isChanged) {
                        // 1. Zapisz powiadomienie do bazy, by pokazać na ekranie powiadomień
                        database.notificationDao().insertNotification(
                            NotificationEntity(
                                title = "Zmiana w planie!",
                                message = message.trim(),
                                timestamp = System.currentTimeMillis(),
                                type = "schedule_change"
                            )
                        )

                        // 2. Wyślij Push / Pokaż na pasku
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Zmiana w planie!",
                            message.trim(),
                            isTask = false
                        )

                        // 3. Nadpisz zajęcia w lokalnej bazie, by zaktualizować ekran
                        database.classDao().insertClass(remoteClass)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Błąd podczas sprawdzania zmian w planie: ${e.message}")
        }
    }
}