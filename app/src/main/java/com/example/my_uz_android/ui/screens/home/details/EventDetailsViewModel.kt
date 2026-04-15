package com.example.my_uz_android.ui.screens.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
 * - pobieranie wydarzenia po ID z nawigacji,
 * - utrzymanie stanu loading/success/error.
 */
class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository? = null
) : ViewModel() {

    private val eventId: Int = savedStateHandle.get<Int>("eventId")
        ?: savedStateHandle.get<String>("eventId")?.toIntOrNull()
        ?: 0

    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        val repository = eventRepository
        if (repository == null) {
            _uiState.value = EventDetailsUiState(
                eventEntity = null,
                isLoading = false,
                error = "Event repository unavailable"
            )
            return
        }

        if (eventId <= 0) {
            _uiState.value = EventDetailsUiState(
                eventEntity = null,
                isLoading = false,
                error = "Invalid event id"
            )
            return
        }

        viewModelScope.launch {
            repository.getEventByIdStream(eventId)
                .catch { e ->
                    _uiState.value = EventDetailsUiState(
                        eventEntity = null,
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { event ->
                    _uiState.value = EventDetailsUiState(
                        eventEntity = event,
                        isLoading = false,
                        error = if (event == null) "Event not found" else null
                    )
                }
        }
    }
}