package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.*
import com.example.my_uz_android.data.repositories.*
import com.example.my_uz_android.util.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupData(
    val settings: SettingsEntity?,
    val classes: List<ClassEntity>,
    val tasks: List<TaskEntity>,
    val grades: List<GradeEntity>,
    val absences: List<AbsenceEntity>,
    val events: List<EventEntity>
)

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val availableGroups: List<String> = emptyList(),
    val availableSubgroups: List<String> = emptyList(),
    val uniqueClassTypes: List<String> = emptyList(), // ✅ Lista typów z planu
    val classColorMap: Map<String, Int> = emptyMap(), // ✅ Mapa kolorów
    val isLoading: Boolean = false,
    val importMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository,
    private val tasksRepository: TasksRepository,
    private val gradesRepository: GradesRepository,
    private val absenceRepository: AbsenceRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val gson = Gson()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val groupsResult = try {
                universityRepository.getGroupCodes()
            } catch (e: Exception) {
                NetworkResult.Error("Błąd sieci")
            }

            val groups = if (groupsResult is NetworkResult.Success) {
                groupsResult.data ?: emptyList()
            } else {
                emptyList()
            }

            combine(
                settingsRepository.getSettingsStream(),
                classRepository.getAllClassesStream()
            ) { settings, classes ->
                // Wyciągnij unikalne typy zajęć z bazy
                val uniqueTypes = classes
                    .map { it.classType }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                // Rozparsuj JSON z kolorami
                val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
                val colorMap: Map<String, Int> = try {
                    gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                Triple(settings, uniqueTypes, colorMap)
            }.collect { (settings, uniqueTypes, colorMap) ->
                val loadedGroups = groups // Używamy pobranych wcześniej

                _uiState.update {
                    it.copy(
                        settings = settings,
                        availableGroups = loadedGroups,
                        uniqueClassTypes = uniqueTypes,
                        classColorMap = colorMap
                    )
                }
            }
        }
    }

    // ✅ Funkcja aktualizacji koloru dla typu zajęć
    fun updateClassColor(classType: String, colorIndex: Int) {
        val currentSettings = _uiState.value.settings ?: return
        val currentMap = _uiState.value.classColorMap.toMutableMap()

        currentMap[classType] = colorIndex

        val newJson = gson.toJson(currentMap)

        viewModelScope.launch {
            settingsRepository.updateSettings(currentSettings.copy(classColorsJson = newJson))
        }
    }

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        updateSettings(current.copy(offlineModeEnabled = enabled))
    }

    fun toggleDarkMode(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        updateSettings(current.copy(isDarkMode = enabled))
    }

    fun onGroupSelected(groupCode: String) {
        val current = _uiState.value.settings ?: return
        updateSettings(current.copy(selectedGroupCode = groupCode, selectedSubgroup = ""))
    }

    suspend fun createBackupJson(): String {
        return withContext(Dispatchers.IO) {
            val settings = settingsRepository.getSettingsStream().first()
            val classes = classRepository.getAllClassesStream().first()
            val tasks = tasksRepository.getAllTasks().first()
            val grades = gradesRepository.getAllGradesStream().first()
            val absences = absenceRepository.getAllAbsencesStream().first()
            val events = eventRepository.getAllEvents().first()

            val backupData = BackupData(settings, classes, tasks, grades, absences, events)
            Gson().toJson(backupData)
        }
    }

    fun restoreBackup(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, importMessage = null) }
            try {
                val backupData = Gson().fromJson(jsonString, BackupData::class.java)

                settingsRepository.clearSettings()
                classRepository.deleteAllClasses()
                tasksRepository.deleteAllTasks()
                gradesRepository.deleteAllGrades()
                absenceRepository.deleteAllAbsences()
                eventRepository.deleteAllEvents()

                val settings = backupData.settings
                if (settings != null) settingsRepository.insertSettings(settings)

                classRepository.insertClasses(backupData.classes)
                for (task in backupData.tasks) tasksRepository.insertTask(task)
                for (grade in backupData.grades) gradesRepository.insertGrade(grade)
                for (absence in backupData.absences) absenceRepository.insertAbsence(absence)
                for (event in backupData.events) eventRepository.insertEvent(event)

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, importMessage = "Przywrócono kopię zapasową!") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, importMessage = "Błąd importu: ${e.message}") }
                }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(importMessage = null) } }
}