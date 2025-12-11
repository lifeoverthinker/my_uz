package com.example.my_uz_android.ui.screens.calendar

import androidx.lifecycle.ViewModel
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.data.repositories.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val tasksStream: Flow<List<TaskEntity>> = tasksRepository.getTasksStream()

    // ✅ NOWA METODA - sprawdza czy plan został wybrany
    fun isPlanSelected(): Boolean {
        return runBlocking {
            val settings = settingsRepository.getSettingsStream().first()
            !settings?.selectedGroupCode.isNullOrBlank()
        }
    }
}
