package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showDayMarker: Boolean = true
) {
    // ✅ Logika ukończonego zadania
    val isCompleted = task.isCompleted

    // Kolory podstawowe (Dostosowane do Light/Dark)
    val baseBackgroundColor = MaterialTheme.extendedColors.classCardBackground
    val baseTitleColor = MaterialTheme.colorScheme.onSurface
    val baseDateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    // Kolory finalne (zależne od statusu)
    val cardBackgroundColor = if (isCompleted) MaterialTheme.extendedColors.grayInactive.copy(alpha = 0.3f) else baseBackgroundColor
    val titleColor = if (isCompleted) baseTitleColor.copy(alpha = 0.5f) else baseTitleColor
    val dateColor = if (isCompleted) baseDateColor.copy(alpha = 0.5f) else baseDateColor

    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. TYTUŁ
        Text(
            text = task.title,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
                color = titleColor,
                textDecoration = textDecoration
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 2. DATA (Opcjonalnie)
        if (showDayMarker) {
            Text(
                text = formatTaskDate(task.dueDate),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.4.sp,
                    color = dateColor
                ),
                maxLines = 1
            )
        }
    }
}

private fun formatTaskDate(dueDate: Long): String {
    return try {
        val date = Instant.ofEpochMilli(dueDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val dayAbbr = getDayAbbr(date.dayOfWeek.value)
        "${dayAbbr} ${date.dayOfMonth}.${date.monthValue}"
    } catch (e: Exception) {
        ""
    }
}

private fun getDayAbbr(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Pn"; 2 -> "Wt"; 3 -> "Śr"; 4 -> "Czw";
        5 -> "Pt"; 6 -> "Sob"; 7 -> "Ndz"; else -> "??"
    }
}