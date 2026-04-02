package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.data.models.ClassEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Worker odpowiedzialny za pobranie danych z repozytoriów,
 * przetworzenie logiki (Dzisiaj/Jutro) i wypchnięcie stanu do widżetu za pomocą Preferences.
 */
class WidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appContainer = (context.applicationContext as MyUZApplication).container
        val classRepository = appContainer.classRepository
        val settingsRepository = appContainer.settingsRepository

        // Pobieramy snapshot ustawień i wszystkich przyszłych zajęć
        val settings = settingsRepository.getSettingsStream().firstOrNull()
        val upcomingClasses = classRepository.getUpcomingClasses().firstOrNull() ?: emptyList()

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()
        val tomorrow = today.plusDays(1)

        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank()

        // Odwzorowanie logiki z HomeViewModel
        val classesForToday = upcomingClasses.filter {
            it.date == today.toString() && runCatching { LocalTime.parse(it.endTime).isAfter(nowTime) }.getOrDefault(true)
        }
        val classesForTomorrow = upcomingClasses.filter {
            it.date == tomorrow.toString()
        }

        val (displayedClasses, dayLabel, emptyMessage) = when {
            !isPlanSelected -> Triple(emptyList(), "", "Skonfiguruj grupę w aplikacji")
            classesForToday.isNotEmpty() -> Triple(classesForToday, "Dzisiaj", "")
            classesForTomorrow.isNotEmpty() -> Triple(classesForTomorrow, "Jutro", "")
            else -> Triple(emptyList(), "", "Brak zajęć w najbliższym czasie")
        }

        val gson = Gson()
        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val classColorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }

        // Aktualizacja stanu wszystkich instancji widżetu
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(Widget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[Widget.dayLabelKey] = dayLabel
                prefs[Widget.messageKey] = emptyMessage
                prefs[Widget.classesJsonKey] = gson.toJson(displayedClasses)
                prefs[Widget.colorMapJsonKey] = gson.toJson(classColorMap)
            }
        }

        // Nakazanie Glance'owi przerysowania UI z nowymi Preferences
        Widget().updateAll(context)
        return Result.success()
    }
}

/**
 * Narzędzie do ręcznego wymuszenia aktualizacji widżetu z poziomu UI (np. ViewModelu).
 */
fun triggerWidgetUpdate(context: Context) {
    val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "widget_update_work",
        ExistingWorkPolicy.REPLACE, // Jeśli update już leci, zacznij od nowa
        request
    )
}