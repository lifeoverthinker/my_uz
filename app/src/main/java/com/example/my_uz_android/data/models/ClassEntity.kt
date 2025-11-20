package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Pola mapowane z Supabase (tabela zajecia_grupy):
    val supabaseId: String? = null, // UUID z bazy zdalnej (jeśli potrzebne do synchronizacji)
    val subjectName: String,        // kolumna: "przedmiot"
    val classType: String,          // kolumna: "rz" (np. Lab, Wykład)
    val startTime: String,          // kolumna: "od" (format "HH:mm:ss" lub "HH:mm")
    val endTime: String,            // kolumna: "do_"
    val dayOfWeek: Int,             // Dzień tygodnia (będzie potrzebny do siatki)

    // Pola do filtrowania (z Twoich zapytań SQL):
    val groupCode: String,          // np. "33INF-SSI-SP" (z tabeli grupy)
    val subgroup: String?,          // np. "1", "A" lub NULL (dla wykładów) - kolumna: "podgrupa"

    // Dodatkowe informacje (z tabeli nauczyciele lub joinów):
    val teacherName: String? = null,
    val room: String? = null,

    // Lokalne atrybuty UI:
    val colorHex: String? = "#3D84FF" // Domyślny kolor
)