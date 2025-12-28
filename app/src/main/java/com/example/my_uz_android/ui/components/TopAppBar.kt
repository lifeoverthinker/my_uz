package com.example.my_uz_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

/**
 * Specjalny pasek dla Kalendarza (Twój "stary wygląd").
 * Zaktualizowany o obsługę podtytułu i zmiennej ikony nawigacji, zachowując styl.
 */
@Composable
fun CalendarTopAppBar(
    title: String,
    subtitle: String? = null, // NOWE: Podtytuł (np. nazwa grupy)
    navigationIcon: Int = R.drawable.ic_menu, // NOWE: Domyślnie menu, ale można zmienić na strzałkę
    isExpanded: Boolean = false,
    onNavigationClick: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null, // NOWE: Ikona info (dla nauczycieli)
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
        // LEWA STRONA: Nawigacja + Tytuł (i podtytuł)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Przycisk nawigacji w kółku
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonBackgroundColor)
                    .clickable(onClick = onNavigationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(navigationIcon),
                    contentDescription = "Nawigacja",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }

            // Tytuł i Podtytuł
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = onTitleClick != null) { onTitleClick?.invoke() }
                    .padding(4.dp)
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.Center
            ) {
                // Wiersz tytułu + strzałka (stary wygląd)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            lineHeight = 24.sp
                        ),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (onTitleClick != null) {
                        Icon(
                            painter = painterResource(
                                if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
                            ),
                            contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                            modifier = Modifier.requiredSize(20.dp),
                            tint = contentColor
                        )
                    }
                }

                // Podtytuł (jeśli jest) - dodany subtelnie pod spodem
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // PRAWA STRONA: Ikony akcji
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Ikona INFO (Nowa)
            if (onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(buttonBackgroundColor)
                        .clickable(onClick = onInfoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = "Info",
                        modifier = Modifier.size(24.dp),
                        tint = contentColor
                    )
                }
            }

            // Ikona Szukaj
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

            // Ikona Dzisiaj
            if (onAddClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(buttonBackgroundColor)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
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
    scrollBehavior: TopAppBarScrollBehavior? = null, // Dodano scrollBehavior
    titleContent: (@Composable () -> Unit)? = null
) {
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
            scrollBehavior = scrollBehavior,
            colors = colors
        )
    } else {
        androidx.compose.material3.TopAppBar(
            title = finalTitleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = colors
        )
    }
}

@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Wstecz",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        "Szukaj planu...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Wyczyść",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}