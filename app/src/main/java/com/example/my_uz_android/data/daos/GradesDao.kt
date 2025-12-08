package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.GradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradesDao {

    // Pobiera wszystkie oceny posortowane po dacie
    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAllGrades(): Flow<List<GradeEntity>>

    // Pobiera oceny dla konkretnego przedmiotu
    @Query("SELECT * FROM grades WHERE subjectName = :subjectName ORDER BY date DESC")
    fun getGradesForSubject(subjectName: String): Flow<List<GradeEntity>>

    // ✅ DODANE: Pobiera pojedynczą ocenę po ID
    @Query("SELECT * FROM grades WHERE id = :id")
    fun getGradeById(id: Int): Flow<GradeEntity?>

    // Wstawia ocenę
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    // ✅ DODANE: Aktualizuje ocenę
    @Update
    suspend fun updateGrade(grade: GradeEntity)

    // Usuwa ocenę
    @Delete
    suspend fun deleteGrade(grade: GradeEntity)

    // Usuwa wszystkie oceny (opcjonalnie)
    @Query("DELETE FROM grades")
    suspend fun deleteAll()
}
