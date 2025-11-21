package com.example.my_uz_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.screens.home.components.UpcomingClasses
import com.example.my_uz_android.ui.theme.MyUZTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (Int) -> Unit,
    onEventClick: (Int) -> Unit // Dodano parametr
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundColor = Color(0xFFF7F2F9)
    val onBackgroundColor = Color(0xFF1D192B)
    val subTextColor = Color(0xFF363535)
    val cardColorPurple = Color(0xFFE8DEF8)
    val cardColorPink = Color(0xFFFFD8E4)
    val cardColorGreen = Color(0xFFDAF5D7)

    MyUZTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 1. GÓRNY PASEK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 58.dp)
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
                    // Mapa
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
                                tint = onBackgroundColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Mail
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
                                tint = onBackgroundColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .background(Color(0xFFB3261E), CircleShape)
                        )
                    }
                }
            }

            // 2. POWITANIE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 106.dp)
                    .padding(horizontal = 16.dp)
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

            // 3. BIAŁA KARTA
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 198.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
                ) {
                    // Zajęcia
                    item {
                        UpcomingClasses(
                            classes = uiState.upcomingClasses,
                            emptyMessage = uiState.classesMessage,
                            onClassClick = onClassClick
                        )
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    // Zadania
                    item {
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
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(uiState.upcomingTasks) { index, task ->
                                        val cardColor = if (index % 2 == 0) cardColorPink else cardColorPurple
                                        TaskCard(
                                            task = task,
                                            backgroundColor = cardColor,
                                            onTaskClick = {},
                                            onCheckedChange = {},
                                            modifier = Modifier.width(264.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    // Wydarzenia
                    item {
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

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(3) { index ->
                                    // Obsługa kliknięcia - testowe ID 999
                                    EventCardMock(
                                        index = index,
                                        backgroundColor = cardColorGreen,
                                        onClick = { onEventClick(999) }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    item {
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
                                    color = Color(0xFF787579),
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

@Composable
fun EventCardMock(index: Int, backgroundColor: Color, onClick: () -> Unit) {
    val titles = listOf("Juwenalia 2025", "Targi Pracy IT", "Wystawa Robotów")
    val descriptions = listOf("Największa impreza roku!", "Znajdź staż marzeń", "Kampus A, Aula C")

    Card(
        onClick = onClick, // Dodano obsługę kliknięcia
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.width(264.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titles[index % titles.size],
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    color = Color(0xFF222222)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = descriptions[index % descriptions.size],
                    fontSize = 12.sp,
                    color = Color(0xFF494949)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF7D5260), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.W500)
            }
        }
    }
}