package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.data.repositories.ClassRepository
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.UniversityRepository
import com.example.my_uz_android.data.repositories.UserCourseRepository
import com.example.my_uz_android.util.NetworkResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class AccountViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository,
    private val userCourseRepository: UserCourseRepository
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()
    private val _userSurname = MutableStateFlow("")
    val userSurname = _userSurname.asStateFlow()
    private val _selectedGender = MutableStateFlow<UserGender?>(null)
    val selectedGender = _selectedGender.asStateFlow()
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()

    private val _faculty = MutableStateFlow("")
    val faculty = _faculty.asStateFlow()
    private val _fieldOfStudy = MutableStateFlow("")
    val fieldOfStudy = _fieldOfStudy.asStateFlow()
    private val _studyMode = MutableStateFlow("")
    val studyMode = _studyMode.asStateFlow()
    private val _selectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubgroups = _selectedSubgroups.asStateFlow()

    private val _mainFaculty = MutableStateFlow("")
    val mainFaculty = _mainFaculty.asStateFlow()
    private val _mainFieldOfStudy = MutableStateFlow("")
    val mainFieldOfStudy = _mainFieldOfStudy.asStateFlow()
    private val _mainStudyMode = MutableStateFlow("")
    val mainStudyMode = _mainStudyMode.asStateFlow()
    private val _mainSelectedSubgroups = MutableStateFlow<Set<String>>(emptySet())
    val mainSelectedSubgroups = _mainSelectedSubgroups.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()
    private val _availableSubgroups = MutableStateFlow<List<String>>(emptyList())
    val availableSubgroups = _availableSubgroups.asStateFlow()

    private val _additionalUserCourses = MutableStateFlow<List<UserCourseEntity>>(emptyList())
    val additionalUserCourses = _additionalUserCourses.asStateFlow()
    val additionalGroups = _additionalUserCourses.map { c -> c.map { it.groupCode } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _additionalSubgroupsMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val additionalSubgroupsMap = _additionalSubgroupsMap.asStateFlow()

    private val _availableDirections = MutableStateFlow<List<String>>(emptyList())
    val availableDirections = _availableDirections.asStateFlow()
    private val _activeDirection = MutableStateFlow<String?>(null)
    val activeDirection = _activeDirection.asStateFlow()

    private val _directionToFieldMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val directionToFieldMap = _directionToFieldMap.asStateFlow()

    private val _allGroups = MutableStateFlow<List<String>>(emptyList())
    val allGroups = _allGroups.asStateFlow()
    private val _groupSearchQuery = MutableStateFlow("")
    val groupSearchQuery = _groupSearchQuery.asStateFlow()
    val filteredGroups = _groupSearchQuery.map { q -> if (q.isBlank()) emptyList() else _allGroups.value.filter { it.contains(q, true) }.take(5) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _loadingSubgroupsFor = MutableStateFlow<Set<String>>(emptySet())
    val loadingSubgroupsFor = _loadingSubgroupsFor.asStateFlow()

    // Mechanizm Auto-Zapisu z Debounce
    private val _triggerSave = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _isSavedFeedback = MutableStateFlow(false)
    val isSavedFeedback = _isSavedFeedback.asStateFlow()

    private var currentSettings: SettingsEntity? = null
    private var isEditingProfile = false

    init {
        loadAllGroups()
        loadCurrentData()

        viewModelScope.launch {
            _triggerSave
                .debounce(1000L) // Czeka 1s od ostatniego wpisania
                .collect {
                    performAutoSave()
                }
        }
    }

    private fun loadAllGroups() = viewModelScope.launch {
        val res = universityRepository.getGroupCodes()
        if (res is NetworkResult.Success) _allGroups.value = (res.data ?: emptyList()).filterNotNull().sorted()
    }

    private fun loadCurrentData() = viewModelScope.launch {
        _isLoading.value = true
        combine(settingsRepository.getSettingsStream(), userCourseRepository.getAllUserCoursesStream()) { s, c -> s to c }
            .collect { (settings, courses) ->
                if (settings != null) {
                    currentSettings = settings

                    if (!isEditingProfile) {
                        val parts = (settings.userName ?: "").split(" ", limit = 2)
                        _userName.value = parts.getOrElse(0) { "" }
                        _userSurname.value = parts.getOrElse(1) { "" }
                        _selectedGender.value = if (settings.gender == "Studentka") UserGender.STUDENTKA else UserGender.STUDENT
                        _isAnonymous.value = settings.isAnonymous
                        _selectedGroup.value = settings.selectedGroupCode

                        _mainFaculty.value = settings.faculty ?: ""
                        _mainFieldOfStudy.value = settings.fieldOfStudy ?: ""
                        _mainStudyMode.value = settings.studyMode ?: ""
                        if (settings.selectedGroupCode != null) {
                            if (_availableSubgroups.value.isEmpty()) {
                                launch { loadSubgroupsForGroup(settings.selectedGroupCode!!) }
                            }
                            _mainSelectedSubgroups.value = settings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                        }
                    }

                    val activeDir = settings.activeDirectionCode ?: settings.selectedGroupCode
                    _activeDirection.value = activeDir

                    if (activeDir == settings.selectedGroupCode) {
                        _faculty.value = settings.faculty ?: ""
                        _fieldOfStudy.value = settings.fieldOfStudy ?: ""
                        _studyMode.value = settings.studyMode ?: ""
                        _selectedSubgroups.value = settings.selectedSubgroup?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                    } else {
                        val activeCourse = courses.find { it.groupCode == activeDir }
                        if (activeCourse != null) {
                            _faculty.value = activeCourse.faculty ?: ""
                            _fieldOfStudy.value = activeCourse.fieldOfStudy ?: ""
                            _studyMode.value = activeCourse.studyMode ?: ""
                            _selectedSubgroups.value = activeCourse.selectedSubgroup?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                        }
                    }

                    val map = mutableMapOf<String, String>()
                    if (settings.selectedGroupCode != null) {
                        map[settings.selectedGroupCode!!] = settings.fieldOfStudy ?: ""
                    }
                    courses.forEach { map[it.groupCode] = it.fieldOfStudy ?: "" }
                    _directionToFieldMap.value = map

                    val directions = mutableListOf<String>()
                    settings.selectedGroupCode?.let { directions.add(it) }
                    directions.addAll(courses.map { it.groupCode })
                    _availableDirections.value = directions.distinct()
                }

                _additionalUserCourses.value = courses
                courses.forEach {
                    if (!_additionalSubgroupsMap.value.containsKey(it.groupCode)) {
                        launch { loadSubgroupsForAdditional(it.groupCode) }
                    }
                }
                _isLoading.value = false
            }
    }

    fun exitEditMode() {
        isEditingProfile = false
        // Wymuszenie zapisu aktualnego stanu przed opuszczeniem ekranu
        _triggerSave.tryEmit(Unit)
    }

    fun setUserName(n: String) { isEditingProfile = true; _userName.update { n }; _triggerSave.tryEmit(Unit) }
    fun setUserSurname(s: String) { isEditingProfile = true; _userSurname.update { s }; _triggerSave.tryEmit(Unit) }
    fun setGender(g: UserGender) { isEditingProfile = true; _selectedGender.update { g }; _triggerSave.tryEmit(Unit) }
    fun setGroupSearchQuery(q: String) = _groupSearchQuery.update { q }

    fun setActiveDirection(code: String) = viewModelScope.launch {
        currentSettings?.let { settingsRepository.insertOrUpdate(it.copy(activeDirectionCode = code)) }
    }

    fun selectGroup(g: String) {
        if (_selectedGroup.value == null) {
            isEditingProfile = true
            _selectedGroup.value = g
            _groupSearchQuery.value = ""

            viewModelScope.launch {
                _loadingSubgroupsFor.update { it + g }
                val detailsDef = async { universityRepository.getGroupDetails(g) }
                val subgroupsDef = async { universityRepository.getSubgroups(g) }

                val detailsRes = detailsDef.await()
                if (detailsRes is NetworkResult.Success) {
                    _mainFaculty.value = detailsRes.data?.fieldInfo?.faculty ?: ""
                    _mainFieldOfStudy.value = detailsRes.data?.fieldInfo?.name ?: g
                    _mainStudyMode.value = detailsRes.data?.studyMode ?: ""
                } else {
                    _mainFieldOfStudy.value = g
                }

                val subgroupsRes = subgroupsDef.await()
                if (subgroupsRes is NetworkResult.Success) {
                    _availableSubgroups.value = (subgroupsRes.data ?: emptyList())
                        .filter { !it.isNullOrBlank() && it != "null" }
                        .sorted()
                }
                _mainSelectedSubgroups.value = emptySet()
                _loadingSubgroupsFor.update { it - g }
                _triggerSave.tryEmit(Unit)
            }
        } else {
            addAdditionalGroup(g)
            _groupSearchQuery.value = ""
        }
    }

    fun clearMainGroup() {
        isEditingProfile = true
        _selectedGroup.value = null
        _mainSelectedSubgroups.value = emptySet()
        _mainFaculty.value = ""
        _mainFieldOfStudy.value = ""
        _mainStudyMode.value = ""
        _triggerSave.tryEmit(Unit)
    }

    private suspend fun loadSubgroupsForGroup(g: String) {
        _loadingSubgroupsFor.update { it + g }
        val res = universityRepository.getSubgroups(g)
        if (res is NetworkResult.Success) {
            _availableSubgroups.value = (res.data ?: emptyList())
                .filter { !it.isNullOrBlank() && it != "null" }
                .sorted()
        }
        _loadingSubgroupsFor.update { it - g }
    }

    fun toggleMainSubgroup(subgroup: String) {
        isEditingProfile = true
        val currentSet = _mainSelectedSubgroups.value.toMutableSet()
        if (currentSet.contains(subgroup)) {
            currentSet.remove(subgroup)
        } else {
            currentSet.add(subgroup)
        }
        _mainSelectedSubgroups.value = currentSet
        _triggerSave.tryEmit(Unit)
    }

    fun addAdditionalGroup(code: String) = viewModelScope.launch {
        if (code == _selectedGroup.value || _additionalUserCourses.value.any { it.groupCode == code }) return@launch

        val tempCourse = UserCourseEntity(groupCode = code, fieldOfStudy = "Wczytywanie...", selectedSubgroup = "", faculty = "", studyMode = "", semester = 1)
        userCourseRepository.insertUserCourse(tempCourse)

        _loadingSubgroupsFor.update { it + code }
        val detailsDef = async { universityRepository.getGroupDetails(code) }
        val subgroupsDef = async { universityRepository.getSubgroups(code) }

        val detailsRes = detailsDef.await()
        val finalField = if (detailsRes is NetworkResult.Success) detailsRes.data?.fieldInfo?.name ?: code else code
        val finalFac = if (detailsRes is NetworkResult.Success) detailsRes.data?.fieldInfo?.faculty ?: "" else ""
        val finalMod = if (detailsRes is NetworkResult.Success) detailsRes.data?.studyMode ?: "" else ""
        val finalSem = if (detailsRes is NetworkResult.Success) detailsRes.data?.semester?.filter { it.isDigit() }?.take(1)?.toIntOrNull() ?: 1 else 1

        val currentCourses = userCourseRepository.getAllUserCoursesStream().first()
        val courseToUpdate = currentCourses.find { it.groupCode == code }
        if (courseToUpdate != null) {
            userCourseRepository.updateUserCourse(courseToUpdate.copy(fieldOfStudy = finalField, faculty = finalFac, studyMode = finalMod, semester = finalSem))
        } else {
            userCourseRepository.updateUserCourse(tempCourse.copy(fieldOfStudy = finalField, faculty = finalFac, studyMode = finalMod, semester = finalSem))
        }

        val subgroupsRes = subgroupsDef.await()
        if (subgroupsRes is NetworkResult.Success) {
            _additionalSubgroupsMap.update {
                it + (code to (subgroupsRes.data ?: emptyList())
                    .filter { s -> !s.isNullOrBlank() && s != "null" }
                    .sorted())
            }
        }
        _loadingSubgroupsFor.update { it - code }

        _triggerSave.tryEmit(Unit)
    }

    private suspend fun loadSubgroupsForAdditional(code: String) {
        _loadingSubgroupsFor.update { it + code }
        val res = universityRepository.getSubgroups(code)
        if (res is NetworkResult.Success) {
            _additionalSubgroupsMap.update {
                it + (code to (res.data ?: emptyList())
                    .filter { s -> !s.isNullOrBlank() && s != "null" }
                    .sorted())
            }
        }
        _loadingSubgroupsFor.update { it - code }
    }

    fun removeAdditionalCourse(c: UserCourseEntity) = viewModelScope.launch {
        // 1. Usuwamy plan z bazy
        userCourseRepository.deleteUserCourse(c)

        // 2. Jeśli usuwany plan był tym aktualnie wybranym do podglądu, wracamy do planu głównego
        if (_activeDirection.value == c.groupCode) {
            _activeDirection.value = _selectedGroup.value
            currentSettings?.let { settings ->
                settingsRepository.insertOrUpdate(settings.copy(activeDirectionCode = _selectedGroup.value))
            }
        }

        // 3. Natychmiastowe wyrzucenie z lokalnej listy, aby AutoSave nie pobrał znów dla niego zajęć
        _additionalUserCourses.update { list -> list.filter { it.groupCode != c.groupCode } }

        // 4. Wymuszenie zapisu i odświeżenia zajęć
        _triggerSave.tryEmit(Unit)
    }
    
    fun updateAdditionalSubgroup(course: UserCourseEntity, subgroup: String) {
        isEditingProfile = true
        val currentSet = course.selectedSubgroup
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toMutableSet() ?: mutableSetOf()

        if (currentSet.contains(subgroup)) {
            currentSet.remove(subgroup)
        } else {
            currentSet.add(subgroup)
        }

        val newSubgroupsString = currentSet.joinToString(",")

        // NATYCHMIASTOWY ZAPIS do lokalnej bazy przy kliknięciu Multiselecta
        viewModelScope.launch {
            userCourseRepository.updateUserCourse(course.copy(selectedSubgroup = newSubgroupsString))
            _triggerSave.tryEmit(Unit)
        }
    }

    private suspend fun performAutoSave() {
        _isLoading.value = true
        val full = "${_userName.value.trim()} ${_userSurname.value.trim()}".trim()
        val gen = if (_selectedGender.value == UserGender.STUDENTKA) "Studentka" else "Student"

        var fFac = _mainFaculty.value
        var fField = _mainFieldOfStudy.value
        var fMod = _mainStudyMode.value
        val fSem = currentSettings?.currentSemester ?: 1

        if (fFac.isBlank() && _selectedGroup.value != null) {
            val res = universityRepository.getGroupDetails(_selectedGroup.value!!)
            if (res is NetworkResult.Success) {
                fFac = res.data?.fieldInfo?.faculty ?: ""
                fField = res.data?.fieldInfo?.name ?: _selectedGroup.value!!
                fMod = res.data?.studyMode ?: ""
            }
        }

        val mainSubgroups = _mainSelectedSubgroups.value.joinToString(",")
        val previousMainGroup = currentSettings?.selectedGroupCode
        val newMainGroup = _selectedGroup.value
        val newActiveDir = if (previousMainGroup != newMainGroup) newMainGroup else (_activeDirection.value ?: newMainGroup)

        isEditingProfile = false

        val settings = currentSettings?.copy(
            userName = full, gender = gen, selectedGroupCode = newMainGroup,
            selectedSubgroup = mainSubgroups,
            faculty = fFac, fieldOfStudy = fField, studyMode = fMod, currentSemester = fSem,
            isAnonymous = false, activeDirectionCode = newActiveDir
        ) ?: SettingsEntity(
            id = 1, userName = full, currentSemester = fSem,
            faculty = fFac, fieldOfStudy = fField, studyMode = fMod, gender = gen,
            selectedGroupCode = newMainGroup, selectedSubgroup = mainSubgroups,
            isAnonymous = false, activeDirectionCode = newActiveDir
        )

        settingsRepository.insertOrUpdate(settings)

        // BEZPIECZNE POBIERANIE PLANU ZAJĘĆ
        var hasNetworkError = false
        val allClasses = mutableListOf<ClassEntity>()

        newMainGroup?.let { code ->
            val res = universityRepository.getSchedule(code, mainSubgroups.split(",").filter { it.isNotBlank() })
            if (res is NetworkResult.Success) {
                allClasses.addAll(res.data ?: emptyList())
            } else {
                hasNetworkError = true
            }
        }

        for (c in _additionalUserCourses.value) {
            val subs = c.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val res = universityRepository.getSchedule(c.groupCode, subs)
            if (res is NetworkResult.Success) {
                allClasses.addAll(res.data ?: emptyList())
            } else {
                hasNetworkError = true
            }
        }

        // Zapisujemy tylko jeśli NIE było błędu połączenia!
        if (!hasNetworkError) {
            classRepository.updateClasses(allClasses.distinctBy { it.supabaseId })
        }

        _isLoading.value = false

        _isSavedFeedback.value = true
        delay(1500)
        _isSavedFeedback.value = false
    }
}