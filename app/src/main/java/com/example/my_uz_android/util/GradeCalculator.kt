package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.GradeEntity

object GradeCalculator {
    fun calculateGPA(grades: List<GradeEntity>): Double {
        // Filtrujemy tylko standardowe oceny wliczane do średniej
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