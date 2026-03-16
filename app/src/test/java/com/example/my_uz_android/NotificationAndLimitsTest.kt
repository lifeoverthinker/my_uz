package com.example.my_uz_android.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime

class NotificationAndLimitsTest {

    // --- Symulacja utilsa do obliczania opóźnienia ---
    private fun calculateNotificationDelayInMinutes(
        currentTime: LocalTime,
        classTime: LocalTime,
        notifyBeforeMinutes: Long
    ): Long {
        val notificationTime = classTime.minusMinutes(notifyBeforeMinutes)
        return Duration.between(currentTime, notificationTime).toMinutes()
    }

    // --- Symulacja utilsa do sprawdzania statusu nieobecności ---
    private fun checkAbsenceStatus(currentAbsences: Int, limit: Int): String {
        return when {
            currentAbsences < limit -> "OK"
            currentAbsences == limit -> "Ostrzeżenie/Limit"
            else -> "Przekroczono limit"
        }
    }

    // --- TESTY JUNIT ---

    @Test
    fun `calculateNotificationDelayInMinutes returns 105 minutes for 1 hour and 45 minutes difference`() {
        // Arrange
        val currentTime = LocalTime.of(8, 0)
        val classTime = LocalTime.of(10, 0)
        val notifyBeforeMinutes = 15L

        // Act
        val delay = calculateNotificationDelayInMinutes(currentTime, classTime, notifyBeforeMinutes)

        // Assert
        // Planowane powiadomienie: 10:00 - 15 min = 09:45
        // Różnica: 08:00 -> 09:45 = 105 minut
        assertEquals(105L, delay)
    }

    @Test
    fun `calculateNotificationDelayInMinutes handles zero delay correctly`() {
        // Arrange
        val currentTime = LocalTime.of(9, 45)
        val classTime = LocalTime.of(10, 0)
        val notifyBeforeMinutes = 15L

        // Act
        val delay = calculateNotificationDelayInMinutes(currentTime, classTime, notifyBeforeMinutes)

        // Assert
        assertEquals(0L, delay)
    }

    @Test
    fun `checkAbsenceStatus returns OK when absences are below limit`() {
        // Arrange & Act
        val status = checkAbsenceStatus(currentAbsences = 1, limit = 2)

        // Assert
        assertEquals("OK", status)
    }

    @Test
    fun `checkAbsenceStatus returns Ostrzeżenie Limit when absences are exactly at the limit`() {
        // Arrange & Act
        val status = checkAbsenceStatus(currentAbsences = 2, limit = 2)

        // Assert
        assertEquals("Ostrzeżenie/Limit", status)
    }

    @Test
    fun `checkAbsenceStatus returns Przekroczono limit when absences exceed the limit`() {
        // Arrange & Act
        val status = checkAbsenceStatus(currentAbsences = 3, limit = 2)

        // Assert
        assertEquals("Przekroczono limit", status)
    }
}