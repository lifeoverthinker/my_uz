package com.example.my_uz_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.my_uz_android.R

object NotificationHelper {
    private const val CHANNEL_CLASSES_ID = "upcoming_classes"
    private const val CHANNEL_TASKS_ID = "task_deadlines"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameClasses = "Nadchodzące zajęcia"
            val descClasses = "Powiadomienia o zbliżających się wykładach i ćwiczeniach"
            val channelClasses = NotificationChannel(
                CHANNEL_CLASSES_ID, nameClasses, NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = descClasses }

            val nameTasks = "Terminy zadań"
            val descTasks = "Przypomnienia o kończących się terminach zadań"
            val channelTasks = NotificationChannel(
                CHANNEL_TASKS_ID, nameTasks, NotificationManager.IMPORTANCE_HIGH
            ).apply { description = descTasks }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channelClasses)
            manager.createNotificationChannel(channelTasks)
        }
    }

    fun showNotification(context: Context, title: String, message: String, isTask: Boolean) {
        val channelId = if (isTask) CHANNEL_TASKS_ID else CHANNEL_CLASSES_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}