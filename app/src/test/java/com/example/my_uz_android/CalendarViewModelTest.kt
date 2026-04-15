package com.example.my_uz_android

import android.app.Application
import android.util.Log
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.repositories.*
import com.example.my_uz_android.ui.screens.calendar.CalendarUiState
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.util.NetworkResult

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

    @Test
    fun `spam refresh planu powinien uruchomic tylko jeden sync naraz`() = runTest {
        every { settingsRepository.getSettingsStream() } returns flowOf(
            SettingsEntity(selectedGroupCode = "GR-1")
        )
        every { userCourseRepository.getAllUserCoursesStream() } returns flowOf(emptyList())
        coEvery {
            universityRepository.refreshSchedule("GR-1", null, classRepository)
        } coAnswers {
            delay(200)
            NetworkResult.Success(Unit)
        }

        viewModel = CalendarViewModel(
            application,
            favoritesRepository,
            classRepository,
            settingsRepository,
            userCourseRepository,
            universityRepository,
            tasksRepository
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.refreshMyPlan()
        viewModel.refreshMyPlan()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            universityRepository.refreshSchedule("GR-1", null, classRepository)
        }

        collectJob.cancel()
    }

    @Test
    fun `przelaczanie preview planow powinno zachowac tylko ostatni wynik`() = runTest {
        val classesA = listOf(
            ClassEntity(
                id = 10,
                subjectName = "Plan A",
                classType = "Wykład",
                startTime = "08:00",
                endTime = "09:30",
                dayOfWeek = 1,
                date = "2026-03-16",
                groupCode = "A",
                subgroup = null
            )
        )
        val classesB = listOf(
            ClassEntity(
                id = 11,
                subjectName = "Plan B",
                classType = "Laboratorium",
                startTime = "10:00",
                endTime = "11:30",
                dayOfWeek = 1,
                date = "2026-03-16",
                groupCode = "B",
                subgroup = null
            )
        )

        coEvery { universityRepository.getSchedule("PLAN_A", any()) } coAnswers {
            delay(200)
            NetworkResult.Success(classesA)
        }
        coEvery { universityRepository.getSchedule("PLAN_B", any()) } returns NetworkResult.Success(classesB)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.selectPreviewPlan("PLAN_A", "group")
        viewModel.selectPreviewPlan("PLAN_B", "group")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("PLAN_B", state.selectedPlanName)
        assertEquals(1, state.visibleClasses.size)
        assertEquals("Plan B", state.visibleClasses.first().subjectName)

        collectJob.cancel()
    }

    @Test
    fun `powrot do mojego planu powinien uniewaznic opozniony wynik preview`() = runTest {
        every { classRepository.getAllClassesStream() } returns flowOf(
            listOf(
                ClassEntity(
                    id = 21,
                    subjectName = "Moj plan",
                    classType = "Wykład",
                    startTime = "12:00",
                    endTime = "13:30",
                    dayOfWeek = 1,
                    date = "2026-03-16",
                    groupCode = "GR-1",
                    subgroup = null
                )
            )
        )
        every { userCourseRepository.getAllUserCoursesStream() } returns flowOf(emptyList())
        every { favoritesRepository.favoritesStream } returns flowOf(emptyList())
        every { settingsRepository.getSettingsStream() } returns flowOf(null)
        every { tasksRepository.getAllTasks() } returns flowOf(emptyList())
        coEvery { universityRepository.getSchedule("PLAN_X", any()) } coAnswers {
            delay(200)
            NetworkResult.Success(
                listOf(
                    ClassEntity(
                        id = 22,
                        subjectName = "Obcy plan",
                        classType = "Ćwiczenia",
                        startTime = "08:00",
                        endTime = "09:30",
                        dayOfWeek = 1,
                        date = "2026-03-16",
                        groupCode = "X",
                        subgroup = null
                    )
                )
            )
        }

        viewModel = CalendarViewModel(
            application,
            favoritesRepository,
            classRepository,
            settingsRepository,
            userCourseRepository,
            universityRepository,
            tasksRepository
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.selectPreviewPlan("PLAN_X", "group")
        viewModel.selectMyPlan(forceRefresh = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.currentSource is com.example.my_uz_android.ui.screens.calendar.ScheduleSource.MyPlan)
        assertEquals("Moj plan", state.visibleClasses.first().subjectName)

        collectJob.cancel()
    }
}