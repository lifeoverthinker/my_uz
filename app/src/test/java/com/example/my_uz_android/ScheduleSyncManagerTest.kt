package com.example.my_uz_android.sync

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testy jednostkowe dla klasy [ScheduleSyncManager].
 * Weryfikują poprawność algorytmu wykrywania zmian w planie zajęć (odwołane, przeniesione, nowe),
 * porównującego dane lokalne (z bazy Room) z danymi z uczelni (Supabase).
 */
class ScheduleSyncManagerTest {

    // Zależności
    private lateinit var classDao: ClassDao
    private lateinit var syncManager: ScheduleSyncManager

    @Before
    fun setUp() {
        // Inicjalizacja mocka dla bazy Room (DAO)
        classDao = mockk(relaxed = true)

        // Wstrzyknięcie zmockowanego DAO do testowanej klasy
        syncManager = ScheduleSyncManager(classDao)
    }

    @Test
    fun `Scenariusz 1 - Brak zmian, nowy plan i stary plan sa identyczne`() = runTest {
        // Given
        val classId = "uuid-math-1"
        val localClass = createDummyClass(classId, "Matematyka", "10:00", "A-29")
        val remoteClass = createDummyClass(classId, "Matematyka", "10:00", "A-29")

        // Mockowanie bazy danych - zwraca listę lokalnych zajęć
        every { classDao.getAllClasses() } returns flowOf(listOf(localClass))

        // When
        val changes = syncManager.compareSchedules(remoteClasses = listOf(remoteClass))

        // Then
        assertTrue("Lista zmian powinna być pusta, gdy plany są identyczne", changes.isEmpty())
    }

    @Test
    fun `Scenariusz 2 - Zajecia odwolane, w starym planie jest Matematyka, w nowym zniknela`() = runTest {
        // Given
        val classId = "uuid-math-1"
        val localClass = createDummyClass(classId, "Matematyka", "10:00", "A-29")

        // W bazie mamy Matematykę
        every { classDao.getAllClasses() } returns flowOf(listOf(localClass))

        // When
        // Z Supabase przychodzi pusta lista (zajęcia zostały usunięte/odwołane)
        val changes = syncManager.compareSchedules(remoteClasses = emptyList())

        // Then
        assertEquals("Powinna zostać wygenerowana jedna zmiana", 1, changes.size)

        val change = changes.first()
        assertTrue("Typ zmiany to powinno być CANCELED (Odwołane)", change is ScheduleChange.Canceled)
        assertEquals("Odwołany przedmiot to Matematyka", "Matematyka", change.classEntity.subjectName)
    }

    @Test
    fun `Scenariusz 3 - Zajecia przeniesione, zmiana sali i godziny dla Fizyki`() = runTest {
        // Given
        val classId = "uuid-physics-1"
        val localClass = createDummyClass(classId, "Fizyka", "10:00", "A-29")
        val remoteClass = createDummyClass(classId, "Fizyka", "12:00", "A-30")

        // Fizyka lokalnie o 10:00 w sali A-29
        every { classDao.getAllClasses() } returns flowOf(listOf(localClass))

        // When
        // Fizyka z Supabase (ten sam supabaseId) ma inną godzinę i salę
        val changes = syncManager.compareSchedules(remoteClasses = listOf(remoteClass))

        // Then
        assertEquals("Powinna zostać wygenerowana jedna zmiana", 1, changes.size)

        val change = changes.first()
        assertTrue("Typ zmiany to powinno być MODIFIED (Przeniesione)", change is ScheduleChange.Modified)

        val modifiedChange = change as ScheduleChange.Modified
        assertEquals("Nowa sala to A-30", "A-30", modifiedChange.newClass.room)
        assertEquals("Nowa godzina to 12:00", "12:00", modifiedChange.newClass.startTime)
        assertEquals("Stara sala to A-29", "A-29", modifiedChange.oldClass.room)
    }

    // --- Helper ---

    /**
     * Metoda pomocnicza generująca obiekt [ClassEntity] zgodnie ze schematem bazy danych.
     */
    private fun createDummyClass(
        supabaseId: String,
        subjectName: String,
        startTime: String,
        room: String
    ): ClassEntity {
        return ClassEntity(
            id = 0,
            supabaseId = supabaseId,
            subjectName = subjectName,
            classType = "Wykład",
            startTime = startTime,
            endTime = "13:30",
            dayOfWeek = 1, // Poniedziałek
            date = "2026-03-16",
            groupCode = "INF-1",
            subgroup = null,
            teacherName = "Jan Kowalski",
            room = room
        )
    }
}