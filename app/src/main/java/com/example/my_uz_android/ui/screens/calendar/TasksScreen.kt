package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBackClick: () -> Unit,
    onTaskClick: (Int) -> Unit,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Scroll behavior dla TopAppBar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // Białe tło (w LightMode) lub surface
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TasksTopBar(
                onSettingsClick = { /* TODO */ },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zadanie")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.groupedTasks.isEmpty()) {
            EmptyTasksState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                uiState.groupedTasks.forEach { (yearMonth, daysMap) ->
                    stickyHeader {
                        MonthHeader(monthName = yearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))))
                    }
                    daysMap.forEach { (date, tasks) ->
                        item {
                            TaskGroupRow(
                                date = date,
                                tasks = tasks,
                                onTaskClick = onTaskClick,
                                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                                onDelete = { viewModel.deleteTask(it) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest, // Białe/Ciemne tło
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                AddTaskContent(
                    subjects = uiState.availableSubjects,
                    onAddTask = { title, date, subject, type ->
                        viewModel.addTask(title, date, subject, type)
                        showBottomSheet = false
                    },
                    onClose = { showBottomSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    onSettingsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Terminarz",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dots_vertical),
                    contentDescription = "Opcje",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun MonthHeader(monthName: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text = monthName.replaceFirstChar { it.titlecase(Locale.getDefault()) },
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun EmptyTasksState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, tint = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Brak zadań", fontFamily = InterFontFamily, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TaskGroupRow(date: LocalDate, tasks: List<TaskEntity>, onTaskClick: (Int) -> Unit, onToggleComplete: (TaskEntity) -> Unit, onDelete: (TaskEntity) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp).padding(top = 4.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("EEE", Locale("pl"))).uppercase(), fontFamily = InterFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(date.dayOfMonth.toString(), fontFamily = InterFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            tasks.forEach { task ->
                SwipeableTaskCard(task, { onTaskClick(task.id) }, { onDelete(task) }, { onToggleComplete(task) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(task: TaskEntity, onClick: () -> Unit, onSwipeDelete: () -> Unit, onSwipeComplete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> { onSwipeComplete(); false }
                SwipeToDismissBoxValue.EndToStart -> { onSwipeDelete(); true }
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFFE6F5E8)
                    else -> Color.Transparent
                }, label = "Color"
            )
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            val icon = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Done else Icons.Default.Delete
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(color).padding(horizontal = 20.dp), contentAlignment = alignment) {
                Icon(icon, null)
            }
        }
    ) {
        TaskCard(task = task, onTaskClick = onClick)
    }
}

// --- FORMULARZ DODAWANIA W STYLU GOOGLE CALENDAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskContent(
    subjects: List<SubjectOption>,
    onAddTask: (String, Long, String?, String?) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedSubject by remember { mutableStateOf<SubjectOption?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding() // Obsługa klawiatury
    ) {
        // Górny pasek: Zamknij | Zapisz
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_x_close), contentDescription = "Zamknij")
            }
            Button(
                onClick = { onAddTask(title, selectedDateMillis, selectedSubject?.name, selectedType) },
                enabled = title.isNotEmpty(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Zapisz", fontWeight = FontWeight.SemiBold)
            }
        }

        // Tytuł zadania (Duży)
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Co masz do zrobienia?", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Wybór Daty
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            val dateString = Instant.ofEpochMilli(selectedDateMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl")))
            Text(dateString, style = MaterialTheme.typography.bodyLarge)
        }

        // Wybór Przedmiotu (Dropdown w formie wiersza)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { subjectExpanded = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_book_open), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = selectedSubject?.name ?: "Wybierz przedmiot (opcjonalnie)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedSubject == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                subjects.forEach { subject ->
                    DropdownMenuItem(text = { Text(subject.name) }, onClick = { selectedSubject = subject; selectedType = null; subjectExpanded = false })
                }
            }
        }

        // Wybór Typu (jeśli wybrano przedmiot)
        if (selectedSubject != null) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { typeExpanded = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_info_circle), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = selectedType ?: "Rodzaj zajęć",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedType == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    selectedSubject!!.types.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { selectedType = type; typeExpanded = false })
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp)) // Extra space at bottom
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}