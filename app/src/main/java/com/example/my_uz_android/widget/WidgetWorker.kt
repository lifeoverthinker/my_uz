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
import java.time.LocalDateTime
import java.time.LocalTime
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

        // Pobierz aktualny stan bazy i ustawień
        val settings = settingsRepository.getSettingsStream().firstOrNull()
        val allClasses = classRepository.getAllClassesStream().firstOrNull() ?: emptyList()

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val tomorrow = today.plusDays(1)
        val todayString = today.toString()
        val tomorrowString = tomorrow.toString()

        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank()

        // Logika wyboru dnia (identyczna jak w HomeViewModel)
        val (displayedClasses, dayLabel, emptyMessage) = if (isPlanSelected) {
            val todaysClasses = allClasses
                .filter { it.date == todayString }
                .filter { classItem ->
                    try {
                        val endTime = LocalTime.parse(classItem.endTime)
                        val endDateTime = LocalDateTime.of(today, endTime)
                        endDateTime.isAfter(now)
                    } catch (e: Exception) { true }
                }
                .sortedBy { it.startTime }

            val tomorrowsClasses = allClasses
                .filter { it.date == tomorrowString }
                .sortedBy { it.startTime }

            when {
                todaysClasses.isNotEmpty() -> Triple(todaysClasses, "Dzisiaj", null)
                tomorrowsClasses.isNotEmpty() -> Triple(tomorrowsClasses, "Jutro", null)
                else -> Triple(emptyList(), null, "Brak zajęć w najbliższych dniach")
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

        // ✅ USUNIĘTO .take(4) - teraz widget przesyła wszystkie dostępne zajęcia
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