package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    // Rozpoznajemy typ na podstawie encji (Logika zachowana!)
    val isPoints = grade.isPoints
    val value = grade.grade

    // Logika tekstu: Punkty z dopiskiem "pkt", aktywność jako "+", reszta normalnie
    val displayText = when {
        isPoints -> if (value % 1.0 == 0.0) "${value.toInt()} pkt" else "$value pkt"
        value == -1.0 -> "+"
        value % 1.0 == 0.0 -> value.toInt().toString()
        else -> value.toString()
    }

    // Kolory: Punkty mają secondaryContainer (np. fioletowy/różowy), oceny primary (niebieski)
    val containerColor = if (isPoints)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val contentColor = if (isPoints)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .height(32.dp) // Stała wysokość
            .defaultMinSize(minWidth = 36.dp) // Minimalna szerokość
            .clip(RoundedCornerShape(8.dp)) // Zmiana na "pigułkę" pasującą do reszty
            .background(containerColor)
            .padding(horizontal = 12.dp), // Większy padding, by pigułka lepiej wyglądała
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}