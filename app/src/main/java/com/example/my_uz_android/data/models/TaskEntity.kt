package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,
    val subjectName: String?,     // Aby powiązać zadanie z przedmiotem z planu
    val dueDate: Long,            // Timestamp
    val isCompleted: Boolean = false,
    val priority: Int = 1         // 1=Low, 2=Medium, 3=High
)