package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.SharedTaskRequest
import com.example.my_uz_android.data.models.TaskEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow

class TasksRepository(
    private val tasksDao: TasksDao,
    private val supabase: SupabaseClient // Dodano zależność Supabase
) {

    fun getAllTasks(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()

    fun getTasksStream(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()

    fun getTaskByIdStream(id: Int): Flow<TaskEntity?> = tasksDao.getTaskById(id)

    suspend fun getTask(id: Int): TaskEntity? = tasksDao.getTaskByIdSuspend(id)

    suspend fun getTaskById(id: Int): TaskEntity? = tasksDao.getTaskByIdSuspend(id)

    suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)

    suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    suspend fun deleteAllTasks() = tasksDao.deleteAll()

    // --- Funkcja udostępniania zadań ---
    suspend fun shareTasks(tasks: List<TaskEntity>): String {
        // 1. Generujemy krótki kod (6 znaków)
        val charPool = ('A'..'Z') + ('0'..'9')
        val code = (1..6)
            .map { charPool.random() }
            .joinToString("")

        // 2. Przygotowujemy obiekt
        val payload = SharedTaskRequest(
            shareId = code,
            tasks = tasks
        )

        // 3. Wysyłamy do tabeli 'shared_tasks' w Supabase
        supabase.from("shared_tasks").insert(payload)

        return code
    }
}