package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.TaskEntity
import kotlinx.coroutines.flow.Flow

class TasksRepository(private val tasksDao: TasksDao) {
    fun getAllTasksStream(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()
    suspend fun insertTask(task: TaskEntity) = tasksDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = tasksDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = tasksDao.deleteTask(task)
    suspend fun clearCompleted() = tasksDao.deleteCompletedTasks()
}