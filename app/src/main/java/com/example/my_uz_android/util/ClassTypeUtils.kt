package com.example.my_uz_android.util

object ClassTypeUtils {
    val abbreviations = mapOf(
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

    fun getFullName(abbreviation: String?): String {
        if (abbreviation.isNullOrEmpty()) return ""
        // Próbujemy znaleźć dokładne dopasowanie, a jeśli nie, zwracamy oryginał
        return abbreviations[abbreviation] ?: abbreviation
    }
}