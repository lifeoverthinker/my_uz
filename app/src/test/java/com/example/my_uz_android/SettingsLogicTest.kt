package com.example.my_uz_android

import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.ThemeMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsLogicTest {

    private lateinit var settingsDao: SettingsDao
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        settingsDao = mockk()
        settingsRepository = SettingsRepository(settingsDao)
    }

    @Test
    fun `when profile data is updated, repository should call dao insertOrUpdate`() = runTest {
        val updatedSettings = SettingsEntity(
            id = 1,
            faculty = "Wydział Informatyki, Elektrotechniki i Automatyki",
            selectedGroupCode = "11-INF-DZ",
            selectedGroupName = "Informatyka - grupa 1",
            themeMode = ThemeMode.DARK.name,
            isDarkMode = true,
            classColorsJson = "{}"
        )

        coEvery { settingsDao.insertOrUpdate(updatedSettings) } returns Unit

        settingsRepository.insertSettings(updatedSettings)

        coVerify(exactly = 1) { settingsDao.insertOrUpdate(updatedSettings) }
    }

    @Test
    fun `when serializing map of colors to json, output format is valid and deserialization restores original map`() {
        val originalColorMap = mapOf(
            "Matematyka" to 0xFF0000L,
            "Programowanie" to 0x00FF00L
        )

        val jsonString = Json.encodeToString(originalColorMap)

        val deserializedMap = Json.decodeFromString<Map<String, Long>>(jsonString)

        assertTrue(jsonString.contains(""""Matematyka":16711680"""))
        assertTrue(jsonString.contains(""""Programowanie":65280"""))

        assertEquals(originalColorMap, deserializedMap)
    }

    @Test
    fun `when deserializing empty json, should return empty map`() {
        val emptyJson = "{}"

        val deserializedMap = Json.decodeFromString<Map<String, Long>>(emptyJson)

        assertTrue(deserializedMap.isEmpty())
    }

    @Test
    fun `when clearing settings, repository should call dao clearAll`() = runTest {
        coEvery { settingsDao.clearAll() } returns Unit

        settingsRepository.clearSettings()

        coVerify(exactly = 1) { settingsDao.clearAll() }
    }
}