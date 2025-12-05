package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isAnonymous: Boolean = false,
    val userName: String = "Student",
    val gender: String? = null, // "STUDENT" lub "STUDENTKA"
    val selectedGroupCode: String? = null,
    val selectedSubgroup: String? = null,
    val faculty: String? = null,
    val fieldOfStudy: String? = null,
    val studyMode: String? = null,
    val isFirstRun: Boolean = true,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
) {
    // ✅ Clean Code: Stałe zamiast "magic strings"
    companion object {
        const val GENDER_MALE = "STUDENT"
        const val GENDER_FEMALE = "STUDENTKA"
    }
}