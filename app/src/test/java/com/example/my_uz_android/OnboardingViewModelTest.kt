package com.example.my_uz_android

// KLUCZOWY IMPORT - wskazujemy, gdzie dokładnie leży ViewModel
import com.example.my_uz_android.ui.screens.onboarding.OnboardingViewModel

import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    // Testowy dispatcher, który pozwala kontrolować upływ czasu w coroutines
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: OnboardingViewModel

    // Mockowanie repozytoriów
    private val settingsRepository: SettingsRepository = mockk()
    private val universityRepository: UniversityRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    @Before
    fun setUp() {
        // Podmiana głównego dispatchera Androida na testowy
        Dispatchers.setMain(testDispatcher)

        // ViewModel w bloku init woła loadGroups(), więc musimy to zmockować przed jego inicjalizacją
        coEvery { universityRepository.getGroupCodes() } returns mockk<NetworkResult.Success<List<String>>> {
            every { data } returns listOf("32INF-SP", "11MED-SP")
        }

        viewModel = OnboardingViewModel(
            settingsRepository,
            universityRepository,
            classRepository
        )
    }

    @After
    fun tearDown() {
        // Sprzątanie po testach
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `sprawdz czy na poczatku stan UI State jest poprawny i nie ma wybranej grupy`() = runTest {
        // Assert - Sprawdzamy stan początkowy ViewModelu
        assertNull("Początkowa grupa powinna być null", viewModel.selectedGroup.value)
        assertTrue("Początkowe imię powinno być puste", viewModel.userName.value.isEmpty())
        assertTrue("Początkowe nazwisko powinno być puste", viewModel.userSurname.value.isEmpty())
        assertNull("Początkowa płeć powinna być null", viewModel.selectedGender.value)
        assertTrue("Lista podgrup powinna być pusta", viewModel.selectedSubgroups.value.isEmpty())
    }

    @Test
    fun `sprawdz czy saveOnboardingData poprawnie wola repository i wysyla sygnal zakonczenia`() = runTest {
        // Arrange
        var isNavigationEventTriggered = false

        // Mockowanie pobierania podgrup dla wybieranej grupy
        coEvery { universityRepository.getSubgroups(any()) } returns mockk<NetworkResult.Success<List<String>>> {
            every { data } returns listOf("L1", "L2")
        }

        // Precyzyjne określenie typu generycznego dla błędu
        coEvery { universityRepository.getGroupDetails(any()) } returns mockk<NetworkResult.Error<com.example.my_uz_android.data.repositories.GroupDetailsDto>>(relaxed = true)

        // Mockowanie logiki zapisu SettingsRepository
        coEvery { settingsRepository.getSettingsStream() } returns flowOf(null) // Brak obecnych ustawień
        coEvery { settingsRepository.insertOrUpdate(any()) } just Runs // Akcja zapisu przechodzi bez błędu

        // Act - Symulacja interakcji użytkownika
        viewModel.setUserName("Jan")
        viewModel.setUserSurname("Kowalski")
        viewModel.setGender(UserGender.STUDENT)
        viewModel.selectGroup("32INF-SP")

        // Wykonanie pending coroutines wywołanych przez `selectGroup` (pobieranie podgrup)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSubgroup("L1")

        // Zapis z przekazaniem callbacku na success
        viewModel.saveOnboardingData(
            onSuccess = { isNavigationEventTriggered = true }
        )

        // Przewijamy czas coroutines do końca zapisu
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert

        // 1. Sprawdzenie czy wywołano insertOrUpdate i przechwycenie danych
        val settingsSlot = slot<SettingsEntity>()
        coVerify(exactly = 1) { settingsRepository.insertOrUpdate(capture(settingsSlot)) }

        val savedEntity = settingsSlot.captured
        assertEquals("Jan Kowalski", savedEntity.userName)
        assertEquals("Student", savedEntity.gender)
        assertEquals("32INF-SP", savedEntity.selectedGroupCode)
        assertEquals("L1", savedEntity.selectedSubgroup)
        assertFalse("Flaga isAnonymous powinna być false przy pełnym zapisie", savedEntity.isAnonymous)
        assertFalse("Flaga isFirstRun powinna być zgaszona po onboardingu", savedEntity.isFirstRun)

        // 2. Sprawdzenie czy callback został wywołany
        assertTrue("Sygnał o zakończeniu onboardingu (onSuccess) nie został wywołany", isNavigationEventTriggered)
    }
}