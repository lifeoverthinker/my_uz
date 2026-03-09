package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.ui.theme.MyUZTheme

@Composable
fun IndexTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IndexTabItem(
            text = "Oceny",
            isSelected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        IndexTabItem(
            text = "Nieobecności",
            isSelected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
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
    // Używamy kolorów systemowych z Twojego Theme.kt
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
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
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            ),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndexTabsPreview() {
    MyUZTheme {
        Column(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surface)) {
            IndexTabs(selectedTabIndex = 0, onTabSelected = {})
            Spacer(modifier = Modifier.height(16.dp))
            IndexTabs(selectedTabIndex = 1, onTabSelected = {})
        }
    }
}