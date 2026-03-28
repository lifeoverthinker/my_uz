package com.example.my_uz_android.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_uz_android.data.models.*
import com.example.my_uz_android.data.repositories.*
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.example.my_uz_android.util.BackupManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val uniqueClassTypes: List<String> = emptyList(),
    val classColorMap: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val importMessage: String? = null,
    val backupPreview: ManualBackupData? = null,
    val showImportDialog: Boolean = false,
    val isSaved: Boolean = false
)

class SettingsViewModel(
    private val backupManager: BackupManager,
    private val settingsRepository: SettingsRepository,
    private val universityRepository: UniversityRepository,
    private val classRepository: ClassRepository,
    private val tasksRepository: TasksRepository,
    private val gradesRepository: GradesRepository,
    private val absenceRepository: AbsenceRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val gson = Gson()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                settingsRepository.getSettingsStream(),
                classRepository.getAllClassesStream()
            ) { settings, classes ->
                val uniqueTypes = classes
                    .map { it.classType.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                Pair(settings, uniqueTypes)
            }.collect { (settings, uniqueTypes) ->
                val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
                var colorMap: Map<String, Int> = try {
                    gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                if (colorMap.isEmpty() && uniqueTypes.isNotEmpty() && settings != null) {
                    colorMap = uniqueTypes.associateWith { type ->
                        abs(type.hashCode()) % ClassColorPalette.size
                    }
                    val newJson = gson.toJson(colorMap)
                    settingsRepository.insertSettings(settings.copy(classColorsJson = newJson))
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        settings = settings,
                        uniqueClassTypes = uniqueTypes,
                        classColorMap = colorMap,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun triggerSaveFeedback() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaved = true) }
            delay(1500)
            _uiState.update { it.copy(isSaved = false) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        val current = _uiState.value.settings ?: return
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(themeMode = mode.name))
            triggerSaveFeedback()
        }
    }

    fun updateClassColor(classType: String, colorIndex: Int) {
        val current = _uiState.value.settings ?: return
        val currentMap = _uiState.value.classColorMap.toMutableMap()
        currentMap[classType.trim()] = colorIndex

        val newJson = gson.toJson(currentMap)
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(classColorsJson = newJson))
            triggerSaveFeedback()
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(offlineModeEnabled = enabled))
            triggerSaveFeedback()
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(notificationsEnabled = enabled))
            triggerSaveFeedback()
        }
    }

    fun toggleTasksNotifications(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(notificationsTasks = enabled))
            triggerSaveFeedback()
        }
    }

    fun toggleClassesNotifications(enabled: Boolean) {
        val current = _uiState.value.settings ?: return
        viewModelScope.launch {
            settingsRepository.insertSettings(current.copy(notificationsClasses = enabled))
            triggerSaveFeedback()
        }
    }

    suspend fun createBackupJson(selectedTypes: Set<BackupDataType>): String =
        withContext(Dispatchers.IO) {
            val backup = ManualBackupData(
                settings = if (selectedTypes.contains(BackupDataType.SETTINGS)) settingsRepository.getSettingsStream().first() else null,
                classes = if (selectedTypes.contains(BackupDataType.CLASSES)) classRepository.getAllClassesStream().first() else emptyList(),
                tasks = if (selectedTypes.contains(BackupDataType.TASKS)) tasksRepository.getAllTasks().first() else emptyList(),
                grades = if (selectedTypes.contains(BackupDataType.GRADES)) gradesRepository.getAllGradesStream().first() else emptyList(),
                absences = if (selectedTypes.contains(BackupDataType.ABSENCES)) absenceRepository.getAllAbsencesStream().first() else emptyList(),
                events = if (selectedTypes.contains(BackupDataType.EVENTS)) eventRepository.getAllEvents().first() else emptyList()
            )
            gson.toJson(backup)
        }

    fun previewBackupFile(jsonString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = gson.fromJson(jsonString, ManualBackupData::class.java)
                _uiState.update {
                    it.copy(backupPreview = data, showImportDialog = true, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(importMessage = "Błędny format pliku", isLoading = false)
                }
            }
        }
    }

    fun confirmImport(selectedTypes: Set<BackupDataType>) {
        val data = _uiState.value.backupPreview ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, showImportDialog = false) }
            try {
                if (selectedTypes.contains(BackupDataType.SETTINGS) && data.settings != null) {
                    settingsRepository.insertSettings(data.settings)
                }
                if (selectedTypes.contains(BackupDataType.CLASSES)) {
                    classRepository.deleteAllClasses()
                    classRepository.insertClasses(data.classes)
                }
                if (selectedTypes.contains(BackupDataType.TASKS)) {
                    tasksRepository.insertTasks(data.tasks)
                }
                if (selectedTypes.contains(BackupDataType.GRADES)) {
                    gradesRepository.insertGrades(data.grades)
                }
                if (selectedTypes.contains(BackupDataType.ABSENCES)) {
                    absenceRepository.insertAbsences(data.absences)
                }
                if (selectedTypes.contains(BackupDataType.EVENTS)) {
                    eventRepository.insertEvents(data.events)
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, importMessage = "Import zakończony") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false, importMessage = "Błąd importu") }
                }
            }
        }
    }

    fun cancelImport() {
        _uiState.update { it.copy(showImportDialog = false) }
    }

    // --- FUNKCJE DLA BackupSettingsSection.kt ---
    fun exportDatabase(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupManager.exportData()
                onResult(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importDatabase(json: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                backupManager.importData(json)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Nieznany błąd podczas importu")
            }
        }
    }
}