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
        // Inicjalizacja mocka i wstrzyknięcie go do repozytorium
        settingsDao = mockk()
        settingsRepository = SettingsRepository(settingsDao)
    }

    @Test
    fun `when profile data is updated, repository should call dao insertOrUpdate`() = runTest {
        // Given
        val updatedSettings = SettingsEntity(
            id = 1,
            faculty = "Wydział Informatyki, Elektrotechniki i Automatyki",
            selectedGroupCode = "11-INF-DZ",
            selectedGroupName = "Informatyka - grupa 1",
            themeMode = ThemeMode.DARK.name,
            isDarkMode = true,
            classColorsJson = "{}"
        )

        // Definiujemy zachowanie mocka dla wywołania suspend wewnątrz DAO
        coEvery { settingsDao.insertOrUpdate(updatedSettings) } returns Unit

        // When
        // POPRAWKA: Używamy prawidłowej nazwy funkcji z SettingsRepository
        settingsRepository.insertSettings(updatedSettings)

        // Then
        // Weryfikujemy, czy dao.insertOrUpdate zostało wywołane dokładnie raz z odpowiednim obiektem
        coVerify(exactly = 1) { settingsDao.insertOrUpdate(updatedSettings) }
    }

    @Test
    fun `when serializing map of colors to json, output format is valid and deserialization restores original map`() {
        // Given - mapa kolorów przedmiotów (używamy Long dla hexów, aby uniknąć problemów ze znakiem przy ARGB np. 0xFFFFFFFF)
        val originalColorMap = mapOf(
            "Matematyka" to 0xFF0000L, // Czerwony
            "Programowanie" to 0x00FF00L // Zielony
        )

        // When
        // Zamiana mapy na string JSON
        val jsonString = Json.encodeToString(originalColorMap)

        // Ponowna deserializacja stringa JSON do mapy
        val deserializedMap = Json.decodeFromString<Map<String, Long>>(jsonString)

        // Then
        // Sprawdzamy czy JSON zawiera poprawne klucze i wartości
        // (0xFF0000 -> 16711680 dziesiętnie, 0x00FF00 -> 65280 dziesiętnie)
        assertTrue(jsonString.contains(""""Matematyka":16711680"""))
        assertTrue(jsonString.contains(""""Programowanie":65280"""))

        // Sprawdzamy czy zdeserializowana mapa jest identyczna z pierwotną
        assertEquals(originalColorMap, deserializedMap)
    }

    @Test
    fun `when deserializing empty json, should return empty map`() {
        // Given - domyślny pusty JSON z SettingsEntity
        val emptyJson = "{}"

        // When
        val deserializedMap = Json.decodeFromString<Map<String, Long>>(emptyJson)

        // Then
        assertTrue(deserializedMap.isEmpty())
    }

    @Test
    fun `when clearing settings, repository should call dao clearAll`() = runTest {
        // Given
        coEvery { settingsDao.clearAll() } returns Unit

        // When
        settingsRepository.clearSettings()

        // Then
        coVerify(exactly = 1) { settingsDao.clearAll() }
    }
}