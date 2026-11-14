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

    private val classId: Int = checkNotNull(savedStateHandle["classId"])

    private val _uiState = MutableStateFlow(ClassDetailsUiState(isLoading = true))
    val uiState: StateFlow<ClassDetailsUiState> = _uiState.asStateFlow()

    init {
        loadClassDetails()
    }

    private fun loadClassDetails() {
        if (classId == -1) {
            _uiState.value = ClassDetailsUiState(isLoading = true)
        } else {
            viewModelScope.launch {
                classRepository.getClassByIdStream(classId)
                    .catch { e ->
                        _uiState.value = ClassDetailsUiState(isLoading = false, error = "Błąd bazy: ${e.message}")
                    }
                    .collect { classEntity ->
                        if (classEntity != null) {
                            _uiState.value = ClassDetailsUiState(classEntity = classEntity, isLoading = false)
                        } else {
                            _uiState.value = ClassDetailsUiState(isLoading = false, error = "Nie znaleziono zajęć")
                        }
                    }
            }
        }
    }

    fun setTemporaryClass(classEntity: ClassEntity?) {
        if (classEntity != null) {
            _uiState.value = ClassDetailsUiState(classEntity = classEntity, isLoading = false)
        } else {
            _uiState.value = ClassDetailsUiState(isLoading = false, error = "Błąd pobierania podglądu")
        }
    }

    fun deleteClass() {
        viewModelScope.launch {
            _uiState.value.classEntity?.let {
                classRepository.deleteClass(it)
            }
        }
    }
}