package com.example.my_uz_android

import android.app.Application
import androidx.work.*
import com.example.my_uz_android.di.AppContainer
import com.example.my_uz_android.di.DefaultAppContainer
import com.example.my_uz_android.util.NotificationHelper
import com.example.my_uz_android.util.NotificationWorker
import java.util.concurrent.TimeUnit

class MyUZApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannels(this)
        scheduleNotificationWorker()
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // działa offline
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            // ZMIANA: przy aktualizacji apki zastępujemy stary Worker nowym
            // (KEEP zostawiłoby starą konfigurację po reinstalacji)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyUZNotificationWork",
            ExistingPeriodicWorkPolicy.UPDATE,  // ZMIANA: UPDATE zamiast KEEP
            workRequest
        )
    }
}