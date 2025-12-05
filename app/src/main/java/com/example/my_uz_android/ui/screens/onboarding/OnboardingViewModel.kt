package com.example.my_uz_android.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.ClassRepository
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
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository // POPRAWKA: Dodano brakujące repozytorium (naprawia crash)
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    val totalPages = 6

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    private fun fetchAllGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val groups = universityRepository.getGroupCodes()
                _allGroups.value = groups
            } catch (e: Exception) {
                Log.e(TAG, "Błąd podczas pobierania grup", e)
                _allGroups.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- ZAPIS DANYCH Z ONBOARDINGU ---
    fun saveOnboardingData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val isAnonymous = _selectedMode.value == OnboardingMode.ANONYMOUS
            val groupCode = _selectedGroup.value

            // 1. Pobierz szczegóły grupy
            var faculty: String? = null
            var fieldOfStudy: String? = null
            var studyMode: String? = null

            if (groupCode != null) {
                try {
                    val details = universityRepository.getGroupDetails(groupCode)
                    if (details != null) {
                        studyMode = details.studyMode
                        details.fieldInfo?.let { info ->
                            faculty = info.faculty
                            fieldOfStudy = info.name
                        }
                    }

                    // POPRAWKA: Nowa logika - Pobieranie i zapisywanie planu zajęć
                    val subgroups = _selectedSubgroups.value.toList()
                    val schedule = universityRepository.getSchedule(groupCode, subgroups)

                    classRepository.deleteAllClasses()
                    classRepository.insertClasses(schedule)
                    Log.d(TAG, "✅ Zapisano ${schedule.size} zajęć")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Błąd pobierania danych/planu", e)
                }
            }

            // 2. Zapisz ustawienia
            val settings = SettingsEntity(
                id = 0,
                isAnonymous = isAnonymous,
                userName = if (isAnonymous) "Student" else _userName.value,
                gender = _selectedGender.value.name,
                selectedGroupCode = groupCode,
                selectedSubgroup = _selectedSubgroups.value.joinToString(","),
                faculty = faculty,
                fieldOfStudy = fieldOfStudy,
                studyMode = studyMode,
                isFirstRun = false,
                isDarkMode = false,
                notificationsEnabled = true
            )

            settingsRepository.insertSettings(settings)
            _isLoading.value = false

            // Wywołaj callback nawigacji dopiero po zakończeniu zapisu
            onSuccess()
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            _isLoading.value = true
            val defaultSettings = SettingsEntity(
                id = 0,
                isAnonymous = false,
                userName = "Gościu",
                gender = null,
                selectedGroupCode = null,
                selectedSubgroup = null,
                faculty = null,
                fieldOfStudy = null,
                studyMode = null,
                isFirstRun = false,
                isDarkMode = false,
                notificationsEnabled = true
            )
            settingsRepository.insertSettings(defaultSettings)
            _isLoading.value = false
        }
    }

    fun onNextClick() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.update { it + 1 }
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
        _filteredGroups.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _availableSubgroups.value = universityRepository.getSubgroups(groupCode)
            } catch (e: Exception) {
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