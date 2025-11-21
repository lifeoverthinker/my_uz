package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.SettingsDao
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    fun getSettingsStream(): Flow<SettingsEntity?> = settingsDao.getSettings()

    // Zmieniono nazwę na insertSettings, aby pasowała do ViewModelu,
    // lub można zmienić w ViewModelu na saveSettings. Tutaj dodaję alias dla wygody.
    suspend fun insertSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    suspend fun saveSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)
}