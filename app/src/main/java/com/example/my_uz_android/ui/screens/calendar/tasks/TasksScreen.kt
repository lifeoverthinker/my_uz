package com.example.my_uz_android.ui.screens.calendar.tasks

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import java.time.format.TextStyle as JavaTextStyle
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
    val clipboardManager = LocalClipboardManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showImportDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    // --- STANY TRYBU WYBORU (Selection Mode) ---
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedTasks by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(shareError, importStatus) {
        val msg = shareError ?: importStatus
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

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
                if (isSelectionMode) {
                    // Specjalny TopBar dla trybu wyboru
                    TopAppBar(
                        title = "Wybierz zadania",
                        navigationIcon = R.drawable.ic_x_close,
                        isNavigationIconFilled = false,
                        onNavigationClick = {
                            isSelectionMode = false
                            selectedTasks = emptySet()
                        }
                    )
                } else {
                    // Standardowy TopBar
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

                            TopBarActionIcon(
                                icon = R.drawable.ic_plus,
                                onClick = onAddTaskClick
                            )

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
                                            viewModel.shareMyTasks() // Domyślnie generuje kod dla wszystkich
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
                                    text = { Text("Aktywne", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Zaliczone", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        com.example.my_uz_android.ui.components.EmptyStateMessage(
                            title = "Brak zadań ✅",
                            subtitle = "Wszystko gotowe",
                            message = "Lista Twoich zadań do zrobienia jest obecnie pusta",
                            iconRes = R.drawable.to_do_list_rafiki // Podmień na nową grafikę z zadań
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groupedByMonth.forEach { (yearMonth, tasksInMonth) ->
                            stickyHeader {
                                MonthHeaderSticky(
                                    yearMonth = yearMonth,
                                    backgroundColor = backgroundColor,
                                    isSelectionMode = isSelectionMode,
                                    selectionCount = selectedTasks.size,
                                    onConfirmSelection = {
                                        viewModel.shareMyTasks(selectedTasks)
                                        isSelectionMode = false
                                        selectedTasks = emptySet()
                                    }
                                )
                            }

                            val tasksByDay = tasksInMonth.groupBy {
                                Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            }

                            items(items = tasksByDay.toList(), key = { (date, _) -> date.toEpochDay() }) { (date, dailyTasks) ->
                                DayScheduleRow(
                                    date = date,
                                    tasks = dailyTasks,
                                    isSelectionMode = isSelectionMode,
                                    selectedTasks = selectedTasks,
                                    onTaskClick = { taskId ->
                                        if (isSelectionMode) {
                                            val newSet = selectedTasks.toMutableSet()
                                            if (newSet.contains(taskId)) newSet.remove(taskId) else newSet.add(taskId)
                                            selectedTasks = newSet
                                        } else {
                                            onTaskClick(taskId)
                                        }
                                    },
                                    onToggleSelect = { taskId ->
                                        val newSet = selectedTasks.toMutableSet()
                                        if (newSet.contains(taskId)) newSet.remove(taskId) else newSet.add(taskId)
                                        selectedTasks = newSet
                                    },
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

    // --- DIALOG UDOSTĘPNIANIA (Zgodny z makietą) ---
    if (sharedCode != null && !isSelectionMode) {
        ShareHubDialog(
            code = sharedCode!!,
            onCopyCode = {
                clipboardManager.setText(AnnotatedString(sharedCode!!))
                Toast.makeText(context, "Skopiowano kod do schowka", Toast.LENGTH_SHORT).show()
                viewModel.clearSharedCode()
            },
            onSelectAll = {
                // Jeśli użytkownik wybierze wszystko, po prostu generujemy nowy kod dla całości
                viewModel.shareMyTasks()
            },
            onSelectSpecific = {
                viewModel.clearSharedCode()
                isSelectionMode = true
                selectedTasks = emptySet()
            },
            onImportTasks = {
                viewModel.clearSharedCode()
                showImportDialog = true
            },
            onDismiss = { viewModel.clearSharedCode() }
        )
    }

    // --- DIALOG IMPORTU (Oczyszczony) ---
    if (showImportDialog) {
        var codeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Importuj zadania", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wpisz 6-znakowy kod dostępu", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if(it.length <= 6) codeInput = it.uppercase() },
                        singleLine = true,
                        placeholder = { Text("ABC123") },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
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
                    enabled = codeInput.length == 6,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Pobierz zadania") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Anuluj") }
            }
        )
    }
}

// --- KOMPONENTY POMOCNICZE ---

@Composable
fun ShareHubDialog(
    code: String,
    onCopyCode: () -> Unit,
    onSelectAll: () -> Unit,
    onSelectSpecific: () -> Unit,
    onImportTasks: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text(
                    text = "Udostępnij zadania",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pudełko z kodem
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        ),
                        modifier = Modifier.padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opcje wyboru
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text("Zaznacz wszystko")
                    }
                    TextButton(onClick = onSelectSpecific) {
                        Text("Wybierz zadania")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Główny przycisk kopiowania
                Button(
                    onClick = onCopyCode,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Skopiuj kod")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Opcja importu
                TextButton(onClick = onImportTasks) {
                    Icon(painterResource(R.drawable.ic_import), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importuj zadania")
                }
            }
        }
    }
}

@Composable
fun MonthHeaderSticky(
    yearMonth: YearMonth,
    backgroundColor: Color,
    isSelectionMode: Boolean = false,
    selectionCount: Int = 0,
    onConfirmSelection: () -> Unit = {}
) {
    val monthName = yearMonth.month.getDisplayName(JavaTextStyle.FULL, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val year = yearMonth.year

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isSelectionMode) {
            Text(
                text = "Zatwierdź wybrane: $selectionCount",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selectionCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(enabled = selectionCount > 0) { onConfirmSelection() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScheduleRow(
    date: LocalDate,
    tasks: List<TaskEntity>,
    isSelectionMode: Boolean,
    selectedTasks: Set<Int>,
    onTaskClick: (Int) -> Unit,
    onToggleSelect: (Int) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val dayOfMonth = date.dayOfMonth.toString()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Kolumna z datą
        Column(modifier = Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = dayOfMonth, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Kolumna z zadaniami
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { task ->
                key(task.id, task.isCompleted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox do trybu wyboru
                        if (isSelectionMode) {
                            Checkbox(
                                checked = selectedTasks.contains(task.id),
                                onCheckedChange = { onToggleSelect(task.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Właściwa karta zadania
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (isSelectionMode) return@rememberSwipeToDismissBoxState false // Zablokuj swipe w trybie wyboru

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

                        Box(modifier = Modifier.weight(1f)) {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    if (!isSelectionMode) {
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
    }
}