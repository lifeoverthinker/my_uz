package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.theme.InterFontFamily

data class GradeItem(
    val id: Int,
    val value: String
)

@Composable
fun GradeBubble(
    grade: GradeItem,
    onGradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE9DEF8))
            .clickable { onGradeClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = grade.value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = Color(0xFF4B4358)
            )
        )
    }
}

@Preview
@Composable
fun GradeBubblePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GradeBubble(
            grade = GradeItem(id = 1, value = "4"),
            onGradeClick = {}
        )
        GradeBubble(
            grade = GradeItem(id = 2, value = "4.5"),
            onGradeClick = {}
        )
    }
}
