package com.example.my_uz_android.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import java.util.Locale

object ClassTypeUtils {
    private val abbreviationsPl = mapOf(
        "R" to "Rezerwacja",
        "BHP" to "Szkolenie BHP",
        "C" to "Ćwiczenia",
        "Cz" to "Ćwiczenia / Zdalne",
        "Ć" to "Ćwiczenia",
        "ĆL" to "Ćwiczenia i laboratorium",
        "E" to "Egzamin",
        "E/Z" to "Egzamin/zdalne",
        "I" to "Inne",
        "K" to "Konwersatorium",
        "L" to "Laboratorium",
        "P" to "Projekt",
        "Pra" to "Praktyka",
        "Pro" to "Proseminarium",
        "PrZ" to "Praktyka zawodowa",
        "P/Z" to "Projekt / Zdalne",
        "S" to "Seminarium",
        "Sk" to "Samokształcenie",
        "T" to "Terenowe",
        "W" to "Wykład",
        "war" to "Warsztaty",
        "W+C" to "Wykład i ćwiczenia",
        "WĆL" to "Wykład + ćwiczenia + laboratorium",
        "W+K" to "Wykłady + Konwersatoria",
        "W+L" to "Wykład i laboratorium",
        "W+P" to "Wykład + projekt",
        "WW" to "Wykład i warsztaty",
        "W/Z" to "Wykład/Zdalne",
        "Z" to "Zdalne",
        "ZK" to "Zajęcia kliniczne",
        "Zp" to "Zajęcia praktyczne"
    )

    private val abbreviationsEn = mapOf(
        "R" to "Reservation",
        "BHP" to "Health and Safety Training",
        "C" to "Exercises",
        "Cz" to "Exercises / Remote",
        "Ć" to "Exercises",
        "ĆL" to "Exercises and Lab",
        "E" to "Exam",
        "E/Z" to "Exam / Remote",
        "I" to "Other",
        "K" to "Seminar Class",
        "L" to "Laboratory",
        "P" to "Project",
        "Pra" to "Internship",
        "Pro" to "Proseminar",
        "PrZ" to "Professional Internship",
        "P/Z" to "Project / Remote",
        "S" to "Seminar",
        "Sk" to "Self-study",
        "T" to "Field Classes",
        "W" to "Lecture",
        "war" to "Workshop",
        "W+C" to "Lecture and Exercises",
        "WĆL" to "Lecture + Exercises + Lab",
        "W+K" to "Lectures + Seminar Classes",
        "W+L" to "Lecture and Lab",
        "W+P" to "Lecture + Project",
        "WW" to "Lecture and Workshops",
        "W/Z" to "Lecture / Remote",
        "Z" to "Remote",
        "ZK" to "Clinical Classes",
        "Zp" to "Practical Classes"
    )

    fun getFullName(abbreviation: String?): String {
        if (abbreviation.isNullOrEmpty()) return ""
        val map = if (Locale.getDefault().language == "en") abbreviationsEn else abbreviationsPl
        return map[abbreviation] ?: abbreviation
    }

    fun getClassTypeColor(
        classType: String?,
        colors: List<Color>,
        classColorMap: Map<String, Int> = emptyMap()
    ): Color {
        if (classType.isNullOrBlank() || colors.isEmpty()) return colors.firstOrNull() ?: Color.Gray

        classColorMap[classType]?.let { savedIndex ->
            if (savedIndex in colors.indices) return colors[savedIndex]
        }

        val index = abs(classType.hashCode()) % colors.size
        return colors[index]
    }
}
