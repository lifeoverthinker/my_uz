package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val userName: String = "Student",
    val isAnonymous: Boolean = false,
    val selectedGroupCode: String? = null,
    val selectedSubgroup: String? = null,
    // Nowe pola
    val faculty: String? = null,        // Wydział
    val fieldOfStudy: String? = null,   // Kierunek
    val studyMode: String? = null,      // Tryb (stacjonarne itp.)

    val isFirstRun: Boolean = true,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)