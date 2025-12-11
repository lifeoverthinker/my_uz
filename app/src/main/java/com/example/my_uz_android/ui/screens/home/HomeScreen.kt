package com.example.my_uz_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.EventCard
import com.example.my_uz_android.ui.components.FabOption
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.home.components.UpcomingClasses
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (Int) -> Unit,
    onEventClick: (Int) -> Unit,
    onTaskClick: (Int) -> Unit,
    onAccountClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAddGradeClick: () -> Unit = {},
    onAddAbsenceClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }

    MyUZTheme {
        val topSectionBackground = MaterialTheme.extendedColors.homeTopBackground
        val iconTextColor = MaterialTheme.extendedColors.iconText
        val buttonBgColor = MaterialTheme.extendedColors.buttonBackground
        val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(topSectionBackground)
            ) {
                // ... (Sekcja Górna - Bez zmian) ...
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(topSectionBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 8.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.currentDate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W500,
                                color = iconTextColor,
                                fontSize = 14.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(buttonBgColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { /* Mapa */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_map),
                                        contentDescription = "Mapa",
                                        tint = iconTextColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(buttonBgColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = { /* Poczta */ }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_mail),
                                            contentDescription = "Poczta",
                                            tint = iconTextColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
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
                                color = iconTextColor,
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

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.isPlanSelected) {
                            item {
                                UpcomingClasses(
                                    classes = uiState.upcomingClasses,
                                    emptyMessage = uiState.classesMessage,
                                    dayLabel = uiState.classesDayLabel,
                                    onClassClick = onClassClick
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            item {
                                EmptyStateMessage(
                                    title = "Wybierz plan zajęć",
                                    message = "Przejdź do ustawień konta, aby wybrać grupę i podgrupę zajęć",
                                    iconRes = R.drawable.ic_calendar_check,
                                    actionText = "Przejdź do ustawień",
                                    onActionClick = onAccountClick,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

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
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Zadania",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.W500,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                if (uiState.upcomingTasks.isEmpty()) {
                                    Text(
                                        text = "Brak zadań",
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
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Wydarzenia",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.W500,
                                            color = MaterialTheme.colorScheme.onSurface
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

            // FAB z kolejnością zgodną z Twoim snippetem (wizualnie od góry do dołu)
            UniversalFab(
                isExpandable = true,
                isExpanded = isFabExpanded,
                onMainFabClick = { isFabExpanded = !isFabExpanded },
                options = listOf(
                    FabOption("Dodaj ocenę", R.drawable.ic_trophy) {
                        isFabExpanded = false
                        onAddGradeClick()
                    },
                    FabOption("Dodaj nieobecność", R.drawable.ic_calendar_minus) {
                        isFabExpanded = false
                        onAddAbsenceClick()
                    },
                    FabOption("Dodaj zadanie", R.drawable.ic_book_open) {
                        isFabExpanded = false
                        onAddTaskClick()
                    }
                )
            )
        }
    }
}