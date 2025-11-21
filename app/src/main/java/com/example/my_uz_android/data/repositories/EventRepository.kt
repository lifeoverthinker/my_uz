package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.EventDao
import com.example.my_uz_android.data.models.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    fun getEventById(id: Int): Flow<EventEntity?> = eventDao.getEventById(id)
    suspend fun insertEvents(events: List<EventEntity>) = eventDao.insertEvents(events)
}