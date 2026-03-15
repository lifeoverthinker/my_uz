package com.example.my_uz_android.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.NotificationEntity
import com.example.my_uz_android.data.repositories.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    // Używamy teraz notificationsRepository zamiast notificationDao
    val notifications: Flow<List<NotificationEntity>> = notificationsRepository.getAllNotifications()

    // Strumień dla licznika Badge
    val unreadCount: Flow<Int> = notificationsRepository.getUnreadCount()

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
        }
    }

    // Funkcja do usuwania pojedynczego powiadomienia
    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationsRepository.deleteNotification(notification)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationsRepository.clearAll()
        }
    }
}