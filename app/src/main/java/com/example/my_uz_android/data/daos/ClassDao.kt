package com.example.my_uz_android.data.db

import androidx.room.*
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    // ✅ Pobiera wszystkie zajęcia posortowane po dniu i godzinie
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    // ✅ Pobiera zajęcia dla konkretnego dnia (1-7)
    @Query("SELECT * FROM classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassEntity>>

    // ✅ Pobiera pojedyncze zajęcia po ID
    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Int): Flow<ClassEntity?>

    // ✅ Wstawia listę zajęć (REPLACE = nadpisuje duplikaty)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassEntity>)

    // ✅ Wstawia pojedyncze zajęcia
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classEntity: ClassEntity)

    // ✅ Usuwa wszystkie zajęcia
    @Query("DELETE FROM classes")
    suspend fun deleteAll()

    // ✅ Usuwa pojedyncze zajęcia
    @Delete
    suspend fun delete(classEntity: ClassEntity)

    // ✅ Aktualizuje zajęcia
    @Update
    suspend fun update(classEntity: ClassEntity)
}
