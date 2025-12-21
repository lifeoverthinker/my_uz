package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    // Pobiera ustawienia (zawsze jeden wiersz)
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    // Alias dla spójności
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsStream(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)

    // Alias
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun clearAll()

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}