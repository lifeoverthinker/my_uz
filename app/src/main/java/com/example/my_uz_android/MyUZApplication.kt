package com.example.my_uz_android

import android.app.Application
import com.example.my_uz_android.di.AppContainer
import com.example.my_uz_android.di.DefaultAppContainer
import com.example.my_uz_android.util.NotificationHelper

class MyUZApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Inicjalizacja kanałów powiadomień przy starcie aplikacji
        NotificationHelper.createNotificationChannels(this)

        // Zauważ, że usunęliśmy stąd wywołania WorkManager'a,
        // ponieważ teraz używamy bezpośrednio AlarmManager'a
    }
}