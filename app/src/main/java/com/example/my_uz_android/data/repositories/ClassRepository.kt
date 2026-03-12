package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ClassRepository(private val classDao: ClassDao) {

    private var temporaryClass: ClassEntity? = null

    fun setTemporaryClass(classEntity: ClassEntity) {
        temporaryClass = classEntity
    }

    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()

    fun getClassesForDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> =
        classDao.getClassesForDay(dayOfWeek)

    fun getClassByIdStream(id: Int): Flow<ClassEntity?> {
        return if (id == -1) {
            flowOf(temporaryClass)
        } else {
            classDao.getClassById(id)
        }
    }

    suspend fun updateClasses(classes: List<ClassEntity>) {
        classDao.deleteAll()
        classDao.insertAll(classes)
    }

    suspend fun syncGroupClasses(groupCode: String, newClasses: List<ClassEntity>) {
        classDao.deleteByGroupCode(groupCode)
        classDao.insertAll(newClasses)
    }

    suspend fun insertClasses(classes: List<ClassEntity>) = classDao.insertAll(classes)
    suspend fun insertClass(classEntity: ClassEntity) = classDao.insertClass(classEntity)
    suspend fun deleteAllClasses() = classDao.deleteAll()
    suspend fun deleteClass(classEntity: ClassEntity) = classDao.delete(classEntity)
    suspend fun updateClass(classEntity: ClassEntity) = classDao.update(classEntity)
}