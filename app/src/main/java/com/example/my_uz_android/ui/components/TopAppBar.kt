package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

// Stała wielkość dla wszystkich okrągłych przycisków w TopBar
private val TopBarButtonSize = 48.dp
private val TopBarIconSize = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int? = R.drawable.ic_chevron_left,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isCenterAligned: Boolean = false,
    isNavigationIconFilled: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val iconTint = MaterialTheme.extendedColors.iconText

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                if (isNavigationIconFilled) {
                    Box(
                        modifier = Modifier
                            .size(TopBarButtonSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.extendedColors.buttonBackground)
                            .clickable(onClick = onNavigationClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = navigationIcon),
                            contentDescription = "Nawigacja",
                            modifier = Modifier.size(TopBarIconSize),
                            tint = iconTint
                        )
                    }
                } else {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            painter = painterResource(id = navigationIcon),
                            contentDescription = "Nawigacja",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (isCenterAligned) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = if (isCenterAligned) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
    }
}

@Composable
fun CalendarTopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int = R.drawable.ic_menu,
    isExpanded: Boolean = false,
    onNavigationClick: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    isShareLoading: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val iconColor = MaterialTheme.extendedColors.iconText

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(TopBarButtonSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.buttonBackground)
                    .clickable(onClick = onNavigationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(navigationIcon), null, Modifier.size(TopBarIconSize), iconColor)
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onTitleClick != null) { onTitleClick?.invoke() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (onTitleClick != null) {
                        Box(
                            modifier = Modifier.size(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (actions != null) {
                    actions()
                } else {
                    if (onShareClick != null) {
                        TopBarActionIcon(
                            icon = R.drawable.ic_share,
                            isLoading = isShareLoading,
                            onClick = onShareClick
                        )
                    }
                    if (onSearchClick != null) {
                        TopBarActionIcon(icon = R.drawable.ic_search, onClick = onSearchClick)
                    }
                    if (onAddClick != null) {
                        TopBarActionIcon(icon = R.drawable.ic_calendar, onClick = onAddClick)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarActionIcon(
    icon: Int,
    isLoading: Boolean = false,
    tint: Color = MaterialTheme.extendedColors.iconText,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(TopBarButtonSize)
            .clip(CircleShape)
            .background(MaterialTheme.extendedColors.buttonBackground)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.extendedColors.iconText
            )
        } else {
            Icon(
                painterResource(icon),
                null,
                Modifier.size(TopBarIconSize),
                tint = tint
            )
        }
    }
}

@Composable
fun PreviewTopAppBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onActionClick: () -> Unit,
    actionIcon: Int,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    val iconColor = MaterialTheme.extendedColors.iconText

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = null,
                    modifier = Modifier.size(TopBarIconSize),
                    tint = iconColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle.ifBlank { "Szczegóły" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopBarActionIcon(icon = actionIcon, onClick = onActionClick)
                TopBarActionIcon(
                    icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onFavoriteClick
                )
            }
        }
    }
}

@Composable
fun SearchTopAppBar(query: String, onQueryChange: (String) -> Unit, onBackClick: () -> Unit) {
    val elementsColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.extendedColors.buttonBackground,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(TopBarButtonSize)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "Wstecz",
                        modifier = Modifier.size(TopBarIconSize),
                        tint = elementsColor
                    )
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            "Szukaj grupy lub nauczyciela...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = elementsColor
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_x_close),
                            contentDescription = "Wyczyść",
                            modifier = Modifier.size(20.dp),
                            tint = elementsColor
                        )
                    }
                }
            }
        }
    }
}