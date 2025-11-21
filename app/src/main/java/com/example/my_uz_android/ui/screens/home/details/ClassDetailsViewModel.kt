package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class ClassDetailsUiState(
    val classItem: ClassEntity? = null,
    val dayName: String = ""
)

class ClassDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val classId: Int = checkNotNull(savedStateHandle["classId"])

    // ZMIANA: Usunięto filterNotNull(), aby obsłużyć przypadek, gdy baza zwraca null (np. dla ID 999)
    val uiState: StateFlow<ClassDetailsUiState> = classRepository.getClassById(classId)
        .map { dbEntity ->
            // Jeśli nie znaleziono w bazie, a ID to 999, użyj danych testowych (Mock)
            val entity = if (dbEntity == null && classId == 999) {
                ClassEntity(
                    id = 999,
                    subjectName = "Podstawy systemów dyskretnych",
                    classType = "Laboratorium",
                    startTime = "10:00",
                    endTime = "10:45",
                    dayOfWeek = LocalDate.now().dayOfWeek.value,
                    groupCode = "32INF-SP",
                    subgroup = null,
                    room = "Sala 102",
                    teacherName = "dr Jan Kowalski"
                )
            } else {
                dbEntity
            }

            if (entity != null) {
                val dayName = try {
                    DayOfWeek.of(entity.dayOfWeek)
                        .getDisplayName(TextStyle.FULL, Locale("pl"))
                        .replaceFirstChar { it.uppercase() }
                } catch (e: Exception) {
                    ""
                }
                ClassDetailsUiState(classItem = entity, dayName = dayName)
            } else {
                ClassDetailsUiState() // Pusty stan (ładowanie)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ClassDetailsUiState()
        )
}