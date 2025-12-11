package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val isAnonymous: Boolean = false,
    val userName: String = "Student",
    val gender: String? = null, // "STUDENT" lub "STUDENTKA"

    // Uczelnia
    val selectedGroupCode: String? = null,
    val selectedSubgroup: String? = null, // np. "L1,P2"
    val faculty: String? = null,
    val fieldOfStudy: String? = null,
    val studyMode: String? = null, // np. "stacjonarne"

    // ✅ NOWE: Globalny licznik semestru
    val currentSemester: Int = 1,

    // App State
    val isFirstRun: Boolean = true,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)