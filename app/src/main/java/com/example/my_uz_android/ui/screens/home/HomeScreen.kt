package com.example.my_uz_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.EventCard
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.screens.home.components.UpcomingClasses
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (Int) -> Unit,
    onEventClick: (Int) -> Unit,
    onTaskClick: (Int) -> Unit,
    onAccountClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    MyUZTheme {
        val backgroundColor = MaterialTheme.colorScheme.background
        val onBackgroundColor = MaterialTheme.colorScheme.onBackground
        val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        val cardColorPurple = MaterialTheme.colorScheme.primaryContainer
        val contentCardColor = MaterialTheme.extendedColors.homeContentBackground

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // --- 1. GÓRNY PASEK I POWITANIE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                // Usunięto padding dolny rodzica, teraz kontroluje go wewnętrzna kolumna
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Padding systemowy
                        .padding(top = 8.dp) // Odstęp od góry (pod status barem)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.currentDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            color = onBackgroundColor,
                            fontSize = 14.sp
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(cardColorPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { /* Mapa */ }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_map),
                                    contentDescription = "Mapa",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(cardColorPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { /* Poczta */ }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mail),
                                    contentDescription = "Poczta",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-10).dp, y = 10.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                        }
                    }
                }

                // Sekcja Powitania i Wydziału
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) // ZMIANA: Padding 16dp z każdej strony (góra, dół, lewo, prawo)
                ) {
                    Text(
                        text = uiState.greeting,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.W600,
                            color = onBackgroundColor,
                            lineHeight = 36.sp
                        )
                    )
                    Text(
                        text = uiState.departmentInfo,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = subTextColor,
                            fontWeight = FontWeight.W400,
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            // --- 2. DOLNA KARTA (Z LAZY COLUMN) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = contentCardColor
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // a) Najbliższe zajęcia
                    item {
                        UpcomingClasses(
                            classes = uiState.upcomingClasses,
                            emptyMessage = uiState.classesMessage,
                            onClassClick = onClassClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // b) Sekcja Zadań
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_book_open),
                                    contentDescription = null,
                                    tint = onBackgroundColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = uiState.tasksMessage ?: "Zadania",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W500,
                                        color = onBackgroundColor
                                    )
                                )
                            }

                            if (uiState.upcomingTasks.isEmpty()) {
                                Text(
                                    text = "Brak zadań do wyświetlenia",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(uiState.upcomingTasks) { _, task ->
                                        TaskCard(
                                            task = task,
                                            onTaskClick = { onTaskClick(task.id) },
                                            modifier = Modifier.width(264.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // c) Sekcja Wydarzeń
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_marker_pin),
                                    contentDescription = null,
                                    tint = onBackgroundColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Wydarzenia",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W500,
                                        color = onBackgroundColor
                                    )
                                )
                            }

                            if (uiState.upcomingEvents.isEmpty()) {
                                Text(
                                    text = "Brak wydarzeń",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.upcomingEvents) { event ->
                                        EventCard(
                                            event = event,
                                            onClick = { onEventClick(event.id) },
                                            modifier = Modifier.width(264.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // d) Stopka
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "MyUZ 2025",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.W500
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}