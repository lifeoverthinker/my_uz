package com.example.my_uz_android.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_uz_android.data.models.GradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradesDao {
    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAllGrades(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE subjectName = :subjectName")
    fun getGradesForSubject(subjectName: String): Flow<List<GradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    @Delete
    suspend fun deleteGrade(grade: GradeEntity)
}