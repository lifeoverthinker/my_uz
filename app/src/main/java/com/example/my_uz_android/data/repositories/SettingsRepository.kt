package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    // Pobiera ustawienia jako Flow (auto-update UI)
    fun getSettingsStream(): Flow<SettingsEntity?> = settingsDao.getSettings()

    // Wstawia lub aktualizuje ustawienia (używa insertOrUpdate z DAO)
    suspend fun insertSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    // Aktualizuje ustawienia (również używa insertOrUpdate)
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    // Usuwa wszystkie ustawienia
    suspend fun clearSettings() = settingsDao.clearAll()
}
