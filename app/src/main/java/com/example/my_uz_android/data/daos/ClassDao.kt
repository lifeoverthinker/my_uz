package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO obsługujące operacje na planie zajęć w lokalnej bazie danych.
 */
@Dao
interface ClassDao {

    /**
     * Pobiera wszystkie zajęcia uporządkowane chronologicznie według dni tygodnia oraz godziny rozpoczęcia.
     */
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    /**
     * Pobiera plan zajęć przefiltrowany dla konkretnego dnia tygodnia (np. do widoku dziennego).
     */
    @Query("SELECT * FROM classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassEntity>>

    /**
     * Zwraca szczegóły pojedynczych zajęć według ich ID.
     */
    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    /**
     * Wstawia masowo zajęcia do bazy danych (wykorzystywane przy synchronizacji planu).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassEntity>)

    /**
     * Wstawia pojedyncze zajęcia do bazy danych.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    /**
     * Usuwa z bazy wszystkie zajęcia należące do określonej grupy.
     * Wykorzystywane przy odświeżaniu planu, aby usunąć nieaktualne dane przed dodaniem nowych.
     */
    @Query("DELETE FROM classes WHERE groupCode = :groupCode")
    suspend fun deleteByGroupCode(groupCode: String)

    /**
     * Czyści cały lokalny plan zajęć.
     */
    @Query("DELETE FROM classes")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Update
    suspend fun update(classEntity: ClassEntity)
}