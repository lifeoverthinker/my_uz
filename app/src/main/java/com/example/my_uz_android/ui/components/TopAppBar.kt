package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // DODANE
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
private fun BaseTopBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun TopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int? = R.drawable.ic_chevron_left,
    onNavigationClick: () -> Unit = {},
    isNavigationIconFilled: Boolean = false,
    isCenterAligned: Boolean = false, // PRZYWRÓCONE
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (() -> Unit)? = null // PRZYWRÓCONE
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navigationIcon != null) {
                    TopBarActionIcon(
                        icon = navigationIcon,
                        onClick = onNavigationClick,
                        isFilled = isNavigationIconFilled
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isCenterAligned) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                actions()
            }
            bottomContent?.invoke() // WYWOŁANIE DOLNEJ ZAWARTOŚCI
        }
    }
}

@Composable
fun CalendarTopAppBar(
    title: String,
    isExpanded: Boolean,
    onNavigationClick: () -> Unit,
    onTitleClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    BaseTopBarContainer {
        TopBarActionIcon(
            icon = R.drawable.ic_menu,
            onClick = onNavigationClick,
            isFilled = true
        )

        Spacer(Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onTitleClick() }, // TERAZ DZIAŁA
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        actions()
    }
}


/**
 * PASEK WYSZUKIWANIA
 */
@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        color = extendedColors.buttonBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(painterResource(R.drawable.ic_chevron_left), "Wstecz")
            }

            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                if (query.isEmpty()) {
                    Text("Szukaj...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
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
                    Icon(painterResource(R.drawable.ic_close), "Wyczyść")
                }
            }
        }
    }
}

/**
 * REUŻYWALNA IKONA NA OKRĄGŁYM TLE
 */

@Composable
fun TopBarActionIcon(
    icon: Int,
    onClick: () -> Unit,
    isFilled: Boolean = false,
    isLoading: Boolean = false,
    tint: Color = extendedColors.iconText
) {
    IconButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .size(48.dp)
            .then(
                if (isFilled) Modifier.background(extendedColors.homeButtonBackground, CircleShape)
                else Modifier
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = tint
            )
        } else {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint
            )
        }
    }
}

/**
 * SPECJALISTYCZNY PASEK DLA PODGLĄDU (Preview)
 */
@Composable
fun PreviewTopAppBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean,
    actionIcon: Int,
    onActionClick: () -> Unit
) {
    TopAppBar(
        title = title,
        subtitle = subtitle,
        onNavigationClick = onBackClick,
        actions = {
            TopBarActionIcon(icon = actionIcon, onClick = onActionClick)
            TopBarActionIcon(
                icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                tint = if (isFavorite) Color.Red else extendedColors.iconText,
                onClick = onFavoriteClick
            )
        }
    )
}