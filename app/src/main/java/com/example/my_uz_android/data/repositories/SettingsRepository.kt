package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repozytorium zarządzające dostępem do ustawień aplikacji.
 * Izoluje warstwę UI od bezpośrednich wywołań bazy danych.
 */
class SettingsRepository(private val settingsDao: SettingsDao) {

    /**
     * Obserwuje zmiany w ustawieniach aplikacji.
     */
    fun getSettingsStream(): Flow<SettingsEntity?> = settingsDao.getSettingsStream()

    /**
     * Zapisuje nowy stan ustawień.
     */
    suspend fun insertSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    /**
     * Trwale usuwa wszystkie ustawienia użytkownika z urządzenia.
     */
    suspend fun clearSettings() = settingsDao.clearAll()
}