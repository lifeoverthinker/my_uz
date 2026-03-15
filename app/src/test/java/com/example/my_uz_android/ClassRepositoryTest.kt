package com.example.my_uz_android

import android.util.Log
import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class ClassRepositoryTest {

    private lateinit var classDao: ClassDao
    private lateinit var classRepository: ClassRepository

    @Before
    fun setUp() {
        classDao = mockk()
        classRepository = ClassRepository(classDao)

        // Pozwala zamockować obecny czas
        mockkStatic(LocalDateTime::class)

        // Mockujemy klasę Log z Androida, aby testy nie "wybuchały" przy próbie logowania
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getUpcomingClasses o 12_00 powinno zwrocic tylko zajecia popoludniowe z dzisiaj`() = runTest {
        // Mockujemy czas: dzisiaj jest 15 Marca 2026, godzina 12:00
        val mockNow = LocalDateTime.of(2026, 3, 15, 12, 0)
        every { LocalDateTime.now() } returns mockNow

        val fakeClasses = listOf(
            createClass(1, "2026-03-15", "08:00", "09:30"), // Zakończone
            createClass(2, "2026-03-15", "10:00", "11:30"), // Zakończone
            createClass(3, "2026-03-15", "11:45", "13:15"), // Trwające (koniec 13:15) -> powinno zwrócić
            createClass(4, "2026-03-15", "14:00", "15:30"), // Przed nami -> powinno zwrócić
            createClass(5, "2026-03-16", "08:00", "09:30")  // Jutro -> ignorujemy, bo dziś są zajęcia
        )

        every { classDao.getAllClasses() } returns flowOf(fakeClasses)

        val result = classRepository.getUpcomingClasses().first()

        assertEquals(2, result.size)
        assertEquals(3, result[0].id)
        assertEquals(4, result[1].id)
    }

    @Test
    fun `getUpcomingClasses o 21_00 powinno zwrocic zajecia z kolejnego dnia`() = runTest {
        // Mockujemy czas: dzisiaj jest 15 Marca 2026, godzina 21:00 (po wszystkich zajęciach)
        val mockNow = LocalDateTime.of(2026, 3, 15, 21, 0)
        every { LocalDateTime.now() } returns mockNow

        val fakeClasses = listOf(
            createClass(1, "2026-03-15", "08:00", "09:30"), // Zakończone
            createClass(2, "2026-03-15", "14:00", "15:30"), // Zakończone
            createClass(3, "2026-03-17", "08:00", "09:30"), // Pojutrze (kolejny dostępny dzień!)
            createClass(4, "2026-03-17", "10:00", "11:30")  // Pojutrze
        )

        every { classDao.getAllClasses() } returns flowOf(fakeClasses)

        val result = classRepository.getUpcomingClasses().first()

        assertEquals(2, result.size)
        assertEquals(3, result[0].id)
        assertEquals("2026-03-17", result[0].date)
    }

    // Funkcja pomocnicza uzupełniona o wymagane parametry
    private fun createClass(id: Int, date: String, start: String, end: String): ClassEntity {
        return ClassEntity(
            id = id,
            date = date,
            startTime = start,
            endTime = end,
            subjectName = "Test",
            classType = "W",
            room = "A1",
            groupCode = "G1",
            teacherName = "Dr Test",
            dayOfWeek = 1,
            subgroup = "Grupa ogólna"
        )
    }
}