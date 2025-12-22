package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserGender {
    STUDENT, STUDENTKA
}

class AccountViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    // --- Dane Użytkownika ---
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userSurname = MutableStateFlow("")
    val userSurname: StateFlow<String> = _userSurname.asStateFlow()

    private val _selectedGender = MutableStateFlow<UserGender?>(null)
    val selectedGender: StateFlow<UserGender?> = _selectedGender.asStateFlow()

    // --- Dane Studiów ---
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    private val _faculty = MutableStateFlow("")
    val faculty: StateFlow<String> = _faculty.asStateFlow()

    private val _fieldOfStudy = MutableStateFlow("")
    val fieldOfStudy: StateFlow<String> = _fieldOfStudy.asStateFlow()

    private val _studyMode = MutableStateFlow("")
    val studyMode: StateFlow<String> = _studyMode.asStateFlow()

    // --- Wybór Grupy ---
    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups: StateFlow<Set<String>> = _selectedSubgroups.asStateFlow()

    // Pełna lista grup pobrana z serwera
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
                .sorted()
                .take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var currentSettingsEntity: SettingsEntity? = null

    init {
        loadCurrentData()
        loadAllGroups()
    }

    private fun loadAllGroups() {
        viewModelScope.launch {
            when (val result = universityRepository.getGroupCodes()) {
                is NetworkResult.Success -> {
                    _allGroups.value = result.data ?: emptyList()
                }
                else -> {
                    _allGroups.value = emptyList()
                }
            }
        }
    }

    private fun loadCurrentData() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.getSettingsStream().collect { settings ->
                if (settings != null) {
                    currentSettingsEntity = settings

                    val nameParts = settings.userName.split(" ", limit = 2)
                    _userName.value = nameParts.getOrElse(0) { "" }
                    _userSurname.value = nameParts.getOrElse(1) { "" }

                    _isAnonymous.value = settings.isAnonymous
                    _faculty.value = settings.faculty ?: ""
                    _fieldOfStudy.value = settings.fieldOfStudy ?: ""
                    _studyMode.value = settings.studyMode ?: ""

                    _selectedGender.value = when (settings.gender?.lowercase()) {
                        "studentka" -> UserGender.STUDENTKA
                        "student" -> UserGender.STUDENT
                        else -> UserGender.STUDENT
                    }

                    if (!settings.selectedGroupCode.isNullOrEmpty()) {
                        _selectedGroup.value = settings.selectedGroupCode

                        // Ustawiamy query tylko jeśli jest puste, żeby nie nadpisywać w trakcie pisania (choć tutaj to load initial)
                        if (_groupSearchQuery.value.isEmpty()) {
                            _groupSearchQuery.value = settings.selectedGroupCode
                        }

                        loadSubgroupsForGroup(settings.selectedGroupCode)

                        // --- POPRAWKA: Rozdzielamy string podgrup na listę ---
                        // Jeśli w bazie jest "Lab 1, Proj 2", tworzymy Set ["Lab 1", "Proj 2"]
                        // Dzięki temu FilterChip w edycji będzie wiedział, co zaznaczyć.
                        _selectedSubgroups.value = if (!settings.selectedSubgroup.isNullOrEmpty()) {
                            settings.selectedSubgroup.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .toSet()
                        } else {
                            emptySet()
                        }
                    }
                }
                _isLoading.value = false
            }
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
            when (val result = universityRepository.getSubgroups(group)) {
                is NetworkResult.Success -> {
                    _availableSubgroups.value = result.data ?: emptyList()
                }
                else -> {
                    _availableSubgroups.value = emptyList()
                }
            }
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

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val fullName = "${_userName.value.trim()} ${_userSurname.value.trim()}".trim()
            val genderString = if (_selectedGender.value == UserGender.STUDENTKA) "Studentka" else "Student"

            // --- POPRAWKA: Łączymy wybrane podgrupy w jeden string po przecinku ---
            val subgroupsString = _selectedSubgroups.value.joinToString(", ")

            val newSettings = currentSettingsEntity?.copy(
                userName = fullName,
                gender = genderString,
                selectedGroupCode = _selectedGroup.value,
                selectedSubgroup = subgroupsString // Zapisujemy jako string
            ) ?: SettingsEntity(
                userName = fullName,
                gender = genderString,
                selectedGroupCode = _selectedGroup.value,
                selectedSubgroup = subgroupsString
            )

            settingsRepository.insertOrUpdate(newSettings)

            _isLoading.value = false
            _isSaved.value = true
            onSuccess()
            _isSaved.value = false
        }
    }
}