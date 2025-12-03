package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long, // Data zadania
    val dueTime: String? = null, // Godzina (np. "14:30") lub null (całodniowe)
    val isCompleted: Boolean = false,
    val subjectId: Int? = null,
    val subjectName: String = "", // Nazwa przedmiotu (dla łatwiejszego wyświetlania)
    val classType: String = "" // Rodzaj zajęć (np. Wykład, Laboratorium)
)