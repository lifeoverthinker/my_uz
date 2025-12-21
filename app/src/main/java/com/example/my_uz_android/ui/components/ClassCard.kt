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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // showBadge pozwala ukryć kropkę np. dla minionych zajęć, jeśli chcesz
    showBadge: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Kolory z Twojego designu (Figma/Flutter)
    val titleColor = Color(0xFF1D192B)
    val detailsColor = Color(0xFF494949)
    val avatarTextColor = Color(0xFFFFFBFE)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top // Wyrównanie do góry (ważne przy długich opisach)
        ) {
            // --- LEWA STRONA (Teksty) ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp) // spacing: 8
            ) {
                // Tytuł przedmiotu
                Text(
                    text = classItem.subjectName,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium, // w500
                        fontSize = 14.sp,
                        lineHeight = 20.sp, // height 1.43 przy 14sp ~ 20sp
                        color = titleColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Wiersz z Czasem i Salą
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // spacing: 16 między grupami
                ) {
                    // Grupa Czasu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // spacing: 4
                    ) {
                        // Container 16x16 z ikoną (w Flutterze był pusty Stack, tu daję ikonę)
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
                                fontWeight = FontWeight.Normal, // w400
                                fontSize = 12.sp,
                                lineHeight = 16.sp, // height 1.33 przy 12sp ~ 16sp
                                color = detailsColor
                            ),
                            maxLines = 1
                        )
                    }

                    // Grupa Sali
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = classItem.room ?: "",
                            style = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal, // w400
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

            // Odstęp między tekstem a badge'm
            Spacer(modifier = Modifier.width(16.dp))

            // --- PRAWA STRONA (Badge) ---
            if (showBadge) {
                when (type) {
                    ClassCardType.HOME -> {
                        // Kółko z literą (32x32)
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
                                    fontFamily = InterFontFamily, // Roboto w oryginale, ale Inter spójniejszy
                                    fontWeight = FontWeight.Medium, // w500
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
                        // Mała kropka (8x8)
                        Box(
                            modifier = Modifier
                                .size(8.dp) // Zgodnie z Figmą 8x8
                                .clip(CircleShape)
                                .background(accentColor) // W Figmie 0xFF7D5260, tu dynamiczny
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClassCardPreview() {
    val mockClass = ClassEntity(
        id = 1,
        subjectName = "Podstawy systemów dyskretnych",
        classType = "Wykład",
        startTime = "10:00",
        endTime = "10:45",
        dayOfWeek = 1,
        date = "2025-01-01",
        groupCode = "32INF",
        subgroup = null,
        room = "Sala 102",
        teacherName = "Jan Kowalski"
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Wariant HOME:")
        ClassCard(
            classItem = mockClass,
            type = ClassCardType.HOME,
            backgroundColor = Color(0xFFE8DEF8),
            accentColor = Color(0xFF6750A4)
        )

        Text("Wariant CALENDAR:")
        ClassCard(
            classItem = mockClass,
            type = ClassCardType.CALENDAR,
            backgroundColor = Color(0xFFE8DEF8),
            accentColor = Color(0xFF7D5260) // Kolor z Figmy
        )
    }
}