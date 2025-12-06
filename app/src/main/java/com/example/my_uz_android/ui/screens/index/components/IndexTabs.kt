package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.theme.InterFontFamily // Zakładam, że masz to zdefiniowane w Type.kt

@Composable
fun IndexTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Odpowiednik: Container(width: 328...) - szerokość będzie kontrolowana przez parenta (IndexScreen)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab 0: Oceny
        IndexTabItem(
            text = "Oceny",
            isSelected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f) // Odpowiednik Expanded
        )

        // Tab 1: Nieobecności
        IndexTabItem(
            text = "Nieobecności",
            isSelected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f) // Odpowiednik Expanded
        )
    }
}

@Composable
fun IndexTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Style zdefiniowane na podstawie Twojego kodu Flutter
    val backgroundColor = if (isSelected) Color(0xFFE9DEF8) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF4B4358) else Color(0xFF49454E)
    val borderColor = if (isSelected) Color.Transparent else Color(0xFF7A757F)
    val borderWidth = if (isSelected) 0.dp else 1.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 12.sp,
                fontFamily = InterFontFamily, // Użycie Twojej czcionki Inter
                fontWeight = FontWeight.W400,
                letterSpacing = 0.4.sp,
                lineHeight = 16.sp // height 1.33 przy font 12 ~= 16sp
            ),
            textAlign = TextAlign.Center
        )
    }
}