package com.example.my_uz_android.data.daos

import androidx.room.*
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    @Query("SELECT * FROM classes WHERE LOWER(TRIM(groupCode)) = LOWER(TRIM(:groupCode))")
    suspend fun getClassesByGroupCode(groupCode: String): List<ClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE LOWER(TRIM(groupCode)) = LOWER(TRIM(:groupCode))")
    suspend fun deleteByGroupCode(groupCode: String)

    @Transaction
    suspend fun replaceGroupClasses(groupCode: String, classes: List<ClassEntity>) {
        deleteByGroupCode(groupCode)
        if (classes.isNotEmpty()) {
            insertAll(classes)
        }
    }

    @Query("DELETE FROM classes")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Update
    suspend fun update(classEntity: ClassEntity)
}