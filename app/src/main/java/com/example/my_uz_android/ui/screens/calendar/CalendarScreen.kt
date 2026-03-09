package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.screens.calendar.components.ScheduleView
import com.example.my_uz_android.ui.theme.MyUZTheme
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
import kotlin.math.abs
import androidx.compose.ui.res.painterResource
import com.example.my_uz_android.R

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

    CalendarScreenContent(
        uiState = uiState,
        onSearchClick = onSearchClick,
        onAccountClick = onAccountClick,
        onClassClick = {
            viewModel.setTemporaryClassForDetails(it)
            onClassClick(it)
        },
        onDateSelected = { viewModel.setSelectedDate(it) },
        onToggleMonthView = { viewModel.setMonthView(it) },
        onToggleGroupVisibility = { viewModel.toggleGroupVisibility(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenContent(
    uiState: CalendarUiState,
    onSearchClick: () -> Unit,
    onAccountClick: () -> Unit,
    onClassClick: (ClassEntity) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onToggleMonthView: (Boolean) -> Unit,
    onToggleGroupVisibility: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val selectedDate = uiState.selectedDate
    val isMonthView = uiState.isMonthView

    val currentDate = remember { LocalDate.now(ZoneId.of("Europe/Warsaw")) }
    val currentMonth = remember { YearMonth.now(ZoneId.of("Europe/Warsaw")) }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atStartOfMonth(),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = currentDate,
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    var swipeDirection by remember { mutableStateOf<DaySwipeDirection?>(null) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    fun navigateDay(offsetDays: Long, direction: DaySwipeDirection) {
        val newDate = selectedDate.plusDays(offsetDays)
        if (newDate == selectedDate) return

        swipeDirection = direction
        onDateSelected(newDate)

        scope.launch {
            if (isMonthView) {
                monthState.scrollToMonth(YearMonth.from(newDate))
            } else {
                weekState.scrollToWeek(newDate)
            }
        }
    }

    val visibleMonth = if (isMonthView) {
        monthState.firstVisibleMonth.yearMonth
    } else {
        val weekDays = weekState.firstVisibleWeek.days
        if (weekDays.isNotEmpty()) YearMonth.from(weekDays.first().date) else YearMonth.from(selectedDate)
    }

    val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
        .replaceFirstChar { it.titlecase(Locale("pl")) }

    val currentRealWorldYear = YearMonth.now(ZoneId.of("Europe/Warsaw")).year
    val calendarTitle = if (visibleMonth.year == currentRealWorldYear) monthName else "$monthName ${visibleMonth.year}"

    Scaffold(
        topBar = {
            CalendarTopAppBar(
                title = calendarTitle,
                isExpanded = isMonthView,
                onNavigationClick = onAccountClick,
                onSearchClick = onSearchClick,
                onTitleClick = { onToggleMonthView(!isMonthView) },
                onAddClick = {
                    val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
                    swipeDirection = null
                    onDateSelected(today)
                    scope.launch {
                        if (isMonthView) {
                            monthState.animateScrollToMonth(YearMonth.from(today))
                        } else {
                            weekState.animateScrollToWeek(today)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(selectedDate, isMonthView) {
                    val swipeThresholdPx = with(density) { 72.dp.toPx() }
                    var accumulatedDx = 0f
                    var accumulatedDy = 0f

                    detectHorizontalDragGestures(
                        onDragStart = {
                            accumulatedDx = 0f
                            accumulatedDy = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            accumulatedDx += dragAmount
                            accumulatedDy += abs(change.position.y - change.previousPosition.y)
                        },
                        onDragEnd = {
                            val isHorizontalIntent = abs(accumulatedDx) > accumulatedDy
                            val passedThreshold = abs(accumulatedDx) >= swipeThresholdPx

                            if (isHorizontalIntent && passedThreshold) {
                                if (accumulatedDx < 0) {
                                    navigateDay(offsetDays = 1, direction = DaySwipeDirection.NEXT)
                                } else {
                                    navigateDay(offsetDays = -1, direction = DaySwipeDirection.PREVIOUS)
                                }
                            }
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // MULTI-KIERUNEK: Spójne menu w formie dropdownu identyczne jak w indeksie
                if (uiState.userCourses.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(onClick = { isFilterExpanded = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filter_funnel), // <-- TWOJA IKONA
                                    contentDescription = "Filtruj kierunki",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = isFilterExpanded,
                                onDismissRequest = { isFilterExpanded = false }
                            ) {
                                uiState.userCourses.forEach { course ->
                                    val isSelected = uiState.selectedGroupCodes.contains(course.groupCode)
                                    DropdownMenuItem(
                                        text = { Text(course.fieldOfStudy ?: course.groupCode) },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Wybrane",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = { onToggleGroupVisibility(course.groupCode) }
                                    )
                                }
                            }
                        }
                    }
                }

                ScheduleView(
                    calendarState = monthState,
                    weekState = weekState,
                    selectedDate = selectedDate,
                    isMonthView = isMonthView,
                    swipeDirection = swipeDirection,
                    onDateSelected = { date ->
                        swipeDirection = null
                        onDateSelected(date)
                    },
                    onToggleView = { onToggleMonthView(!isMonthView) },
                    classes = uiState.visibleClasses,
                    classColorMap = uiState.classColorMap,
                    onClassClick = onClassClick,
                    modifier = Modifier.weight(1f),
                    showHeader = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    MyUZTheme {
        CalendarScreenContent(
            uiState = CalendarUiState(
                selectedDate = LocalDate.now(),
                isMonthView = false,
                userCourses = listOf(
                    UserCourseEntity(id = 1, groupCode = "12IN", fieldOfStudy = "Informatyka", semester = 1),
                    UserCourseEntity(id = 2, groupCode = "34MA", fieldOfStudy = "Matematyka", semester = 3)
                ),
                selectedGroupCodes = setOf("12IN"),
                visibleClasses = emptyList()
            ),
            onSearchClick = {},
            onAccountClick = {},
            onClassClick = {},
            onDateSelected = {},
            onToggleMonthView = {},
            onToggleGroupVisibility = {}
        )
    }
}