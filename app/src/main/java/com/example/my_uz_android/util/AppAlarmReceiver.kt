package com.example.my_uz_android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.my_uz_android.data.db.AppDatabase
import com.example.my_uz_android.data.models.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppAlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_TEST", "1. ODBIORNIK OBUDZONY! Akcja: ${intent.action}")

        val action = intent.action ?: return
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "MyUZ"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""

        Log.d("ALARM_TEST", "2. Odczytano dane: Tytuł=$title, Wiadomość=$message")

        val isTask = action == ACTION_TASK_ALARM
        val type = if (isTask) "task_reminder" else "class_reminder"

        // 1. Wyświetl powiadomienie systemowe
        try {
            NotificationHelper.showNotification(
                context = context,
                title = title,
                message = message,
                isTask = isTask,
                notificationId = id
            )
            Log.d("ALARM_TEST", "3. Wywołano showNotification pomyślnie!")
        } catch (e: Exception) {
            Log.e("ALARM_TEST", "BŁĄD przy wyświetlaniu powiadomienia!", e)
        }

        // 2. Zapisz do bazy historii powiadomień
        val pendingResult = goAsync()
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val notification = NotificationEntity(
                    id = 0, // Auto-generate, żeby się nie nadpisywały w historii
                    title = title,
                    message = message,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                database.notificationDao().insertNotification(notification)
                Log.d("ALARM_TEST", "4. Zapisano do bazy danych pomyślnie!")
            } catch (e: Exception) {
                Log.e("ALARM_TEST", "Błąd zapisu do bazy", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TASK_ALARM = "com.example.my_uz_android.ACTION_TASK_ALARM"
        const val ACTION_CLASS_ALARM = "com.example.my_uz_android.ACTION_CLASS_ALARM"

        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }
}