package com.example.my_uz_android.ui.screens.calendar.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.SearchTopAppBar
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.DashboardEmptyCard

@Composable
fun ScheduleSearchScreen(
    navController: NavController,
    searchViewModel: ScheduleSearchViewModel,
    calendarViewModel: CalendarViewModel
) {
    val uiState by searchViewModel.uiState.collectAsState()

    ScheduleSearchContent(
        uiState = uiState,
        onQueryChange = searchViewModel::onQueryChange,
        onBackClick = { navController.popBackStack() },
        onItemClick = { item ->
            calendarViewModel.selectFavoritePlan(
                FavoriteEntity(name = item.name, type = item.type, resourceId = item.name)
            )
            navController.navigate("schedule_preview")
        },
        onFavoriteClick = { searchViewModel.toggleFavorite(it) }
    )
}

@Composable
fun ScheduleSearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (SearchResultItem) -> Unit,
    onFavoriteClick: (SearchResultItem) -> Unit
) {
    Scaffold(
        topBar = {
            SearchTopAppBar(
                query = uiState.searchQuery,
                onQueryChange = onQueryChange,
                onBackClick = onBackClick
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

            val groups = uiState.searchResults.filter { it.type == "group" }
            val teachers = uiState.searchResults.filter { it.type == "teacher" }

            if (!uiState.isLoading && uiState.searchQuery.isNotEmpty() && uiState.searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateMessage(
                        title = "Brak wyników",
                        subtitle = "Nie znaleźliśmy tego, czego szukasz",
                        message = "Spróbuj wpisać inną nazwę grupy lub nazwisko prowadzącego. Upewnij się, że nie ma literówek.",
                        iconRes = R.drawable.paper_map_rafiki, // Pasuje do szukania drogi/planu
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (groups.isNotEmpty()) {
                        item {
                            Text(
                                text = "Grupy",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        items(groups) { item ->
                            SearchListItem(item, { onItemClick(item) }, { onFavoriteClick(item) })
                        }
                    }

                    if (teachers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Nauczyciele",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        items(teachers) { item ->
                            SearchListItem(item, { onItemClick(item) }, { onFavoriteClick(item) })
                        }
                    }
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
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (item.type == "group") "Grupa studencka" else "Nauczyciel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onFavoriteClick) {
            AnimatedContent(
                targetState = item.isFavorite,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(tween(110)))
                },
                label = "FavoriteAnimation"
            ) { isFavorite ->
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleSearchScreenResultsPreview() {
    MyUZTheme {
        ScheduleSearchContent(
            uiState = SearchUiState(
                searchQuery = "Kowalski",
                searchResults = listOf(
                    SearchResultItem("311-EA-ZI", "group", false),
                    SearchResultItem("Jan Kowalski", "teacher", true, "j.kowalski@uz.zgora.pl", "Instytut Informatyki"),
                    SearchResultItem("Adam Kowal", "teacher", false, null, "Wydział Mechaniczny")
                )
            ),
            onQueryChange = {},
            onBackClick = {},
            onItemClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleSearchScreenEmptyPreview() {
    MyUZTheme {
        ScheduleSearchContent(
            uiState = SearchUiState(
                searchQuery = "Zxcvbnm",
                searchResults = emptyList()
            ),
            onQueryChange = {},
            onBackClick = {},
            onItemClick = {},
            onFavoriteClick = {}
        )
    }
}