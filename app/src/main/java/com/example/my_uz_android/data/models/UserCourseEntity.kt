package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_courses")
data class UserCourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupCode: String,          // To jest Twoje "groupId" (unikalny kod grupy z UZ)
    val fieldOfStudy: String? = null, // To jest Twoje "courseName" (np. Informatyka)
    val selectedSubgroup: String? = null, // Podgrupa wybrana przez użytkownika (np. "Lab 1")
    val colorHex: String? = null,     // Kolor przypisany do całego kierunku
    val faculty: String? = null,      // Wydział (opcjonalnie, do ładnego wyświetlania)
    val studyMode: String? = null,    // Tryb (stacjonarne/niestacjonarne)
    val semester: Int? = null         // Semestr
)