package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.repositories.AbsenceRepository
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddEditAbsenceUiState(
    val id: Int = 0,
    val subjectName: String? = null,
    val classType: String? = null,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val availableSubjects: List<Pair<String, List<String>>> = emptyList()
)

class AddEditAbsenceViewModel(
    private val absenceRepository: AbsenceRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditAbsenceUiState())
    val uiState: StateFlow<AddEditAbsenceUiState> = _uiState.asStateFlow()

    private var loadedAbsenceId: Int? = null

    init {
        loadAvailableSubjects()
    }

    fun initNewAbsence(subject: String?, type: String?) {
        _uiState.update {
            it.copy(
                id = 0,
                // Jeśli nie przekazano wstępnych danych, ustawiamy na null, aby zablokować przycisk
                subjectName = subject,
                classType = type,
                description = "",
                date = System.currentTimeMillis()
            )
        }
    }

    fun loadAbsence(absenceId: Int) {
        if (loadedAbsenceId == absenceId) return
        loadedAbsenceId = absenceId

        viewModelScope.launch {
            val allAbsences = absenceRepository.getAllAbsencesStream().first()
            val absence = allAbsences.find { it.id == absenceId }

            if (absence != null) {
                _uiState.update {
                    it.copy(
                        id = absence.id,
                        subjectName = absence.subjectName,
                        classType = absence.classType,
                        description = absence.description ?: "",
                        date = absence.date
                    )
                }
            }
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

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun saveAbsence(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.subjectName == null) return@launch

            val entity = AbsenceEntity(
                id = loadedAbsenceId ?: 0,
                subjectName = state.subjectName,
                classType = state.classType ?: "",
                date = state.date,
                description = state.description.ifBlank { null }
            )

            absenceRepository.insertAbsence(entity)
            onSuccess()
        }
    }

    fun deleteAbsence(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (loadedAbsenceId != null && loadedAbsenceId != 0) {
                val entity = AbsenceEntity(
                    id = loadedAbsenceId!!,
                    subjectName = state.subjectName ?: "",
                    date = state.date,
                    classType = state.classType ?: ""
                )
                absenceRepository.deleteAbsence(entity)
                onSuccess()
            }
        }
    }
}