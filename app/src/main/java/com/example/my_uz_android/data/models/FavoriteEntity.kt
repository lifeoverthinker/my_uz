package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // np. "32INF-SP" lub "Dr Jan Kowalski"
    val type: String, // Używamy wartości z ScheduleType (np. "GROUP", "TEACHER")
    val code: String  // Unikalny identyfikator do pobierania planu
)

object ScheduleType {
    const val GROUP = "GROUP"
    const val TEACHER = "TEACHER"
}