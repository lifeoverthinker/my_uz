package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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

@Composable
fun AverageCard(
    label: String, // np. "Średnia z bieżącego semestru" lub "Średnia ocen"
    average: Double?,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(0xFF68548E)
    val textColor = Color.White

    // Wartość średniej do wyświetlenia (zaokrąglamy do jednego miejsca po przecinku lub '-')
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
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W500,
                    lineHeight = 20.sp,
                    letterSpacing = 0.10.sp
                )
            )

            Text(
                text = displayAverage,
                style = TextStyle(
                    color = textColor,
                    fontSize = 22.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W400,
                    lineHeight = 28.sp
                )
            )
        }
    }
}