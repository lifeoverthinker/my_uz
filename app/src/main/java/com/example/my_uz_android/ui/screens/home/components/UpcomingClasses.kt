package com.example.my_uz_android.ui.screens.home.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun UpcomingClasses(
    classes: List<ClassEntity>,
    emptyMessage: String?,
    dayLabel: String?,
    modifier: Modifier = Modifier,
    onClassClick: (Int) -> Unit
) {
    val classCardColor = MaterialTheme.extendedColors.classCardBackground
    val now = java.time.LocalTime.now()

    // ✅ UKRYWA MINIONE ZAJĘCIA - filtruje tylko przyszłe zajęcia
    val currentClasses = classes.filter { clazz ->
        try {
            val startParts = clazz.startTime.split(":")
            if (startParts.size == 2) {
                val startHour = startParts[0].toIntOrNull() ?: 0
                val startMinute = startParts[1].toIntOrNull() ?: 0
                val startTime = java.time.LocalTime.of(startHour, startMinute)
                startTime.isAfter(now) || startTime == now // Pokazuj bieżące i przyszłe
            } else {
                true // Bezpiecznik dla niepoprawnego formatu
            }
        } catch (e: Exception) {
            true // Bezpiecznik
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nagłówek z labelem po prawej stronie
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lewa strona: Ikona + Tytuł
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

            // Prawa strona: Label "Dzisiaj"/"Jutro"
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

        if (currentClasses.isEmpty()) {
            Text(
                text = emptyMessage ?: "Brak zajęć",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentClasses) { classItem ->
                    ClassCard(
                        classItem = classItem,
                        backgroundColor = classCardColor,
                        modifier = Modifier
                            .width(264.dp)
                            .clickable { onClassClick(classItem.id) }
                    )
                }
            }
        }
    }
}

// ============================================
// PREVIEW
// ============================================

@Preview(name = "Upcoming Classes - Dzisiaj", showBackground = true)
@Composable
private fun PreviewUpcomingClassesToday() {
    MyUZTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            UpcomingClasses(
                classes = listOf(
                    ClassEntity(
                        id = 1,
                        subjectName = "Programowanie Mobilne",
                        classType = "Laboratorium",
                        startTime = "10:00",
                        endTime = "11:30",
                        dayOfWeek = 1,
                        groupCode = "INF4A",
                        subgroup = "L1",
                        room = "A-2/112",
                        teacherName = "Dr Kowalski"
                    ),
                    ClassEntity(
                        id = 2,
                        subjectName = "Bazy Danych",
                        classType = "Wykład",
                        startTime = "12:00",
                        endTime = "13:30",
                        dayOfWeek = 1,
                        groupCode = "INF4A",
                        subgroup = null,
                        room = "C-1/201",
                        teacherName = "Prof. Nowak"
                    )
                ),
                emptyMessage = null,
                dayLabel = "Dzisiaj",
                onClassClick = {}
            )
        }
    }
}

@Preview(name = "Upcoming Classes - Jutro", showBackground = true)
@Composable
private fun PreviewUpcomingClassesTomorrow() {
    MyUZTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            UpcomingClasses(
                classes = listOf(
                    ClassEntity(
                        id = 3,
                        subjectName = "Sieci Komputerowe",
                        classType = "Ćwiczenia",
                        startTime = "08:00",
                        endTime = "09:30",
                        dayOfWeek = 2,
                        groupCode = "INF4A",
                        subgroup = "C2",
                        room = "B-1/305",
                        teacherName = "Dr Lewandowski"
                    )
                ),
                emptyMessage = null,
                dayLabel = "Jutro",
                onClassClick = {}
            )
        }
    }
}

@Preview(name = "Upcoming Classes - Brak zajęć", showBackground = true)
@Composable
private fun PreviewUpcomingClassesEmpty() {
    MyUZTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            UpcomingClasses(
                classes = emptyList(),
                emptyMessage = "Brak zajęć na dzisiaj",
                dayLabel = null,
                onClassClick = {}
            )
        }
    }
}

@Preview(name = "Upcoming Classes - Dark Mode", showBackground = true)
@Composable
private fun PreviewUpcomingClassesDark() {
    MyUZTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            UpcomingClasses(
                classes = listOf(
                    ClassEntity(
                        id = 1,
                        subjectName = "Programowanie Mobilne",
                        classType = "Laboratorium",
                        startTime = "10:00",
                        endTime = "11:30",
                        dayOfWeek = 1,
                        groupCode = "INF4A",
                        subgroup = "L1",
                        room = "A-2/112",
                        teacherName = "Dr Kowalski"
                    )
                ),
                emptyMessage = null,
                dayLabel = "Dzisiaj",
                onClassClick = {}
            )
        }
    }
}
