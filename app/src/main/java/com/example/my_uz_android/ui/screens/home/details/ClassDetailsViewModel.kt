package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClassDetailsUiState(
    val classEntity: ClassEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ClassDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val classRepository: ClassRepository
) : ViewModel() {

    // ✅ Pobieranie ID z argumentów nawigacji
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