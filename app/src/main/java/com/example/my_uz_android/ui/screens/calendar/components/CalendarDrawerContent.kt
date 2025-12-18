package com.example.my_uz_android.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.data.models.ScheduleType
import com.example.my_uz_android.ui.screens.calendar.ScheduleSource
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun CalendarDrawerContent(
    selectedSource: ScheduleSource,
    favorites: List<FavoriteEntity>,
    onSelect: (ScheduleSource) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // --- Nagłówek Menu ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "MyUZ Terminarz",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Mój Plan ---
        NavigationDrawerItem(
            icon = { Icon(painterResource(R.drawable.ic_home), null, Modifier.size(24.dp)) },
            label = { Text("Mój Plan", fontFamily = InterFontFamily) },
            selected = selectedSource is ScheduleSource.MyPlan,
            onClick = {
                onSelect(ScheduleSource.MyPlan)
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // --- Udostępnione (Placeholder) ---
        NavigationDrawerItem(
            icon = { Icon(painterResource(R.drawable.ic_users), null, Modifier.size(24.dp)) },
            label = { Text("Udostępnione (Wkrótce)", fontFamily = InterFontFamily) },
            selected = selectedSource is ScheduleSource.Shared,
            onClick = {
                // TODO: Logika udostępniania
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // --- Sekcja Ulubione ---
        Text(
            text = "ULUBIONE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )

        if (favorites.isEmpty()) {
            Text(
                text = "Brak zapisanych planów",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = InterFontFamily),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp)
            )
        } else {
            favorites.forEach { fav ->
                val iconRes = if (fav.type == ScheduleType.TEACHER) R.drawable.ic_user else R.drawable.ic_users
                NavigationDrawerItem(
                    icon = { Icon(painterResource(iconRes), null, Modifier.size(24.dp)) },
                    label = { Text(fav.name, fontFamily = InterFontFamily) },
                    selected = (selectedSource as? ScheduleSource.Favorite)?.entity?.id == fav.id,
                    onClick = {
                        onSelect(ScheduleSource.Favorite(fav))
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}