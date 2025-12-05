package com.example.my_uz_android

import android.app.Application
import com.example.my_uz_android.di.AppContainer
import com.example.my_uz_android.di.DefaultAppContainer

class MyUZApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}