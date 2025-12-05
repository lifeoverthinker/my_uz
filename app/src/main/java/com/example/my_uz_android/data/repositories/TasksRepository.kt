package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.TaskEntity
import kotlinx.coroutines.flow.Flow

class TasksRepository(private val tasksDao: TasksDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()

    fun getTasksStream(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()  // DODANE

    fun getTaskByIdStream(id: Int): Flow<TaskEntity?> = tasksDao.getTaskById(id)

    suspend fun getTask(id: Int): TaskEntity? = tasksDao.getTaskByIdSuspend(id)  // DODANE

    suspend fun getTaskById(id: Int): TaskEntity? = tasksDao.getTaskByIdSuspend(id)

    suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)

    suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    suspend fun deleteAllTasks() = tasksDao.deleteAll()
}
