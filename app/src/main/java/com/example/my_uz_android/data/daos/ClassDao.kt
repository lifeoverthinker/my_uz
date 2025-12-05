package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTime")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    @Query("SELECT * FROM classes WHERE id = :id")  // DODANE
    suspend fun getClassByIdSuspend(id: Int): ClassEntity?

    @Query("SELECT * FROM classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getClassesByDay(dayOfWeek: Int): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classEntity: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassEntity>)

    @Update
    suspend fun update(classEntity: ClassEntity)

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Query("DELETE FROM classes")
    suspend fun deleteAll()
}
