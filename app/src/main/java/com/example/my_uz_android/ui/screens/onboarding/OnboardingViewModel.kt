package com.example.my_uz_android.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
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
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    // Strony: 0=Welcome, 1=Personalization, 2=GroupSelection, 3-5=Info
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    val totalPages = 6

    // Stan ładowania (dla paska postępu)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Ekran 2 (Personalizacja) ---
    private val _selectedMode = MutableStateFlow<OnboardingMode?>(null)
    val selectedMode: StateFlow<OnboardingMode?> = _selectedMode.asStateFlow()

    private val _selectedGender = MutableStateFlow(UserGender.STUDENT)
    val selectedGender: StateFlow<UserGender> = _selectedGender.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userSurname = MutableStateFlow("")
    val userSurname: StateFlow<String> = _userSurname.asStateFlow()

    // --- Ekran 3 (Wybór Grupy) ---
    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()

    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups: StateFlow<Set<String>> = _selectedSubgroups.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    // Lista wszystkich grup pobrana z bazy
    private val _allGroups = MutableStateFlow<List<String>>(emptyList())
    // Lista przefiltrowana do wyświetlenia w dropdownie
    private val _filteredGroups = MutableStateFlow<List<String>>(emptyList())
    val filteredGroups: StateFlow<List<String>> = _filteredGroups.asStateFlow()

    init {
        // Nasłuchiwanie wpisywania tekstu i filtrowanie
        _groupSearchQuery
            .onEach { query ->
                if (query.isEmpty()) {
                    _filteredGroups.value = emptyList()
                } else {
                    // Filtrowanie lokalne
                    val result = _allGroups.value.filter {
                        it.contains(query, ignoreCase = true)
                    }.take(5)
                    _filteredGroups.value = result
                }
            }
            .launchIn(viewModelScope)

        fetchAllGroups()
    }

    private fun fetchAllGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Rozpoczynam pobieranie grup z Supabase...")
                val groups = universityRepository.getGroupCodes()
                Log.d(TAG, "Pobrano grup: ${groups.size}")
                _allGroups.value = groups
            } catch (e: Exception) {
                Log.e(TAG, "Błąd podczas pobierania grup: ${e.message}", e)
                _allGroups.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Logika Zapisu (Przycisk "Gotowe!") ---
    fun saveOnboardingData() {
        viewModelScope.launch {
            val isAnonymous = _selectedMode.value == OnboardingMode.ANONYMOUS

            val settings = SettingsEntity(
                id = 1,
                isAnonymous = isAnonymous,
                userName = if (isAnonymous) "Student" else _userName.value,
                selectedGroupCode = _selectedGroup.value,
                selectedSubgroup = _selectedSubgroups.value.joinToString(","),
                isFirstRun = false,
                isDarkMode = false,
                notificationsEnabled = true
            )

            settingsRepository.insertSettings(settings)
        }
    }

    // --- Nawigacja ---
    fun onNextClick() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.update { it + 1 }
            // Ponowna próba pobrania grup jeśli wchodzimy na ekran wyboru, a lista pusta
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

    // --- Settery ---
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
        _filteredGroups.value = emptyList() // Ukryj listę po wybraniu

        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Pobieranie podgrup dla: $groupCode")
                _availableSubgroups.value = universityRepository.getSubgroups(groupCode)
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania podgrup", e)
                _availableSubgroups.value = emptyList()
            } finally {
                _isLoading.value = false
            }
            _selectedSubgroups.value = emptySet()
        }
    }

    fun toggleSubgroup(subgroup: String) {
        _selectedSubgroups.update { currentSet ->
            if (currentSet.contains(subgroup)) currentSet - subgroup else currentSet + subgroup
        }
    }
}