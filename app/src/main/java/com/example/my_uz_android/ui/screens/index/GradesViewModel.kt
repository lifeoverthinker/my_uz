package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClassTypeState(
    val name: String,
    val average: Double?,
    val grades: List<GradeEntity>
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
    val allGrades: List<GradeEntity> = emptyList(),
    // Nowe pola do filtrowania kierunków
    val userCourses: List<UserCourseEntity> = emptyList(),
    val selectedGroupCodes: Set<String> = emptySet()
)

class GradesViewModel(
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository // Dodane do repo
) : ViewModel() {

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<GradesUiState> = combine(
        gradesRepository.getAllGradesStream(),
        classRepository.getAllClassesStream(),
        userCourseRepository.getAllUserCoursesStream(),
        settingsRepository.getSettingsStream(),
        _selectedGroups
    ) { grades, allClasses, courses, settings, selectedGroups ->

        // 1. Zbieranie aktywnych kodów grup
        val activeCodes = if (selectedGroups.isEmpty()) {
            val codes = courses.map { it.groupCode }.toMutableSet()
            if (!settings?.selectedGroupCode.isNullOrBlank()) codes.add(settings!!.selectedGroupCode!!)
            codes
        } else {
            selectedGroups
        }

        // 2. Filtrowanie klas tylko dla zaznaczonych grup
        val classesForSelectedGroups = allClasses.filter { activeCodes.contains(it.groupCode) }

        // 3. Generowanie przedmiotów na bazie przefiltrowanych klas
        val subjectsFromSchedule = classesForSelectedGroups
            .groupBy { it.subjectName }
            .map { (subjectName, subjectClasses) ->
                val classTypes = subjectClasses.map { it.classType }.distinct().sorted()
                subjectName to classTypes
            }
            .sortedBy { it.first }

        val subjects = subjectsFromSchedule.map { (subjectName, classTypes) ->
            val subjectGrades = grades.filter { it.subjectName == subjectName }

            val types = classTypes.map { typeName ->
                val typeGrades = subjectGrades.filter { it.classType == typeName }
                val gradesForTypeAverage = typeGrades.filter { !it.isPoints && it.grade != -1.0 && it.weight > 0 }
                val typeAverage = if (gradesForTypeAverage.isNotEmpty()) {
                    val sum = gradesForTypeAverage.sumOf { it.grade * it.weight }
                    val weightSum = gradesForTypeAverage.sumOf { it.weight }
                    if (weightSum > 0) sum / weightSum else null
                } else null

                ClassTypeState(name = typeName, average = typeAverage, grades = typeGrades)
            }

            val gradesForSubjectAverage = subjectGrades.filter { !it.isPoints && it.grade != -1.0 && it.weight > 0 }
            val subjectAverage = if (gradesForSubjectAverage.isNotEmpty()) {
                val sum = gradesForSubjectAverage.sumOf { it.grade * it.weight }
                val weightSum = gradesForSubjectAverage.sumOf { it.weight }
                if (weightSum > 0) sum / weightSum else null
            } else null

            SubjectState(code = subjectName.take(3).uppercase(), name = subjectName, average = subjectAverage, types = types)
        }

        val allGradesForAverage = grades.filter {
            subjectsFromSchedule.any { s -> s.first == it.subjectName } // Zlicza tylko do średniej tych wybranych kierunków
                    && !it.isPoints && it.grade != -1.0 && it.weight > 0
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
            allGrades = grades,
            userCourses = courses,
            selectedGroupCodes = activeCodes
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GradesUiState(isLoading = true))

    fun toggleGroupVisibility(groupCode: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(groupCode)) current.remove(groupCode) else current.add(groupCode)
        _selectedGroups.value = current
    }

    suspend fun isPlanSelected(): Boolean {
        val settings = settingsRepository.getSettingsStream().first()
        return !settings?.selectedGroupCode.isNullOrBlank()
    }

    fun deleteGrade(grade: GradeEntity) {
        viewModelScope.launch { gradesRepository.deleteGrade(grade) }
    }

    fun duplicateGrade(grade: GradeEntity) {
        viewModelScope.launch { gradesRepository.insertGrade(grade.copy(id = 0)) }
    }
}