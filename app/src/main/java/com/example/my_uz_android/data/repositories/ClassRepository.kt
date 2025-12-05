package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

class ClassRepository(private val classDao: ClassDao) {

    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()

    fun getAllClasses(): Flow<List<ClassEntity>> = classDao.getAllClasses()  // DODANE

    fun getClassByIdStream(id: Int): Flow<ClassEntity?> = classDao.getClassById(id)

    fun getClassesByDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> = classDao.getClassesByDay(dayOfWeek)

    suspend fun getClassById(id: Int): ClassEntity? = classDao.getClassByIdSuspend(id)

    suspend fun insertClass(classEntity: ClassEntity) = classDao.insert(classEntity)

    suspend fun insertClasses(classes: List<ClassEntity>) = classDao.insertAll(classes)

    suspend fun updateClass(classEntity: ClassEntity) = classDao.update(classEntity)

    suspend fun deleteClass(classEntity: ClassEntity) = classDao.delete(classEntity)

    suspend fun deleteAllClasses() = classDao.deleteAll()
}
