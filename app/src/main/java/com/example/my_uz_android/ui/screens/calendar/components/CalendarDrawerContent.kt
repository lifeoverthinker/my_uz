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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity

// Definicje kolorów z Twojego schematu
private val ColorSurface = Color(0xffffffff)
private val ColorSelectedContainer = Color(0xffe8def8)
private val ColorOnSurfaceVariant = Color(0xff49454f)
private val ColorOnSecondaryContainer = Color(0xff4a4459)
private val ColorOutline = Color(0xff787579)

@Composable
fun CalendarDrawerContent(
    favorites: List<FavoriteEntity>,
    selectedResourceId: String?, // ID aktualnie wybranego planu (lub null dla "Mój Plan")
    currentScreen: String, // "calendar" lub "tasks"
    onMyPlanClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFavoriteClick: (FavoriteEntity) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()
    val groupFavorites = favorites.filter { it.type == "group" }
    val teacherFavorites = favorites.filter { it.type == "teacher" }

    Column(
        modifier = Modifier
            .width(300.dp) // Lekko poszerzone dla bezpieczeństwa, w Twoim kodzie było 288dp + padding
            .fillMaxHeight()
            .background(ColorSurface, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
    ) {
        // --- Sekcja: Menu ---
        DrawerSectionHeader(text = "Menu")

        // Item: Kalendarz (Mój Plan)
        DrawerItem(
            label = "Kalendarz",
            iconRes = R.drawable.ic_calendar,
            isSelected = currentScreen == "calendar" && selectedResourceId == null,
            onClick = onMyPlanClick
        )

        // Item: Terminarz (Zadania)
        DrawerItem(
            label = "Terminarz",
            iconRes = R.drawable.ic_check_square_broken,
            isSelected = currentScreen == "tasks",
            onClick = onTasksClick
        )

        DrawerDivider()

        // --- Sekcja: Grupy ---
        if (groupFavorites.isNotEmpty()) {
            DrawerSectionHeader(text = "Grupy")
            groupFavorites.forEach { fav ->
                DrawerItem(
                    label = fav.name,
                    iconRes = R.drawable.ic_users,
                    isSelected = selectedResourceId == fav.resourceId,
                    onClick = { onFavoriteClick(fav) }
                )
            }
            DrawerDivider()
        }

        // --- Sekcja: Nauczyciele ---
        if (teacherFavorites.isNotEmpty()) {
            DrawerSectionHeader(text = "Nauczyciele")
            teacherFavorites.forEach { fav ->
                DrawerItem(
                    label = fav.name,
                    iconRes = R.drawable.ic_user, // lub ic_stand / ic_graduation_hat
                    isSelected = selectedResourceId == fav.resourceId,
                    onClick = { onFavoriteClick(fav) }
                )
            }
            // Opcjonalny divider jeśli coś byłoby dalej
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
            style = TextStyle(fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 20.sp),
            text = text,
            color = ColorOnSurfaceVariant,
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
    val backgroundColor = if (isSelected) ColorSelectedContainer else Color.Transparent
    val contentColor = if (isSelected) ColorOnSecondaryContainer else ColorOnSurfaceVariant
    val fontWeight = if (isSelected) FontWeight(600) else FontWeight(500)

    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp)) // Kształt pigułki dla zaznaczenia
            .background(backgroundColor)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            // Label
            Text(
                modifier = Modifier.weight(1f),
                style = TextStyle(fontWeight = fontWeight, fontSize = 14.sp, lineHeight = 20.sp),
                text = label,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DrawerDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Dodano lekki padding pionowy dla oddechu
        color = Color.LightGray.copy(alpha = 0.5f) // Subtelny separator
    )
}