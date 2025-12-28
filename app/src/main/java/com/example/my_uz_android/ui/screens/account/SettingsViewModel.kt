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

// Enum definiujący typy danych do backupu
enum class BackupDataType(val displayName: String) {
    SETTINGS("Ustawienia aplikacji"),
    CLASSES("Plan zajęć"),
    TASKS("Zadania"),
    GRADES("Oceny"),
    ABSENCES("Nieobecności"),
    EVENTS("Wydarzenia")
}

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
    val uniqueClassTypes: List<String> = emptyList(),
    val classColorMap: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val importMessage: String? = null,
    // Pola do obsługi selektywnego importu
    val backupPreview: BackupData? = null,
    val showImportDialog: Boolean = false
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

            // TUTAJ ZMIANA: Filtrujemy nulle i puste ciągi, aby nie pokazywać "całej grupy" jako opcji
            val groups = if (groupsResult is NetworkResult.Success) {
                (groupsResult.data ?: emptyList())
                    .filter { !it.isNullOrBlank() && it != "null" } // Usuwa puste opcje i ewentualne stringi "null"
                    .sorted() // Opcjonalnie: sortuje grupy alfabetycznie
            } else {
                emptyList()
            }

            combine(
                settingsRepository.getSettingsStream(),
                classRepository.getAllClassesStream()
            ) { settings, classes ->
                val uniqueTypes = classes
                    .map { it.classType }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
                val colorMap: Map<String, Int> = try {
                    gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                Triple(settings, uniqueTypes, colorMap)
            }.collect { (settings, uniqueTypes, colorMap) ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        availableGroups = groups,
                        uniqueClassTypes = uniqueTypes,
                        classColorMap = colorMap
                    )
                }
            }
        }
    }

    fun updateClassColor(classType: String, colorIndex: Int) {
        val currentSettings = _uiState.value.settings ?: return
        val currentMap = _uiState.value.classColorMap.toMutableMap()
        currentMap[classType] = colorIndex
        val newJson = gson.toJson(currentMap)

        viewModelScope.launch {
            settingsRepository.insertSettings(currentSettings.copy(classColorsJson = newJson))
        }
    }

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.insertSettings(newSettings)
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

    // --- LOGIKA EKSPORTU ---

    suspend fun createBackupJson(selectedTypes: Set<BackupDataType>): String {
        return withContext(Dispatchers.IO) {
            val settings = if (selectedTypes.contains(BackupDataType.SETTINGS))
                settingsRepository.getSettingsStream().first() else null

            val classes = if (selectedTypes.contains(BackupDataType.CLASSES))
                classRepository.getAllClassesStream().first() else emptyList()

            val tasks = if (selectedTypes.contains(BackupDataType.TASKS))
                tasksRepository.getAllTasks().first() else emptyList()

            val grades = if (selectedTypes.contains(BackupDataType.GRADES))
                gradesRepository.getAllGradesStream().first() else emptyList()

            val absences = if (selectedTypes.contains(BackupDataType.ABSENCES))
                absenceRepository.getAllAbsencesStream().first() else emptyList()

            val events = if (selectedTypes.contains(BackupDataType.EVENTS))
                eventRepository.getAllEvents().first() else emptyList()

            val backupData = BackupData(settings, classes, tasks, grades, absences, events)
            Gson().toJson(backupData)
        }
    }

    // --- LOGIKA IMPORTU ---

    fun previewBackupFile(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, importMessage = null) }
            try {
                val backupData = Gson().fromJson(jsonString, BackupData::class.java)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            backupPreview = backupData,
                            showImportDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(isLoading = false, importMessage = "Błąd odczytu pliku: ${e.message}")
                    }
                }
            }
        }
    }

    fun confirmImport(selectedTypes: Set<BackupDataType>) {
        val backupData = uiState.value.backupPreview ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, showImportDialog = false) }
            try {
                // Importujemy tylko wybrane sekcje
                if (selectedTypes.contains(BackupDataType.SETTINGS) && backupData.settings != null) {
                    settingsRepository.clearSettings()
                    settingsRepository.insertSettings(backupData.settings)
                }

                if (selectedTypes.contains(BackupDataType.CLASSES) && backupData.classes.isNotEmpty()) {
                    classRepository.deleteAllClasses()
                    classRepository.insertClasses(backupData.classes)
                }

                if (selectedTypes.contains(BackupDataType.TASKS)) {
                    tasksRepository.deleteAllTasks()
                    backupData.tasks.forEach { tasksRepository.insertTask(it) }
                }

                if (selectedTypes.contains(BackupDataType.GRADES)) {
                    gradesRepository.deleteAllGrades()
                    backupData.grades.forEach { gradesRepository.insertGrade(it) }
                }

                if (selectedTypes.contains(BackupDataType.ABSENCES)) {
                    absenceRepository.deleteAllAbsences()
                    backupData.absences.forEach { absenceRepository.insertAbsence(it) }
                }

                if (selectedTypes.contains(BackupDataType.EVENTS)) {
                    eventRepository.deleteAllEvents()
                    backupData.events.forEach { eventRepository.insertEvent(it) }
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            importMessage = "Pomyślnie zaimportowano wybrane dane!",
                            backupPreview = null
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(isLoading = false, importMessage = "Błąd zapisu: ${e.message}")
                    }
                }
            }
        }
    }

    fun cancelImport() {
        _uiState.update { it.copy(showImportDialog = false, backupPreview = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(importMessage = null) }
    }
}