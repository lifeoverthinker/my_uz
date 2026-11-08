package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.my_uz_android.MyUZApplication
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.util.SubgroupMatcher
import com.example.my_uz_android.util.classesStillRemainingToday
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appContainer = (context.applicationContext as MyUZApplication).container
        val classRepository = appContainer.classRepository
        val settingsRepository = appContainer.settingsRepository
        val userCourseRepository = appContainer.userCourseRepository

        val settings = settingsRepository.getSettingsStream().firstOrNull()
        val allClasses = classRepository.getAllClassesStream().firstOrNull() ?: emptyList()
        val userCourses = userCourseRepository.getAllUserCoursesStream().firstOrNull() ?: emptyList()

        val zoneId = ZoneId.of("Europe/Warsaw")
        val now = LocalDateTime.now(zoneId)
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()
        val tomorrow = today.plusDays(1)

        val isPlanSelected = !settings?.selectedGroupCode.isNullOrBlank() || userCourses.isNotEmpty()

        val allUserCodesLower = mutableSetOf<String>()
        settings?.selectedGroupCode?.let { allUserCodesLower.add(it.trim().lowercase()) }
        userCourses.forEach { allUserCodesLower.add(it.groupCode.trim().lowercase()) }

        val userEnrollments = SubgroupMatcher.buildUserEnrollments(settings, userCourses, allUserCodesLower)

        val visibleClasses = allClasses.filter { classItem ->
            SubgroupMatcher.isClassVisible(
                classGroupCode = classItem.groupCode,
                classType = classItem.classType,
                classSubgroup = classItem.subgroup,
                userEnrollments = userEnrollments
            )
        }

        val finalVisibleClasses = if (visibleClasses.isEmpty() && allClasses.isNotEmpty() && userEnrollments.isEmpty()) {
            allClasses
        } else {
            visibleClasses
        }

        val classesForToday = classesStillRemainingToday(finalVisibleClasses, today, nowTime)
            .sortedBy { parseTime(it.startTime) ?: LocalTime.MIN }

        val classesForTomorrow = finalVisibleClasses
            .filter { it.date == tomorrow.toString() }
            .sortedBy { parseTime(it.startTime) ?: LocalTime.MIN }

        val (displayedClasses, dayLabel, emptyMessage) = when {
            !isPlanSelected -> Triple(emptyList(), null, context.getString(R.string.setup_plan_message))
            classesForToday.isNotEmpty() -> Triple(classesForToday, context.getString(R.string.dzisiaj), null)
            classesForTomorrow.isNotEmpty() -> Triple(classesForTomorrow, context.getString(R.string.jutro), null)
            else -> Triple(emptyList(), context.getString(R.string.dzisiaj), context.getString(R.string.no_classes_today_tomorrow))
        }

        val gson = Gson()
        val colorMapType = object : TypeToken<Map<String, Int>>() {}.type
        val classColorMap: Map<String, Int> = try {
            gson.fromJson(settings?.classColorsJson ?: "{}", colorMapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

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

        Widget().updateAll(context)

        scheduleNextDynamicUpdate(context, finalVisibleClasses, now)

        return Result.success()
    }

    private fun scheduleNextDynamicUpdate(
        context: Context,
        visibleClasses: List<ClassEntity>,
        now: LocalDateTime
    ) {
        val today = now.toLocalDate()

        val candidateTimes = mutableListOf<LocalDateTime>()

        visibleClasses.forEach { classItem ->
            if (classItem.date == today.toString()) {
                val startTime = parseTime(classItem.startTime)
                val endTime = parseTime(classItem.endTime)

                if (startTime != null) {
                    val startDateTime = LocalDateTime.of(today, startTime)
                    if (startDateTime.isAfter(now)) {
                        candidateTimes.add(startDateTime)
                    }
                }
                if (endTime != null) {
                    val endDateTime = LocalDateTime.of(today, endTime)
                    if (endDateTime.isAfter(now)) {
                        candidateTimes.add(endDateTime)
                    }
                }
            }
        }

        val nextMidnight = today.plusDays(1).atStartOfDay().plusSeconds(5)
        candidateTimes.add(nextMidnight)

        val nextUpdate = candidateTimes.filter { it.isAfter(now) }.minOrNull() ?: nextMidnight
        val delayMillis = Duration.between(now, nextUpdate).toMillis().coerceAtLeast(1000L)

        val nextRequest = OneTimeWorkRequestBuilder<WidgetWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WIDGET_SCHEDULED_NEXT_WORK,
            ExistingWorkPolicy.REPLACE,
            nextRequest
        )
    }

    private fun parseTime(timeStr: String): LocalTime? {
        return runCatching { LocalTime.parse(timeStr) }.getOrNull()
            ?: runCatching {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    LocalTime.of(parts[0].trim().toInt(), parts[1].trim().toInt())
                } else null
            }.getOrNull()
    }
}

private const val WIDGET_UPDATE_WORK = "widget_update_work"
private const val WIDGET_PERIODIC_WORK = "widget_periodic_update_work"
private const val WIDGET_SCHEDULED_NEXT_WORK = "widget_scheduled_next_work"

fun scheduleWidgetPeriodicUpdates(context: Context) {
    val periodicRequest = PeriodicWorkRequestBuilder<WidgetWorker>(
        15, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WIDGET_PERIODIC_WORK,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicRequest
    )
}

fun triggerWidgetUpdate(context: Context) {
    scheduleWidgetPeriodicUpdates(context)
    val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WIDGET_UPDATE_WORK,
        ExistingWorkPolicy.REPLACE,
        request
    )
}