package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.util.classesStillRemainingToday
import com.example.my_uz_android.util.SubgroupMatcher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

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
        val userCourseRepository = appContainer.userCourseRepository

        // Pobieramy snapshot ustawień i wszystkich przyszłych zajęć
        val settings = settingsRepository.getSettingsStream().firstOrNull()
        val upcomingClasses = classRepository.getUpcomingClasses().firstOrNull() ?: emptyList()
        val userCourses = userCourseRepository.getAllUserCoursesStream().firstOrNull() ?: emptyList()

        val now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"))
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()
        val tomorrow = today.plusDays(1)

        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank()

        val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, userCourses)
        val visibleClasses = upcomingClasses.filter { classItem ->
            SubgroupMatcher.isClassVisible(
                classGroupCode = classItem.groupCode,
                classType = classItem.classType,
                classSubgroup = classItem.subgroup,
                userEnrollments = userEnrollments
            )
        }

        // Odwzorowanie logiki z HomeViewModel
        val classesForToday = classesStillRemainingToday(visibleClasses, today, nowTime)
        val classesForTomorrow = visibleClasses.filter {
            it.date == tomorrow.toString()
        }

        val (displayedClasses, dayLabel, emptyMessage) = when {
            !isPlanSelected -> Triple(emptyList(), null, context.getString(com.example.my_uz_android.R.string.setup_plan_message))
            classesForToday.isNotEmpty() -> Triple(classesForToday, context.getString(com.example.my_uz_android.R.string.dzisiaj), null)
            classesForTomorrow.isNotEmpty() -> Triple(classesForTomorrow, context.getString(com.example.my_uz_android.R.string.jutro), null)
            else -> Triple(emptyList(), context.getString(com.example.my_uz_android.R.string.dzisiaj), context.getString(com.example.my_uz_android.R.string.no_classes_today_tomorrow))
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
                prefs[Widget.dayLabelKey] = dayLabel ?: ""
                prefs[Widget.messageKey] = emptyMessage ?: ""
                prefs[Widget.classesJsonKey] = gson.toJson(displayedClasses)
                prefs[Widget.colorMapJsonKey] = gson.toJson(classColorMap)
            }
        }

        // Nakazanie Glance'owi przerysowania UI z nowymi Preferences
        Widget().updateAll(context)
        return Result.success()
    }
}

private const val WIDGET_UPDATE_WORK = "widget_update_work"
private const val WIDGET_PERIODIC_WORK = "widget_periodic_update_work"

fun scheduleWidgetPeriodicUpdates(context: Context) {
    val periodicRequest = PeriodicWorkRequestBuilder<WidgetWorker>(
        3, TimeUnit.HOURS,
        30, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WIDGET_PERIODIC_WORK,
        ExistingPeriodicWorkPolicy.UPDATE,
        periodicRequest
    )
}

/**
 * Narzędzie do ręcznego wymuszenia aktualizacji widżetu z poziomu UI (np. ViewModelu).
 */
fun triggerWidgetUpdate(context: Context) {
    scheduleWidgetPeriodicUpdates(context)
    val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WIDGET_UPDATE_WORK,
        ExistingWorkPolicy.REPLACE, // Jeśli update już leci, zacznij od nowa
        request
    )
}