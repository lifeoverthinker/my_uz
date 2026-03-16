package com.example.my_uz_android.ui.screens.calendar.tasks

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.theme.extendedColors
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
    onTaskClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
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

    val sharedCode by viewModel.sharedCode.collectAsState()
    val isSharing by viewModel.isSharing.collectAsState()
    val shareError by viewModel.shareError.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showImportDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // --- NOWOŚĆ: Stan zakładek (0 = Aktywne, 1 = Zaliczone) ---
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(shareError, importStatus) {
        val msg = shareError ?: importStatus
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Filtrowanie zadań wg wybranej zakładki
    val filteredTasks = remember(tasks, selectedTab) {
        tasks.filter { task ->
            if (selectedTab == 0) !task.isCompleted else task.isCompleted
        }
    }

    val groupedByMonth = remember(filteredTasks) {
        filteredTasks.sortedBy { it.dueDate }.groupBy {
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
                onTasksClick = { scope.launch { drawerState.close() } },
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
                TopAppBar(
                    title = "Terminarz",
                    navigationIcon = R.drawable.ic_menu,
                    isNavigationIconFilled = true,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    actions = {
                        if (isSharing || isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.extendedColors.iconText,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // --- PRZYCISK DODAWANIA W PASKU ---
                        TopBarActionIcon(
                            icon = R.drawable.ic_plus,
                            onClick = onAddTaskClick
                        )

                        // --- MENU 3 KROPKI ---
                        Box {
                            TopBarActionIcon(
                                icon = R.drawable.ic_dots_vertical,
                                onClick = { showMenu = true }
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Udostępnij zadania") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shareMyTasks()
                                    },
                                    leadingIcon = {
                                        Icon(painterResource(R.drawable.ic_share), null, Modifier.size(20.dp))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Importuj zadania") },
                                    onClick = {
                                        showMenu = false
                                        showImportDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(painterResource(R.drawable.ic_import), null, Modifier.size(20.dp))
                                    }
                                )
                            }
                        }
                    },
                    // --- ZAKŁADKI W PASKU GÓRNYM ---
                    bottomContent = {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) },
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    height = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "Aktywne",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "Zaliczone",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.time_flies_rafiki),
                                contentDescription = null,
                                modifier = Modifier.size(220.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = if (selectedTab == 0) "Super! Brak aktywnych zadań." else "Jeszcze nic tu nie ma. Do dzieła!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groupedByMonth.forEach { (yearMonth, tasksInMonth) ->
                            stickyHeader { MonthHeaderSticky(yearMonth = yearMonth, backgroundColor = backgroundColor) }
                            val tasksByDay = tasksInMonth.groupBy {
                                Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            items(items = tasksByDay.toList(), key = { (date, _) -> date.toEpochDay() }) { (date, dailyTasks) ->
                                DayScheduleRow(
                                    date = date,
                                    tasks = dailyTasks,
                                    onTaskClick = onTaskClick,
                                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                                    onDeleteTask = { viewModel.deleteTask(it) }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

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

    if (showImportDialog) {
        var codeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Importuj zadania") },
            text = {
                Column {
                    Text("Wpisz 6-znakowy kod udostępnienia:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if(it.length <= 6) codeInput = it.uppercase() },
                        singleLine = true,
                        label = { Text("KOD") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importTasks(codeInput)
                        showImportDialog = false
                    },
                    enabled = codeInput.length == 6
                ) { Text("Pobierz") }
            },
            dismissButton = { TextButton(onClick = { showImportDialog = false }) { Text("Anuluj") } }
        )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScheduleRow(
    date: LocalDate,
    tasks: List<TaskEntity>,
    onTaskClick: (Int) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val dayOfMonth = date.dayOfMonth.toString()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = dayOfMonth, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { task ->
                key(task.id, task.isCompleted) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when (it) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onToggleTask(task)
                                    false
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    onDeleteTask(task)
                                    true
                                }
                                else -> false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color.Transparent
                                }, label = "SwipeColor"
                            )
                            val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                val scale by animateFloatAsState(
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                                    label = "IconScale"
                                )
                                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                    Icon(
                                        painter = painterResource(if (task.isCompleted) R.drawable.ic_x_close else R.drawable.ic_check_square_broken),
                                        contentDescription = null,
                                        modifier = Modifier.scale(scale).size(24.dp),
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_trash),
                                        contentDescription = null,
                                        modifier = Modifier.scale(scale).size(24.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        },
                        content = {
                            TaskCard(
                                task = task,
                                modifier = Modifier.fillMaxWidth(),
                                onTaskClick = { onTaskClick(task.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareCodeDialog(code: String, onDismiss: () -> Unit, onShareSystem: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Zadania udostępnione!") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                Icon(painter = painterResource(R.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wyślij kod")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zamknij") } }
    )
}