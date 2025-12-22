package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.theme.InterFontFamily
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ClassCardType {
    HOME,
    CALENDAR
}

@Composable
fun ClassCard(
    classItem: ClassEntity,
    type: ClassCardType = ClassCardType.HOME,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showBadge: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val titleColor = Color(0xFF1D192B)
    val detailsColor = Color(0xFF494949)
    val avatarTextColor = Color(0xFFFFFBFE) // Biały tekst na kolorowym badge'u

    val isPast = remember(classItem) {
        try {
            val datePart = LocalDate.parse(classItem.date)
            val timePart = LocalTime.parse(classItem.endTime)
            val classEndDateTime = LocalDateTime.of(datePart, timePart)
            val currentDateTime = LocalDateTime.now()
            classEndDateTime.isBefore(currentDateTime)
        } catch (e: Exception) {
            false
        }
    }

    // Aplikuj alpha tylko dla kalendarza (wyszarzenie przeszłych)
    val contentAlpha = if (isPast && type == ClassCardType.CALENDAR) 0.6f else 1f

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = classItem.subjectName,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = titleColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = detailsColor
                        )
                        Text(
                            text = "${classItem.startTime} - ${classItem.endTime}",
                            style = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = detailsColor
                            ),
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = classItem.room ?: "",
                            style = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = detailsColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (showBadge) {
                when (type) {
                    ClassCardType.HOME -> {
                        val letter = classItem.classType.firstOrNull()?.uppercase() ?: "A"
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        ) {
                            Text(
                                text = letter,
                                style = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.15.sp,
                                    color = avatarTextColor
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    ClassCardType.CALENDAR -> {
                        // Mała kropka w kalendarzu
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }
                }
            }
        }
    }
}