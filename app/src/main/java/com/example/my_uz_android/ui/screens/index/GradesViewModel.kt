package com.example.my_uz_android.ui.screens.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.data.repositories.GradesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SubjectType {
    ALL,
    LECTURE,
    LAB,
    CLASS,
    PROJECT,
    EXAM
}

data class GradesUiState(
    val grades: List<GradeEntity> = emptyList(),
    val selectedType: SubjectType = SubjectType.ALL,
    val averageGrade: Double = 0.0
)

class GradesViewModel(
    private val gradesRepository: GradesRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow(SubjectType.ALL)

    val uiState: StateFlow<GradesUiState> = combine(
        gradesRepository.getAllGradesStream(),
        _selectedType
    ) { grades: List<GradeEntity>, selectedType: SubjectType ->

        val filteredGrades = when (selectedType) {
            SubjectType.ALL -> grades
            else -> grades // TODO: Filtruj po typie zajęć gdy będzie pole classType
        }

        val average = if (filteredGrades.isNotEmpty()) {
            val totalWeighted = filteredGrades.sumOf { it.grade * it.weight }
            val totalWeight = filteredGrades.sumOf { it.weight }
            if (totalWeight > 0) totalWeighted / totalWeight else 0.0
        } else {
            0.0
        }

        GradesUiState(
            grades = filteredGrades,
            selectedType = selectedType,
            averageGrade = average
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GradesUiState()
    )

    fun selectType(type: SubjectType) {
        _selectedType.value = type
    }

    fun addSampleGrades() {
        viewModelScope.launch {
            val sampleGrades = listOf(
                GradeEntity(
                    subjectName = "Programowanie Mobilne",
                    grade = 5.0,
                    weight = 3,
                    description = "Projekt końcowy",
                    date = System.currentTimeMillis(),
                    semester = 1
                ),
                GradeEntity(
                    subjectName = "Bazy Danych",
                    grade = 4.5,
                    weight = 2,
                    description = "Kolokwium 1",
                    date = System.currentTimeMillis() - 86400000L,
                    semester = 1
                )
            )

            sampleGrades.forEach { gradesRepository.insertGrade(it) }
        }
    }
}
