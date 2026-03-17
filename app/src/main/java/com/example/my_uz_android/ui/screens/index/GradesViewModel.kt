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
import com.example.my_uz_android.util.GradeCalculator

data class ClassTypeState(
    val name: String,
    val average: Double?,
    val grades: List<GradeEntity>
)

data class SubjectState(
    val code: String,
    val name: String,
    val courseName: String,
    val average: Double?,
    val types: List<ClassTypeState>
)

data class GradesUiState(
    val subjects: List<SubjectState> = emptyList(),
    val average: Double? = null,
    val isLoading: Boolean = false,
    val allGrades: List<GradeEntity> = emptyList(),
    val userCourses: List<UserCourseEntity> = emptyList(),
    val selectedGroupCodes: Set<String> = emptySet()
)

class GradesViewModel(
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository
) : ViewModel() {

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<GradesUiState> = combine(
        gradesRepository.getAllGradesStream(),
        classRepository.getAllClassesStream(),
        userCourseRepository.getAllUserCoursesStream(),
        settingsRepository.getSettingsStream(),
        _selectedGroups
    ) { grades, allClasses, courses, settings, selectedGroups ->

        val allCoursesForUi = mutableListOf<UserCourseEntity>()
        settings?.selectedGroupCode?.let { mainCode ->
            allCoursesForUi.add(
                UserCourseEntity(
                    id = -1,
                    groupCode = mainCode,
                    fieldOfStudy = settings.fieldOfStudy ?: mainCode,
                    semester = settings.currentSemester
                )
            )
        }
        allCoursesForUi.addAll(courses)

        val activeCodes = if (selectedGroups.isEmpty()) {
            val codes = allCoursesForUi.map { it.groupCode }.toMutableSet()
            codes
        } else {
            selectedGroups
        }

        val classesForSelectedGroups = allClasses.filter { activeCodes.contains(it.groupCode) }
        val courseMap = allCoursesForUi.associateBy { it.groupCode }
        val subjectToCourseMap = classesForSelectedGroups.associate {
            val course = courseMap[it.groupCode]
            it.subjectName to (course?.fieldOfStudy ?: course?.groupCode ?: it.groupCode)
        }

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
                val rawTypeAverage = GradeCalculator.calculateGPA(typeGrades)
                val typeAverage = if (rawTypeAverage > 0.0) rawTypeAverage else null

                ClassTypeState(name = typeName, average = typeAverage, grades = typeGrades)
            }

            val rawSubjectAverage = GradeCalculator.calculateGPA(subjectGrades)
            val subjectAverage = if (rawSubjectAverage > 0.0) rawSubjectAverage else null

            val courseName = subjectToCourseMap[subjectName] ?: "Inne"

            SubjectState(
                code = subjectName.take(3).uppercase(),
                name = subjectName,
                courseName = courseName,
                average = subjectAverage,
                types = types
            )
        }

        val allGradesForAverage = grades.filter {
            subjectsFromSchedule.any { s -> s.first == it.subjectName }
        }

        val rawOverallAverage = GradeCalculator.calculateGPA(allGradesForAverage)
        val overallAverage = if (rawOverallAverage > 0.0) rawOverallAverage else null

        GradesUiState(
            subjects = subjects,
            average = overallAverage,
            isLoading = false,
            allGrades = grades,
            userCourses = allCoursesForUi,
            selectedGroupCodes = activeCodes
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GradesUiState(isLoading = true)
        )

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

    // NOWE: Szybki zapis dla modala
    fun saveGrade(grade: GradeEntity) {
        viewModelScope.launch {
            if (grade.id == 0) {
                gradesRepository.insertGrade(grade)
            } else {
                gradesRepository.updateGrade(grade)
            }
        }
    }
}