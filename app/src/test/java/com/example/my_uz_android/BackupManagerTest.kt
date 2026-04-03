package com.example.my_uz_android.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupManagerTest {

    @Test
    fun `test BackupData serializes and deserializes properly to identical object`() {
        val json = Json { ignoreUnknownKeys = true }

        // Tworzymy przykładowy obiekt BackupData (puste listy wystarczą,
        // by potwierdzić prawidłowe działanie struktury i opcjonalnych nulli)
        val originalData = BackupData(
            classes = emptyList(),
            tasks = emptyList(),
            grades = emptyList(),
            settings = null, // Testujemy obsługę nulla w SettingsEntity
            favorites = emptyList()
        )

        // Wykonujemy serializację
        val serializedJsonString = json.encodeToString(originalData)

        // Wykonujemy deserializację
        val deserializedData = json.decodeFromString<BackupData>(serializedJsonString)

        // Porównujemy oba obiekty
        assertEquals(
            "Zdeserializowany obiekt powinien być dokładnie taki sam jak oryginalny",
            originalData,
            deserializedData
        )
    }
}