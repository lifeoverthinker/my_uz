package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.data.models.GradeEntity

@Composable
fun GradeBubble(
    grade: GradeEntity,
    modifier: Modifier = Modifier
) {
    val isPoints = grade.isPoints
    val value = grade.grade

    val displayText = when {
        isPoints -> if (value % 1.0 == 0.0) "${value.toInt()} pkt" else "$value pkt"
        value == -1.0 -> "+"
        value % 1.0 == 0.0 -> value.toInt().toString()
        else -> value.toString()
    }

    val containerColor = if (isPoints) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isPoints) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .height(32.dp)
            .defaultMinSize(minWidth = 36.dp)
            .clip(CircleShape) // PEŁNE ZAOKRĄGLENIE ("Pill Shape" - Material 3)
            .background(containerColor)
            .padding(horizontal = 14.dp), // Odrobinę szerszy padding dla kapsułki
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}