package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily

private const val MaxBarFontScale = 1.3f

@Composable
fun TopBarActionIcon(
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    isLoading: Boolean = false,
    isFilled: Boolean = true
) {
    val finalBackgroundColor = if (isFilled) backgroundColor else Color.Transparent

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = finalBackgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = iconTint
                )
            } else {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }
        }
    }
}

@Composable
fun SaveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(100.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.btn_save),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Composable
private fun BaseTopBarContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun TopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: Int? = null,
    onNavigationClick: () -> Unit = {},
    isNavigationIconFilled: Boolean = true,
    isCenterAligned: Boolean = false,
    isFilled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.background,
    bottomContent: @Composable (() -> Unit)? = null,
    titleClickable: Boolean = false,
    onTitleClick: () -> Unit = {},
    titleIcon: Int? = null,
    titleColor: Color? = null,
    titleMaxLines: Int = 2,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val cappedDensity = Density(
        density = density.density,
        fontScale = minOf(density.fontScale, MaxBarFontScale)
    )

    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        Column(modifier = Modifier.background(containerColor)) {
            BaseTopBarContainer(backgroundColor = containerColor) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isCenterAligned) Arrangement.Center else Arrangement.spacedBy(12.dp)
                ) {
                    if (navigationIcon != null) {
                        TopBarActionIcon(
                            icon = navigationIcon,
                            onClick = onNavigationClick,
                            isFilled = isNavigationIconFilled,
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable(enabled = titleClickable, onClick = onTitleClick)
                            .padding(vertical = 2.dp),
                        horizontalAlignment = if (isCenterAligned) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (titleColor != null) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color = titleColor, shape = RoundedCornerShape(4.dp))
                                )
                            }
                            Text(
                                text = title,
                                modifier = Modifier.weight(1f, fill = false),
                                style = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = if (subtitle != null) FontWeight.Medium else FontWeight.SemiBold,
                                    fontSize = if (subtitle != null) 16.sp else 22.sp,
                                    lineHeight = if (subtitle != null) 24.sp else 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = if (subtitle != null) 1 else titleMaxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (titleIcon != null) {
                                Icon(
                                    painter = painterResource(id = titleIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!subtitle.isNullOrEmpty()) {
                            Text(
                                text = subtitle,
                                style = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }

            bottomContent?.invoke()
        }
    }
}

@Composable
fun AddEditTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    saveButtonEnabled: Boolean = true,
    titleColor: Color? = null,
    navigationIcon: Int = R.drawable.ic_close
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        onNavigationClick = onBackClick,
        isNavigationIconFilled = true,
        titleColor = titleColor,
        actions = {
            SaveActionButton(
                onClick = onSaveClick,
                enabled = saveButtonEnabled
            )
        }
    )
}

@Composable
fun CalendarTopAppBar(
    title: String,
    isExpanded: Boolean,
    onNavigationClick: () -> Unit,
    onTitleClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = R.drawable.ic_menu,
        onNavigationClick = onNavigationClick,
        titleClickable = true,
        onTitleClick = onTitleClick,
        titleIcon = if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
        isNavigationIconFilled = true,
        actions = actions
    )
}

@Composable
fun PreviewTopAppBar(
    title: String,
    subtitle: String? = null,
    isFavorite: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    actionIcon: Int? = null,
    onActionClick: () -> Unit = {}
) {
    TopAppBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = R.drawable.ic_chevron_left,
        onNavigationClick = onBackClick,
        isNavigationIconFilled = true,
        actions = {
            if (actionIcon != null) {
                TopBarActionIcon(icon = actionIcon, onClick = onActionClick, isFilled = true)
            }
            TopBarActionIcon(
                icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                onClick = onFavoriteClick,
                isFilled = true,
                iconTint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .statusBarsPadding()
                    .heightIn(min = 72.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TopBarActionIcon(
                    icon = R.drawable.ic_chevron_left,
                    onClick = onBackClick,
                    isFilled = true,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp
            )
        }
    }
}