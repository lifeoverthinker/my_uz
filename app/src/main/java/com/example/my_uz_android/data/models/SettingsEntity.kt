package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val selectedGroupCode: String? = null,
    val selectedSubgroup: String? = null,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val userName: String = "Student"
)