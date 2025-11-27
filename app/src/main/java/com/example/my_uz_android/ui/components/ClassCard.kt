package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.theme.InterFontFamily

enum class ClassCardType {
    HOME,
    CALENDAR
}

@Composable
fun ClassCard(
    classItem: ClassEntity,
    type: ClassCardType = ClassCardType.HOME,
    // Domyślny kolor z motywu, ale można nadpisać
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    modifier: Modifier = Modifier
) {
    // Używamy kolorów z motywu zamiast sztywnych wartości
    val titleColor = MaterialTheme.colorScheme.onSurface
    val detailsColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Kolory avatara
    val avatarBackgroundColor = MaterialTheme.colorScheme.primary
    val avatarTextColor = MaterialTheme.colorScheme.onPrimary
    // Kolor kropki w kalendarzu
    val statusDotColor = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nazwa przedmiotu
            Text(
                text = classItem.subjectName,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.1.sp,
                    color = titleColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Godzina
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
                            letterSpacing = 0.4.sp,
                            color = detailsColor
                        )
                    )
                }

                // Sala
                Text(
                    text = classItem.room ?: "",
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.4.sp,
                        color = detailsColor
                    )
                )
            }
        }

        when (type) {
            ClassCardType.HOME -> {
                val letter = classItem.classType.firstOrNull()?.uppercase() ?: "A"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(avatarBackgroundColor)
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
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
            }
        }
    }
}