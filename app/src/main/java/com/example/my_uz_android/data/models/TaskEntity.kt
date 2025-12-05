package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val subjectName: String = "", // Zmieniono nazwę pola w DB lub mapowaniu, ujednolicone
    val classType: String = "",
    val priority: Int = 1,
    val isAllDay: Boolean = false,
    val dueDate: Long = Instant.now().toEpochMilli(),
    val dueTime: String? = null,
    val endDate: Long = Instant.now().toEpochMilli(),
    // Fioletowy (Primary) jako domyślny, rzutowany na Int
    val color: Int = 0xFF68548E.toInt(),
    val isCompleted: Boolean = false,
    val subjectId: Int? = null,
    // Pola pomocnicze (ignorowane przez Room jeśli nie ma kolumn, ale tutaj są częścią encji w poprzednich wersjach)
    // Dla uproszczenia przyjmuję strukturę z Twojego ostatniego poprawnego pliku,
    // ale dostosowuję color i typy.
    // UWAGA: Upewnij się, że nazwy pól pasują do Twojej bazy danych.
    // W poprzednim pliku używałeś `classSubject` w ViewModelu, ale w Entity było `subjectName`.
    // Poniżej wersja spójna z Twoim ostatnim `TaskEntity.kt` z poprawką błędu kompilacji.
) {
    // Właściwości pomocnicze dla kompatybilności z kodem UI, który używa `classSubject`
    val classSubject: String get() = subjectName
}