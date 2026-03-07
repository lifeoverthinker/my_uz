package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userSurname = MutableStateFlow("")
    val userSurname: StateFlow<String> = _userSurname.asStateFlow()

    private val _selectedGender = MutableStateFlow<UserGender?>(null)
    val selectedGender: StateFlow<UserGender?> = _selectedGender.asStateFlow()

    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    private val _faculty = MutableStateFlow("")
    val faculty: StateFlow<String> = _faculty.asStateFlow()

    private val _fieldOfStudy = MutableStateFlow("")
    val fieldOfStudy: StateFlow<String> = _fieldOfStudy.asStateFlow()

    private val _studyMode = MutableStateFlow("")
    val studyMode: StateFlow<String> = _studyMode.asStateFlow()

    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery: StateFlow<String> = _groupSearchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()

    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups: StateFlow<List<String>> = _availableSubgroups.asStateFlow()

    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups: StateFlow<Set<String>> = _selectedSubgroups.asStateFlow()

    // Obsługa dodatkowych kierunków
    private val _additionalGroups = MutableStateFlow<List<String>>(emptyList())
    val additionalGroups: StateFlow<List<String>> = _additionalGroups.asStateFlow()

    private val _availableDirections = MutableStateFlow<List<String>>(emptyList())
    val availableDirections: StateFlow<List<String>> = _availableDirections.asStateFlow()

    private val _activeDirection = MutableStateFlow<String?>(null)
    val activeDirection: StateFlow<String?> = _activeDirection.asStateFlow()

    private val _allGroups = MutableStateFlow<List<String>>(emptyList())

    val filteredGroups: StateFlow<List<String>> = combine(
        _groupSearchQuery,
        _allGroups
    ) { query, allGroups ->
        if (query.isBlank()) {
            emptyList()
        } else {
            allGroups.filter { it.contains(query, ignoreCase = true) }.take(5)
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
                    val groups = (result.data ?: emptyList())
                        .filter { !it.isNullOrBlank() && it != "null" }
                        .sorted()
                    _allGroups.value = groups
                }
                else -> _allGroups.value = emptyList()
            }
        }
    }

    private fun loadCurrentData() {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                settingsRepository.getSettingsStream(),
                classRepository.getAllClassesStream()
            ) { settings, classes ->
                settings to classes
            }.collect { (settings, classes) ->
                if (settings != null) {
                    currentSettingsEntity = settings

                    val nameParts = (settings.userName ?: "").trim().split(" ", limit = 2)
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

                    // Wczytanie dodatkowych kierunków
                    _additionalGroups.value = settings.additionalGroupCodes
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val directionsFromClasses = classes
                        .map { it.groupCode.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()

                    val fallbackDirections = if (directionsFromClasses.isNotEmpty()) {
                        directionsFromClasses
                    } else {
                        listOfNotNull(settings.selectedGroupCode?.trim()).filter { it.isNotBlank() }
                    }
                    _availableDirections.value = fallbackDirections

                    val resolvedActiveDirection = settings.activeDirectionCode
                        ?.takeIf { it.isNotBlank() && fallbackDirections.contains(it) }
                        ?: settings.selectedGroupCode?.takeIf { !it.isNullOrBlank() && fallbackDirections.contains(it) }
                        ?: fallbackDirections.firstOrNull()
                    _activeDirection.value = resolvedActiveDirection

                    if (!settings.selectedGroupCode.isNullOrEmpty()) {
                        _selectedGroup.value = settings.selectedGroupCode
                        if (_groupSearchQuery.value.isEmpty()) {
                            _groupSearchQuery.value = settings.selectedGroupCode
                        }
                        loadSubgroupsForGroup(settings.selectedGroupCode)
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

    fun setUserName(name: String) { _userName.value = name }
    fun setUserSurname(surname: String) { _userSurname.value = surname }
    fun setGender(gender: UserGender) { _selectedGender.value = gender }

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

    fun addAdditionalGroup(group: String) {
        val current = _additionalGroups.value.toMutableList()
        if (group.isNotBlank() && !current.contains(group) && group != _selectedGroup.value) {
            current.add(group)
            _additionalGroups.value = current
        }
    }

    fun removeAdditionalGroup(group: String) {
        val current = _additionalGroups.value.toMutableList()
        current.remove(group)
        _additionalGroups.value = current
    }

    private fun loadSubgroupsForGroup(group: String) {
        viewModelScope.launch {
            when (val result = universityRepository.getSubgroups(group)) {
                is NetworkResult.Success -> {
                    _availableSubgroups.value = (result.data ?: emptyList())
                        .filter { !it.isNullOrBlank() && it != "null" }
                        .sorted()
                }
                else -> _availableSubgroups.value = emptyList()
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

    fun setActiveDirection(directionCode: String) {
        val normalizedDirection = directionCode.trim()
        if (normalizedDirection.isEmpty()) return

        viewModelScope.launch {
            val current = currentSettingsEntity ?: SettingsEntity(id = 1)
            if (current.activeDirectionCode == normalizedDirection) return@launch
            settingsRepository.insertOrUpdate(current.copy(activeDirectionCode = normalizedDirection))
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val fullName = "${_userName.value.trim()} ${_userSurname.value.trim()}".trim()
            val genderString = if (_selectedGender.value == UserGender.STUDENTKA) "Studentka" else "Student"
            val subgroupsString = _selectedSubgroups.value.joinToString(",")
            val additionalGroupsString = _additionalGroups.value.joinToString(",")

            val newSettings = currentSettingsEntity?.copy(
                userName = fullName,
                gender = genderString,
                selectedGroupCode = _selectedGroup.value,
                activeDirectionCode = _selectedGroup.value, // Resetujemy aktywny kierunek po zapisie głównego
                selectedSubgroup = subgroupsString,
                additionalGroupCodes = additionalGroupsString
            ) ?: SettingsEntity(
                id = 1,
                userName = fullName,
                gender = genderString,
                selectedGroupCode = _selectedGroup.value,
                activeDirectionCode = _selectedGroup.value,
                selectedSubgroup = subgroupsString,
                additionalGroupCodes = additionalGroupsString
            )

            settingsRepository.insertOrUpdate(newSettings)
            _isLoading.value = false
            _isSaved.value = true
            onSuccess()
            _isSaved.value = false
        }
    }
}