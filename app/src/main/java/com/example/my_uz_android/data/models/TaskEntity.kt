package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: String, // Format ISO: "2023-10-27"
    val isCompleted: Boolean = false,
    val subject: String? = null
)