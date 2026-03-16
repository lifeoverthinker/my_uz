package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.AbsenceRepository
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SubjectAbsences(
    val subjectName: String,
    val courseName: String, // NOWOŚĆ: Przypisany kierunek
    val types: List<AbsenceTypeGroup>
)

data class AbsenceTypeGroup(
    val classType: String,
    val absences: List<AbsenceEntity>,
    val limit: Int
)

sealed interface AbsencesUiState {
    data object Loading : AbsencesUiState
    data class Success(val data: List<SubjectAbsences>) : AbsencesUiState
    data class Error(val message: String) : AbsencesUiState
}

class AbsencesViewModel(
    private val absenceRepository: AbsenceRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository,
    private val userCourseRepository: UserCourseRepository
) : ViewModel() {

    private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())

    private val _uiState = MutableStateFlow<AbsencesUiState>(AbsencesUiState.Loading)
    val uiState: StateFlow<AbsencesUiState> = _uiState.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    private val absencesDataFlow: Flow<List<SubjectAbsences>> = combine(
        absenceRepository.getAllAbsencesStream(),
        classRepository.getAllClassesStream(),
        _limits,
        userCourseRepository.getAllUserCoursesStream(),
        settingsRepository.getSettingsStream(),
        _selectedGroups
    ) { args: Array<Any?> ->
        val absences = args[0] as List<AbsenceEntity>
        val allClasses = args[1] as List<ClassEntity>
        val limits = args[2] as Map<String, Int>
        val courses = args[3] as List<UserCourseEntity>
        val settings = args[4] as SettingsEntity?
        val selectedGroups = args[5] as Set<String>

        val activeCodes = if (selectedGroups.isEmpty()) {
            val codes = courses.map { it.groupCode }.toMutableSet()
            if (!settings?.selectedGroupCode.isNullOrBlank()) codes.add(settings!!.selectedGroupCode!!)
            codes
        } else {
            selectedGroups
        }

        // Filtrowanie planu po kierunku
        val validClasses = allClasses.filter { activeCodes.contains(it.groupCode) }
        val validSubjectNames = validClasses.map { it.subjectName }.toSet()

        // Mapowanie: Przedmiot -> Nazwa Kierunku (fieldOfStudy lub groupCode)
        val courseMap = courses.associateBy { it.groupCode }
        val subjectToCourseMap = validClasses.associate {
            val course = courseMap[it.groupCode]
            it.subjectName to (course?.fieldOfStudy ?: course?.groupCode ?: it.groupCode)
        }

        val filteredAbsences = absences.filter { validSubjectNames.contains(it.subjectName) }

        filteredAbsences.groupBy { it.subjectName }
            .map { (subjectName, subjectAbsences) ->
                val typeGroups = subjectAbsences.groupBy { it.classType ?: "Inne" }
                    .map { (type, typeList) ->
                        val limitKey = "$subjectName|$type"
                        val currentLimit = limits[limitKey] ?: 2
                        AbsenceTypeGroup(classType = type, absences = typeList.sortedBy { it.date }, limit = currentLimit)
                    }.sortedBy { it.classType }

                val courseName = subjectToCourseMap[subjectName] ?: "Inne"
                SubjectAbsences(subjectName, courseName, typeGroups)
            }.sortedBy { it.subjectName }
    }

    init {
        viewModelScope.launch {
            absencesDataFlow.catch { e -> _uiState.value = AbsencesUiState.Error(e.message ?: "Wystąpił błąd") }
                .collect { data -> _uiState.value = AbsencesUiState.Success(data) }
        }
    }

    fun toggleGroupVisibility(groupCode: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(groupCode)) current.remove(groupCode) else current.add(groupCode)
        _selectedGroups.value = current
    }

    val absencesState: StateFlow<List<SubjectAbsences>> = _uiState.map { state ->
        if (state is AbsencesUiState.Success) state.data else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteAbsence(absence: AbsenceEntity) = viewModelScope.launch { absenceRepository.deleteAbsence(absence) }

    fun updateLimit(subjectName: String, classType: String, newLimit: Int) {
        val key = "$subjectName|$classType"
        _limits.update { it + (key to newLimit) }
    }
}