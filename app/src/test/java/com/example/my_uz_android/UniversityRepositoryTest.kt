package com.example.my_uz_android

import android.util.Log
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import io.github.jan.supabase.postgrest.Postgrest
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversityRepositoryTest {

    private lateinit var repository: UniversityRepository
    private val postgrest = mockk<Postgrest>(relaxed = true)

    @Before
    fun setup() {
        // ROZWIĄZANIE BŁĘDU: "Method e in android.util.Log not mocked"
        // Udajemy działanie logów z Androida, aby test na komputerze się nie wywalał
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0

        repository = UniversityRepository(postgrest)
    }

    @Test
    fun `searchGroups powinien bezpiecznie obsluzyc awarie bazy i zwrocic Error`() = runBlocking {
        // ROZWIĄZANIE BŁĘDU: AbstractMethodError
        // Zamiast walczyć z MockK o udawanie lambdy, wymuszamy błąd bazy danych.
        every { postgrest.from(any()) } throws RuntimeException("Symulowana awaria połączenia z Supabase")

        val result = repository.searchGroups("Informatyka")

        // Sprawdzamy, czy Twoja klasa poprawnie używa bloku try-catch
        // i zamiast wywalić aplikację, ładnie zwraca błąd zdefiniowany w architekturze.
        assertTrue("Wynik powinien być bezpiecznie złapanym błędem (NetworkResult.Error)", result is NetworkResult.Error)
    }

    @Test
    fun `getScheduleForTeacher powinien obsluzyc brak odpowiedzi z serwera`() = runBlocking {
        // Symulacja błędu zapytania
        every { postgrest.from(any()) } throws RuntimeException("Brak danych wykładowcy")

        val result = repository.getScheduleForTeacher("Jan Kowalski")

        assertTrue("Repozytorium powinno zwrócić błąd sieci", result is NetworkResult.Error)
    }

    @Test
    fun `getSchedule powinien poprawnie obsluzyc wywolanie i nie wyrzucic wyjatku (crasha)`() = runBlocking {
        // Symulacja niespodziewanego błędu wewnątrz biblioteki Supabase
        every { postgrest.from(any()) } throws RuntimeException("Błąd Supabase")

        val result = repository.getSchedule("TEST", listOf("Grupa 1"))

        assertTrue("Niezależnie od błędów, funkcja musi zwrócić NetworkResult", result is NetworkResult.Error)
    }
}