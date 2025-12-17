package com.example.my_uz_android.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingMode {
    ANONYMOUS,
    DATA
}

enum class UserGender {
    STUDENT,
    STUDENTKA
}

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    val totalPages = 6 // Liczba stron onboardingu

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Personalizacja
    private val _selectedMode = MutableStateFlow<OnboardingMode?>(null)
    val selectedMode: StateFlow<OnboardingMode?> = _selectedMode.asStateFlow()

    private val _selectedGender = MutableStateFlow(UserGender.STUDENT)
    val selectedGender: StateFlow<UserGender> = _selectedGender.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userSurname = MutableStateFlow("")
    val userSurname: StateFlow<String> = _userSurname.asStateFlow()

    // Grupa
    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()

    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups: StateFlow<Set<String>> = _selectedSubgroups.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    private val _allGroups = MutableStateFlow<List<String>>(emptyList())
    private val _filteredGroups = MutableStateFlow<List<String>>(emptyList())
    val filteredGroups: StateFlow<List<String>> = _filteredGroups.asStateFlow()

    init {
        // Obsługa wyszukiwania grup
        _groupSearchQuery
            .onEach { query ->
                if (query.isEmpty()) {
                    _filteredGroups.value = emptyList()
                } else {
                    val result = _allGroups.value.filter {
                        it.contains(query, ignoreCase = true)
                    }.take(5)
                    _filteredGroups.value = result
                }
            }
            .launchIn(viewModelScope)

        fetchAllGroups()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun fetchAllGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = universityRepository.getGroupCodes()) {
                is NetworkResult.Success -> {
                    _allGroups.value = result.data ?: emptyList()
                }
                is NetworkResult.Error -> {
                    // Nie blokujemy UI błędem na start, po prostu lista będzie pusta
                    // _errorMessage.value = result.message
                    _allGroups.value = emptyList()
                    Log.e(TAG, "Błąd pobierania grup: ${result.message}")
                }
                is NetworkResult.Loading -> { }
            }

            _isLoading.value = false
        }
    }

    // --- ZAPIS DANYCH Z ONBOARDINGU (Zatwierdzenie) ---
    fun saveOnboardingData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val isAnonymous = _selectedMode.value == OnboardingMode.ANONYMOUS
            val groupCode = _selectedGroup.value

            // 1. Pobierz szczegóły grupy i plan (jeśli wybrano grupę)
            var faculty: String? = null
            var fieldOfStudy: String? = null
            var studyMode: String? = null
            var downloadSuccess = true

            if (!groupCode.isNullOrBlank()) {
                // A. Szczegóły grupy (Wydział, Kierunek)
                when (val detailsResult = universityRepository.getGroupDetails(groupCode)) {
                    is NetworkResult.Success -> {
                        val details = detailsResult.data
                        if (details != null) {
                            studyMode = details.studyMode
                            details.fieldInfo?.let { info ->
                                faculty = info.faculty
                                fieldOfStudy = info.name
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Błąd pobierania szczegółów grupy: ${detailsResult.message}")
                    }
                    else -> {}
                }

                // B. Pobierz Plan Zajęć
                val subgroups = _selectedSubgroups.value.toList()
                // Jeśli nie wybrano podgrup, pobieramy dla wszystkich (pusta lista w API często to oznacza)
                // lub obsługujemy to w repozytorium

                when (val scheduleResult = universityRepository.getSchedule(groupCode, subgroups)) {
                    is NetworkResult.Success -> {
                        val schedule = scheduleResult.data ?: emptyList()
                        classRepository.deleteAllClasses() // Czyścimy stare (bezpiecznik)
                        classRepository.insertClasses(schedule)
                        Log.d(TAG, "✅ Zapisano ${schedule.size} zajęć")
                    }
                    is NetworkResult.Error -> {
                        // Jeśli użytkownik wybrał grupę, a nie udało się pobrać planu - to błąd
                        _errorMessage.value = "Nie udało się pobrać planu zajęć. Sprawdź połączenie."
                        downloadSuccess = false
                    }
                    else -> {}
                }
            }

            if (!downloadSuccess) {
                _isLoading.value = false
                return@launch
            }

            // 2. Utwórz obiekt ustawień
            val settings = SettingsEntity(
                id = 0, // Room nadpisze/zinkrementuje jeśli to Insert
                isAnonymous = isAnonymous,
                // Jeśli anonimowy lub nie wpisał imienia -> "Student" lub puste
                userName = if (isAnonymous) "" else _userName.value.ifBlank { "" },
                gender = _selectedGender.value.name,
                selectedGroupCode = groupCode,
                selectedSubgroup = _selectedSubgroups.value.joinToString(","),
                faculty = faculty,
                fieldOfStudy = fieldOfStudy,
                studyMode = studyMode,
                // Flagi systemowe
                isFirstRun = false,
                isDarkMode = false,
                notificationsEnabled = true,
                offlineModeEnabled = false // Domyślnie online po instalacji
            )

            // 3. Zapisz do bazy -> To wyzwoli Flow w HomeViewModel i zaktualizuje UI
            settingsRepository.insertSettings(settings)

            _isLoading.value = false
            onSuccess() // Nawigacja do ekranu głównego
        }
    }

    // --- POMINIĘCIE ONBOARDINGU (Tryb Gościa) ---
    fun skipOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            // ✅ Ustawienia dla Gościa (Zgodne z wymaganiami)
            val guestSettings = SettingsEntity(
                id = 0,
                isAnonymous = true,      // Jest anonimowy
                userName = "",           // Puste imię -> Powitanie "Cześć!"
                gender = null,
                selectedGroupCode = null,
                selectedSubgroup = null,
                faculty = null,
                fieldOfStudy = null,
                studyMode = null,
                isFirstRun = false,      // Onboarding zakończony
                isDarkMode = false,
                notificationsEnabled = true,
                offlineModeEnabled = false
            )

            // Czyścimy ewentualne śmieci z bazy (opcjonalnie)
            classRepository.deleteAllClasses()

            // Zapisz ustawienia
            settingsRepository.insertSettings(guestSettings)

            _isLoading.value = false
            onSuccess()
        }
    }

    // --- NAWIGACJA W STRONACH ONBOARDINGU ---
    fun onNextClick() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.update { it + 1 }
            // Jeśli przechodzimy do strony wyboru grupy (zakładamy że to np. index 2),
            // a lista jest pusta, ponawiamy próbę pobrania.
            if (_currentPage.value == 2 && _allGroups.value.isEmpty()) {
                fetchAllGroups()
            }
        }
    }

    fun onBackClick() {
        if (_currentPage.value > 0) {
            _currentPage.update { it - 1 }
        }
    }

    // --- SETTERY DANYCH UI ---
    fun setMode(mode: OnboardingMode) { _selectedMode.value = mode }
    fun setGender(gender: UserGender) { _selectedGender.value = gender }
    fun setUserName(name: String) { _userName.value = name }
    fun setUserSurname(surname: String) { _userSurname.value = surname }

    fun setGroupSearchQuery(query: String) {
        _groupSearchQuery.value = query
        if (query.isEmpty()) {
            _selectedGroup.value = null
            _availableSubgroups.value = emptyList()
        }
    }

    fun selectGroup(groupCode: String) {
        _selectedGroup.value = groupCode
        _groupSearchQuery.value = groupCode
        _filteredGroups.value = emptyList() // Ukryj podpowiedzi po wyborze

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Pobierz podgrupy dla wybranej grupy
            when (val result = universityRepository.getSubgroups(groupCode)) {
                is NetworkResult.Success -> {
                    _availableSubgroups.value = result.data ?: emptyList()
                }
                is NetworkResult.Error -> {
                    // Nie blokujemy, po prostu brak podgrup
                    Log.e(TAG, "Błąd pobierania podgrup: ${result.message}")
                    _availableSubgroups.value = emptyList()
                }
                else -> {}
            }

            _isLoading.value = false
            _selectedSubgroups.value = emptySet() // Reset wyboru podgrup
        }
    }

    fun toggleSubgroup(subgroup: String) {
        _selectedSubgroups.update { currentSet ->
            if (currentSet.contains(subgroup)) currentSet - subgroup else currentSet + subgroup
        }
    }
}