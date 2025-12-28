package com.example.my_uz_android.ui.screens.calendar.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.SearchTopAppBar
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSearchScreen(
    navController: NavController,
    searchViewModel: ScheduleSearchViewModel,
    calendarViewModel: CalendarViewModel
) {
    val uiState by searchViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SearchTopAppBar(
                query = uiState.searchQuery,
                onQueryChange = { newValue -> searchViewModel.onQueryChange(newValue) },
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.searchResults) { item ->
                    SearchListItem(
                        item = item,
                        onClick = {
                            calendarViewModel.selectPreviewPlan(item.name, item.type)
                            navController.navigate("schedule_preview")
                        },
                        onFavoriteClick = { searchViewModel.toggleFavorite(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchListItem(
    item: SearchResultItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (item.type == "group") R.drawable.ic_users else R.drawable.ic_user
            ),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (item.type == "group") "Grupa studencka" else "Nauczyciel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                painter = painterResource(
                    id = if (item.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart
                ),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                // Po polubieniu używamy koloru primary (fioletowy z Twojego theme)
                tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}