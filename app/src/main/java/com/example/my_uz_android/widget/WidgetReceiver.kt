package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Punkt wejścia dla systemu Android, zarządzający cyklem życia widżetu.
 */
class WidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = Widget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Zapobiegamy dublowaniu wywołań za pomocą REPLACE
        val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "widget_update_work",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}