package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.data.models.TaskEntity
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
    val isCompleted = task.isCompleted

    val baseBackgroundColor = MaterialTheme.extendedColors.taskCardBackground // pastelowe tło Task
    val cardBackgroundColor = if (isCompleted)
        MaterialTheme.extendedColors.grayInactive.copy(alpha = 0.2f)
    else
        baseBackgroundColor

    val titleColor = Color(0xFF1D192B)
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = titleColor,
                textDecoration = textDecoration
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (showDayMarker) {
            val dateText = formatTaskDateShort(task.dueDate)
            val subjectText = task.subjectName?.takeIf { it.isNotBlank() }
            val fullText = subjectText?.let { "$dateText • $it" } ?: dateText

            Text(
                text = fullText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = titleColor
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
                val dayAbbr = when (date.dayOfWeek.value) {
                    1 -> "Pn"; 2 -> "Wt"; 3 -> "Śr"; 4 -> "Czw";
                    5 -> "Pt"; 6 -> "Sob"; 7 -> "Ndz"; else -> "??"
                }
                val monthAbbr = when (date.monthValue) {
                    1 -> "sty"; 2 -> "lut"; 3 -> "mar"; 4 -> "kwi"
                    5 -> "maj"; 6 -> "cze"; 7 -> "lip"; 8 -> "sie"
                    9 -> "wrz"; 10 -> "paź"; 11 -> "lis"; 12 -> "gru"
                    else -> ""
                }
                "${dayAbbr}, ${date.dayOfMonth} $monthAbbr"
            }
        }
    } catch (_: Exception) {
        ""
    }
}