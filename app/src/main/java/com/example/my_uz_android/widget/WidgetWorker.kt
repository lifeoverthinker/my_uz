package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.MyUZApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate // <-- Brakujący import dodany
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appContainer = (context.applicationContext as MyUZApplication).container
        val classRepository = appContainer.classRepository
        val settingsRepository = appContainer.settingsRepository

        // Pobierz ustawienia i NAJBLIŻSZE zajęcia (repozytorium załatwia filtrowanie)
        val settings = settingsRepository.getSettingsStream().firstOrNull()
        val upcomingClasses = classRepository.getUpcomingClasses().firstOrNull() ?: emptyList()

        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank()

        val (displayedClasses, dayLabel, emptyMessage) = if (isPlanSelected) {
            if (upcomingClasses.isNotEmpty()) {
                val classDateStr = upcomingClasses.first().date
                val classDate = LocalDate.parse(classDateStr)
                val label = when (classDate) {
                    today -> "Dzisiaj"
                    today.plusDays(1) -> "Jutro"
                    else -> classDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))).replaceFirstChar { it.uppercase() }
                }
                Triple(upcomingClasses, label, null)
            } else {
                Triple(emptyList(), null, "Brak zajęć w najbliższych dniach")
            }
        } else {
            Triple(emptyList(), null, "Skonfiguruj grupę w aplikacji")
        }

        val gson = Gson()
        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val classColorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }

        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
        val dateString = today.format(dateFormatter).replaceFirstChar { it.uppercase() }

        val widgetClasses = displayedClasses

        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(Widget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[Widget.dateKey] = dateString
                prefs[Widget.dayLabelKey] = dayLabel ?: ""
                prefs[Widget.messageKey] = emptyMessage ?: ""
                prefs[Widget.classesJsonKey] = gson.toJson(widgetClasses)
                prefs[Widget.colorMapJsonKey] = gson.toJson(classColorMap)
            }
        }

        Widget().updateAll(context)
        return Result.success()
    }
}