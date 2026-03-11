package com.example.my_uz_android.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    // --- Stany UI ---
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val totalPages = 6

    // --- Dane Użytkownika ---
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userSurname = MutableStateFlow("")
    val userSurname: StateFlow<String> = _userSurname.asStateFlow()

    private val _selectedGender = MutableStateFlow<UserGender?>(null)
    val selectedGender: StateFlow<UserGender?> = _selectedGender.asStateFlow()

    // --- Wybór Grupy ---
    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups: StateFlow<Set<String>> = _selectedSubgroups.asStateFlow()

    // Lista grup z serwera
    private val _allGroups = MutableStateFlow<List<String>>(emptyList())

    // Filtrowanie grup
    val filteredGroups: StateFlow<List<String>> = combine(
        _groupSearchQuery,
        _allGroups
    ) { query, allGroups ->
        if (query.isBlank()) {
            emptyList()
        } else {
            allGroups.filter { it.contains(query, ignoreCase = true) }
                .sorted() // Sortowanie alfabetyczne
                .take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            when (val result = universityRepository.getGroupCodes()) {
                is NetworkResult.Success -> {
                    // POPRAWKA: Filtrowanie pustych wartości i "null" oraz sortowanie
                    val groups = (result.data ?: emptyList())
                        .filter { !it.isNullOrBlank() && it != "null" }
                        .sorted()
                    _allGroups.value = groups
                }

                else -> {
                    _allGroups.value = emptyList()
                }
            }
        }
    }

    // --- Metody obsługi UI ---

    fun onNextClick() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value += 1
        }
    }

    fun onBackClick() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
        }
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setUserSurname(surname: String) {
        _userSurname.value = surname
    }

    fun setGender(gender: UserGender) {
        _selectedGender.value = gender
    }

    fun setGroupSearchQuery(query: String) {
        _groupSearchQuery.value = query
        if (query.isBlank()) {
            _selectedGroup.value = null
            _availableSubgroups.value = emptyList()
            _selectedSubgroups.value = emptySet()
        }
    }

    fun selectGroup(group: String) {
        _selectedGroup.value = group
        _groupSearchQuery.value = group
        _selectedSubgroups.value = emptySet()
        loadSubgroupsForGroup(group)
    }

    private fun loadSubgroupsForGroup(group: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = universityRepository.getSubgroups(group)) {
                is NetworkResult.Success -> {
                    // POPRAWKA: Filtrowanie podgrup (żeby nie pokazywało pustej opcji)
                    val subgroups = (result.data ?: emptyList())
                        .filter { !it.isNullOrBlank() && it != "null" }
                        .sorted()
                    _availableSubgroups.value = subgroups
                }

                else -> {
                    _availableSubgroups.value = emptyList()
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleSubgroup(subgroup: String) {
        val current = _selectedSubgroups.value.toMutableSet()
        if (current.contains(subgroup)) {
            current.remove(subgroup)
        } else {
            current.add(subgroup)
        }
        _selectedSubgroups.value = current
    }

    // Funkcja Skip (Pomiń) - zachowana oryginalna logika
    fun skipOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val currentSettings = settingsRepository.getSettingsStream().firstOrNull()

            val guestSettings = SettingsEntity(
                id = currentSettings?.id ?: 0,
                userName = "Gość",
                isAnonymous = true, // Ustawiamy flagę anonimowości
                selectedGroupCode = null,
                activeDirectionCode = null,
                selectedSubgroup = null,
                gender = null,
                isFirstRun = false,
                isDarkMode = currentSettings?.isDarkMode ?: false
            )

            settingsRepository.insertOrUpdate(guestSettings)

            _isLoading.value = false
            onSuccess()
        }
    }

    // Funkcja Zapisu - zachowana oryginalna logika (pobieranie szczegółów grupy)
// Funkcja Zapisu
    fun saveOnboardingData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            // Pobieranie szczegółów grupy (Wydział, Kierunek, Tryb)
            var fetchedFaculty: String? = null
            var fetchedFieldOfStudy: String? = null
            var fetchedStudyMode: String? = null

            val groupCode = _selectedGroup.value
            if (!groupCode.isNullOrEmpty()) {
                val detailsResult = universityRepository.getGroupDetails(groupCode)
                if (detailsResult is NetworkResult.Success && detailsResult.data != null) {
                    val details = detailsResult.data
                    fetchedFaculty = details.fieldInfo?.faculty
                    fetchedFieldOfStudy = details.fieldInfo?.name
                    fetchedStudyMode = details.studyMode
                }
            }

            val currentSettings = settingsRepository.getSettingsStream().firstOrNull()

            val fullName = "${_userName.value.trim()} ${_userSurname.value.trim()}".trim()
            val genderString =
                if (_selectedGender.value == UserGender.STUDENTKA) "Studentka" else "Student"

            // NAPRAWA: Zapis bez spacji między przecinkami, tak aby multiselect w AccountViewModel widział je poprawnie
            val subgroupsString = _selectedSubgroups.value.joinToString(",")

            val newSettings = SettingsEntity(
                id = currentSettings?.id ?: 0,
                userName = fullName,
                isAnonymous = false,
                selectedGroupCode = groupCode,
                activeDirectionCode = groupCode,
                selectedSubgroup = subgroupsString,
                gender = genderString,
                isFirstRun = false,
                isDarkMode = currentSettings?.isDarkMode ?: false,
                faculty = fetchedFaculty,
                fieldOfStudy = fetchedFieldOfStudy,
                studyMode = fetchedStudyMode
            )

            settingsRepository.insertOrUpdate(newSettings)

            _isLoading.value = false
            onSuccess()
        }
    }
}

