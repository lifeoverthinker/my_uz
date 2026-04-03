package com.example.my_uz_android

import android.app.Application
import android.util.Log
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.*
import com.example.my_uz_android.ui.screens.calendar.CalendarUiState
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private lateinit var viewModel: CalendarViewModel

    private val application: Application = mockk(relaxed = true)
    private val favoritesRepository: FavoritesRepository = mockk(relaxed = true)
    private val classRepository: ClassRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val userCourseRepository: UserCourseRepository = mockk(relaxed = true)
    private val universityRepository: UniversityRepository = mockk(relaxed = true)
    private val tasksRepository: TasksRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Mockujemy systemowego Log'a, aby testy na PC nie wyrzucały błędu "not mocked"
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)

        val fakeClasses = listOf(
            ClassEntity(id = 1, subjectName = "Matematyka", classType = "Wykład", startTime = "08:00", endTime = "09:30", dayOfWeek = 1, date = "2026-03-16", groupCode = "GR-1", subgroup = null)
        )

        every { classRepository.getAllClassesStream() } returns flowOf(fakeClasses)
        every { userCourseRepository.getAllUserCoursesStream() } returns flowOf(emptyList())
        every { favoritesRepository.favoritesStream } returns flowOf(emptyList())
        every { settingsRepository.getSettingsStream() } returns flowOf(null)
        every { tasksRepository.getAllTasks() } returns flowOf(emptyList())

        viewModel = CalendarViewModel(
            application, favoritesRepository, classRepository, settingsRepository,
            userCourseRepository, universityRepository, tasksRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `gdy brakuje aktywnych kodow grup z powodu opoznienia, wszystkie pobrane zajecia powinny byc widoczne`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        val currentState = viewModel.uiState.value

        assertEquals("Brak widocznych zajęć! Zabezpieczenie nie zadziałało.", 1, currentState.visibleClasses.size)
        assertEquals("Matematyka", currentState.visibleClasses.first().subjectName)

        collectJob.cancel()
    }
}