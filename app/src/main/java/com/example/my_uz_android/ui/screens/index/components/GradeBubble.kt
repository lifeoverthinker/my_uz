package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.theme.InterFontFamily

// Uproszczony model do starego wariantu (używany w ExpandableSubjectCard z Folderu komponentów)
data class GradeItem(
    val id: Int,
    val value: Double? // null => "+", w naszej implementacji plus to 6.0, więc tu null raczej nie będzie używane
)

@Composable
fun GradeBubble(grade: GradeItem) {
    if (grade.value == null) return // Zabezpieczenie przed null

    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFE9DEF8),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (grade.value != null) {
                String.format("%.1f", grade.value).removeSuffix(".0")
            } else {
                "+"
            },
            style = TextStyle(
                color = Color(0xFF4B4358),
                fontSize = 12.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.W400,
                letterSpacing = 0.4.sp,
                lineHeight = 16.sp
            )
        )
    }
}

// NOWY wariant do wyświetlania wartości liczbowej z wagą i rozmiarami
@Composable
fun GradeBubbleValue(
    grade: Float,
    isWeightImportant: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isWeightImportant) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE9DEF8)
    val fg = if (isWeightImportant) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF4B4358)

    Box(
        modifier = modifier
            .background(bg, shape = CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%.1f", grade).removeSuffix(".0"),
            style = TextStyle(
                color = fg,
                fontSize = 12.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.W500,
                letterSpacing = 0.1.sp,
                lineHeight = 16.sp
            )
        )
    }
}