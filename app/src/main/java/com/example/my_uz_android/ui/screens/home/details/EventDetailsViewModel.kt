package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.repositories.EventRepository
import kotlinx.coroutines.flow.*

class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    val uiState: StateFlow<EventDetailsUiState> =
        eventRepository.getEventByIdStream(eventId)  // POPRAWKA: używamy Flow zamiast suspend
            .map { eventEntity ->
                EventDetailsUiState(
                    eventEntity = eventEntity,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = EventDetailsUiState(isLoading = true)
            )
}

data class EventDetailsUiState(
    val eventEntity: EventEntity? = null,
    val isLoading: Boolean = false
)
