package com.example.my_uz_android.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun CalendarDrawerContent(
    favorites: List<FavoriteEntity>,
    selectedResourceId: String?,
    currentScreen: String,
    onMyPlanClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFavoriteClick: (FavoriteEntity) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(Color(0xffffffff), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp)) {
            Text(text = "Menu", style = TextStyle(fontWeight = FontWeight(500), fontSize = 14.sp), color = Color(0xff49454f))
        }

        DrawerNavItem(label = "Kalendarz", iconRes = R.drawable.ic_calendar, isSelected = selectedResourceId == null, onClick = onMyPlanClick)
        DrawerNavItem(label = "Terminarz", iconRes = R.drawable.ic_calendar_check, isSelected = currentScreen == "tasks", onClick = onTasksClick)

        val groups = favorites.filter { it.type == "group" }
        if (groups.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp)) {
                Text(text = "Grupy", style = TextStyle(fontWeight = FontWeight(500), fontSize = 14.sp), color = Color(0xff49454f))
            }
            groups.forEach { favorite ->
                DrawerNavItem(label = favorite.name, iconRes = R.drawable.ic_users, isSelected = selectedResourceId == favorite.resourceId, onClick = { onFavoriteClick(favorite) })
            }
        }

        val teachers = favorites.filter { it.type == "teacher" }
        if (teachers.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp)) {
                Text(text = "Nauczyciele", style = TextStyle(fontWeight = FontWeight(500), fontSize = 14.sp), color = Color(0xff49454f))
            }
            teachers.forEach { favorite ->
                DrawerNavItem(label = favorite.name, iconRes = R.drawable.ic_user, isSelected = selectedResourceId == favorite.resourceId, onClick = { onFavoriteClick(favorite) })
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xffe0e0e0))
        DrawerNavItem(label = "Szukaj planu", iconRes = R.drawable.ic_search, isSelected = false, onClick = onSearchClick)
        DrawerNavItem(label = "Ustawienia", iconRes = R.drawable.ic_settings, isSelected = false, onClick = onSettingsClick)
    }
}

@Composable
fun DrawerNavItem(label: String, iconRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xffe8def8) else Color.Transparent
    val labelColor = if (isSelected) Color(0xff4a4459) else Color(0xff49454f)
    val weight = if (isSelected) FontWeight(600) else FontWeight(500)

    Box(modifier = Modifier.height(56.dp).fillMaxWidth().clip(RoundedCornerShape(100.dp)).background(bgColor).clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(iconRes), null, Modifier.size(24.dp), tint = labelColor)
            Text(text = label, modifier = Modifier.weight(1f), style = TextStyle(fontWeight = weight, fontSize = 14.sp), color = labelColor)
        }
    }
}