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
    val totalPages = 6

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Personalizacja - Gender jest nullem na początku (nic nie wybrane)
    private val _selectedGender = MutableStateFlow<UserGender?>(null)
    val selectedGender: StateFlow<UserGender?> = _selectedGender.asStateFlow()

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
            when (val result = universityRepository.getGroupCodes()) {
                is NetworkResult.Success -> _allGroups.value = result.data ?: emptyList()
                is NetworkResult.Error -> {
                    _allGroups.value = emptyList()
                    Log.e(TAG, "Błąd pobierania grup: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Metoda Save - zawsze zapisuje "zalogowanego" usera
    fun saveOnboardingData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val groupCode = _selectedGroup.value
            var faculty: String? = null
            var fieldOfStudy: String? = null
            var studyMode: String? = null
            var downloadSuccess = true

            // Pobieranie planu
            if (!groupCode.isNullOrBlank()) {
                val detailsResult = universityRepository.getGroupDetails(groupCode)
                if (detailsResult is NetworkResult.Success) {
                    val details = detailsResult.data
                    if (details != null) {
                        studyMode = details.studyMode
                        details.fieldInfo?.let { info ->
                            faculty = info.faculty
                            fieldOfStudy = info.name
                        }
                    }
                }

                val subgroups = _selectedSubgroups.value.toList()
                when (val scheduleResult = universityRepository.getSchedule(groupCode, subgroups)) {
                    is NetworkResult.Success -> {
                        val schedule = scheduleResult.data ?: emptyList()
                        classRepository.deleteAllClasses()
                        classRepository.insertClasses(schedule)
                    }
                    is NetworkResult.Error -> {
                        _errorMessage.value = "Nie udało się pobrać planu. Sprawdź internet."
                        downloadSuccess = false
                    }
                    else -> {}
                }
            }

            if (!downloadSuccess) {
                _isLoading.value = false
                return@launch
            }

            // Tworzenie pełnego imienia
            val fullName = if (_userSurname.value.isNotBlank()) {
                "${_userName.value.trim()} ${_userSurname.value.trim()}"
            } else {
                _userName.value.trim()
            }

            // Domyślnie STUDENT jeśli jakimś cudem null (ale UI blokuje)
            val genderStr = _selectedGender.value?.name ?: UserGender.STUDENT.name

            val settings = SettingsEntity(
                id = 0,
                isAnonymous = false, // Zawsze false, bo user podał dane
                userName = fullName,
                gender = genderStr,
                selectedGroupCode = groupCode,
                selectedSubgroup = _selectedSubgroups.value.joinToString(","),
                faculty = faculty,
                fieldOfStudy = fieldOfStudy,
                studyMode = studyMode,
                isFirstRun = false,
                isDarkMode = false,
                notificationsEnabled = true,
                offlineModeEnabled = false
            )

            settingsRepository.clearSettings()
            settingsRepository.insertSettings(settings)

            _isLoading.value = false
            onSuccess()
        }
    }

    // Metoda pomocnicza dla LandingScreen (skip nie jest już używany w UI, ale może zostać w kodzie)
    fun skipOnboarding(onSuccess: () -> Unit) {
        saveOnboardingData(onSuccess)
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
        if (_currentPage.value > 0) _currentPage.update { it - 1 }
    }

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
            when (val result = universityRepository.getSubgroups(groupCode)) {
                is NetworkResult.Success -> _availableSubgroups.value = result.data ?: emptyList()
                else -> _availableSubgroups.value = emptyList()
            }
            _isLoading.value = false
            _selectedSubgroups.value = emptySet()
        }
    }

    fun toggleSubgroup(subgroup: String) {
        _selectedSubgroups.update { currentSet ->
            if (currentSet.contains(subgroup)) currentSet - subgroup else currentSet + subgroup
        }
    }
}