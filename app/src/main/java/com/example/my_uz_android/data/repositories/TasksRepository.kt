package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.TaskEntity
import kotlinx.coroutines.flow.Flow

class TasksRepository(private val tasksDao: TasksDao) {

    // Strumień wszystkich zadań (naprawia błąd Unresolved reference)
    fun getTasksStream(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)

    suspend fun getTask(id: Int): TaskEntity? = tasksDao.getTask(id)
}