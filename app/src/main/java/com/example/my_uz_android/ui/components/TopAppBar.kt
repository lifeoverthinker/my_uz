package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isCenterAligned: Boolean = false,
    isNavigationIconFilled: Boolean = false
) {
    val navigationIconContent: @Composable () -> Unit = {
        if (navigationIcon != null) {
            if (isNavigationIconFilled) {
                // Nowy styl przycisku (okrągłe tło)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.extendedColors.homeTopBackground)
                        .clickable(onClick = onNavigationClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = navigationIcon),
                        contentDescription = "Nawigacja",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Standardowy styl przycisku
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        painter = painterResource(id = navigationIcon),
                        contentDescription = "Nawigacja",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    val titleContent: @Composable () -> Unit = {
        Column(horizontalAlignment = if (isCenterAligned) Alignment.CenterHorizontally else Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily,
                    // Dla centrowanego tytułu dajemy nieco grubszą czcionkę (jak w projekcie)
                    fontWeight = if (isCenterAligned) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 22.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (isCenterAligned) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            colors = colors
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            colors = colors
        )
    }
}