package com.example.my_uz_android.ui.screens.calendar.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel

@Composable
fun ScheduleSearchScreen(
    navController: NavController,
    searchViewModel: ScheduleSearchViewModel,
    calendarViewModel: CalendarViewModel
) {
    val uiState by searchViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Szukaj planu",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { searchViewModel.onQueryChange(it) },
                label = { Text("Wpisz grupę lub nazwisko") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, Modifier.size(24.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.searchResults) { item ->
                        SearchResultItemCard(
                            item = item,
                            onClick = {
                                // 1. Załaduj dane do ViewModelu
                                calendarViewModel.selectPreviewPlan(item.name, item.type)
                                // 2. Zamiast wracać do kalendarza, idź do podglądu
                                navController.navigate("schedule_preview")
                            },
                            onFavoriteClick = { searchViewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItemCard(item: SearchResultItem, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(if (item.type == "group") "Grupa" else "Nauczyciel", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onFavoriteClick) {
            if (item.isFavorite) Icon(Icons.Filled.Favorite, null, Modifier.size(24.dp), tint = Color(0xFF6750A4))
            else Icon(painterResource(R.drawable.ic_heart), null, Modifier.size(24.dp), tint = Color(0xFF49454F))
        }
    }
}