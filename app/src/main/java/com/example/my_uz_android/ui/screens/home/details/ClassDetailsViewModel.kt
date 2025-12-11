package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClassDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val classId: Int = checkNotNull(savedStateHandle["classId"])

    val uiState: StateFlow<ClassDetailsUiState> =
        classRepository.getClassByIdStream(classId)
            .map { classEntity ->
                if (classEntity != null) {
                    ClassDetailsUiState(
                        classEntity = classEntity,
                        isLoading = false
                    )
                } else {
                    // ✅ NAPRAWA: Obsługa null (gdy nie ma danych)
                    ClassDetailsUiState(
                        classEntity = null,
                        isLoading = false,
                        error = "Nie znaleziono zajęć"
                    )
                }
            }
            .catch { e ->
                emit(ClassDetailsUiState(
                    isLoading = false,
                    error = "Błąd: ${e.message}"
                ))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ClassDetailsUiState(isLoading = true)
            )

    fun deleteClass() {
        viewModelScope.launch {
            uiState.value.classEntity?.let {
                classRepository.deleteClass(it)
            }
        }
    }
}

data class ClassDetailsUiState(
    val classEntity: ClassEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
