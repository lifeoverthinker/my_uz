package com.example.my_uz_android.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupManagerTest {

    @Test
    fun `test BackupData serializes and deserializes properly to identical object`() {
        val json = Json { ignoreUnknownKeys = true }

        val originalData = BackupData(
            classes = emptyList(),
            tasks = emptyList(),
            grades = emptyList(),
            settings = null,
            favorites = emptyList()
        )

        val serializedJsonString = json.encodeToString(originalData)

        val deserializedData = json.decodeFromString<BackupData>(serializedJsonString)

        assertEquals(
            "Zdeserializowany obiekt powinien być dokładnie taki sam jak oryginalny",
            originalData,
            deserializedData
        )
    }
}