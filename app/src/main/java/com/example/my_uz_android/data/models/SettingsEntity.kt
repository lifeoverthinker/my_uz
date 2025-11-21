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
    val selectedSubgroup: String? = null, // Przechowujemy jako String oddzielony przecinkami lub JSON
    val isFirstRun: Boolean = true,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)