package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class ClassRepository(private val classDao: ClassDao) {

    private var temporaryClass: ClassEntity? = null

    fun setTemporaryClass(classEntity: ClassEntity) {
        temporaryClass = classEntity
    }

    // NAPRAWA KRYTYCZNA: Dodano ochronę przepływu. Jeśli zapytanie do bazy wywoła błąd,
    // flow złapie wyjątek i wyemituje pustą listę zamiast przerwać cały potok UI w ViewModelu.
    fun getAllClassesStream(): Flow<List<ClassEntity>> = classDao.getAllClasses()
        .onStart { Log.d("ClassRepository", "Rozpoczynam nasłuchiwanie strumienia klas z DB") }
        .onEach { Log.d("ClassRepository", "Baza danych wyemitowała: ${it.size} klas") }
        .catch { e ->
            Log.e("ClassRepository", "Wystąpił błąd podczas nasłuchiwania getAllClasses", e)
            emit(emptyList())
        }

    fun getClassesForDayStream(dayOfWeek: Int): Flow<List<ClassEntity>> = classDao.getClassesForDay(dayOfWeek)
        .catch { e ->
            Log.e("ClassRepository", "Wystąpił błąd dla getClassesForDayStream", e)
            emit(emptyList())
        }

    fun getClassByIdStream(id: Int): Flow<ClassEntity?> {
        return if (id == -1) {
            flowOf(temporaryClass)
        } else {
            classDao.getClassById(id)
                .catch { e ->
                    Log.e("ClassRepository", "Błąd podczas getClassByIdStream", e)
                    emit(null)
                }
        }
    }

    suspend fun updateClasses(classes: List<ClassEntity>) {
        try {
            classDao.deleteAll()
            classDao.insertAll(classes)
            Log.d("ClassRepository", "Zaktualizowano klasy. Nowa liczba to: ${classes.size}")
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd podczas updateClasses", e)
        }
    }

    suspend fun syncGroupClasses(groupCode: String, newClasses: List<ClassEntity>) {
        try {
            classDao.deleteByGroupCode(groupCode)
            classDao.insertAll(newClasses)
            Log.d("ClassRepository", "Zsynchronizowano grupę: $groupCode. Wstawiono: ${newClasses.size} zajęć.")
        } catch (e: Exception) {
            Log.e("ClassRepository", "Błąd podczas syncGroupClasses dla kodu $groupCode", e)
        }
    }

    suspend fun insertClasses(classes: List<ClassEntity>) {
        try { classDao.insertAll(classes) }
        catch (e: Exception) { Log.e("ClassRepository", "Błąd insertClasses", e) }
    }

    suspend fun insertClass(classEntity: ClassEntity) {
        try { classDao.insertClass(classEntity) }
        catch (e: Exception) { Log.e("ClassRepository", "Błąd insertClass", e) }
    }

    suspend fun deleteAllClasses() {
        try { classDao.deleteAll() }
        catch (e: Exception) { Log.e("ClassRepository", "Błąd deleteAllClasses", e) }
    }

    suspend fun deleteClass(classEntity: ClassEntity) {
        try { classDao.delete(classEntity) }
        catch (e: Exception) { Log.e("ClassRepository", "Błąd deleteClass", e) }
    }

    suspend fun updateClass(classEntity: ClassEntity) {
        try { classDao.update(classEntity) }
        catch (e: Exception) { Log.e("ClassRepository", "Błąd updateClass", e) }
    }
}