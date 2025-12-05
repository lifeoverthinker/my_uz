package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun EventCard(
    event: EventEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardBackgroundColor = MaterialTheme.extendedColors.eventCardBackground

    // Używamy kolorów z motywu zamiast sztywnych wartości (jak w ClassCard)
    val titleColor = MaterialTheme.colorScheme.onSurface
    val detailsColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = event.title,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
                color = titleColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = event.description,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = detailsColor
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
