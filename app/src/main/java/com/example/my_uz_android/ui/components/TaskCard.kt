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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier,
    showDayMarker: Boolean = true
) {
    // ✅ FIOLKTOWY JAK CLASS/EVENT (E8DEF8)
    val cardBackgroundColor = MaterialTheme.extendedColors.classCardBackground  // E8DEF8!

    // ✅ CZARNY TEKST jak w ClassCard/EventCard
    val titleColor = Color(0xFF000000)  // Dokładnie czarny!
    val dateColor = Color(0xFF000000).copy(alpha = 0.7f)  // Czarny z alpha

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))  // Jak EventCard
            .background(cardBackgroundColor)  // Fiolet E8DEF8
            .clickable { onTaskClick() }
            .padding(12.dp),  // Dokładnie jak EventCard
        verticalArrangement = Arrangement.spacedBy(8.dp)  // Jak EventCard
    ) {
        // ✅ 1. TYTUŁ (duży, czarny)
        Text(
            text = task.title,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,  // Medium jak EventCard
                fontSize = 14.sp,
                lineHeight = 20.sp,  // Dokładnie jak EventCard
                letterSpacing = 0.1.sp,
                color = titleColor  // CZARNY!
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ✅ 2. DATA (czarny, mniejszy)
        if (showDayMarker) {
            Text(
                text = formatTaskDate(task.dueDate),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,  // Jak EventCard
                    letterSpacing = 0.4.sp,
                    color = dateColor  // Czarny z alpha
                ),
                maxLines = 1
            )
        }
    }
}

// ✅ FORMAT DATA (Pn 5.12)
private fun formatTaskDate(dueDate: Long): String {
    return try {
        val date = java.time.Instant.ofEpochMilli(dueDate)
            .atZone(java.time.ZoneId.systemDefault())
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
