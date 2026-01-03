package com.example.my_uz_android

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.my_uz_android.di.AppContainer
import com.example.my_uz_android.di.DefaultAppContainer
import com.example.my_uz_android.util.NotificationHelper
import com.example.my_uz_android.util.NotificationWorker
import java.util.concurrent.TimeUnit

class MyUZApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Zmieniono na DefaultAppContainer zgodnie z Twoim projektem
        container = DefaultAppContainer(this)

        // 1. Tworzymy kanały powiadomień (wymagane od Android 8.0)
        NotificationHelper.createNotificationChannels(this)

        // 2. Rejestrujemy Workera sprawdzającego terminy co 15 minut
        setupNotificationWork()
    }

    private fun setupNotificationWork() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyUZNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}