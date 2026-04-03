package com.example.my_uz_android

import com.example.my_uz_android.data.models.GradeEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.my_uz_android.util.GradeCalculator

class GradeCalculatorTest {

    @Test
    fun `calculateGPA returns 0_0 for empty list`() {
        // Arrange
        val emptyGrades = emptyList<GradeEntity>()

        // Act
        val result = GradeCalculator.calculateGPA(emptyGrades)

        // Assert
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculateGPA returns correct average for all 5_0 grades`() {
        // Arrange
        val grades = listOf(
            createMockGrade(grade = 5.0, weight = 1),
            createMockGrade(grade = 5.0, weight = 2),
            createMockGrade(grade = 5.0, weight = 3)
        )

        // Act
        val result = GradeCalculator.calculateGPA(grades)

        // Assert
        assertEquals(5.0, result, 0.001)
    }

    @Test
    fun `calculateGPA returns correct average when half of records are points`() {
        // Arrange
        val grades = listOf(
            // Oceny (wliczane do średniej)
            createMockGrade(grade = 3.0, weight = 1, isPoints = false),
            createMockGrade(grade = 4.0, weight = 1, isPoints = false),
            // Punkty (nie wliczane do średniej - waga domyślnie 0 dla punktów)
            createMockGrade(grade = 15.0, weight = 0, isPoints = true),
            createMockGrade(grade = 20.0, weight = 0, isPoints = true)
        )

        // Act
        val result = GradeCalculator.calculateGPA(grades)

        // Assert: Srednia z (3.0 * 1) + (4.0 * 1) / 2 = 3.5
        assertEquals(3.5, result, 0.001)
    }

    // Funkcja pomocnicza do tworzenia obiektów GradeEntity na potrzeby testów
    private fun createMockGrade(
        grade: Double,
        weight: Int,
        isPoints: Boolean = false
    ): GradeEntity {
        return GradeEntity(
            id = 0,
            subjectName = "Test Subject",
            classType = "Wykład",
            grade = grade,
            weight = weight,
            description = "Test Grade",
            comment = null,
            date = System.currentTimeMillis(),
            semester = 1,
            isPoints = isPoints
        )
    }
}