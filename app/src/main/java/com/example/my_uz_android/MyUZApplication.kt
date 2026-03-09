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
        // Tutaj inicjalizujemy nasz naprawiony DefaultAppContainer
        container = DefaultAppContainer(this)

        NotificationHelper.createNotificationChannels(this)
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