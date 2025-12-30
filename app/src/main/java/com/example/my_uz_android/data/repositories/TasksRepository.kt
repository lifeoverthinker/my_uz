package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.SharedTaskRequest
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.util.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class TasksRepository(
    private val tasksDao: TasksDao,
    private val supabase: SupabaseClient
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = tasksDao.getAllTasks()

    fun getTaskById(id: Int): Flow<TaskEntity?> = tasksDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)

    suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    suspend fun deleteAllTasks() = tasksDao.deleteAll()

    // --- Udostępnianie (Eksport) ---
    suspend fun shareTasks(tasks: List<TaskEntity>): String {
        val gson = Gson()
        // Konwersja listy zadań na JSON String
        val payloadJson = gson.toJson(tasks)

        // Generowanie krótkiego kodu (6 znaków: A-Z, 0-9)
        val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = (1..6)
            .map { Random.nextInt(0, charPool.length) }
            .map(charPool::get)
            .joinToString("")

        // Tworzymy obiekt żądania
        val request = SharedTaskRequest(
            shareId = code,
            payload = payloadJson
        )

        // Wstawiamy do tabeli 'shared_tasks' w Supabase
        supabase.postgrest["shared_tasks"].insert(request)

        return code
    }

    // --- Importowanie ---
    suspend fun importTasks(code: String): NetworkResult<List<TaskEntity>> {
        return try {
            // Pobieramy rekord o danym kodzie
            val result = supabase.postgrest["shared_tasks"]
                .select {
                    filter {
                        eq("share_id", code)
                    }
                }.decodeSingleOrNull<SharedTaskRequest>()

            if (result != null) {
                val gson = Gson()
                val type = object : TypeToken<List<TaskEntity>>() {}.type
                // Deserializujemy JSON String z pola payload na listę zadań
                val tasks: List<TaskEntity> = gson.fromJson(result.payload, type)

                // Zapisujemy pobrane zadania w lokalnej bazie
                // Resetujemy ID, aby Room nadał nowe
                val tasksToInsert = tasks.map { it.copy(id = 0) }
                tasksToInsert.forEach { tasksDao.insert(it) }

                NetworkResult.Success(tasksToInsert)
            } else {
                NetworkResult.Error("Nie znaleziono zadań dla kodu: $code")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Błąd importu: ${e.message}")
        }
    }
}