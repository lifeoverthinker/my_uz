package com.example.my_uz_android.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.daos.NotificationDao
import com.example.my_uz_android.data.models.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationsViewModel(private val notificationDao: NotificationDao) : ViewModel() {

    val notifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    // Nowy strumień dla licznika Badge
    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    // Funkcja do usuwania pojedynczego powiadomienia
    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationDao.deleteNotification(notification)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationDao.clearAll()
        }
    }
}