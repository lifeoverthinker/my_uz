package com.example.my_uz_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
    onTaskClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    MyUZTheme {
        val backgroundColor = MaterialTheme.extendedColors.homeHeaderBackground
        val onBackgroundColor = MaterialTheme.colorScheme.onBackground
        val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        val cardColorPurple = MaterialTheme.colorScheme.primaryContainer

        val contentCardColor = MaterialTheme.extendedColors.homeContentBackground

        // Główny kontener ekranu - brak scrolla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // --- 1. GÓRNY PASEK (Sztywny) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 4.dp),
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

            // --- 2. POWITANIE (Sztywne) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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

            // --- 3. DOLNA KARTA (STATYCZNA) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = contentCardColor
            ) {
                // ZMIANA: Zwykła Column zamiast LazyColumn.
                // Brak modifier.verticalScroll() oznacza brak przewijania pionowego.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp, bottom = 16.dp), // Padding zamiast contentPadding
                    verticalArrangement = Arrangement.Top // Elementy od góry
                ) {
                    // a) Najbliższe zajęcia
                    UpcomingClasses(
                        classes = uiState.upcomingClasses,
                        emptyMessage = uiState.classesMessage,
                        onClassClick = onClassClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // b) Sekcja Zadań
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            // LazyRow POZOSTAJE - to pozwala przesuwać karty na boki
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // c) Sekcja Wydarzeń
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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

                        // LazyRow POZOSTAJE
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                EventCard(
                                    title = "Juwenalia 2025",
                                    description = "Największa impreza roku!",
                                    onClick = { onEventClick(999) },
                                    modifier = Modifier.width(264.dp)
                                )
                            }
                            item {
                                EventCard(
                                    title = "Targi Pracy IT",
                                    description = "Znajdź staż marzeń",
                                    onClick = { onEventClick(999) },
                                    modifier = Modifier.width(264.dp)
                                )
                            }
                            item {
                                EventCard(
                                    title = "Wystawa Robotów",
                                    description = "Kampus A, Aula C",
                                    onClick = { onEventClick(999) },
                                    modifier = Modifier.width(264.dp)
                                )
                            }
                        }
                    }

                    // d) Stopka
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
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