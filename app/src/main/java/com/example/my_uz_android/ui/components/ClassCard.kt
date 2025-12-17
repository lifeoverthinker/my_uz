package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    accentColor: Color = MaterialTheme.colorScheme.primary, // ✅ Używane do kółka
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Stałe kolory tekstów dla jasnego tła (zgodne z Twoim kodem Fluttera)
    val titleColor = Color(0xFF1D192B)
    val detailsColor = Color(0xFF49454F)
    val avatarTextColor = Color.White // Biały tekst na ciemnym akcencie

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top // Tytuł i Kółko wyrównane do góry
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tytuł
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

                // Wiersz: Czas + Sala
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Czas (Ikona + Tekst)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mała kropka/ikona zamiast pustego kontenera 16x16
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
                            ),
                            maxLines = 1
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
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false) // Zapobiega rozpychaniu
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            when (type) {
                ClassCardType.HOME -> {
                    val letter = classItem.classType.firstOrNull()?.uppercase() ?: "A"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor) // ✅ Ciemniejszy kolor akcentu
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
                            .background(accentColor)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClassCardPreview() {
    val mockClass = ClassEntity(
        subjectName = "Podstawy systemów dyskretnych",
        classType = "Wykład",
        startTime = "10:00",
        endTime = "10:45",
        dayOfWeek = 1,
        date = "2025-01-01",
        groupCode = "32INF",
        subgroup = "L1",
        room = "Sala 102 A-2"
    )
    ClassCard(
        classItem = mockClass,
        backgroundColor = Color(0xFFE8DEF8),
        accentColor = Color(0xFF6750A4)
    )
}