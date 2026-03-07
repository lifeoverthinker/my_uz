package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,

    // Dane użytkownika
    val isAnonymous: Boolean = false,
    val userName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,

    // Plan zajęć i Uczelnia
    val selectedGroupCode: String? = null,
    val selectedGroupName: String? = null,
    val activeDirectionCode: String? = null,
    val selectedSubgroup: String? = null,
    val faculty: String? = null,
    val department: String? = null,
    val fieldOfStudy: String? = null,
    val studyMode: String? = null,
    val currentSemester: Int = 1,

    // Multi-kierunek (Dodatkowe kierunki i stan dla indeksu)
    val additionalGroupCodes: String = "",
    val activeIndexDirectionCode: String? = null,

    // Wygląd i Stan aplikacji
    val isFirstRun: Boolean = true,
    val isDarkMode: Boolean = false,
    val offlineModeEnabled: Boolean = false,
    val classColorsJson: String = "{}",

    // Powiadomienia
    val notificationsEnabled: Boolean = true,
    val notificationsTasks: Boolean = true,
    val notificationsClasses: Boolean = true
)