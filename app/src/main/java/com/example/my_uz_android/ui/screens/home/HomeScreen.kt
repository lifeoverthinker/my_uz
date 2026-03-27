package com.example.my_uz_android.ui.screens.home

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.EventCard
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.screens.home.components.UpcomingClasses
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.ui.screens.notifications.NotificationsViewModel
import com.example.my_uz_android.ui.components.DashboardEmptyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    notificationsViewModel: NotificationsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (Int) -> Unit,
    onEventClick: (Int) -> Unit,
    onTaskClick: (Int) -> Unit,
    onAccountClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onAddGradeClick: () -> Unit = {},
    onAddAbsenceClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {}
) {
    // --- STATE ---
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isFabExpanded by remember { mutableStateOf(false) }

    // --- THEME & SYSTEM BARS ---
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    val topSectionBackground = extendedColors.homeTopBackground
    val headerContentColor = MaterialTheme.colorScheme.onSurface
    val subHeaderContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    val view = LocalView.current
    val defaultStatusBarColor = MaterialTheme.colorScheme.background
    val defaultNavBarColor = MaterialTheme.colorScheme.surface
    val homeStatusBarColor = topSectionBackground
    val homeNavBarColor = MaterialTheme.colorScheme.surface

    if (!view.isInEditMode) {
        DisposableEffect(isDark) {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = homeStatusBarColor.toArgb()
                window.navigationBarColor = homeNavBarColor.toArgb()
                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = false
                insets.isAppearanceLightNavigationBars = !isDark
            }
            onDispose {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    window.statusBarColor = defaultStatusBarColor.toArgb()
                    window.navigationBarColor = defaultNavBarColor.toArgb()
                    val insets = WindowCompat.getInsetsController(window, view)
                    insets.isAppearanceLightStatusBars = !isDark
                    insets.isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    // --- MAIN CONTENT ---
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Scaffold(
            floatingActionButton = {
                UniversalFab(
                    onAddGrade = onAddGradeClick,
                    onAddAbsence = onAddAbsenceClick,
                    onAddTask = onAddTaskClick
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Tło górnej sekcji
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(topSectionBackground)
                )

                Column(modifier = Modifier.fillMaxSize()) {

                    // --- HEADER ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(72.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.currentDate,
                                style = MaterialTheme.typography.titleSmall,
                                color = headerContentColor
                            )

                            val unreadCount by notificationsViewModel.unreadCount.collectAsState(initial = 0)

                            IconButton(onClick = onNotificationsClick) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = Color.White
                                            ) {
                                                Text(text = unreadCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bell),
                                        contentDescription = "Powiadomienia",
                                        tint = headerContentColor
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = uiState.greeting,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = headerContentColor
                            )
                            Text(
                                text = uiState.departmentInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = subHeaderContentColor
                            )
                        }
                    }

                    // --- DASHBOARD CONTENT ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        LazyColumn(contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                            if (uiState.isPlanSelected) {
                                item {
                                    UpcomingClasses(
                                        classes = uiState.upcomingClasses,
                                        emptyMessage = uiState.classesMessage,
                                        dayLabel = uiState.classesDayLabel,
                                        classColorMap = uiState.classColorMap,
                                        isDarkMode = isDark,
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
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(painter = painterResource(id = R.drawable.ic_book_open), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                        Text(text = "Zadania", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    if (uiState.upcomingTasks.isEmpty()) {
                                        DashboardEmptyCard(
                                            title = "Czysta lista!",
                                            message = "Nie masz żadnych zadań",
                                            iconRes = R.drawable.ic_check_circle_broken,
                                            containerColor = extendedColors.taskCardBackground,
                                            contentColor = Color(0xFF38608F),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            itemsIndexed(uiState.upcomingTasks) { _, task ->
                                                TaskCard(task = task, onTaskClick = { onTaskClick(task.id) }, modifier = Modifier.width(264.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(painter = painterResource(id = R.drawable.ic_marker_pin), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                        Text(text = "Wydarzenia", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    if (uiState.upcomingEvents.isEmpty()) {
                                        DashboardEmptyCard(
                                            title = "Spokojny czas",
                                            message = "Brak nadchodzących wydarzeń",
                                            iconRes = R.drawable.ic_marker_pin,
                                            containerColor = extendedColors.eventCardBackground,
                                            contentColor = Color(0xFF3C6839),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            items(uiState.upcomingEvents) { event ->
                                                EventCard(event = event, onClick = { onEventClick(event.id) }, modifier = Modifier.width(264.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 24.dp), contentAlignment = Alignment.CenterStart) {
                                    Text(text = "MyUZ 2026", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }
                        }
                    }
                }

                // --- FAB SCRIM OVERLAY ---
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isFabExpanded = false
                            }
                    )
                }
            }
        }
    }
}