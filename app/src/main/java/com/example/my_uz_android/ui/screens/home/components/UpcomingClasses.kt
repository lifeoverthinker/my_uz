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
                    ClassCard(
                        classItem = classItem,
                        backgroundColor = classCardColor,
                        // ✅ POPRAWKA: Przekazujemy onClick do parametru, a nie do modifiera
                        onClick = { onClassClick(classItem.id) },
                        modifier = Modifier.width(264.dp)
                    )
                }
            }
        }
    }
}

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
                        date = "2025-12-09",
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