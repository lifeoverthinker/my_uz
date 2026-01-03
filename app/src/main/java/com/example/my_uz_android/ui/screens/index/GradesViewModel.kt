package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- Modele Stanu ---
data class ClassTypeState(
    val name: String,
    val average: Double?,
    val grades: List<GradeEntity> // ZMIANA: Przekazujemy całe encje, żeby GradeBubble widział flagę isPoints
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
        // 1. Grupujemy przedmioty z planu zajęć
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

                // --- OBLICZANIE ŚREDNIEJ DLA TYPU ZAJĘĆ ---
                // Ignorujemy:
                // - Punkty (isPoints == true)
                // - Aktywność (grade == -1.0)
                // - Oceny z wagą 0 (weight == 0)
                val gradesForTypeAverage = typeGrades.filter {
                    !it.isPoints && it.grade != -1.0 && it.weight > 0
                }

                val typeAverage = if (gradesForTypeAverage.isNotEmpty()) {
                    val sum = gradesForTypeAverage.sumOf { it.grade * it.weight }
                    val weightSum = gradesForTypeAverage.sumOf { it.weight }
                    if (weightSum > 0) sum / weightSum else null
                } else null

                ClassTypeState(
                    name = typeName,
                    average = typeAverage,
                    grades = typeGrades // Przekazujemy wszystkie oceny (też punkty), żeby je wyświetlić
                )
            }

            // --- ŚREDNIA PRZEDMIOTU (WAŻONA) ---
            val gradesForSubjectAverage = subjectGrades.filter {
                !it.isPoints && it.grade != -1.0 && it.weight > 0
            }

            val subjectAverage = if (gradesForSubjectAverage.isNotEmpty()) {
                val sum = gradesForSubjectAverage.sumOf { it.grade * it.weight }
                val weightSum = gradesForSubjectAverage.sumOf { it.weight }
                if (weightSum > 0) sum / weightSum else null
            } else null

            SubjectState(
                code = subjectName.take(3).uppercase(),
                name = subjectName,
                average = subjectAverage,
                types = types
            )
        }

        // 3. Średnia całkowita (ważona ze wszystkich ocen semestru)
        val allGradesForAverage = grades.filter {
            !it.isPoints && it.grade != -1.0 && it.weight > 0
        }

        val overallAverage = if (allGradesForAverage.isNotEmpty()) {
            val sum = allGradesForAverage.sumOf { it.grade * it.weight }
            val weightSum = allGradesForAverage.sumOf { it.weight }
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

    fun deleteGrade(grade: GradeEntity) {
        viewModelScope.launch {
            gradesRepository.deleteGrade(grade)
        }
    }

    fun duplicateGrade(grade: GradeEntity) {
        viewModelScope.launch {
            val duplicatedGrade = grade.copy(id = 0)
            gradesRepository.insertGrade(duplicatedGrade)
        }
    }
}