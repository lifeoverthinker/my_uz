package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontStyle
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
    val cardAlpha = if (task.isCompleted) 0.5f else 1f
    val backgroundColor = MaterialTheme.extendedColors.classCardBackground
    val shape = RoundedCornerShape(8.dp)
    val isDarkTheme = isSystemInDarkTheme()
    val contentColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF1D1B20)
    val titleDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(shape)
            .background(backgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tytuł + Godzina
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = task.title,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
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

        // Szczegóły (Przedmiot • Typ)
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
                    color = contentColor.copy(alpha = 0.8f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Opis (Zawsze widoczny)
        val hasDescription = !task.description.isNullOrEmpty()
        Text(
            text = if (hasDescription) task.description!! else "Brak opisu",
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = contentColor.copy(alpha = if (hasDescription) 0.6f else 0.4f), // Jaśniejszy dla placeholder
                fontStyle = if (hasDescription) FontStyle.Normal else FontStyle.Italic // Kursywa dla placeholder
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}