package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectName: String,
    val classType: String = "",
    val grade: Double,
    val weight: Int = 1,
    val description: String?,
    val comment: String? = null,
    val date: Long,
    val semester: Int = 1
)