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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val contentColor = Color(0xFF1D1B20)

    // Dla Kalendarza mniejsze zaokrąglenie (6dp), dla Home (16dp - jak w Twoim wzorze)
    val cornerRadius = if (type == ClassCardType.CALENDAR) 6.dp else 16.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (type == ClassCardType.CALENDAR) {
            // --- WIDOK KALENDARZA (Kompaktowy, tekst u góry) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Top // Wyrównanie do góry (jak kropka)
            ) {
                // Mały odstęp od góry
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = classItem.subjectName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${classItem.room ?: ""} (${classItem.classType})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    ),
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            // --- WIDOK HOME (Twój wzór z poprzedniego zgłoszenia) ---
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Kolumna Czasu
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) {
                    Text(
                        text = classItem.startTime,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        ),
                        color = contentColor
                    )
                    Text(
                        text = classItem.endTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = InterFontFamily
                        ),
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }

                // 2. Separator pionowy
                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight(),
                    thickness = 1.dp,
                    color = contentColor.copy(alpha = 0.1f)
                )

                // 3. Informacje o zajęciach
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = classItem.subjectName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily,
                            fontSize = 18.sp
                        ),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ikona Sali
                        Icon(
                            painter = painterResource(id = R.drawable.ic_marker_pin),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = contentColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = classItem.room ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = InterFontFamily
                            ),
                            color = contentColor.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Ikona Nauczyciela
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = contentColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = classItem.teacherName ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = InterFontFamily
                            ),
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 4. Badge z typem zajęć
                Surface(
                    color = contentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = classItem.classType.take(1).uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        ),
                        color = contentColor
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
        subjectName = "Wstęp do programowania",
        classType = "Laboratorium",
        startTime = "08:00",
        endTime = "09:30",
        dayOfWeek = 1,
        date = "2025-01-01",
        groupCode = "G1",
        subgroup = null,
        room = "301",
        teacherName = "dr inż. Jan Kowalski"
    )
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ClassCard(mockClass, type = ClassCardType.HOME, backgroundColor = Color(0xFFE8DEF8))

        // Symulacja małego boxa w kalendarzu
        Box(modifier = Modifier.height(90.dp)) {
            ClassCard(mockClass, type = ClassCardType.CALENDAR, backgroundColor = Color(0xFFE8DEF8), modifier = Modifier.fillMaxSize())
        }
    }
}