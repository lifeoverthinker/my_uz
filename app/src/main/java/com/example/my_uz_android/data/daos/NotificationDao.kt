package com.example.my_uz_android.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_uz_android.data.models.NotificationEntity
import kotlinx.coroutines.flow.Flow

// Powiadomienia w aplikacji
@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    // Liczba nieprzeczytanych powiadomień (do czerwonej kropki/badge)
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM notifications WHERE title = :title AND message = :message AND abs(timestamp - :timestamp) < :timeWindowMs)")
    suspend fun existsRecentNotification(title: String, message: String, timestamp: Long, timeWindowMs: Long = 60000): Boolean

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}