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
import android.content.Intent
import android.net.Uri
import androidx.glance.appwidget.action.actionStartActivity

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
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (dayLabel.isNotEmpty()) {
                    Text(
                        text = "Plan na: $dayLabel",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // ✅ Użycie LazyColumn pozwala na przewijanie, jeśli zajęć jest dużo
            if (classes.isEmpty()) {
                Box(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.ifEmpty { "Brak danych" },
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
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
        val colorIndex = colorMap[classItem.classType]
            ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)
        val colorSet = ClassColorPalette.getOrElse(colorIndex) { ClassColorPalette[0] }
        val isDark =
            (LocalContext.current.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val accentColor = if (isDark) colorSet.darkAccent else colorSet.lightAccent
        val bgColor = if (isDark) colorSet.darkBg else colorSet.lightBg
        val textColor = if (isDark) Color.White else Color.Black

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(bgColor))
                .padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Pasek boczny
            Box(
                modifier = GlanceModifier.width(3.dp).fillMaxHeight()
                    .background(ColorProvider(accentColor))
            ) {}
            Spacer(modifier = GlanceModifier.width(8.dp))

            // ✅ Nowy układ poziomy: Przedmiot i dane w jednej kolumnie, ale bardzo ciasno
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = classItem.subjectName,
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
                // Godzina i sala obok siebie w jednej linii
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = classItem.startTime,
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!classItem.room.isNullOrBlank()) {
                        Text(
                            text = " • ${classItem.room}",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                // NOWE: przycisk na dole widgetu
                val addTaskIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("myuz://add_task")
                ).apply {
                    setPackage(LocalContext.current.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(GlanceTheme.colors.primary)
                        .clickable(actionStartActivity(addTaskIntent))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Dodaj zadanie",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Litera typu zajęć (L, C, W)
            Text(
                text = classItem.classType.take(1).uppercase(),
                style = TextStyle(
                    color = ColorProvider(accentColor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}