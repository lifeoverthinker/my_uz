package com.example.my_uz_android.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.my_uz_android.MainActivity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs

class Widget : GlanceAppWidget() {

    companion object {
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
        val prefs = currentState<Preferences>()
        val dayLabel = prefs[dayLabelKey] ?: ""
        val message = prefs[messageKey] ?: "Ładowanie danych..."
        val classesJson = prefs[classesJsonKey]
        val colorMapJson = prefs[colorMapJsonKey]

        val gson = Gson()
        val classes = if (!classesJson.isNullOrEmpty()) {
            gson.fromJson(classesJson, Array<ClassEntity>::class.java).toList()
        } else emptyList()

        val colorMap: Map<String, Int> = if (!colorMapJson.isNullOrEmpty()) {
            gson.fromJson(colorMapJson, object : TypeToken<Map<String, Int>>() {}.type)
        } else emptyMap()

        WidgetContentLayout(dayLabel, message, classes, colorMap)
    }

    @Composable
    internal fun WidgetContentLayout(
        dayLabel: String,
        message: String,
        classes: List<ClassEntity>,
        colorMap: Map<String, Int>
    ) {
        // TWORZYMY INTENT ZAMIAST COMPONENT NAME
        val context = LocalContext.current
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(12.dp)
        ) {
            // --- NAGŁÓWEK ---
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Używamy mainIntent w actionStartActivity
                Column(modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity(mainIntent))) {
                    Text(
                        text = "Moje Zajęcia",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (dayLabel.isNotEmpty()) {
                        Text(
                            text = dayLabel,
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Odśwież",
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(actionRunCallback<RefreshAction>())
                )
            }

            // --- ZAWARTOŚĆ ---
            if (classes.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(mainIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(classes) { classItem ->
                        ClassItemCard(classItem, colorMap, mainIntent)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ClassItemCard(
        classItem: ClassEntity,
        colorMap: Map<String, Int>,
        mainIntent: Intent
    ) {
        val colorIndex = colorMap[classItem.classType]
            ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)
        val colorSet = ClassColorPalette.getOrElse(colorIndex) { ClassColorPalette[0] }

        val context = LocalContext.current
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // Kolory dynamiczne (Tło karty, Kolor Akcentu, Tekst na akcencie)
        val bgColorProvider = ColorProvider(if (isDark) colorSet.darkBg else colorSet.lightBg)
        val accentColorProvider = ColorProvider(if (isDark) colorSet.darkAccent else colorSet.lightAccent)
        val onAccentColorProvider = ColorProvider(if (isDark) Color(0xFF1D192B) else Color.White)

        // Kolory tekstu
        val titleColorProvider = ColorProvider(if (isDark) Color.White else Color(0xFF1D192B))
        val subtitleColorProvider = ColorProvider(if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F))

        // Pobieramy pierwszą literę typu zajęć (np. "W" dla Wykład, "L" dla Laboratorium)
        val typeInitial = if (classItem.classType.isNotEmpty()) classItem.classType.take(1).uppercase() else "?"

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(bgColorProvider)
                .cornerRadius(16.dp) // Zaokrąglona karta tak jak ClassCard
                .clickable(actionStartActivity(mainIntent))
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Lewa strona - informacje o przedmiocie (zajmuje dostępną szerokość)
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = classItem.subjectName,
                    style = TextStyle(
                        color = titleColorProvider,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Czas i sala
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = "${classItem.startTime} - ${classItem.endTime}",
                        style = TextStyle(
                            color = subtitleColorProvider,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (!classItem.room.isNullOrBlank()) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "• Sala ${classItem.room}",
                            style = TextStyle(
                                color = subtitleColorProvider,
                                fontSize = 13.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Prawa strona - okrągły Badge z literą
            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(accentColorProvider)
                    .cornerRadius(18.dp), // Promień równy połowie boku tworzy idealne koło
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeInitial,
                    style = TextStyle(
                        color = onAccentColorProvider,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val request = OneTimeWorkRequestBuilder<WidgetWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}