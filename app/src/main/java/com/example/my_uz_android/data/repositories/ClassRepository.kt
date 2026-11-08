package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ClassRepository(private val classDao: ClassDao) {

    private var temporaryClass: ClassEntity? = null

    fun setTemporaryClass(classEntity: ClassEntity) {
        temporaryClass = classEntity
    }

    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()
        .catch { e ->
            Log.e("ClassRepository", "Błąd getAllClasses", e)
            emit(emptyList())
        }

    fun getClassesForDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> = classDao.getClassesForDay(dayOfWeek)
        .catch { e ->
            Log.e("ClassRepository", "Błąd getClassesForDayStream", e)
            emit(emptyList())
        }

    fun getClassByIdStream(id: Int): Flow<ClassEntity?> {
        return if (id == -1) {
            flowOf(temporaryClass)
        } else {
            classDao.getClassById(id)
                .catch { e ->
                    Log.e("ClassRepository", "Błąd getClassByIdStream", e)
                    emit(null)
                }
        }
    }

    suspend fun getClassesByGroupCode(groupCode: String): List<ClassEntity> {
        return try {
            classDao.getClassesByGroupCode(groupCode.trim())
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd getClassesByGroupCode", e)
            emptyList()
        }
    }

    suspend fun deleteClassesForGroup(groupCode: String): List<ClassEntity> {
        return try {
            val trimmedCode = groupCode.trim()
            val classesToDelete = classDao.getClassesByGroupCode(trimmedCode)
            classDao.deleteByGroupCode(trimmedCode)
            Log.d("ClassRepository", "Usunięto zajęcia dla grupy $trimmedCode: ${classesToDelete.size}")
            classesToDelete
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd deleteClassesForGroup", e)
            emptyList()
        }
    }

    suspend fun deleteOrphanedClasses(validGroupCodes: Set<String>): List<ClassEntity> {
        return try {
            val validLower = validGroupCodes.map { it.trim().lowercase() }.toSet()
            val allClasses = classDao.getAllClasses().first()
            val orphaned = allClasses.filter { it.groupCode.trim().lowercase() !in validLower }
            orphaned.forEach { classItem ->
                classDao.delete(classItem)
            }
            if (orphaned.isNotEmpty()) {
                Log.d("ClassRepository", "Usunięto osierocone zajęcia: ${orphaned.size}")
            }
            orphaned
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd deleteOrphanedClasses", e)
            emptyList()
        }
    }

    suspend fun updateClasses(classes: List<ClassEntity>) {
        try {
            classDao.deleteAll()
            if (classes.isNotEmpty()) {
                classDao.insertAll(classes)
            }
            Log.d("ClassRepository", "Zaktualizowano bazę zajęć. Liczba: ${classes.size}")
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd updateClasses", e)
        }
    }

    suspend fun syncGroupClasses(groupCode: String, newClasses: List<ClassEntity>) {
        try {
            val deduplicated = newClasses.distinctBy {
                listOf(
                    it.supabaseId ?: "",
                    it.groupCode.trim().lowercase(),
                    it.date,
                    it.startTime,
                    it.endTime,
                    it.subjectName.trim().lowercase(),
                    it.classType.trim().lowercase(),
                    it.subgroup?.trim()?.lowercase().orEmpty(),
                    it.room?.trim()?.lowercase().orEmpty()
                ).joinToString("|")
            }
            classDao.replaceGroupClasses(groupCode, deduplicated)
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd syncGroupClasses", e)
        }
    }

    suspend fun insertClasses(classes: List<ClassEntity>) {
        try { classDao.insertAll(classes) } catch (_: Exception) { }
    }

    suspend fun insertClass(classEntity: ClassEntity) {
        try { classDao.insertClass(classEntity) } catch (_: Exception) { }
    }

    suspend fun deleteAllClasses() {
        try { classDao.deleteAll() } catch (_: Exception) { }
    }

    suspend fun deleteClass(classEntity: ClassEntity) {
        try { classDao.delete(classEntity) } catch (_: Exception) { }
    }

    suspend fun updateClass(classEntity: ClassEntity) {
        try { classDao.update(classEntity) } catch (_: Exception) { }
    }

    fun getUpcomingClasses(limit: Int = Int.MAX_VALUE): Flow<List<ClassEntity>> {
        return getAllClassesStream().map { classes ->
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            classes
                .mapNotNull { classItem ->
                    try {
                        val classDate = LocalDate.parse(classItem.date)
                        val endTime = LocalTime.parse(classItem.endTime)
                        val endDateTime = LocalDateTime.of(classDate, endTime)
                        if (endDateTime.isAfter(now)) classItem to endDateTime else null
                    } catch (_: Exception) {
                        null
                    }
                }
                .sortedBy { it.second }
                .map { it.first }
                .take(limit)
        }
    }
}