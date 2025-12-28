package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ClassRepository(private val classDao: ClassDao) {

    // Przechowuje zajęcia "podglądane", które nie są w bazie danych
    private var temporaryClass: ClassEntity? = null

    fun setTemporaryClass(classEntity: ClassEntity) {
        temporaryClass = classEntity
    }

    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()

    fun getClassesForDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> =
        classDao.getClassesForDay(dayOfWeek)

    fun getClassByIdStream(id: Int): Flow<ClassEntity?> {
        // Jeśli ID to -1, zwracamy zajęcia z pamięci
        return if (id == -1) {
            flowOf(temporaryClass)
        } else {
            classDao.getClassById(id)
        }
    }

    suspend fun insertClasses(classes: List<ClassEntity>) = classDao.insertAll(classes)

    suspend fun insertClass(classEntity: ClassEntity) = classDao.insert(classEntity)

    suspend fun deleteAllClasses() = classDao.deleteAll()

    suspend fun deleteClass(classEntity: ClassEntity) = classDao.delete(classEntity)

    suspend fun updateClass(classEntity: ClassEntity) = classDao.update(classEntity)
}