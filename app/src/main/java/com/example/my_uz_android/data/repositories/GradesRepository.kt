package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.GradesDao
import com.example.my_uz_android.data.models.GradeEntity
import kotlinx.coroutines.flow.Flow

class GradesRepository(private val gradesDao: GradesDao) {
    fun getAllGradesStream(): Flow<List<GradeEntity>> = gradesDao.getAllGrades()
    fun getGradesForSubjectStream(subject: String): Flow<List<GradeEntity>> =
        gradesDao.getGradesForSubject(subject)
    suspend fun insertGrade(grade: GradeEntity) = gradesDao.insertGrade(grade)
    suspend fun deleteGrade(grade: GradeEntity) = gradesDao.deleteGrade(grade)
}