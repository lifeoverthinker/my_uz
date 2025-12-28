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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    // Używamy secondaryContainer (jasny fiolet w light mode)
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onGradeClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = grade.value,
            // bodySmall: 12sp.
            style = MaterialTheme.typography.bodySmall,
            color = textColor
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