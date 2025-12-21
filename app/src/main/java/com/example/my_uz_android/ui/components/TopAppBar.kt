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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

/**
 * Specjalny pasek dla Kalendarza i Terminarza (Styl z Figmy).
 * Posiada przyciski z tłem w kolorze 0xfff7f2f9.
 */
@Composable
fun CalendarTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onTitleClick: (() -> Unit)? = null
) {
    // Kolory z Figmy / Theme
    val buttonBackgroundColor = MaterialTheme.extendedColors.homeTopBackground // 0xfff7f2f9
    val contentColor = MaterialTheme.colorScheme.onSurface // 0xff1d192b

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .statusBarsPadding(), // Obsługa wcięcia na kamerę/pasek stanu
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // --- LEWA STRONA: Menu + Tytuł ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Przycisk Menu (Kółko)
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

            // Tytuł (np. "Lipiec" lub "Terminarz") + Opcjonalna strzałka
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = onTitleClick != null) { onTitleClick?.invoke() }
                    .padding(4.dp) // padding dla efektu kliknięcia
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

        // --- PRAWA STRONA: Szukaj + Dodaj ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Przycisk Szukaj
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

            // Przycisk Plus (Dodaj)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonBackgroundColor)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "Dodaj",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Standardowy pasek dla innych ekranów aplikacji.
 */
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
                // Styl przycisku z tłem (jak w Home)
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
        androidx.compose.material3.TopAppBar(
            title = titleContent,
            navigationIcon = navigationIconContent,
            actions = actions,
            colors = colors
        )
    }
}