package com.example.my_uz_android.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.my_uz_android.MainActivity
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {
    private const val CHANNEL_ID = "my_uz_notifications"
    private const val CHANNEL_NAME = "Powiadomienia i Zmiany MyUZ"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ważne powiadomienia dotyczące zajęć i zadań"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d("ALARM_TEST", "Utworzono kanał powiadomień")
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        isTask: Boolean,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("ALARM_TEST", "BRAK UPRAWNIENIA POST_NOTIFICATIONS w showNotification!")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val iconRes = if (isTask) R.drawable.ic_book_open else R.drawable.ic_marker_pin

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        Log.d("ALARM_TEST", "Powiadomienie zostało wypchnięte do systemu: ID=$notificationId")
    }

    fun scheduleExactAlarm(
        context: Context,
        timeInMillis: Long,
        id: Int,
        title: String,
        message: String,
        isTask: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createAlarmPendingIntent(context, id, title, message, isTask)

        try {
            // Bezpośrednio wymuszamy dokładny alarm z wybudzaniem (Doze mode)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("ALARM_TEST", "USTAWIONO ALARM: na $timeInMillis (obecnie jest ${System.currentTimeMillis()}), Tytuł: $title")
        } catch (e: SecurityException) {
            Log.e("ALARM_TEST", "BŁĄD: System zablokował dokładny alarm! Brak uprawnień USE_EXACT_ALARM", e)
            // Fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, id: Int, isTask: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createAlarmPendingIntent(context, id, "", "", isTask)
        alarmManager.cancel(pendingIntent)
        Log.d("ALARM_TEST", "Anulowano alarm dla ID: $id")
    }

    private fun createAlarmPendingIntent(
        context: Context, id: Int, title: String, message: String, isTask: Boolean
    ): PendingIntent {
        val intent = Intent(context, AppAlarmReceiver::class.java).apply {
            action = if (isTask) AppAlarmReceiver.ACTION_TASK_ALARM else AppAlarmReceiver.ACTION_CLASS_ALARM
            putExtra(AppAlarmReceiver.EXTRA_ID, id)
            putExtra(AppAlarmReceiver.EXTRA_TITLE, title)
            putExtra(AppAlarmReceiver.EXTRA_MESSAGE, message)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleClassAlarms(context: Context, classes: List<ClassEntity>) {
        val now = LocalDateTime.now()
        val limitDate = now.plusDays(7)

        var zaplanowano = 0
        classes.forEach { classItem ->
            try {
                val classDate = LocalDate.parse(classItem.date)
                val classTime = LocalTime.parse(classItem.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                val classDateTime = LocalDateTime.of(classDate, classTime)

                val notificationTime = classDateTime.minusMinutes(15)

                if (notificationTime.isAfter(now) && notificationTime.isBefore(limitDate)) {
                    val timeInMillis = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    scheduleExactAlarm(
                        context = context,
                        timeInMillis = timeInMillis,
                        id = classItem.id.hashCode(), // HASH użyty jako unikalne ID, by uniknąć kolizji
                        title = "Zajęcia za 15 minut!",
                        message = "${classItem.subjectName} · sala ${classItem.room}",
                        isTask = false
                    )
                    zaplanowano++
                }
            } catch (e: Exception) {
                // Ignore parse error
            }
        }
        Log.d("ALARM_TEST", "Odświeżanie planu: Zaplanowano $zaplanowano alarmów na najbliższe 7 dni.")
    }
}