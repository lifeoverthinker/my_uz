package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO odpowiadające za dostęp do ustawień aplikacji.
 * Tabela settings z założenia powinna zawierać maksymalnie jeden wiersz.
 */
@Dao
interface SettingsDao {

    /**
     * Pobiera aktualne ustawienia aplikacji jako reaktywny strumień danych.
     */
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsStream(): Flow<SettingsEntity?>

    /**
     * Wstawia nowe ustawienia lub nadpisuje istniejące, jeśli wystąpi konflikt klucza głównego.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)

    /**
     * Całkowicie czyści tabelę ustawień (przydatne np. przy wylogowywaniu lub czyszczeniu danych).
     */
    @Query("DELETE FROM settings")
    suspend fun clearAll()
}