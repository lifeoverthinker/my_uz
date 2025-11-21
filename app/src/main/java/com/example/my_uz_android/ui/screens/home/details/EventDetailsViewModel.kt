package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.repositories.EventRepository // Ważny import
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class EventDetailsUiState(
    val event: EventEntity? = null
)

class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    val uiState: StateFlow<EventDetailsUiState> = eventRepository.getEventById(eventId)
        .map { dbEvent ->
            // MOCK DATA
            val event = if (dbEvent == null && eventId == 999) {
                EventEntity(
                    id = 999,
                    title = "Koncert studentów UZ: „Piosenka filmowa dobra na wszystko”",
                    date = "Piątek, 4 wrz 2025",
                    timeRange = "18:00 - 19:00",
                    location = "sala im. Janusza Koniusza, Wojewódzka i Miejska Biblioteka Publiczna im. Cypriana Norwida w Zielonej Górze, al. Wojska Polskiego 9, 65-077 Zielona Góra",
                    description = "wstęp wolny"
                )
            } else {
                dbEvent
            }
            EventDetailsUiState(event = event)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EventDetailsUiState()
        )
}