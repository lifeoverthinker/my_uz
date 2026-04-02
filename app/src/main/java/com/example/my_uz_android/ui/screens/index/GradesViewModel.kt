package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.util.GradeCalculator
import com.example.my_uz_android.util.SubgroupMatcher
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
    val selectedGroupCodes: Set<String> = emptySet(),
    val isPlanSelected: Boolean = false
)

class GradesViewModel(
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository
) : ViewModel() {

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    private var isGroupsInitialized = false

    val uiState: StateFlow<GradesUiState> = combine(
        gradesRepository.getAllGradesStream(),
        classRepository.getAllClassesStream(),
        userCourseRepository.getAllUserCoursesStream(),
        settingsRepository.getSettingsStream(),
        _selectedGroups
    ) { grades, allClasses, courses, settings, selectedGroupsRaw ->

        fun normalizeGroupCode(v: String?): String = v?.trim()?.uppercase().orEmpty()
        fun normalizeSubject(v: String?): String = v?.trim()?.lowercase().orEmpty()
        fun normalizeType(v: String?): String {
            val raw = v?.trim().orEmpty()
            return if (raw.isBlank()) "Inne" else raw
        }

        val allCoursesForUi = mutableListOf<UserCourseEntity>()
        settings?.selectedGroupCode?.let { main ->
            allCoursesForUi.add(
                UserCourseEntity(
                    id = -1,
                    groupCode = main,
                    fieldOfStudy = settings.fieldOfStudy ?: main,
                    semester = settings.currentSemester,
                    selectedSubgroup = settings.selectedSubgroup
                )
            )
        }
        courses.forEach { course ->
            val code = normalizeGroupCode(course.groupCode)
            if (allCoursesForUi.none { normalizeGroupCode(it.groupCode) == code }) {
                allCoursesForUi.add(course)
            }
        }

        val allUserCodes = allCoursesForUi.map { normalizeGroupCode(it.groupCode) }.toSet()

        val activeCodesUpper: Set<String> = run {
            val selectedNormalized = selectedGroupsRaw.map { normalizeGroupCode(it) }.toSet()
            if (!isGroupsInitialized && allUserCodes.isNotEmpty()) {
                _selectedGroups.value = allUserCodes
                isGroupsInitialized = true
                allUserCodes
            } else {
                val source = if (selectedNormalized.isNotEmpty()) selectedNormalized else _selectedGroups.value
                source.map { normalizeGroupCode(it) }.filter { it in allUserCodes }.toSet()
            }
        }

        val activeCodesLower = activeCodesUpper.map { it.lowercase() }.toSet()

        // Wspolna mapa uprawnien podgrup oparta o intersection
        val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, courses, activeCodesLower)
        val activeClasses = allClasses.filter { clazz ->
            SubgroupMatcher.isClassVisible(
                clazz.groupCode,
                clazz.classType,
                clazz.subgroup,
                userEnrollments
            )
        }

        val subjectToGroup = activeClasses
            .groupBy { normalizeSubject(it.subjectName) }
            .mapValues { (_, list) -> normalizeGroupCode(list.firstOrNull()?.groupCode) }
            .filterValues { it.isNotBlank() }

        val courseMap = allCoursesForUi.associateBy { normalizeGroupCode(it.groupCode) }

        val activeGrades = grades.filter { grade ->
            val group = subjectToGroup[normalizeSubject(grade.subjectName)]
            if (group.isNullOrBlank()) true else group in activeCodesUpper
        }

        val subjectsFromClasses = activeClasses.map { normalizeSubject(it.subjectName) }.toSet()
        val subjectsFromGrades = activeGrades.map { normalizeSubject(it.subjectName) }.toSet()
        val allSubjects = (subjectsFromClasses + subjectsFromGrades).toList().sorted()

        val subjects = allSubjects.mapNotNull { subjectKey ->
            val classesForSubject = activeClasses.filter { normalizeSubject(it.subjectName) == subjectKey }
            val gradesForSubject = activeGrades.filter { normalizeSubject(it.subjectName) == subjectKey }

            if (classesForSubject.isEmpty() && gradesForSubject.isEmpty()) return@mapNotNull null

            val displayName = classesForSubject.firstOrNull()?.subjectName
                ?: gradesForSubject.firstOrNull()?.subjectName
                ?: subjectKey

            val typesFromClasses = classesForSubject.map { normalizeType(it.classType) }
            val typesFromGrades = gradesForSubject.map { normalizeType(it.classType) }
            val allTypes = (typesFromClasses + typesFromGrades).distinct().sorted()

            val typeStates = allTypes.map { typeName ->
                val gradesForType = gradesForSubject.filter { normalizeType(it.classType) == typeName }
                val avgRaw = GradeCalculator.calculateGPA(gradesForType)
                ClassTypeState(
                    name = typeName,
                    average = if (avgRaw > 0.0) avgRaw else null,
                    grades = gradesForType
                )
            }

            val subjAvgRaw = GradeCalculator.calculateGPA(gradesForSubject)
            val mappedGroup = subjectToGroup[subjectKey]
            val courseName = if (!mappedGroup.isNullOrBlank()) {
                val c = courseMap[mappedGroup]
                c?.fieldOfStudy ?: c?.groupCode ?: mappedGroup
            } else {
                "Inne / Dawne"
            }

            SubjectState(
                code = displayName.take(3).uppercase(),
                name = displayName,
                courseName = courseName,
                average = if (subjAvgRaw > 0.0) subjAvgRaw else null,
                types = typeStates
            )
        }

        val overallRaw = GradeCalculator.calculateGPA(activeGrades)

        GradesUiState(
            subjects = subjects,
            average = if (overallRaw > 0.0) overallRaw else null,
            isLoading = false,
            allGrades = grades,
            userCourses = allCoursesForUi,
            selectedGroupCodes = activeCodesUpper,
            isPlanSelected = (settings?.selectedGroupCode?.isNotBlank() == true) || courses.isNotEmpty()
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GradesUiState(isLoading = true)
        )

    fun toggleGroupVisibility(groupCode: String) {
        val normalized = groupCode.trim().uppercase()
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(normalized)) current.remove(normalized) else current.add(normalized)
        _selectedGroups.value = current
    }

    suspend fun isPlanSelected(): Boolean {
        val settings = settingsRepository.getSettingsStream().first()
        val courses = userCourseRepository.getAllUserCoursesStream().first()
        return !settings?.selectedGroupCode.isNullOrBlank() || courses.isNotEmpty()
    }

    fun deleteGrade(grade: GradeEntity) { viewModelScope.launch { gradesRepository.deleteGrade(grade) } }
    fun duplicateGrade(grade: GradeEntity) { viewModelScope.launch { gradesRepository.insertGrade(grade.copy(id = 0)) } }
    fun saveGrade(grade: GradeEntity) { viewModelScope.launch { if (grade.id == 0) gradesRepository.insertGrade(grade) else gradesRepository.updateGrade(grade) } }

}