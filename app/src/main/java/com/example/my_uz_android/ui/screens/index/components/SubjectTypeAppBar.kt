package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun SubjectTypeAppBar(
    subjectName: String,
    className: String,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit, // ✅ Nowa akcja
    onSortClick: () -> Unit,   // ✅ Nowa akcja
    modifier: Modifier = Modifier,
) {
    val iconTint = Color(0xFF1D1B20)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Przycisk Wstecz (ic_chevron_left)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_left), // Ikona MyUZ
                contentDescription = "Wróć",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Tytuły
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = subjectName,
                style = TextStyle(
                    color = iconTint,
                    fontSize = 22.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W400,
                    lineHeight = 28.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = className,
                style = TextStyle(
                    color = Color(0xFF49454F),
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W400,
                    lineHeight = 16.sp,
                    letterSpacing = 0.40.sp
                )
            )
        }

        // Akcje (Filter i Sort/Opcje)
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(48.dp)
        ) {
            // Przycisk Filtrowania
            Box(modifier = Modifier
                .size(48.dp)
                .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter_funnel),
                    contentDescription = "Filtruj",
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Przycisk Sortowania / Opcji
            Box(modifier = Modifier
                .size(48.dp)
                .clickable { onSortClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_2),
                    contentDescription = "Sortuj/Opcje",
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}