package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.GradesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GradeDetailsUiState(
    val grade: GradeEntity? = null
)

class GradeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val gradesRepository: GradesRepository
) : ViewModel() {

    private val gradeId: Int = checkNotNull(savedStateHandle["gradeId"])

    val uiState: StateFlow<GradeDetailsUiState> = gradesRepository
        .getGradeByIdStream(gradeId)
        .map { GradeDetailsUiState(grade = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GradeDetailsUiState()
        )

    fun deleteGrade(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState.value.grade?.let {
                gradesRepository.deleteGrade(it)
                onSuccess()
            }
        }
    }
}