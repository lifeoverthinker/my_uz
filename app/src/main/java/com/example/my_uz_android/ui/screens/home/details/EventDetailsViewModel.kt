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
 * - pobranie wydarzenia po ID,
 * - utrzymanie stanu loading/success/error,
 * - usunięcie wydarzenia z bazy.
 */
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

    /**
     * Ładuje wydarzenie z bazy danych.
     *
     * Zasada:
     * - brak rekordu = stan błędu (bez sztucznych danych mock),
     * - wyjątek = stan błędu technicznego.
     */
    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.value = EventDetailsUiState(isLoading = true)

            runCatching { eventRepository.getEventById(eventId) }
                .onSuccess { event ->
                    if (event != null) {
                        _uiState.value = EventDetailsUiState(
                            eventEntity = event,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = EventDetailsUiState(
                            eventEntity = null,
                            isLoading = false,
                            error = "Nie znaleziono wydarzenia o ID=$eventId"
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = EventDetailsUiState(
                        eventEntity = null,
                        isLoading = false,
                        error = throwable.message ?: "Błąd pobierania wydarzenia"
                    )
                }
        }
    }

    /**
     * Usuwa aktualne wydarzenie z bazy.
     */
    fun deleteEvent() {
        viewModelScope.launch {
            uiState.value.eventEntity?.let { eventRepository.deleteEvent(it) }
        }
    }
}