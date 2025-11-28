package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val subjectId: Int? = null // ID powiązanego przedmiotu (np. Matematyka)
)