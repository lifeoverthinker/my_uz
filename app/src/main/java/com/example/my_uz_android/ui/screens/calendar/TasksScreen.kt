package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.theme.InterFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    onAddTaskClick: () -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasks by viewModel.uiState.collectAsState()

    // Grupowanie po miesiącach (jak Google Calendar)
    val groupedTasks = remember(tasks) {
        tasks
            .sortedBy { it.dueDate }
            .groupBy {
                val date = Instant.ofEpochMilli(it.dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.withDayOfMonth(1) // Grupuj po pierwszym dniu miesiąca
            }
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Terminarz",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Dodaj zadanie",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Brak zadań",
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            // Lista zadań z sticky headers (miesiące)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedTasks.forEach { (monthDate, tasksInMonth) ->
                    // Sticky header dla miesiąca
                    stickyHeader {
                        MonthHeader(
                            date = monthDate,
                            backgroundColor = backgroundColor
                        )
                    }

                    // Zadania w tym miesiącu
                    items(
                        items = tasksInMonth,
                        key = { it.id }
                    ) { task ->
                        val date = Instant.ofEpochMilli(task.dueDate)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        TaskRowItem(
                            date = date,
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                }

                // Bottom spacer dla FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ========== MONTH HEADER (STICKY) ==========
@Composable
fun MonthHeader(
    date: LocalDate,
    backgroundColor: Color
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl"))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = date.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ========== TASK ROW (Data lewa + Card prawa) ==========
@Composable
fun TaskRowItem(
    date: LocalDate,
    task: TaskEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEWA STRONA: Data (dzień tygodnia + numer dnia)
        Column(
            modifier = Modifier
                .width(50.dp)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dzień tygodnia (np. "PN")
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEE", Locale("pl"))).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            // Numer dnia (np. "5")
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // PRAWA STRONA: Task Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
        ) {
            TaskCard(
                task = task,
                onTaskClick = onClick
            )
        }
    }
}

// ========== COMPOSE PREVIEW ==========
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    com.example.my_uz_android.ui.theme.MyUZTheme {
        TasksScreen(
            onAddTaskClick = {},
            onTaskClick = {}
        )
    }
}
