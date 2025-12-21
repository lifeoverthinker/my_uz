package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun getSettingsStream(): Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun insertSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    // Alias dla wstecznej kompatybilności
    suspend fun insertOrUpdate(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    suspend fun clearSettings() = settingsDao.clearAll()

    // Alias
    suspend fun deleteAllSettings() = settingsDao.clearAll()
}