package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventDetailsUiState(
    val eventEntity: EventEntity? = null,
    val isLoading: Boolean = true
)

class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventById(eventId)

                if (event != null) {
                    _uiState.value = EventDetailsUiState(
                        eventEntity = event,
                        isLoading = false
                    )
                } else {
                    // Jeśli nie ma w bazie, utwórz mock event
                    _uiState.value = EventDetailsUiState(
                        eventEntity = EventEntity(
                            id = eventId,
                            title = "Juwenalia 2026",
                            description = "Największa impreza roku! Muzyka na żywo, food trucki i mnóstwo atrakcji.",
                            date = "Piątek, 20 maja 2026",
                            location = "Kampus A, Uniwersytet Zielonogórski",
                            timeRange = "18:00 - 02:00"
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // W przypadku błędu też pokaż mock
                _uiState.value = EventDetailsUiState(
                    eventEntity = EventEntity(
                        id = eventId,
                        title = "Przykładowe wydarzenie",
                        description = "Opis wydarzenia",
                        date = "Wkrótce",
                        location = "Do ustalenia",
                        timeRange = "TBA"
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            _uiState.value.eventEntity?.let {
                eventRepository.deleteEvent(it)
            }
        }
    }
}
