package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.GradesRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class GradeType {
    STANDARD,
    CUSTOM,
    ACTIVITY
}

data class AddEditGradeUiState(
    val subjectName: String? = null,
    val classType: String? = null,
    val gradeType: GradeType = GradeType.STANDARD,
    val gradeValue: Double? = null,
    val customGradeValue: String = "",
    val weight: String = "1",
    val description: String = "", // Tytuł
    val comment: String = "",     // ✅ NOWE: Opis
    val date: Long = System.currentTimeMillis(),
    val availableSubjects: List<Pair<String, List<String>>> = emptyList()
)

class AddEditGradeViewModel(
    private val gradesRepository: GradesRepository,
    private val classRepository: ClassRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditGradeUiState())
    val uiState: StateFlow<AddEditGradeUiState> = _uiState.asStateFlow()

    private var currentSemesterFromSettings: Int = 1

    init {
        loadAvailableSubjects()
        loadCurrentSemester()
    }

    private fun loadCurrentSemester() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsStream().first()
            currentSemesterFromSettings = settings?.currentSemester ?: 1
        }
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            classRepository.getAllClassesStream().collect { classes ->
                val subjectsMap = classes.groupBy { it.subjectName }
                    .mapValues { (_, classList) ->
                        classList.map { it.classType }.distinct().sorted()
                    }
                    .toList()
                    .sortedBy { it.first }

                _uiState.update { it.copy(availableSubjects = subjectsMap) }
            }
        }
    }

    fun updateSubjectName(name: String?) {
        _uiState.update { it.copy(subjectName = name) }
    }

    fun updateClassType(type: String?) {
        _uiState.update { it.copy(classType = type) }
    }

    fun updateGradeType(type: GradeType) {
        _uiState.update { it.copy(gradeType = type) }
    }

    fun updateGradeValue(value: Double?) {
        _uiState.update { it.copy(gradeValue = value) }
    }

    fun updateCustomGradeValue(value: String) {
        _uiState.update { it.copy(customGradeValue = value) }
    }

    fun updateWeight(weight: String) {
        _uiState.update { it.copy(weight = weight) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    // ✅ NOWE: Aktualizacja komentarza/opisu
    fun updateComment(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun saveGrade(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.subjectName == null) return@launch

            val finalGrade = when (state.gradeType) {
                GradeType.STANDARD -> state.gradeValue ?: return@launch
                GradeType.ACTIVITY -> -1.0
                GradeType.CUSTOM -> state.customGradeValue.toDoubleOrNull() ?: -1.0
            }

            val weightInt = state.weight.toIntOrNull() ?: 1

            val entity = GradeEntity(
                subjectName = state.subjectName,
                classType = state.classType ?: "",
                grade = finalGrade,
                weight = weightInt,
                description = state.description.ifBlank { "Ocena" }, // Tytuł
                comment = state.comment.ifBlank { null },            // ✅ Zapis opisu
                date = state.date,
                semester = currentSemesterFromSettings
            )

            gradesRepository.insertGrade(entity)
            onSuccess()
        }
    }
}