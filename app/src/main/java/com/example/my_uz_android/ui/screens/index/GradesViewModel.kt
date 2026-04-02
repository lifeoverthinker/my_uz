package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.models.SettingsEntity
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

private data class ActiveCourseFilter(
    val groupCode: String,
    val subgroupRaw: String?
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

        // 1) Kursy UI: główny + dodatkowe
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

        // 2) Stabilna inicjalizacja selected groups (jak w CalendarVM)
        val activeCodes: Set<String> = run {
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

        // 3) Filtry per groupCode z podgrupami
        val activeFilters = buildActiveFilters(settings, courses)
            .filter { normalizeGroupCode(it.groupCode) in activeCodes }

        val filtersByGroup = activeFilters.groupBy { normalizeGroupCode(it.groupCode) }

        // 4) Klasy widoczne: najpierw groupCode, potem wspólny matcher podgrup
        val activeClasses = allClasses.filter { clazz ->
            val groupCode = normalizeGroupCode(clazz.groupCode)
            if (groupCode !in activeCodes) return@filter false

            val filtersForGroup = filtersByGroup[groupCode] ?: return@filter false
            SubgroupMatcher.matches(
                classSubgroupRaw = clazz.subgroup,
                selectedSubgroupsRaw = filtersForGroup.map { it.subgroupRaw }
            )
        }

        // Mapowanie subject -> groupCode (dla przypisania kierunku)
        val subjectToGroup = activeClasses
            .groupBy { normalizeSubject(it.subjectName) }
            .mapValues { (_, list) -> normalizeGroupCode(list.firstOrNull()?.groupCode) }
            .filterValues { it.isNotBlank() }

        val courseMap = allCoursesForUi.associateBy { normalizeGroupCode(it.groupCode) }

        // 5) Oceny aktywne: jeśli brak mapowania do planu, nie gubimy (historyczne)
        val activeGrades = grades.filter { grade ->
            val group = subjectToGroup[normalizeSubject(grade.subjectName)]
            if (group.isNullOrBlank()) true else group in activeCodes
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
            selectedGroupCodes = activeCodes,
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

    fun deleteGrade(grade: GradeEntity) {
        viewModelScope.launch { gradesRepository.deleteGrade(grade) }
    }

    fun duplicateGrade(grade: GradeEntity) {
        viewModelScope.launch { gradesRepository.insertGrade(grade.copy(id = 0)) }
    }

    fun saveGrade(grade: GradeEntity) {
        viewModelScope.launch {
            if (grade.id == 0) gradesRepository.insertGrade(grade) else gradesRepository.updateGrade(grade)
        }
    }

    private fun buildActiveFilters(
        settings: SettingsEntity?,
        courses: List<UserCourseEntity>
    ): List<ActiveCourseFilter> {
        val out = mutableListOf<ActiveCourseFilter>()
        settings?.selectedGroupCode?.let { out.add(ActiveCourseFilter(it, settings.selectedSubgroup)) }
        courses.forEach { out.add(ActiveCourseFilter(it.groupCode, it.selectedSubgroup)) }
        return out
    }
}