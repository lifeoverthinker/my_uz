package com.example.my_uz_android.ui.screens.home.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.ui.theme.getClassAccentColor
import kotlin.math.abs

@Composable
fun UpcomingClasses(
    classes: List<ClassEntity>,
    emptyMessage: String?,
    dayLabel: String?,
    classColorMap: Map<String, Int>, // ✅ Dodano brakujący parametr
    modifier: Modifier = Modifier,
    onClassClick: (Int) -> Unit
) {
    val classCardColor = MaterialTheme.extendedColors.classCardBackground
    val isDark = isSystemInDarkTheme() // Potrzebne do wyboru wariantu koloru (light/dark) z palety

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nagłówek
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (classes.isEmpty()) {
            Text(
                text = emptyMessage ?: "Brak zajęć",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes) { classItem ->
                    // ✅ Logika synchronizacji kolorów:
                    // 1. Sprawdź, czy użytkownik wybrał kolor w ustawieniach (classColorMap)
                    // 2. Jeśli nie, użyj hasha nazwy typu zajęć (fallback), aby kolor był stały
                    val colorIndex = classColorMap[classItem.classType]
                        ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)

                    // ✅ Pobierz kolor z globalnej palety (tej samej co w Settings i Calendar)
                    val accentColor = getClassAccentColor(colorIndex, isDark)

                    ClassCard(
                        classItem = classItem,
                        backgroundColor = classCardColor,
                        accentColor = accentColor, // Przekazujemy wyliczony kolor
                        onClick = { onClassClick(classItem.id) },
                        modifier = Modifier.width(264.dp)
                    )
                }
            }
        }
    }
}