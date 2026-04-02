package com.example.my_uz_android.ui.screens.calendar.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.FavoriteEntity
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.SearchTopAppBar
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme

@Composable
fun ScheduleSearchScreen(
    navController: NavController,
    searchViewModel: ScheduleSearchViewModel,
    calendarViewModel: CalendarViewModel
) {
    val uiState by searchViewModel.uiState.collectAsStateWithLifecycle()

    ScheduleSearchContent(
        uiState = uiState,
        onQueryChange = searchViewModel::onQueryChange,
        onBackClick = { navController.popBackStack() },
        onItemClick = { item ->
            calendarViewModel.selectFavoritePlan(
                FavoriteEntity(
                    name = item.name,
                    type = item.type,
                    resourceId = item.name
                )
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
                        subtitle = "Nie znaleziono pasujących pozycji",
                        message = "Spróbuj wpisać kod grupy, fragment nazwy kierunku lub nazwisko prowadzącego.",
                        iconRes = R.drawable.paper_map_rafiki,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (groups.isNotEmpty()) {
                        item { SearchSectionHeader(title = "Grupy") }
                        items(
                            items = groups,
                            key = { "group_${it.name}" }
                        ) { item ->
                            SearchListItem(
                                item = item,
                                onClick = { onItemClick(item) },
                                onFavoriteClick = { onFavoriteClick(item) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }
                    }

                    if (teachers.isNotEmpty()) {
                        item { SearchSectionHeader(title = "Nauczyciele") }
                        items(
                            items = teachers,
                            key = { "teacher_${it.name}" }
                        ) { item ->
                            SearchListItem(
                                item = item,
                                onClick = { onItemClick(item) },
                                onFavoriteClick = { onFavoriteClick(item) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        )
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
            .padding(vertical = 14.dp, horizontal = 24.dp),
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (item.type == "group") "Grupa studencka" else "Nauczyciel",
                style = MaterialTheme.typography.bodySmall,
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