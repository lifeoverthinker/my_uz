package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.my_uz_android.data.models.EventEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stan UI ekranu szczegółów wydarzenia.
 *
 * Zawiera:
 * - dane wydarzenia (jeśli istnieje),
 * - flagę ładowania,
 * - komunikat błędu do bezpiecznego renderowania stanu pustego.
 */
data class EventDetailsUiState(
    val eventEntity: EventEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel szczegółów wydarzenia.
 *
 * Odpowiada za:
 * - przygotowanie mockowanego wydarzenia po ID z nawigacji,
 * - utrzymanie stanu loading/success/error,
 * - brak operacji usuwania (brak persystencji wydarzen).
 */
class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Int = savedStateHandle.get<Int>("eventId")
        ?: savedStateHandle.get<String>("eventId")?.toIntOrNull()
        ?: 0

    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    init {
        loadMockEvent()
    }

    /**
     * Ładuje sztuczne wydarzenie do podglądu.
     */
    private fun loadMockEvent() {
        val fakeEvent = EventEntity(
            id = eventId,
            title = "Wydarzenie pokazowe #$eventId",
            description = "To jest statyczny podglad wydarzenia. Szczegoly nie sa pobierane z bazy danych.",
            date = "Sobota, 10 maja 2026",
            location = "Kampus B, Budynek A",
            timeRange = "10:00 - 12:00"
        )

        _uiState.value = EventDetailsUiState(
            eventEntity = fakeEvent,
            isLoading = false,
            error = null
        )
    }
}