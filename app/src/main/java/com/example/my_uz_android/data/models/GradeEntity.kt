package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val subjectName: String,      // Np. "Analiza Matematyczna"
    val grade: Double,            // Np. 3.5
    val weight: Int = 1,          // Waga oceny
    val description: String?,     // Np. "Kolokwium 1"
    val date: Long,
    val semester: Int = 1
)