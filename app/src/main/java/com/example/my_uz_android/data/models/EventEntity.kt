package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: String,       // np. "Piątek, 4 wrz 2025"
    val timeRange: String,  // np. "18:00 - 19:00"
    val location: String,
    val description: String // np. "wstęp wolny"
)