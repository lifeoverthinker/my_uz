package com.example.my_uz_android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Encja reprezentująca ocenę ucznia przypisaną do konkretnego przedmiotu.
 * * UWAGA BIZNESOWA:
 * Powiązanie tej encji z zajęciami ([ClassEntity]) opiera się niejawnie na dopasowaniu
 * pól [subjectName] oraz [classType]. Domyślna wartość [classType] to puste pole ("").
 * Może to prowadzić do błędów w wyświetlaniu indeksu, jeśli ocena dla formy
 * takiej jak "Laboratorium" zostanie zapisana z pustym typem zajęć.
 */
@Serializable
@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectName: String,
    val classType: String = "",
    val grade: Double,
    val weight: Int = 1,
    val description: String?,
    val comment: String? = null,
    val date: Long,
    val semester: Int = 1,
    val isPoints: Boolean = false
)