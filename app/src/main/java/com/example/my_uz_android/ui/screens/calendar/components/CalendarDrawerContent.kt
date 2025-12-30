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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity

@Composable
fun CalendarDrawerContent(
    favorites: List<FavoriteEntity>,
    selectedResourceId: String?,
    currentScreen: String,
    onMyPlanClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFavoriteClick: (FavoriteEntity) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Filtrowanie po Stringach (zgodnie z "dobrym starym kodem")
    val groupFavorites = favorites.filter { it.type == "group" }
    val teacherFavorites = favorites.filter { it.type == "teacher" }

    // Kolory pobierane z motywu
    val backgroundColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(backgroundColor, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
    ) {
        // --- Sekcja: Menu ---
        DrawerSectionHeader(text = "Menu")

        DrawerItem(
            label = "Kalendarz",
            iconRes = R.drawable.ic_calendar,
            isSelected = currentScreen == "calendar" && selectedResourceId == null,
            onClick = onMyPlanClick
        )

        DrawerItem(
            label = "Terminarz",
            iconRes = R.drawable.ic_book_open,
            isSelected = currentScreen == "tasks",
            onClick = onTasksClick
        )

        DrawerDivider()

        // --- Sekcja: Grupy ---
        DrawerSectionHeader(text = "Grupy")
        if (groupFavorites.isNotEmpty()) {
            groupFavorites.forEach { fav ->
                DrawerItem(
                    label = fav.name,
                    iconRes = R.drawable.ic_users,
                    isSelected = selectedResourceId == fav.resourceId,
                    onClick = { onFavoriteClick(fav) }
                )
            }
        } else {
            // Napis gdy brak grup
            EmptyFavoritesText("Brak ulubionych grup")
        }

        DrawerDivider()

        // --- Sekcja: Nauczyciele ---
        DrawerSectionHeader(text = "Nauczyciele")
        if (teacherFavorites.isNotEmpty()) {
            teacherFavorites.forEach { fav ->
                DrawerItem(
                    label = fav.name,
                    iconRes = R.drawable.ic_user,
                    isSelected = selectedResourceId == fav.resourceId,
                    onClick = { onFavoriteClick(fav) }
                )
            }
        } else {
            // Napis gdy brak nauczycieli
            EmptyFavoritesText("Brak ulubionych nauczycieli")
        }
    }
}

@Composable
fun DrawerSectionHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Text(
            style = MaterialTheme.typography.titleSmall,
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DrawerItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = fontWeight),
                text = label,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyFavoritesText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DrawerDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}