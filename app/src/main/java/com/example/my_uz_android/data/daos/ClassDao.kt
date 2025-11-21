package com.example.my_uz_android.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE dayOfWeek = :day")
    fun getClassesForDay(day: Int): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    @Query("DELETE FROM classes")
    suspend fun clearSchedule()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)
}