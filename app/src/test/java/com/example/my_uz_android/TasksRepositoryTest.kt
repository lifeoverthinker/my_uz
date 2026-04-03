package com.example.my_uz_android

import com.example.my_uz_android.data.daos.TasksDao
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.TasksRepository
import com.example.my_uz_android.util.NetworkResult
import io.github.jan.supabase.SupabaseClient
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksRepositoryTest {

    private lateinit var tasksDao: TasksDao
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var repository: TasksRepository

    @Before
    fun setup() {
        tasksDao = mockk(relaxed = true)
        supabaseClient = mockk(relaxed = true)
        repository = TasksRepository(tasksDao, supabaseClient)
    }

    @Test
    fun `shareTasks catches network exception and returns Error`() = runTest {
        val mockTasks = listOf(TaskEntity(id = 1, title = "Zadanie", description = null))

        // Upraszczamy: symulujemy od razu błąd sieci / błąd biblioteki
        every { supabaseClient.pluginManager } throws RuntimeException("Brak połączenia z siecią")

        val result = repository.shareTasks(mockTasks)

        // Sprawdzamy czy nasza logika try-catch poprawnie to wyłapała
        assertTrue("Powinien zwrócić błąd sieciowy", result is NetworkResult.Error)
        assertTrue((result as NetworkResult.Error).message?.contains("Błąd udostępniania") == true)
    }

    @Test
    fun `importTasks catches network exception and returns Error`() = runTest {
        every { supabaseClient.pluginManager } throws RuntimeException("Brak połączenia z siecią")

        val result = repository.importTasks("ABC123")

        assertTrue("Powinien zwrócić błąd sieciowy", result is NetworkResult.Error)
        assertTrue((result as NetworkResult.Error).message?.contains("Błąd importu") == true)
    }

    @Test
    fun `getAllTasks returns flow from DAO directly`() = runTest {
        val mockTasks = listOf(TaskEntity(id = 1, title = "Test DAO", description = null))

        // Testujemy komunikację z lokalną bazą (Room)
        every { tasksDao.getAllTasks() } returns flowOf(mockTasks)

        repository.getAllTasks().collect { tasks ->
            assertEquals("Powinno zwrócić jedno zadanie", 1, tasks.size)
            assertEquals("Test DAO", tasks[0].title)
        }
    }
}