package com.example.my_uz_android.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.NotificationEntity
import com.example.my_uz_android.data.repositories.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    val notifications: Flow<List<NotificationEntity>> = notificationsRepository.getAllNotifications()

    val unreadCount: Flow<Int> = notificationsRepository.getUnreadCount()

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
        }
    }

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