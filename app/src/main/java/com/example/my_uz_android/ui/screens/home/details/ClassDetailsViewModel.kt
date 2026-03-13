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

    // Używamy MutableStateFlow, żeby móc nadpisać stan, jeśli otrzymamy zajęcia z pamięci tymczasowej
    private val _uiState = MutableStateFlow(ClassDetailsUiState(isLoading = true))
    val uiState: StateFlow<ClassDetailsUiState> = _uiState.asStateFlow()

    init {
        loadClassDetails()
    }

    private fun loadClassDetails() {
        if (classId == -1) {
            // To znaczy, że jesteśmy w trybie podglądu (wyszukiwania).
            // Dane zostaną wstrzyknięte z zewnątrz przez funkcję setTemporaryClass.
            _uiState.value = ClassDetailsUiState(isLoading = true)
        } else {
            // Normalny tryb z kalendarza - idziemy do bazy SQLite
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

    // Nowa funkcja - pozwala "wstrzyknąć" zajęcia bez szukania ich w bazie
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