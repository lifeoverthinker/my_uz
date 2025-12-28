package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.ui.screens.index.components.GradeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

// --- Modele Stanu (lokalne dla ViewModelu, mapowane później na UI) ---
data class ClassTypeState(
    val name: String,
    val average: Double?,
    val grades: List<GradeItem>
)

data class SubjectState(
    val code: String,
    val name: String,
    val average: Double?,
    val types: List<ClassTypeState>
)

data class GradesUiState(
    val subjects: List<SubjectState> = emptyList(),
    val average: Double? = null,
    val isLoading: Boolean = false,
    val allGrades: List<GradeEntity> = emptyList()
)

class GradesViewModel(
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<GradesUiState> = combine(
        gradesRepository.getAllGradesStream(),
        classRepository.getAllClassesStream()
    ) { grades, classes ->
        // 1. Grupujemy przedmioty z planu zajęć (żeby widzieć przedmioty bez ocen)
        val subjectsFromSchedule = classes
            .groupBy { it.subjectName }
            .map { (subjectName, subjectClasses) ->
                val classTypes = subjectClasses
                    .map { it.classType }
                    .distinct()
                    .sorted()
                subjectName to classTypes
            }
            .sortedBy { it.first }

        // 2. Mapujemy na SubjectState
        val subjects = subjectsFromSchedule.map { (subjectName, classTypes) ->
            val subjectGrades = grades.filter { it.subjectName == subjectName }

            // Typy zajęć (Wykład, Ćwiczenia)
            val types = classTypes.map { typeName ->
                val typeGrades = subjectGrades.filter { it.classType == typeName }

                // Mapujemy oceny na GradeItem (do wyświetlenia w bąbelkach)
                val gradeItems = typeGrades.map { grade ->
                    GradeItem(
                        id = grade.id,
                        value = if (grade.grade == -1.0) "+" else {
                            if (grade.grade % 1.0 == 0.0) grade.grade.toInt().toString() else grade.grade.toString()
                        }
                    )
                }

                // Liczymy średnią dla typu zajęć
                val numericGrades = typeGrades.mapNotNull {
                    if (it.grade == -1.0) null else it.grade
                }

                val typeAverage = if (numericGrades.isNotEmpty()) {
                    numericGrades.average()
                } else null

                ClassTypeState(
                    name = typeName,
                    average = typeAverage,
                    grades = gradeItems
                )
            }

            // Średnia przedmiotu (ważona)
            val numericSubjectGrades = subjectGrades.filter { it.grade != -1.0 }
            val subjectAverage = if (numericSubjectGrades.isNotEmpty()) {
                val sum = numericSubjectGrades.sumOf { it.grade * it.weight }
                val weightSum = numericSubjectGrades.sumOf { it.weight }
                if (weightSum > 0) sum / weightSum else null
            } else null

            SubjectState(
                code = subjectName.take(3).uppercase(),
                name = subjectName,
                average = subjectAverage,
                types = types
            )
        }

        // 3. Średnia całkowita (ważona ze wszystkich ocen)
        val allNumericGrades = grades.filter { it.grade != -1.0 }
        val overallAverage = if (allNumericGrades.isNotEmpty()) {
            val sum = allNumericGrades.sumOf { it.grade * it.weight }
            val weightSum = allNumericGrades.sumOf { it.weight }
            if (weightSum > 0) sum / weightSum else null
        } else null

        GradesUiState(
            subjects = subjects,
            average = overallAverage,
            isLoading = false,
            allGrades = grades
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GradesUiState(isLoading = true)
        )

    suspend fun isPlanSelected(): Boolean {
        val settings = settingsRepository.getSettingsStream().first()
        return !settings?.selectedGroupCode.isNullOrBlank()
    }
}