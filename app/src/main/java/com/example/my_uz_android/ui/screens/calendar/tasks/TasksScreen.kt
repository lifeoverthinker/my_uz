package com.example.my_uz_android.ui.screens.calendar.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.theme.InterFontFamily
import kotlinx.coroutines.launch
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
    onHomeClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onIndexClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
    calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasks by viewModel.tasksStream.collectAsState(initial = emptyList())
    val calendarUiState by calendarViewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    val backgroundColor = MaterialTheme.colorScheme.surface

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CalendarDrawerContent(
                favorites = calendarUiState.favorites,
                selectedResourceId = calendarUiState.selectedResourceId,
                onMyPlanClick = {
                    calendarViewModel.selectMyPlan()
                    scope.launch { drawerState.close() }
                    onCalendarClick()
                },
                onTasksClick = {
                    scope.launch { drawerState.close() }
                },
                onFavoriteClick = { fav ->
                    calendarViewModel.selectFavoritePlan(fav)
                    scope.launch { drawerState.close() }
                    onCalendarClick()
                },
                onSearchClick = {
                    scope.launch { drawerState.close() }
                    onSearchClick()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onAccountClick()
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    CalendarTopAppBar(
                        title = "Terminarz",
                        onNavigationClick = { scope.launch { drawerState.open() } },
                        onSearchClick = onSearchClick,
                        onAddClick = onAddTaskClick,
                        onTitleClick = null
                    )
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
                                Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            items(
                                items = tasksByDay.toList(),
                                key = { (date, _) -> date.toEpochDay() }
                            ) { (date, dailyTasks) ->
                                DayScheduleRow(date = date, tasks = dailyTasks, onTaskClick = onTaskClick)
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            UniversalFab(
                isExpandable = false,
                isExpanded = false,
                onMainFabClick = onAddTaskClick,
                options = emptyList()
            )
        }
    }
}

@Composable
fun MonthHeaderSticky(yearMonth: YearMonth, backgroundColor: Color) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl")) }
    val title = yearMonth.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    Box(modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface))
    }
}

@Composable
fun DayScheduleRow(date: LocalDate, tasks: List<TaskEntity>, onTaskClick: (TaskEntity) -> Unit) {
    val isToday = date == LocalDate.now()
    val dayOfWeekShort = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl")).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }.replace(".", "")
    val dayOfMonth = date.dayOfMonth.toString()

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.width(50.dp).padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = dayOfWeekShort, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent), contentAlignment = Alignment.Center) {
                Text(text = dayOfMonth, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { task ->
                TaskCard(task = task, onTaskClick = { onTaskClick(task) }, showDayMarker = true, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}