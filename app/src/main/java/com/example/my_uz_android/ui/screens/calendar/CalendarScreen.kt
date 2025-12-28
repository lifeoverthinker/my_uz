package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.screens.calendar.components.ScheduleView
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAccountClick: () -> Unit,
    onClassClick: (ClassEntity) -> Unit = {},
    onShowPreview: () -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Obsługa różnych źródeł danych (Mój plan / Sieć)
    val displayedClasses by produceState(initialValue = emptyList<ClassEntity>(), key1 = uiState.currentSource) {
        if (uiState.currentSource is ScheduleSource.MyPlan) {
            viewModel.myPlanClasses.collect { value = it }
        } else {
            viewModel.networkClasses.collect { value = it }
        }
    }

    val classColorMap = uiState.classColorMap

    val currentDate = remember { LocalDate.now(ZoneId.of("Europe/Warsaw")) }
    val currentMonth = remember { YearMonth.now(ZoneId.of("Europe/Warsaw")) }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }
    val firstDayOfWeek = DayOfWeek.MONDAY

    var selectedDate by remember { mutableStateOf(currentDate) }
    var isMonthView by remember { mutableStateOf(false) }

    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atStartOfMonth(),
        endDate = endMonth.atEndOfMonth(),
        firstDayOfWeek = firstDayOfWeek,
    )

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstDayOfWeek = firstDayOfWeek,
        firstVisibleMonth = currentMonth
    )

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val visibleMonth = if (isMonthView) {
        monthState.firstVisibleMonth.yearMonth
    } else {
        val weekDays = weekState.firstVisibleWeek.days
        if (weekDays.isNotEmpty()) weekDays.first().date.let { YearMonth.from(it) } else currentMonth
    }
    val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
    val title = if (visibleMonth.year == YearMonth.now().year) monthName else "$monthName ${visibleMonth.year}"

    // --- LOGIKA STANU (Nowe funkcje na starym wyglądzie) ---
    val isMyPlan = uiState.currentSource is ScheduleSource.MyPlan

    val isTeacherView = when (val source = uiState.currentSource) {
        is ScheduleSource.Favorite -> source.type == "teacher"
        is ScheduleSource.Preview -> source.type == "teacher"
        else -> false
    }

    // Podtytuł (np. imię nauczyciela lub nazwa grupy) - null jeśli to "Mój Plan"
    val subtitleText = if (!isMyPlan) uiState.selectedPlanName else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CalendarDrawerContent(
                favorites = uiState.favorites,
                selectedResourceId = uiState.selectedResourceId,
                currentScreen = "calendar",
                onMyPlanClick = {
                    if (!isMyPlan) viewModel.selectMyPlan()
                    scope.launch { drawerState.close() }
                },
                onTasksClick = {
                    scope.launch { drawerState.close() }
                    onTasksClick()
                },
                onFavoriteClick = { fav ->
                    viewModel.selectFavoritePlan(fav)
                    scope.launch {
                        drawerState.close()
                        // Jeśli jesteśmy w trybie podglądu, to tutaj zostajemy, UI się odświeży
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                // UŻYCIE STAREGO KOMPONENTU Z NOWYMI PARAMETRAMI
                CalendarTopAppBar(
                    title = title,
                    subtitle = subtitleText, // Wyświetli się pod datą
                    navigationIcon = if (isMyPlan) R.drawable.ic_menu else R.drawable.ic_chevron_left, // Zmiana ikony
                    isExpanded = isMonthView,
                    onNavigationClick = {
                        if (isMyPlan) {
                            scope.launch { drawerState.open() }
                        } else {
                            // Powrót do mojego planu
                            viewModel.selectMyPlan()
                        }
                    },
                    onSearchClick = onSearchClick,
                    onAddClick = {
                        val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
                        selectedDate = today
                        scope.launch {
                            if (isMonthView) monthState.animateScrollToMonth(YearMonth.from(today))
                            else weekState.animateScrollToWeek(today)
                        }
                    },
                    onInfoClick = if (isTeacherView) { { /* TODO: Dialog info */ } } else null, // Tylko dla nauczycieli
                    onTitleClick = {
                        isMonthView = !isMonthView
                        scope.launch {
                            if (isMonthView) monthState.scrollToMonth(YearMonth.from(selectedDate))
                            else weekState.scrollToWeek(selectedDate)
                        }
                    }
                )
            },
            containerColor = Color.White
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ScheduleView(
                    calendarState = monthState,
                    weekState = weekState,
                    selectedDate = selectedDate,
                    isMonthView = isMonthView,
                    onDateSelected = { date ->
                        selectedDate = date
                        if (isMonthView) {
                            isMonthView = false
                            scope.launch { weekState.scrollToWeek(date) }
                        }
                    },
                    onToggleView = {
                        isMonthView = !isMonthView
                        scope.launch {
                            if (isMonthView) monthState.scrollToMonth(YearMonth.from(selectedDate))
                            else weekState.scrollToWeek(selectedDate)
                        }
                    },
                    classes = displayedClasses, // Poprawione źródło danych
                    classColorMap = classColorMap,
                    onClassClick = onClassClick,
                    modifier = Modifier.padding(innerPadding),
                    showHeader = false
                )
            }
        }
    }
}