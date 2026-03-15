package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val subjectName: String? = null,
    val classType: String? = null,
    val priority: Int = 1,
    val isAllDay: Boolean = false,
    val dueDate: Long = Instant.now().toEpochMilli(),
    val dueTime: String? = null,
    val endDate: Long = Instant.now().toEpochMilli(),
    val color: Int = 0xFF68548E.toInt(),
    val isCompleted: Boolean = false,
    val subjectId: Int? = null
) {
    // Helper dla kompatybilności
    val classSubject: String? get() = subjectName
}