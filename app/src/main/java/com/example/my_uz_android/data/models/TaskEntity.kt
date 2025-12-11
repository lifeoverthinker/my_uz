package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val subjectName: String? = null,  // ← NULLABLE
    val classType: String? = null,    // ← NULLABLE
    val priority: Int = 1,
    val isAllDay: Boolean = false,
    val dueDate: Long = Instant.now().toEpochMilli(),
    val dueTime: String? = null,
    val endDate: Long = Instant.now().toEpochMilli(),
    val color: Int = 0xFF68548E.toInt(),
    val isCompleted: Boolean = false,
    val subjectId: Int? = null
) {
    val classSubject: String? get() = subjectName
}
