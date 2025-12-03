package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun TaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Logika wizualna statusu (wyszarzenie)
    val cardAlpha = if (task.isCompleted) 0.5f else 1f

    // Kolor tła (zachowujemy fioletowy z theme)
    val backgroundColor = MaterialTheme.extendedColors.classCardBackground

    // Style spójne z EventCard
    val shape = RoundedCornerShape(8.dp)
    val isDarkTheme = isSystemInDarkTheme()
    val contentColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF1D1B20)

    // Logika przekreślenia
    val titleDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(shape)
            .background(backgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp), // Padding jak w EventCard
        verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing jak w EventCard
    ) {
        // Górny wiersz: Tytuł + Godzina
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = task.title,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium, // Jak w EventCard
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.1.sp,
                    color = contentColor,
                    textDecoration = titleDecoration
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!task.dueTime.isNullOrEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.dueTime,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                )
            }
        }

        // Dolny wiersz: Przedmiot / Typ / Opis (połączone stylem Description z EventCard)
        val detailsParts = listOfNotNull(
            task.subjectName.takeIf { it.isNotEmpty() },
            task.classType.takeIf { it.isNotEmpty() }
        )

        val detailsText = detailsParts.joinToString(" • ")

        if (detailsText.isNotEmpty()) {
            Text(
                text = detailsText,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.4.sp,
                    color = contentColor.copy(alpha = 0.8f) // Lekko jaśniejszy
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ewentualny opis w nowej linii, jeśli jest ważny
        if (!task.description.isNullOrEmpty()) {
            Text(
                text = task.description,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.4.sp,
                    color = contentColor.copy(alpha = 0.6f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}