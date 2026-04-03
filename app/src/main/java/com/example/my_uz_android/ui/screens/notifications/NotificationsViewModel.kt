package com.example.my_uz_android.ui.screens.notifications

/**
 * ViewModel modułu powiadomień.
 * Odpowiada za udostępnianie strumienia powiadomień, licznika nieodczytanych
 * oraz akcje oznaczania i usuwania wpisów.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.NotificationEntity
import com.example.my_uz_android.data.repositories.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Zarządza stanem i akcjami ekranu powiadomień.
 *
 * @param notificationsRepository Repozytorium danych powiadomień.
 */
class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    // Używamy teraz notificationsRepository zamiast notificationDao
    val notifications: Flow<List<NotificationEntity>> = notificationsRepository.getAllNotifications()

    // Strumień dla licznika Badge
    val unreadCount: Flow<Int> = notificationsRepository.getUnreadCount()

    /** Oznacza wszystkie powiadomienia jako przeczytane. */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
        }
    }

    // Funkcja do usuwania pojedynczego powiadomienia
    /**
     * Usuwa pojedyncze powiadomienie.
     *
     * @param notification Powiadomienie przeznaczone do usunięcia.
     */
    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationsRepository.deleteNotification(notification)
        }
    }

    /** Usuwa wszystkie powiadomienia z listy. */
    fun clearAll() {
        viewModelScope.launch {
            notificationsRepository.clearAll()
        }
    }
}