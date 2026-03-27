package com.example.my_uz_android.ui.screens.calendar.tasks

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import kotlinx.coroutines.launch
import java.time.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    onNavigateBack: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCalendarClick: () -> Unit,
    onAccountClick: () -> Unit,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
    calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasks by viewModel.tasksStream.collectAsState(initial = emptyList())
    val calendarUiState by calendarViewModel.uiState.collectAsState()
    val sharedCode by viewModel.sharedCode.collectAsState()
    val isSharing by viewModel.isSharing.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showImportDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedTasks by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(viewModel.shareError, importStatus) {
        viewModel.shareError.value?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        importStatus?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        viewModel.clearError()
    }

    val groupedTasks = remember(tasks, selectedTab) {
        tasks.filter { if (selectedTab == 0) !it.isCompleted else it.isCompleted }
            .sortedBy { it.dueDate }
            .groupBy {
                YearMonth.from(Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate())
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CalendarDrawerContent(
                favorites = calendarUiState.favorites,
                selectedResourceId = calendarUiState.selectedResourceId,
                currentScreen = "tasks",
                onMyPlanClick = {
                    calendarViewModel.selectMyPlan()
                    scope.launch { drawerState.close() }; onCalendarClick()
                },
                onTasksClick = { scope.launch { drawerState.close() } },
                onFavoriteClick = { fav ->
                    calendarViewModel.selectFavoritePlan(fav)
                    scope.launch { drawerState.close() }; onCalendarClick()
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TasksTopBar(
                    isSelectionMode = isSelectionMode,
                    selectedTab = selectedTab,
                    isLoading = isSharing || isImporting,
                    onSelectionClose = { isSelectionMode = false; selectedTasks = emptySet() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onAddClick = onAddTaskClick,
                    onShareAll = { viewModel.shareMyTasks() },
                    onImportClick = { showImportDialog = true },
                    onTabChange = { selectedTab = it }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (groupedTasks.isEmpty()) {
                    EmptyStateMessage(
                        title = if (selectedTab == 0) "Brak aktywnych zadań" else "Brak zaliczonych zadań",
                        subtitle = "Twoja lista jest pusta",
                        message = "Dodaj nowe zadanie przyciskiem +",
                        iconRes = R.drawable.calendar_rafiki
                    )
                } else {
                    TasksList(
                        groupedTasks = groupedTasks,
                        isSelectionMode = isSelectionMode,
                        selectedTasks = selectedTasks,
                        onTaskClick = { id ->
                            if (isSelectionMode) {
                                selectedTasks = if (selectedTasks.contains(id)) selectedTasks - id else selectedTasks + id
                            } else onTaskClick(id)
                        },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onConfirmSelection = {
                            viewModel.shareMyTasks(selectedTasks)
                            isSelectionMode = false; selectedTasks = emptySet()
                        }
                    )
                }
            }
        }
    }

    if (sharedCode != null && !isSelectionMode) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSharedCode() },
            title = { Text("Zadania gotowe!") },
            text = { Text("Kod do udostępnienia: $sharedCode") },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(sharedCode!!))
                    viewModel.clearSharedCode()
                }) { Text("Kopiuj kod") }
            }
        )
    }

    if (showImportDialog) {
        ImportDialog(
            onImport = { viewModel.importTasks(it); showImportDialog = false },
            onDismiss = { showImportDialog = false }
        )
    }
}

// --- KOMPONENTY WIZUALNE ---

@Composable
fun TasksTopBar(
    isSelectionMode: Boolean,
    selectedTab: Int,
    isLoading: Boolean,
    onSelectionClose: () -> Unit,
    onOpenDrawer: () -> Unit,
    onAddClick: () -> Unit,
    onShareAll: () -> Unit,
    onImportClick: () -> Unit,
    onTabChange: (Int) -> Unit
) {
    TopAppBar(
        title = if (isSelectionMode) "Wybierz zadania" else "Terminarz",
        navigationIcon = if (isSelectionMode) R.drawable.ic_close else R.drawable.ic_menu,
        onNavigationClick = if (isSelectionMode) onSelectionClose else onOpenDrawer,
        isNavigationIconFilled = !isSelectionMode, // KÓŁKO TYLKO GDY NIE MA TRYBU ZAZNACZANIA
        actions = {
            if (!isSelectionMode) {
                TopBarActionIcon(icon = R.drawable.ic_import, onClick = onImportClick, isLoading = isLoading, isFilled = true)
                TopBarActionIcon(icon = R.drawable.ic_share, onClick = onShareAll, isLoading = isLoading, isFilled = true)
            }
            TopBarActionIcon(icon = R.drawable.ic_plus, onClick = onAddClick, isFilled = true)
        },
        bottomContent = {
            // TABS WRACAJĄ DO GÓRNEGO PASKA, MAJĄ PADDINGI I BRAK PODKREŚLENIA
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { onTabChange(0) }, text = { Text("Aktywne") })
                    Tab(selected = selectedTab == 1, onClick = { onTabChange(1) }, text = { Text("Zaliczone") })
                }
            }
        }
    )
}

@Composable
fun TasksList(
    groupedTasks: Map<YearMonth, List<TaskEntity>>,
    isSelectionMode: Boolean,
    selectedTasks: Set<Int>,
    onTaskClick: (Int) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onConfirmSelection: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            groupedTasks.forEach { (month, tasks) ->
                item {
                    Text(
                        text = month.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("pl")).uppercase(),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    val isSelected = isSelectionMode && selectedTasks.contains(task.id)

                    // Opatulamy Twoją kartę w Boxa, który zarządza klikaniem i tłem
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp) // Odstępy od brzegów
                            .clip(RoundedCornerShape(16.dp))
                            // W trybie wyboru podświetlamy tło karty:
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onTaskClick(task.id) }
                            .padding(if (isSelected) 4.dp else 0.dp)
                    ) {
                        // Wywołanie BEZ brakujących parametrów (TaskCard ich nie potrzebuje)
                        TaskCard(task = task)
                    }
                }
            }
        }

        // Pływający przycisk udostępniania
        if (isSelectionMode && selectedTasks.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = onConfirmSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(painterResource(R.drawable.ic_share), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Udostępnij (${selectedTasks.size})")
            }
        }
    }
}

@Composable
fun ImportDialog(onImport: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importuj zadania") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.uppercase() },
                label = { Text("Kod 6-znakowy") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onImport(code) }, enabled = code.length == 6) { Text("Importuj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}