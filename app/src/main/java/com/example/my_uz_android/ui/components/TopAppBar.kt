package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun CalendarTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null
) {
    val buttonBackgroundColor = MaterialTheme.extendedColors.homeTopBackground
    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonBackgroundColor)
                    .clickable(onClick = onNavigationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = onTitleClick != null) { onTitleClick?.invoke() }
                    .padding(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 24.sp
                    ),
                    color = contentColor
                )

                if (onTitleClick != null) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onSearchClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(buttonBackgroundColor)
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Szukaj",
                        modifier = Modifier.size(24.dp),
                        tint = contentColor
                    )
                }
            }

            if (onAddClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(buttonBackgroundColor)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    // ZMIANA: Zamiast ic_plus jest teraz ic_calendar (powrót do dzisiaj)
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = "Dzisiaj",
                        modifier = Modifier.size(24.dp),
                        tint = contentColor
                    )
                }
            }
        }
    }
}

// ... Reszta (TopAppBar) bez zmian ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isCenterAligned: Boolean = false,
    isNavigationIconFilled: Boolean = false,
    titleContent: (@Composable () -> Unit)? = null
) {
    // ...
    val navigationIconContent: @Composable () -> Unit = {
        if (navigationIcon != null) {
            if (isNavigationIconFilled) {
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

    val defaultTitleContent: @Composable () -> Unit = {
        Column(horizontalAlignment = if (isCenterAligned) Alignment.CenterHorizontally else Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily,
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

    val finalTitleContent = titleContent ?: defaultTitleContent

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (isCenterAligned) {
        CenterAlignedTopAppBar(
            title = finalTitleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            colors = colors
        )
    } else {
        androidx.compose.material3.TopAppBar(
            title = finalTitleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            colors = colors
        )
    }
}