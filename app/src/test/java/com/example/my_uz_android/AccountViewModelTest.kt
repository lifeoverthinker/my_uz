package com.example.my_uz_android

import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var universityRepository: UniversityRepository
    private lateinit var classRepository: ClassRepository
    private lateinit var userCourseRepository: UserCourseRepository
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        universityRepository = mockk(relaxed = true)
        classRepository = mockk(relaxed = true)
        userCourseRepository = mockk(relaxed = true)

        every { settingsRepository.getSettingsStream() } returns flowOf(
            SettingsEntity(id = 1, selectedGroupCode = "MAIN-101")
        )
        every { userCourseRepository.getAllUserCoursesStream() } returns flowOf(emptyList())

        viewModel = AccountViewModel(
            settingsRepository = settingsRepository,
            universityRepository = universityRepository,
            classRepository = classRepository,
            userCourseRepository = userCourseRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeAdditionalCourse usuwa kierunek i czysci powiazane oraz osierocone zajecia`() = runTest {
        val course = UserCourseEntity(
            groupCode = "EXTRA-202",
            fieldOfStudy = "Informatyka",
            selectedSubgroup = "",
            faculty = "WEiA",
            studyMode = "Stacjonarne",
            semester = 1
        )
        val dummyClass = ClassEntity(
            id = 1,
            subjectName = "Bazy danych",
            classType = "Lab",
            startTime = "08:00",
            endTime = "09:30",
            dayOfWeek = 1,
            date = "2026-05-01",
            groupCode = "EXTRA-202",
            subgroup = null
        )

        coEvery { userCourseRepository.deleteUserCourse(course) } returns Unit
        coEvery { classRepository.deleteClassesForGroup("EXTRA-202") } returns listOf(dummyClass)
        coEvery { classRepository.deleteOrphanedClasses(any()) } returns emptyList()

        viewModel.removeAdditionalCourse(course)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { userCourseRepository.deleteUserCourse(course) }
        coVerify(exactly = 1) { classRepository.deleteClassesForGroup("EXTRA-202") }
        coVerify(exactly = 1) { classRepository.deleteOrphanedClasses(setOf("MAIN-101")) }
        assertFalse(viewModel.additionalUserCourses.value.contains(course))
    }
}