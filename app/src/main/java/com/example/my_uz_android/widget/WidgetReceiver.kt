package com.example.my_uz_android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Uruchom worker przy każdej aktualizacji widgetu
        val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    // Opcjonalnie: można dodać onEnabled do cyklicznego odświeżania WorkManagerem
}