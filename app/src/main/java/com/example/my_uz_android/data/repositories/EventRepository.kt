package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.EventDao
import com.example.my_uz_android.data.models.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    fun getAllEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEventByIdStream(id: Int): Flow<EventEntity?> = eventDao.getEventById(id)

    suspend fun getEventById(id: Int): EventEntity? = eventDao.getEventByIdSuspend(id)

    suspend fun insertEvent(event: EventEntity) = eventDao.insert(event)

    // ✅ DODANE: Metoda do zapisu listy wydarzeń (wykorzystywana przy imporcie)
    suspend fun insertEvents(events: List<EventEntity>) {
        events.forEach { eventDao.insert(it) }
    }

    suspend fun updateEvent(event: EventEntity) = eventDao.update(event)

    suspend fun deleteEvent(event: EventEntity) = eventDao.delete(event)

    suspend fun deleteAllEvents() = eventDao.deleteAll()
}