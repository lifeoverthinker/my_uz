package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.GradesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddEditGradeUiState(
    val subjectName: String = "",
    val grade: String = "",
    val weight: String = "1",
    val description: String = "",
    val isEditing: Boolean = false
)

class AddEditGradeViewModel(
    savedStateHandle: SavedStateHandle,
    private val gradesRepository: GradesRepository
) : ViewModel() {

    private val gradeId: Int? = savedStateHandle["gradeId"]

    private val _uiState = MutableStateFlow(AddEditGradeUiState())
    val uiState: StateFlow<AddEditGradeUiState> = _uiState.asStateFlow()

    init {
        gradeId?.let { id ->
            viewModelScope.launch {
                gradesRepository.getGradeByIdStream(id)
                    .filterNotNull()
                    .collect { grade ->
                        _uiState.update {
                            it.copy(
                                subjectName = grade.subjectName,
                                grade = grade.grade.toString(),
                                weight = grade.weight.toString(),
                                description = grade.description ?: "",
                                isEditing = true
                            )
                        }
                    }
            }
        }
    }

    fun updateSubjectName(value: String) {
        _uiState.update { it.copy(subjectName = value) }
    }

    fun updateGradeValue(value: String) {
        _uiState.update { it.copy(grade = value) }
    }

    fun updateWeight(value: String) {
        _uiState.update { it.copy(weight = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun saveGrade(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.subjectName.isBlank() || state.grade.isBlank()) return

        val gradeValue = state.grade.toDoubleOrNull() ?: return
        val weightValue = state.weight.toIntOrNull() ?: 1

        viewModelScope.launch {
            val entity = GradeEntity(
                id = gradeId ?: 0,
                subjectName = state.subjectName,
                grade = gradeValue,
                weight = weightValue,
                description = state.description.ifBlank { null },
                date = System.currentTimeMillis(),
                semester = 1
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
