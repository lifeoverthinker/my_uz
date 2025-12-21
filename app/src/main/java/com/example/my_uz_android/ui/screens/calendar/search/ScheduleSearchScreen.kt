package com.example.my_uz_android.ui.screens.calendar.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar

@Composable
fun ScheduleSearchScreen(
    onNavigateBack: () -> Unit, // ✅ Teraz nazwa pasuje do AppNavigation
    viewModel: ScheduleSearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Szukaj planu",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                label = { Text("Wpisz kod grupy lub nazwisko") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn {
                items(uiState.searchResults) { result ->
                    ListItem(
                        headlineContent = { Text(result.name) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.toggleFavorite(result) }) {
                                Icon(
                                    painter = painterResource(if (result.isFavorite) R.drawable.ic_heart else R.drawable.ic_heart), // Użyj odpowiednich ikon wypełnionych/pustych jeśli masz
                                    contentDescription = null,
                                    tint = if (result.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}