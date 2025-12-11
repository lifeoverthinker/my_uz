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
// Usunięto import runBlocking - to było źródło ryzyka

data class ClassTypeState(
    val name: String,
    val average: Double,
    val grades: List<GradeItem>
)

data class SubjectState(
    val code: String,
    val name: String,
    val average: Double,
    val types: List<ClassTypeState>
)

data class GradesUiState(
    val subjects: List<SubjectState> = emptyList(),
    val average: Double = 0.0,
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
        // Obliczenia wykonywane są teraz na Dispatchers.Default (dzięki flowOn poniżej)

        // Pobierz wszystkie unikalne przedmioty z planu zajęć
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

        val subjects = subjectsFromSchedule.map { (subjectName, classTypes) ->
            val subjectGrades = grades.filter { it.subjectName == subjectName }

            val types = classTypes.map { typeName ->
                val typeGrades = subjectGrades.filter { it.classType == typeName }
                val gradeItems = typeGrades.map { grade ->
                    GradeItem(
                        id = grade.id,
                        value = if (grade.grade == -1.0) "+" else {
                            if (grade.grade % 1.0 == 0.0) grade.grade.toInt().toString() else grade.grade.toString()
                        }
                    )
                }

                val numericGrades = typeGrades.mapNotNull {
                    if (it.grade == -1.0) null else it.grade
                }

                val typeAverage = if (numericGrades.isNotEmpty()) {
                    numericGrades.average()
                } else {
                    0.0
                }

                ClassTypeState(
                    name = typeName,
                    average = typeAverage,
                    grades = gradeItems
                )
            }

            val numericSubjectGrades = subjectGrades.mapNotNull {
                if (it.grade == -1.0) null else it.grade
            }

            val subjectAverage = if (numericSubjectGrades.isNotEmpty()) {
                numericSubjectGrades.average()
            } else {
                0.0
            }

            SubjectState(
                code = subjectName.take(3).uppercase(),
                name = subjectName,
                average = subjectAverage,
                types = types
            )
        }

        val numericAllGrades = grades.mapNotNull {
            if (it.grade == -1.0) null else it.grade
        }

        val overallAverage = if (numericAllGrades.isNotEmpty()) {
            numericAllGrades.average()
        } else {
            0.0
        }

        GradesUiState(
            subjects = subjects,
            average = overallAverage,
            isLoading = false,
            allGrades = grades
        )
    }
        .flowOn(Dispatchers.Default) // ZMIANA: Przeniesienie obliczeń na wątek tła
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GradesUiState(isLoading = true)
        )

    // ZMIANA: suspend zamiast runBlocking
    suspend fun isPlanSelected(): Boolean {
        val settings = settingsRepository.getSettingsStream().first()
        return !settings?.selectedGroupCode.isNullOrBlank()
    }
}