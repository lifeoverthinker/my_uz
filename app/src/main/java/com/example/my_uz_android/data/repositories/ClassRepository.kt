package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow

class ClassRepository(private val classDao: ClassDao) {
    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()
    fun getClassesForDay(day: Int): Flow<List<ClassEntity>> = classDao.getClassesForDay(day)
    suspend fun refreshSchedule(classes: List<ClassEntity>) {
        classDao.clearSchedule()
        classDao.insertClasses(classes)
    }
}