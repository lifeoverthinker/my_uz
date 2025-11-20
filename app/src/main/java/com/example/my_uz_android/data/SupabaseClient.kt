package com.example.my_uz_android.data

// Ważny import - to jest klasa wygenerowana przez Gradle
import com.example.my_uz_android.BuildConfig

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: Int = 0,
    val name: String,
)

fun provideSupabaseClient() = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
}