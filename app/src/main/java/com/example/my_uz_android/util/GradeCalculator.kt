package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.GradeEntity

// =========================================================================
// KALKULATOR OCEN
// Odpowiada za bezpieczne wyliczanie średniej ważonej oraz
// priorytetyzację oceny końcowej, co daje idealne zachowanie widoku Indeksu.
// =========================================================================
object GradeCalculator {

    fun calculateGPA(grades: List<GradeEntity>): Double {
        // 1. Priorytet Oceny Końcowej
        // Szukamy, czy ocena ma w opisie lub komentarzu słowo "końcow" (np. "Ocena końcowa").
        val finalGrade = grades.find {
            it.description?.contains("końcow", ignoreCase = true) == true ||
                    it.comment?.contains("końcow", ignoreCase = true) == true
        }

        if (finalGrade != null && finalGrade.grade > 0.0) {
            return finalGrade.grade // Ocena końcowa twardo nadpisuje wyliczaną średnią
        }

        // 2. Standardowe wyliczanie średniej ważonej
        val validGrades = grades.filter {
            !it.isPoints && it.grade != -1.0 && it.weight > 0
        }

        if (validGrades.isEmpty()) {
            return 0.0
        }

        val sum = validGrades.sumOf { it.grade * it.weight }
        val weightSum = validGrades.sumOf { it.weight }

        return if (weightSum > 0) sum / weightSum else 0.0
    }
}