package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.repositories.AbsenceRepository
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ✅ Klasy danych potrzebne dla listy
data class SubjectAbsences(
    val subjectName: String,
    val types: List<AbsenceTypeGroup>
)

data class AbsenceTypeGroup(
    val classType: String,
    val absences: List<AbsenceEntity>,
    val limit: Int
)

sealed interface AbsencesUiState {
    data object Loading : AbsencesUiState
    data class Success(val data: List<SubjectAbsences>) : AbsencesUiState
    data class Error(val message: String) : AbsencesUiState
}

class AbsencesViewModel(
    private val absenceRepository: AbsenceRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Potrzebne do odświeżania widoku
    private val _uiState = MutableStateFlow<AbsencesUiState>(AbsencesUiState.Loading)
    val uiState: StateFlow<AbsencesUiState> = _uiState.asStateFlow()

    private val absencesDataFlow: Flow<List<SubjectAbsences>> = combine(
        absenceRepository.getAllAbsencesStream(),
        classRepository.getAllClassesStream(), // Trigger odświeżania przy zmianie planu
        _limits
    ) { absences, _, limits ->
        absences.groupBy { it.subjectName }
            .map { (subjectName, subjectAbsences) ->
                val typeGroups = subjectAbsences.groupBy { it.classType ?: "Inne" }
                    .map { (type, typeList) ->
                        val limitKey = "$subjectName|$type"
                        val currentLimit = limits[limitKey] ?: 2

                        AbsenceTypeGroup(
                            classType = type,
                            absences = typeList.sortedBy { it.date },
                            limit = currentLimit
                        )
                    }.sortedBy { it.classType }

                SubjectAbsences(subjectName, typeGroups)
            }.sortedBy { it.subjectName }
    }

    init {
        viewModelScope.launch {
            absencesDataFlow
                .catch { e ->
                    _uiState.value = AbsencesUiState.Error(e.message ?: "Wystąpił błąd")
                }
                .collect { data ->
                    _uiState.value = AbsencesUiState.Success(data)
                }
        }
    }

    // Dla kompatybilności z UI (jeśli UI używa collectAsState na liście)
    val absencesState: StateFlow<List<SubjectAbsences>> = _uiState
        .map { state ->
            if (state is AbsencesUiState.Success) state.data else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteAbsence(absence: AbsenceEntity) {
        viewModelScope.launch {
            absenceRepository.deleteAbsence(absence)
        }
    }

    fun updateLimit(subjectName: String, classType: String, newLimit: Int) {
        val key = "$subjectName|$classType"
        _limits.update { it + (key to newLimit) }
    }
}