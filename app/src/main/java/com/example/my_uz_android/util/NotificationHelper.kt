package com.example.my_uz_android.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.my_uz_android.MainActivity
import com.example.my_uz_android.R

object NotificationHelper {
    private const val CHANNEL_ID = "my_uz_notifications"
    private const val CHANNEL_NAME = "Powiadomienia i Zmiany MyUZ"

    // Tworzy kanał (wymagane od Androida 8.0 Oreo)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Ważne powiadomienia dotyczące zajęć i zadań"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Główna funkcja do pokazywania powiadomień na górnym pasku telefonu
    fun showNotification(context: Context, title: String, message: String, isTask: Boolean) {
        // Blokada dla Android 13+ jeśli użytkownik nie dał zgody na powiadomienia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        createNotificationChannel(context)

        // Intencja uruchamiająca aplikację po kliknięciu powiadomienia
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Wybór ikonki na podstawie tego czy powiadomienie dotyczy zadań czy planu
        val iconRes = if (isTask) R.drawable.ic_book_open else R.drawable.ic_marker_pin

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes) // Ważne: to musi być ikona jednokolorowa (wektor) z przezroczystym tłem!
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Rozwija długi tekst
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Usuwa z paska po kliknięciu
            .setContentIntent(pendingIntent) // Przekierowanie do MainActivity

        // Wyświetlenie
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}