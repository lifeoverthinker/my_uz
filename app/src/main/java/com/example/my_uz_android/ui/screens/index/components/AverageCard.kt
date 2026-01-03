package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp

@Composable
fun AverageCard(
    label: String,
    average: Double?,
    pointsSum: Double? = null, // Nowy parametr
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary
    val subTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)

    val displayAverage = average?.let { String.format("%.2f", it) } ?: "-"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(12.dp) // Lekko zaokrąglone rogi
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona: Tytuł
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            // Prawa strona: Średnia + Punkty (w kolumnie)
            Column(horizontalAlignment = Alignment.End) {
                // Średnia
                Text(
                    text = displayAverage,
                    style = MaterialTheme.typography.headlineMedium, // Większa czcionka dla średniej
                    color = textColor
                )

                // Suma punktów (jeśli istnieje)
                if (pointsSum != null && pointsSum > 0) {
                    val pointsText = if (pointsSum % 1.0 == 0.0) pointsSum.toInt().toString() else pointsSum.toString()
                    Text(
                        text = "Suma punktów: $pointsText",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor
                    )
                }
            }
        }
    }
}