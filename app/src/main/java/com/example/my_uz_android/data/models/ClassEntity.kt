package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Encja reprezentująca pojedyncze zajęcia w planie zajęć.
 * * Niejawnie pełni funkcję nadrzędną dla [GradeEntity] oraz [TaskEntity] na podstawie
 * nazwy przedmiotu ([subjectName]) oraz typu zajęć ([classType]).
 * Należy uważać na spójność wielkości liter oraz białych znaków w polu [subjectName],
 * aby nie zerwać relacji z ocenami i zadaniami.
 */
@Serializable
@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supabaseId: String? = null,
    val subjectName: String,
    val classType: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: Int,
    val date: String,
    val groupCode: String,
    val subgroup: String?,
    val teacherName: String? = null,
    val teacherEmail: String? = null,
    val teacherInstitute: String? = null,
    val room: String? = null,
    val colorHex: String? = "#3D84FF"
)