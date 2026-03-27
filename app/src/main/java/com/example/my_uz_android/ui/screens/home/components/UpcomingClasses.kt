package com.example.my_uz_android.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.theme.ClassColorPalette
import kotlin.math.abs
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.ui.components.DashboardEmptyCard

@Composable
fun UpcomingClasses(
    classes: List<ClassEntity>,
    emptyMessage: String?,
    dayLabel: String?,
    classColorMap: Map<String, Int>,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClassClick: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Najbliższe zajęcia",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (dayLabel != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (classes.isEmpty()) {
            DashboardEmptyCard(
                title = "Wszystko gotowe!",
                message = "Brak zajęć na dziś i jutro",
                iconRes = R.drawable.ic_calendar_check,
                containerColor = extendedColors.classCardBackground,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes) { classItem ->
                    // 1. Pobieramy indeks koloru po classType (dokładnie jak w kalendarzu!)
                    val colorIndex = classColorMap[classItem.classType]
                        ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)

                    // 2. Wyciągamy zestaw kolorów z palety
                    val colorSet = ClassColorPalette.getOrElse(colorIndex) { ClassColorPalette[0] }

                    // 3. Dopasowujemy do trybu jasnego / ciemnego
                    val bgColor = if (isDarkMode) colorSet.darkBg else colorSet.lightBg
                    val accentColor = if (isDarkMode) colorSet.darkAccent else colorSet.lightAccent

                    ClassCard(
                        classItem = classItem,
                        backgroundColor = bgColor,     // Teraz karta na Home używa dedykowanego tła pastelowego/ciemnego!
                        accentColor = accentColor,     // Akcent kółka również bierze się z palety
                        onClick = { onClassClick(classItem.id) },
                        modifier = Modifier.width(264.dp)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun UpcomingClassesEmptyPreview() {
    com.example.my_uz_android.ui.theme.MyUZTheme {
        Surface {
            UpcomingClasses(
                classes = emptyList(),
                emptyMessage = "Dzisiaj masz wolne! Brak zajęć do wyświetlenia.",
                dayLabel = "Dzisiaj",
                classColorMap = emptyMap(),
                isDarkMode = false,
                onClassClick = {}
            )
        }
    }
}