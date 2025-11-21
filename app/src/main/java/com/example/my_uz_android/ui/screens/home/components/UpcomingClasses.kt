package com.example.my_uz_android.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard

@Composable
fun UpcomingClasses(
    classes: List<ClassEntity>,
    emptyMessage: String?,
    modifier: Modifier = Modifier
) {
    val purpleColor = Color(0xFFE8DEF8)
    val pinkColor = Color(0xFFFFD8E4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nagłówek sekcji z ikoną
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_check),
                contentDescription = null,
                tint = Color(0xFF1D192B),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Najbliższe zajęcia",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF1D192B)
                )
            )
        }

        if (classes.isEmpty()) {
            Text(
                text = emptyMessage ?: "Brak zajęć",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(classes) { index, classItem ->
                    val cardColor = if (index % 2 == 0) purpleColor else pinkColor
                    ClassCard(
                        classItem = classItem,
                        backgroundColor = cardColor,
                        modifier = Modifier.width(264.dp)
                    )
                }
            }
        }
    }
}