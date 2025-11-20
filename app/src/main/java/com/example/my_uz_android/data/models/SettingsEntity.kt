package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0, // Tylko jeden wiersz ustawień

    // Kontekst studenta (używany w zapytaniach SQL do pobierania planu):
    val selectedGroupCode: String? = null, // Np. "33INF-SSI-SP"
    val selectedSubgroup: String? = null,  // Np. "grupa laboratoryjna 1" (do filtrowania `AND zg.podgrupa`)

    // Wygląd i inne:
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val userName: String = "Student"
)