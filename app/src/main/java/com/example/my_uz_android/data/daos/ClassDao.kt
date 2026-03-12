package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    // TA FUNKCJA WRÓCIŁA, BY EKRAN GŁÓWNY MÓGŁ POBIERAĆ ZAJĘCIA NA DZIŚ
    @Query("SELECT * FROM classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassEntity>)

    // TA FUNKCJA WRÓCIŁA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE groupCode = :groupCode")
    suspend fun deleteByGroupCode(groupCode: String)

    @Query("DELETE FROM classes")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Update
    suspend fun update(classEntity: ClassEntity)
}