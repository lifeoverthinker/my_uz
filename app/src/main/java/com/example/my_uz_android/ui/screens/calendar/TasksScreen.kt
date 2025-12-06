package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.MyUZTheme
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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

    val groupedByMonth = remember(tasks) {
        tasks
            .sortedBy { it.dueDate }
            .groupBy {
                Instant.ofEpochMilli(it.dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .let { date -> YearMonth.from(date) }
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
                        text = stringResource(R.string.tasks_screen_title),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.time_rafiki),
                        contentDescription = null,
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.tasks_empty_title),
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                groupedByMonth.forEach { (yearMonth, tasksInMonth) ->
                    stickyHeader {
                        MonthHeaderSticky(yearMonth = yearMonth, backgroundColor = backgroundColor)
                    }

                    val tasksByDay = tasksInMonth.groupBy {
                        Instant.ofEpochMilli(it.dueDate)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }

                    items(
                        items = tasksByDay.toList(),
                        key = { (date, _) -> date.toEpochDay() }
                    ) { (date, dailyTasks) ->
                        DayScheduleRow(
                            date = date,
                            tasks = dailyTasks,
                            onTaskClick = onTaskClick
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun MonthHeaderSticky(
    yearMonth: YearMonth,
    backgroundColor: Color
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl")) }
    val title = yearMonth.format(formatter).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun DayScheduleRow(
    date: LocalDate,
    tasks: List<TaskEntity>,
    onTaskClick: (TaskEntity) -> Unit
) {
    val isToday = date == LocalDate.now()

    val dayOfWeekShort = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
        .replace(".", "")

    val dayOfMonth = date.dayOfMonth.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .width(50.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeekShort,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfMonth,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onTaskClick = { onTaskClick(task) },
                    showDayMarker = true
                )
            }
        }
    }
}

@Preview(name = "Tasks Screen - Light", showBackground = true)
@Composable
private fun PreviewTasksScreenLight() {
    MyUZTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                MonthHeaderSticky(YearMonth.now(), MaterialTheme.colorScheme.surfaceContainerLowest)
                Text(
                    "Tu pojawią się Twoje zadania",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Tasks Screen - Dark", showBackground = true)
@Composable
private fun PreviewTasksScreenDark() {
    MyUZTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                MonthHeaderSticky(YearMonth.now(), MaterialTheme.colorScheme.surfaceContainerLowest)
                Text(
                    "Tu pojawią się Twoje zadania",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}