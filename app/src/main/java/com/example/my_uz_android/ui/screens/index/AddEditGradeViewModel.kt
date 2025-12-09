package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddEditGradeUiState(
    val subjectName: String? = null,
    val classType: String? = null,  // ← DODANE
    val gradeType: GradeType = GradeType.STANDARD,
    val gradeValue: Double? = null,
    val customGradeValue: String = "",
    val weight: String = "1",
    val semester: Int = 1,
    val description: String = "",
    val availableSubjects: List<Pair<String, List<String>>> = emptyList(),  // ← ZMIENIONE na mapę
    val isEditing: Boolean = false,
    val isSubjectValid: Boolean = true,
    val isGradeValid: Boolean = true
)

enum class GradeType {
    STANDARD,      // Oceny 2.0-5.0
    CUSTOM,        // Niestandardowa liczba
    ACTIVITY       // Aktywność + (nie liczy się do średniej)
}

class AddEditGradeViewModel(
    savedStateHandle: SavedStateHandle,
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val gradeId: Int? = savedStateHandle["gradeId"]

    private val _uiState = MutableStateFlow(AddEditGradeUiState())
    val uiState: StateFlow<AddEditGradeUiState> = _uiState.asStateFlow()

    init {
        loadAvailableSubjects()
        gradeId?.let { id ->
            viewModelScope.launch {
                gradesRepository.getGradeByIdStream(id)
                    .filterNotNull()
                    .collect { grade ->
                        // ✅ Rozpoznaj typ oceny
                        val (type, value, customValue) = when {
                            grade.grade == -1.0 -> Triple(GradeType.ACTIVITY, null, "")
                            grade.grade in listOf(2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0) ->
                                Triple(GradeType.STANDARD, grade.grade, "")
                            else -> Triple(GradeType.CUSTOM, null, grade.grade.toString())
                        }

                        _uiState.update {
                            it.copy(
                                subjectName = grade.subjectName,
                                classType = grade.classType.ifBlank { null },
                                gradeType = type,
                                gradeValue = value,
                                customGradeValue = customValue,
                                weight = grade.weight.toString(),
                                semester = grade.semester,
                                description = grade.description ?: "",
                                isEditing = true,
                                availableSubjects = it.availableSubjects
                            )
                        }
                    }
            }
        }
    }

    // ✅ TAK JAK W TaskAddEditViewModel - mapa subject -> lista types
    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            classRepository.getAllClassesStream().collect { classes ->
                val subjectsMap = classes
                    .groupBy { it.subjectName }
                    .mapValues { entry ->
                        entry.value
                            .map { it.classType }
                            .distinct()
                            .toList()
                    }

                _uiState.update {
                    it.copy(availableSubjects = subjectsMap.toList())
                }
            }
        }
    }

    fun updateSubjectName(value: String?) {
        _uiState.update { it.copy(subjectName = value, classType = null, isSubjectValid = true) }
    }

    fun updateClassType(value: String?) {
        _uiState.update { it.copy(classType = value) }
    }

    fun updateGradeType(type: GradeType) {
        _uiState.update { it.copy(gradeType = type, isGradeValid = true) }
    }

    fun updateGradeValue(value: Double?) {
        _uiState.update { it.copy(gradeValue = value, isGradeValid = true) }
    }

    fun updateCustomGradeValue(value: String) {
        _uiState.update { it.copy(customGradeValue = value, isGradeValid = true) }
    }

    fun updateWeight(value: String) {
        _uiState.update { it.copy(weight = value) }
    }

    fun updateSemester(value: Int) {
        _uiState.update { it.copy(semester = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun saveGrade(onSuccess: () -> Unit) {
        val state = _uiState.value

        // ✅ Walidacja
        if (state.subjectName.isNullOrBlank()) {
            _uiState.update { it.copy(isSubjectValid = false) }
            return
        }

        // ✅ Określ wartość oceny
        val finalGrade: Double = when (state.gradeType) {
            GradeType.STANDARD -> state.gradeValue ?: run {
                _uiState.update { it.copy(isGradeValid = false) }
                return
            }
            GradeType.CUSTOM -> state.customGradeValue.toDoubleOrNull() ?: run {
                _uiState.update { it.copy(isGradeValid = false) }
                return
            }
            GradeType.ACTIVITY -> -1.0 // Znacznik aktywności
        }

        val weightValue = state.weight.toIntOrNull() ?: 1

        viewModelScope.launch {
            val entity = GradeEntity(
                id = gradeId ?: 0,
                subjectName = state.subjectName,
                classType = state.classType ?: "",
                grade = finalGrade,
                weight = weightValue,
                description = state.description.ifBlank { null },
                date = System.currentTimeMillis(),
                semester = state.semester
            )

            if (state.isEditing && gradeId != null) {
                gradesRepository.updateGrade(entity)
            } else {
                gradesRepository.insertGrade(entity)
            }

            onSuccess()
        }
    }
}
