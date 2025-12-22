package com.example.my_uz_android.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.ScheduleType
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun CalendarDrawerContent(
    favorites: List<FavoriteEntity>,
    selectedResourceId: String?,
    currentScreen: String = "calendar", // "calendar" lub "tasks"
    onMyPlanClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFavoriteClick: (FavoriteEntity) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    ModalDrawerSheet(
        drawerContainerColor = backgroundColor,
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            SectionHeader(text = "Menu", textColor = onSurfaceVariant)

            DrawerItem(
                label = "Mój Plan",
                iconRes = R.drawable.ic_home,
                // Zaznaczone tylko gdy jesteśmy w kalendarzu i brak wybranego zasobu (plan domyślny)
                selected = currentScreen == "calendar" && selectedResourceId == null,
                onClick = {
                    onMyPlanClick()
                    onCloseDrawer()
                },
                activeColor = secondaryContainer,
                activeContentColor = onSecondaryContainer,
                inactiveContentColor = onSurfaceVariant
            )

            DrawerItem(
                label = "Terminarz",
                iconRes = R.drawable.ic_calendar_check,
                // Zaznaczone gdy jesteśmy w tasks
                selected = currentScreen == "tasks",
                onClick = {
                    onTasksClick()
                    onCloseDrawer()
                },
                activeColor = secondaryContainer,
                activeContentColor = onSecondaryContainer,
                inactiveContentColor = onSurfaceVariant
            )

            DrawerItem(
                label = "Wyszukaj plan",
                iconRes = R.drawable.ic_search,
                selected = false,
                onClick = {
                    onSearchClick()
                    onCloseDrawer()
                },
                activeColor = secondaryContainer,
                activeContentColor = onSecondaryContainer,
                inactiveContentColor = onSurfaceVariant
            )

            DrawerDivider(color = dividerColor)

            val groupFavorites = favorites.filter { it.type == ScheduleType.GROUP.name }
            if (groupFavorites.isNotEmpty()) {
                SectionHeader(text = "Grupy", textColor = onSurfaceVariant)
                groupFavorites.forEach { fav ->
                    DrawerItem(
                        label = fav.name,
                        iconRes = R.drawable.ic_users,
                        // Zaznaczone gdy Kalendarz i ID się zgadza
                        selected = currentScreen == "calendar" && selectedResourceId == fav.resourceId,
                        onClick = {
                            onFavoriteClick(fav)
                            onCloseDrawer()
                        },
                        activeColor = secondaryContainer,
                        activeContentColor = onSecondaryContainer,
                        inactiveContentColor = onSurfaceVariant
                    )
                }
                DrawerDivider(color = dividerColor)
            }

            val teacherFavorites = favorites.filter { it.type == ScheduleType.TEACHER.name }
            if (teacherFavorites.isNotEmpty()) {
                SectionHeader(text = "Nauczyciele", textColor = onSurfaceVariant)
                teacherFavorites.forEach { fav ->
                    DrawerItem(
                        label = fav.name,
                        iconRes = R.drawable.ic_user,
                        selected = currentScreen == "calendar" && selectedResourceId == fav.resourceId,
                        onClick = {
                            onFavoriteClick(fav)
                            onCloseDrawer()
                        },
                        activeColor = secondaryContainer,
                        activeContentColor = onSecondaryContainer,
                        inactiveContentColor = onSurfaceVariant
                    )
                }
                DrawerDivider(color = dividerColor)
            }

            SectionHeader(text = "Inne", textColor = onSurfaceVariant)

            DrawerItem(
                label = "Ustawienia",
                iconRes = R.drawable.ic_settings,
                selected = false,
                onClick = {
                    onSettingsClick()
                    onCloseDrawer()
                },
                activeColor = secondaryContainer,
                activeContentColor = onSecondaryContainer,
                inactiveContentColor = onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight(500),
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = textColor
        )
    }
}

@Composable
private fun DrawerItem(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    activeContentColor: Color,
    inactiveContentColor: Color
) {
    val backgroundColor = if (selected) activeColor else Color.Transparent
    val contentColor = if (selected) activeContentColor else inactiveContentColor
    val fontWeight = if (selected) FontWeight(600) else FontWeight(500)

    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = fontWeight,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun DrawerDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HorizontalDivider(color = color)
    }
}