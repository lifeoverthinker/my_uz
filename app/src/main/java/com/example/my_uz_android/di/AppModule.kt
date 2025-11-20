package com.example.my_uz_android.di

import com.example.my_uz_android.data.provideSupabaseClient

object AppModule {
    // Singleton klienta Supabase
    val supabase by lazy {
        provideSupabaseClient()
    }
}