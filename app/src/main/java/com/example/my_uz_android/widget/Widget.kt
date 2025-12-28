package com.example.my_uz_android.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.my_uz_android.MainActivity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs

class Widget : GlanceAppWidget() {

    companion object {
        val dateKey = stringPreferencesKey("date")
        val dayLabelKey = stringPreferencesKey("day_label")
        val messageKey = stringPreferencesKey("message")
        val classesJsonKey = stringPreferencesKey("classes_json")
        val colorMapJsonKey = stringPreferencesKey("color_map_json")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val date = prefs[dateKey] ?: ""
        val dayLabel = prefs[dayLabelKey] ?: ""
        val message = prefs[messageKey] ?: ""
        val classesJson = prefs[classesJsonKey]
        val colorMapJson = prefs[colorMapJsonKey]

        val gson = Gson()
        val classes = if (classesJson != null) {
            gson.fromJson(classesJson, Array<ClassEntity>::class.java).toList()
        } else emptyList()

        val colorMap: Map<String, Int> = if (colorMapJson != null) {
            gson.fromJson(colorMapJson, object : TypeToken<Map<String, Int>>() {}.type)
        } else emptyMap()

        WidgetContentLayout(date, dayLabel, message, classes, colorMap)
    }

    @Composable
    internal fun WidgetContentLayout(
        date: String,
        dayLabel: String,
        message: String,
        classes: List<ClassEntity>,
        colorMap: Map<String, Int>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // Nagłówek
            Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = date,
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
                if (dayLabel.isNotEmpty()) {
                    Text(
                        text = "Plan na: $dayLabel",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                    )
                }
            }

            // ✅ Użycie LazyColumn pozwala na przewijanie, jeśli zajęć jest dużo
            if (classes.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message.ifEmpty { "Brak danych" }, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                    items(classes) { classItem ->
                        ClassItem(classItem, colorMap)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ClassItem(classItem: ClassEntity, colorMap: Map<String, Int>) {
        val colorIndex = colorMap[classItem.classType] ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)
        val colorSet = ClassColorPalette.getOrElse(colorIndex) { ClassColorPalette[0] }
        val isDark = (LocalContext.current.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val accentColor = if (isDark) colorSet.darkAccent else colorSet.lightAccent
        val bgColor = if (isDark) colorSet.darkBg else colorSet.lightBg
        val textColor = if (isDark) Color.White else Color.Black

        Row(
            modifier = GlanceModifier.fillMaxWidth().background(ColorProvider(bgColor)).padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Box(modifier = GlanceModifier.width(4.dp).height(32.dp).background(ColorProvider(accentColor))) {}
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = classItem.subjectName, style = TextStyle(color = ColorProvider(textColor), fontWeight = FontWeight.Medium, fontSize = 13.sp), maxLines = 1)
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(text = "${classItem.startTime} - ${classItem.endTime}", style = TextStyle(color = ColorProvider(accentColor), fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    if (classItem.room != null) {
                        Text(text = classItem.room ?: "", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp))
                    }
                }
            }
            Text(text = classItem.classType.take(1).uppercase(), style = TextStyle(color = ColorProvider(accentColor), fontWeight = FontWeight.Bold, fontSize = 12.sp))
        }
    }
}