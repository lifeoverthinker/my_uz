package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.GradesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// DODANO: właściwość isLoading
data class GradeDetailsUiState(
    val grade: GradeEntity? = null,
    val isLoading: Boolean = false
)

class GradeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val gradesRepository: GradesRepository
) : ViewModel() {

    private val gradeId: Int = checkNotNull(savedStateHandle["gradeId"])

    val uiState: StateFlow<GradeDetailsUiState> = gradesRepository
        .getGradeByIdStream(gradeId)
        .map { GradeDetailsUiState(grade = it, isLoading = false) } // Kiedy pobierze, isLoading na false
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GradeDetailsUiState(isLoading = true) // Domyślnie na starcie ekran ładuje dane
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