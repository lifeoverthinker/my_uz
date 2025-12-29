package com.example.my_uz_android.ui.screens.calendar.tasks

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
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

    // Stany udostępniania
    val sharedCode by viewModel.sharedCode.collectAsState()
    val isSharing by viewModel.isSharing.collectAsState()
    val shareError by viewModel.shareError.collectAsState()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Obsługa błędów udostępniania
    LaunchedEffect(shareError) {
        shareError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

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
                currentScreen = "tasks",
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
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                // [NAPRAWA] Użyto R.drawable.ic_menu (zamiast ic_menu_burger) i dodano onShareClick
                CalendarTopAppBar(
                    title = "Terminarz",
                    navigationIcon = R.drawable.ic_menu,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    onShareClick = if (tasks.isNotEmpty()) {
                        { viewModel.shareMyTasks() }
                    } else null,
                    isShareLoading = isSharing
                )
            },
            floatingActionButton = {
                UniversalFab(
                    isExpandable = false,
                    isExpanded = false,
                    onMainFabClick = onAddTaskClick,
                    options = emptyList()
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
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
        }
    }

    // --- DIALOG Z KODEM ---
    if (sharedCode != null) {
        ShareCodeDialog(
            code = sharedCode!!,
            onDismiss = { viewModel.clearSharedCode() },
            onShareSystem = { code ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Hej! Pobrałem listę zadań w aplikacji MyUZ. Mój kod to: $code")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Udostępnij kod zadań")
                context.startActivity(shareIntent)
            }
        )
    }
}

@Composable
fun ShareCodeDialog(
    code: String,
    onDismiss: () -> Unit,
    onShareSystem: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zadania udostępnione!") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Twój kod dostępu:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Podaj ten kod innej osobie, aby mogła pobrać Twoją listę zadań.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = { onShareSystem(code) }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wyślij kod")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}

@Composable
fun MonthHeaderSticky(yearMonth: YearMonth, backgroundColor: Color) {
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val year = yearMonth.year

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayScheduleRow(
    date: LocalDate,
    tasks: List<TaskEntity>,
    onTaskClick: (TaskEntity) -> Unit
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val dayOfMonth = date.dayOfMonth.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dayOfMonth,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    modifier = Modifier.fillMaxWidth(),
                    onTaskClick = { onTaskClick(task) }
                )
            }
        }
    }
}