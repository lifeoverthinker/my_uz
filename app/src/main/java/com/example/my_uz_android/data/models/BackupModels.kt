package com.example.my_uz_android.data.models

enum class BackupDataType(val displayName: String) {
    SETTINGS("Ustawienia aplikacji"),
    CLASSES("Plan zajęć"),
    TASKS("Zadania"),
    GRADES("Oceny"),
    ABSENCES("Nieobecności"),
    EVENTS("Wydarzenia")
}

data class ManualBackupData(
    val settings: SettingsEntity?,
    val classes: List<ClassEntity>,
    val tasks: List<TaskEntity>,
    val grades: List<GradeEntity>,
    val absences: List<AbsenceEntity>,
    val events: List<EventEntity>
)