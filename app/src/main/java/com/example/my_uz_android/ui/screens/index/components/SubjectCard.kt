package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun SubjectCard(
    subjectName: String,
    subjectCode: String,
    average: Double? = null, // Jeśli null, wyświetli "-"
    onClick: () -> Unit = {}
) {
    // Tło i cień z Figmy
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp, // Przybliżenie cieni
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
            .clickable { onClick() },
        color = Color(0xFFF8F1FA), // Kolor: F8F1FA
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // --- Lewa strona: Nazwa i Kod ---
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subjectName,
                    style = TextStyle(
                        color = Color(0xFF1D1B20),
                        fontSize = 16.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W500,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp
                    )
                )
                Text(
                    text = subjectCode,
                    style = TextStyle(
                        color = Color(0xFF1D1B20),
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W400,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp
                    )
                )
            }

            // --- Prawa strona: Średnia i Ikona ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kolumna ze średnią
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Wartość średniej (lub myślnik, jeśli average == null)
                    Box(
                        modifier = Modifier.width(45.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            // Używamy formatowania do wyświetlenia średniej z jednym miejscem po przecinku
                            text = average?.let { String.format("%.1f", it) } ?: "-",
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                color = Color(0xFF1D1B20),
                                fontSize = 22.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.W400,
                                lineHeight = 28.sp
                            )
                        )
                    }

                    // Napis "średnia"
                    Box(
                        modifier = Modifier.width(45.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "średnia",
                            style = TextStyle(
                                color = Color(0xFF1D1B20),
                                fontSize = 12.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.W400,
                                lineHeight = 16.sp,
                                letterSpacing = 0.4.sp
                            )
                        )
                    }
                }

                // Ikona rozwijania
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Rozwiń",
                        tint = Color(0xFF1D1B20)
                    )
                }
            }
        }
    }
}

// Przykład użycia komponentu z wartością średniej "5.0"
@Preview
@Composable
fun PreviewSubjectCardWithAverage() {
    Box(modifier = Modifier.padding(16.dp)) {
        SubjectCard(
            subjectName = "Bazy danych",
            subjectCode = "BD-2025",
            average = 5.0 // Średnia 5.0
        )
    }
}