package com.example.my_uz_android

import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.repositories.FavoritesRepository
import com.example.my_uz_android.data.repositories.TeacherDetailsDto
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchViewModel
import com.example.my_uz_android.util.NetworkResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleSearchViewModelTest {

    private val universityRepository: UniversityRepository = mockk(relaxed = true)
    private val favoritesRepository: FavoritesRepository = mockk(relaxed = true)
    private val favoritesFlow = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { favoritesRepository.favoritesStream } returns favoritesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favorites search consistency rozroznia ten sam resourceId po typie`() = runTest {
        favoritesFlow.value = listOf(
            FavoriteEntity(name = "Jan Kowalski", resourceId = "Jan Kowalski", type = "teacher")
        )

        coEvery { universityRepository.getGroupCodes() } returns NetworkResult.Success(
            listOf("Jan Kowalski")
        )
        coEvery { universityRepository.getAllTeachersWithDetails() } returns NetworkResult.Success(
            listOf(TeacherDetailsDto(name = "Jan Kowalski", email = null, institute = null))
        )
        coEvery { universityRepository.searchGroups(any()) } returns NetworkResult.Success(emptyList())

        val viewModel = ScheduleSearchViewModel(universityRepository, favoritesRepository)

        viewModel.onQueryChange("Jan")
        advanceTimeBy(300)
        advanceUntilIdle()

        val grouped = viewModel.uiState.value.searchResults.groupBy { it.type }
        val groupResult = grouped["group"]?.firstOrNull { it.name == "Jan Kowalski" }
        val teacherResult = grouped["teacher"]?.firstOrNull { it.name == "Jan Kowalski" }

        assertEquals(false, groupResult?.isFavorite)
        assertEquals(true, teacherResult?.isFavorite)
    }
}

