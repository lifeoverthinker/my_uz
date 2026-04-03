package com.example.my_uz_android.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext as ComposeLocalContext
import androidx.compose.ui.tooling.preview.Preview
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
    internal fun WidgetContent() {
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
        val context = LocalContext.current
        val mainIntent = try {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } catch (e: Throwable) {
            Intent()
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
                        text = "Najbliższe zajęcia",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
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
                        Spacer(modifier = GlanceModifier.height(12.dp))
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
        val isDark = try {
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            false
        }

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
                .cornerRadius(8.dp)
                .clickable(actionStartActivity(mainIntent))
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Lewa strona - informacje o przedmiocie (zajmuje dostępną szerokość)
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = classItem.subjectName,
                    style = TextStyle(
                        color = titleColorProvider,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    if (!classItem.room.isNullOrBlank()) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "• Sala ${classItem.room}",
                            style = TextStyle(
                                color = subtitleColorProvider,
                                fontSize = 12.sp
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
                    .size(32.dp)
                    .background(accentColorProvider)
                    .cornerRadius(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeInitial,
                    style = TextStyle(
                        color = onAccentColorProvider,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        triggerWidgetUpdate(context)
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 400)
@Composable
fun WidgetContentPreview() {
    val context = ComposeLocalContext.current
    CompositionLocalProvider(LocalContext provides context) {
        GlanceTheme {
            val sampleClasses = listOf(
                ClassEntity(
                    subjectName = "Programowanie urządzeń mobilnych",
                    classType = "Laboratorium",
                    startTime = "08:15",
                    endTime = "09:45",
                    dayOfWeek = 1,
                    date = "2024-05-20",
                    groupCode = "K11",
                    subgroup = "1",
                    room = "A-1"
                ),
                ClassEntity(
                    subjectName = "Matematyka Dyskretna",
                    classType = "Wykład",
                    startTime = "10:00",
                    endTime = "11:30",
                    dayOfWeek = 1,
                    date = "2024-05-20",
                    groupCode = "K11",
                    subgroup = null,
                    room = "L-102"
                )
            )

            Widget().WidgetContentLayout(
                dayLabel = "Poniedziałek, 20 maja",
                message = "",
                classes = sampleClasses,
                colorMap = emptyMap()
            )
        }
    }
}
