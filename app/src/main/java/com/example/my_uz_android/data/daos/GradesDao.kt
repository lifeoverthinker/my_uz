package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.GradeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO służące do zarządzania ocenami w lokalnej bazie danych.
 */
@Dao
interface GradesDao {

    /**
     * Pobiera pełną historię ocen posortowaną od najnowszych.
     */
    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAllGrades(): Flow<List<GradeEntity>>

    /**
     * Pobiera oceny powiązane z konkretnym przedmiotem.
     * Dopasowanie odbywa się na podstawie dokładnej nazwy przedmiotu.
     */
    @Query("SELECT * FROM grades WHERE subjectName = :subjectName ORDER BY date DESC")
    fun getGradesForSubject(subjectName: String): Flow<List<GradeEntity>>

    /**
     * Pobiera pojedynczą ocenę na podstawie jej unikalnego identyfikatora.
     */
    @Query("SELECT * FROM grades WHERE id = :id")
    fun getGradeById(id: Int): Flow<GradeEntity?>

    /**
     * Zapisuje nową ocenę. W przypadku duplikatu ID, nadpisuje istniejącą.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    /**
     * Aktualizuje parametry istniejącej oceny.
     */
    @Update
    suspend fun updateGrade(grade: GradeEntity)

    /**
     * Usuwa wybraną ocenę z bazy danych.
     */
    @Delete
    suspend fun deleteGrade(grade: GradeEntity)

    /**
     * Usuwa wszystkie oceny. Przeznaczone dla mechanizmów resetowania aplikacji lub importu na czysto.
     */
    @Query("DELETE FROM grades")
    suspend fun deleteAll()
}