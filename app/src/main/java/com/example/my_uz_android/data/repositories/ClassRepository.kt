package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.db.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

class ClassRepository(private val classDao: ClassDao) {

    // Pobiera wszystkie zajęcia (Flow automatycznie odświeża UI)
    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()

    // Pobiera zajęcia dla konkretnego dnia tygodnia (1=Poniedziałek, 7=Niedziela)
    fun getClassesForDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> =
        classDao.getClassesForDay(dayOfWeek)

    // Pobiera pojedyncze zajęcia po ID
    fun getClassByIdStream(id: Int): Flow<ClassEntity?> = classDao.getClassById(id)

    // Wstawia listę zajęć (używane podczas Onboarding)
    suspend fun insertClasses(classes: List<ClassEntity>) = classDao.insertAll(classes)

    // Wstawia pojedyncze zajęcia
    suspend fun insertClass(classEntity: ClassEntity) = classDao.insert(classEntity)

    // Usuwa wszystkie zajęcia (reset planu)
    suspend fun deleteAllClasses() = classDao.deleteAll()

    // Usuwa pojedyncze zajęcia
    suspend fun deleteClass(classEntity: ClassEntity) = classDao.delete(classEntity)

    // Aktualizuje zajęcia
    suspend fun updateClass(classEntity: ClassEntity) = classDao.update(classEntity)
}
