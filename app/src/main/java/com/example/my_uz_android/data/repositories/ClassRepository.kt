package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    fun getUpcomingClasses(limit: Int = Int.MAX_VALUE): Flow<List<ClassEntity>> {
        return getAllClassesStream().map { classes ->
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            // 1. Odrzucamy przeszłe zajęcia
            val upcoming = classes.filter { classItem ->
                try {
                    val classDate = LocalDate.parse(classItem.date) // Format: yyyy-MM-dd
                    if (classDate.isBefore(today)) {
                        false // Minione dni
                    } else if (classDate.isEqual(today)) {
                        val endTime = LocalTime.parse(classItem.endTime)
                        val endDateTime = LocalDateTime.of(today, endTime)
                        endDateTime.isAfter(now) // Zwraca true, jeśli zajęcia wciąż trwają lub są przed nami
                    } else {
                        true // Przyszłe dni
                    }
                } catch (e: Exception) {
                    false // W razie błędu parsowania ignorujemy wpis
                }
            }

            // 2. Szukamy najbliższej daty z dostępnymi zajęciami
            val nextDate = upcoming.mapNotNull {
                try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            }.minOrNull()

            // 3. Zwracamy zajęcia z tego najbliższego dnia (posortowane po czasie)
            if (nextDate != null) {
                upcoming.filter { it.date == nextDate.toString() }
                    .sortedBy { it.startTime }
                    .take(limit)
            } else {
                emptyList()
            }
        }
    }
}