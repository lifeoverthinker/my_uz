package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Potrzebne do nadpisania rozmiaru

@Composable
fun AverageCard(
    label: String,
    average: Double?,
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary

    val displayAverage = average?.let { String.format("%.1f", it) } ?: "-"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                // titleSmall: 14sp, Medium.
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )

            Text(
                text = displayAverage,
                // titleLarge: 22sp, Normal. Idealnie pasuje do rozmiaru średniej.
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
        }
    }
}