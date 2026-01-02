package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.repositories.AbsenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AbsenceDetailsUiState(
    val absence: AbsenceEntity? = null,
    val isLoading: Boolean = true
)

class AbsenceDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val absenceRepository: AbsenceRepository
) : ViewModel() {

    private val absenceId: Int = checkNotNull(savedStateHandle["absenceId"])

    val uiState: StateFlow<AbsenceDetailsUiState> = absenceRepository.getAbsence(absenceId)
        .map { absence ->
            AbsenceDetailsUiState(absence = absence, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AbsenceDetailsUiState()
        )

    fun deleteAbsence(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState.value.absence?.let {
                absenceRepository.deleteAbsence(it)
                onSuccess()
            }
        }
    }
}