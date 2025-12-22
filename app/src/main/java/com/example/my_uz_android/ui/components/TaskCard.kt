package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    // onCheckClick usunięty, bo stary kod go nie używał w TasksScreen
    modifier: Modifier = Modifier,
    showDayMarker: Boolean = true
) {
    val isCompleted = task.isCompleted
    val baseBackgroundColor = MaterialTheme.extendedColors.classCardBackground
    val baseTitleColor = MaterialTheme.colorScheme.onSurface
    val baseDateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    val cardBackgroundColor = if (isCompleted)
        MaterialTheme.extendedColors.grayInactive.copy(alpha = 0.3f)
    else baseBackgroundColor

    val titleColor = if (isCompleted) baseTitleColor.copy(alpha = 0.5f) else baseTitleColor
    val dateColor = if (isCompleted) baseDateColor.copy(alpha = 0.5f) else baseDateColor
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // LINIA 1: TYTUŁ
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

        // LINIA 2: DATA + PRZEDMIOT
        if (showDayMarker) {
            val dateText = formatTaskDateShort(task.dueDate)
            val subjectText = task.subjectName?.takeIf { it.isNotBlank() }

            val fullText = if (subjectText != null) {
                "$dateText • $subjectText"
            } else {
                dateText
            }

            Text(
                text = fullText,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.4.sp,
                    color = dateColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTaskDateShort(dueDate: Long): String {
    return try {
        val date = Instant.ofEpochMilli(dueDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        when {
            date == today -> "Dziś"
            date == tomorrow -> "Jutro"
            else -> {
                val dayAbbr = getDayAbbr(date.dayOfWeek.value)
                "${dayAbbr}, ${date.dayOfMonth} ${getMonthShort(date.monthValue)}"
            }
        }
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

private fun getMonthShort(month: Int): String {
    return when (month) {
        1 -> "sty"; 2 -> "lut"; 3 -> "mar"; 4 -> "kwi"
        5 -> "maj"; 6 -> "cze"; 7 -> "lip"; 8 -> "sie"
        9 -> "wrz"; 10 -> "paź"; 11 -> "lis"; 12 -> "gru"
        else -> ""
    }
}