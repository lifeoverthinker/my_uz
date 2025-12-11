package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.AbsenceRepository
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectAbsences(
    val subjectName: String,
    val types: List<AbsenceTypeGroup>
)

data class AbsenceTypeGroup(
    val classType: String,
    val absences: List<AbsenceEntity>,
    val limit: Int
)

class AbsencesViewModel(
    private val absenceRepository: AbsenceRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())

    val availableClasses: StateFlow<List<ClassEntity>> = classRepository.getAllClassesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val absencesState: StateFlow<List<SubjectAbsences>> = combine(
        absenceRepository.getAllAbsencesStream(),
        availableClasses,
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ ZMIANA: Dodano parametr description
    fun addAbsence(subjectName: String, classType: String, date: Long, description: String?) {
        viewModelScope.launch {
            val absence = AbsenceEntity(
                subjectName = subjectName,
                classType = classType,
                date = date,
                description = description
            )
            absenceRepository.insertAbsence(absence)
        }
    }

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