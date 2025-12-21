package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.components.ClassCardType
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Strefa czasowa
private val PolandZone = ZoneId.of("Europe/Warsaw")

// --- STAŁE WYMIARY I KOLORY ---
private val HourHeight = 60.dp
private val HourColWidth = 50.dp
private val LineOverlap = 8.dp
private val TextVerticalOffset = (-5).dp

private val ColorSelectedBg = Color(0xFF6750A4)
private val ColorTextDark = Color(0xFF4A4A4A)
private val ColorTextGray = Color(0xFF787579)
private val ColorDivider = Color(0xFFE0E0E0)
private val ColorCurrentTime = Color(0xFFEA4335)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAccountClick: () -> Unit,
    onClassClick: (Int) -> Unit = {},
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val allClasses by viewModel.classes.collectAsState()
    val classColorMap = uiState.classColorMap

    val currentDate = remember { LocalDate.now(PolandZone) }
    val currentMonth = remember { YearMonth.now(PolandZone) }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = firstDayOfWeek) }

    var selectedDate by remember { mutableStateOf(currentDate) }
    var isMonthView by remember { mutableStateOf(false) }

    val classesForDay = remember(allClasses, selectedDate) {
        allClasses.filter {
            it.dayOfWeek == (selectedDate.dayOfWeek.value - 1)
        }.sortedBy { it.startTime }
    }

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.scrollTo(480 * 2)
    }

    val titleMonth = if (isMonthView) {
        monthState.firstVisibleMonth.yearMonth
    } else {
        val weekDays = weekState.firstVisibleWeek.days
        if (weekDays.isNotEmpty()) weekDays.first().date.let { YearMonth.from(it) } else currentMonth
    }

    val formatter = remember { DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl")) }
    val monthName = titleMonth.format(formatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CalendarDrawerContent(
                favorites = uiState.favorites,
                selectedResourceId = uiState.selectedResourceId,
                onMyPlanClick = { viewModel.selectMyPlan(); scope.launch { drawerState.close() } },
                onTasksClick = { scope.launch { drawerState.close() }; onTasksClick() },
                onFavoriteClick = { fav -> viewModel.selectFavoritePlan(fav); scope.launch { drawerState.close() } },
                onSearchClick = { scope.launch { drawerState.close() }; onSearchClick() },
                onSettingsClick = { scope.launch { drawerState.close() }; onAccountClick() },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CalendarTopAppBar(
                    title = monthName,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    onSearchClick = onSearchClick,
                    onAddClick = { /* TODO: Add event */ },
                    onTitleClick = {
                        isMonthView = !isMonthView
                        scope.launch {
                            if (isMonthView) {
                                monthState.scrollToMonth(YearMonth.from(selectedDate))
                            } else {
                                weekState.scrollToWeek(selectedDate)
                            }
                        }
                    }
                )
            },
            containerColor = Color.White
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
            ) {
                // --- 1. NAGŁÓWEK DNI TYGODNIA ---
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(HourColWidth))
                    Box(modifier = Modifier.weight(1f)) {
                        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                    }
                }

                // --- 2. KALENDARZ (DNI) ---
                AnimatedContent(
                    targetState = isMonthView,
                    label = "CalendarViewTransition"
                ) { targetIsMonthView ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.width(HourColWidth))

                        Box(modifier = Modifier.weight(1f)) {
                            if (targetIsMonthView) {
                                HorizontalCalendar(
                                    state = monthState,
                                    dayContent = { day ->
                                        Day(
                                            date = day.date,
                                            isDateInMonth = day.position == DayPosition.MonthDate,
                                            isSelected = selectedDate == day.date,
                                            onClick = { clickedDate ->
                                                selectedDate = clickedDate
                                                isMonthView = false
                                                scope.launch { weekState.scrollToWeek(clickedDate) }
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                WeekCalendar(
                                    state = weekState,
                                    dayContent = { day ->
                                        Day(
                                            date = day.date,
                                            isDateInMonth = true,
                                            isSelected = selectedDate == day.date,
                                            onClick = { clickedDate ->
                                                selectedDate = clickedDate
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // --- 3. GŁÓWNA SIATKA GODZINOWA ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Godziny 0..24
                        // 24 to linia zamykająca (1dp)
                        repeat(25) { index ->
                            HourRow(hour = index, isLastLine = index == 24)
                        }
                    }

                    // --- WARSTWA ZAJĘĆ ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = HourColWidth)
                    ) {
                        classesForDay.forEach { classEntity ->
                            ScheduledClassItem(
                                classEntity = classEntity,
                                selectedDate = selectedDate,
                                classColorMap = classColorMap,
                                onClassClick = onClassClick
                            )
                        }
                    }

                    if (selectedDate == LocalDate.now(PolandZone)) {
                        CurrentTimeIndicator()
                    }
                }
            }
        }
    }
}

// --- KOMPONENTY ---

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<java.time.DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl"))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
                    .take(1),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = ColorTextGray
            )
        }
    }
}

@Composable
fun Day(
    date: LocalDate,
    isDateInMonth: Boolean,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now(PolandZone)
    val textColor = if (isDateInMonth) {
        if (isSelected) Color.White
        else if (isToday) Color(0xFF6750A4)
        else ColorTextDark
    } else {
        Color(0xFFE0E0E0)
    }

    val circleSize = if (isSelected || isToday) 28.dp else 24.dp
    val circleColor = if (isSelected && isDateInMonth) ColorSelectedBg else Color.Transparent
    val borderModifier = if (isToday && !isSelected && isDateInMonth) {
        Modifier.border(1.dp, ColorSelectedBg, CircleShape)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.aspectRatio(1f).padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(circleColor)
                    .then(borderModifier)
                    .clickable(enabled = isDateInMonth) { onClick(date) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HourRow(hour: Int, isLastLine: Boolean) {
    val rowHeight = if (isLastLine) 1.dp else HourHeight

    Row(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
        Box(modifier = Modifier.width(HourColWidth).fillMaxHeight()) {
            if (!isLastLine && hour != 0) {
                Text(
                    text = String.format("%d:00", hour),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = ColorTextGray,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFontFamily
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = (LineOverlap + 4.dp)) // TextGap = 4.dp
                        .offset(y = TextVerticalOffset)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .width(LineOverlap)
                        .align(Alignment.TopEnd),
                    thickness = 1.dp,
                    color = ColorDivider
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = ColorDivider
        )

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (hour != 0 || isLastLine) {
                if (hour != 0 && !isLastLine) {
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.TopStart),
                        thickness = 1.dp,
                        color = ColorDivider
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledClassItem(
    classEntity: ClassEntity,
    selectedDate: LocalDate,
    classColorMap: Map<String, Int>,
    onClassClick: (Int) -> Unit
) {
    val startParts = classEntity.startTime.split(":")
    val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
    val endParts = classEntity.endTime.split(":")
    val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
    val durationMinutes = endMinutes - startMinutes

    val topOffset = startMinutes.dp
    val height = durationMinutes.dp

    val now = LocalDateTime.now(PolandZone)
    val classEndDateTime = LocalDateTime.of(selectedDate, LocalTime.of(endParts[0].toInt(), endParts[1].toInt()))
    val classStartDateTime = LocalDateTime.of(selectedDate, LocalTime.of(startParts[0].toInt(), startParts[1].toInt()))

    val isPast = now.isAfter(classEndDateTime)
    val isFuture = now.isBefore(classStartDateTime)

    val assignedColorInt = classColorMap[classEntity.subjectName]
    val baseColor = if (assignedColorInt != null) Color(assignedColorInt) else MaterialTheme.colorScheme.secondaryContainer

    val containerAlpha = if (isPast) 0.5f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .offset(y = topOffset)
            .height(height)
            .alpha(containerAlpha) // Przezroczystość dla przeszłych
    ) {
        ClassCard(
            classItem = classEntity,
            type = ClassCardType.CALENDAR,
            backgroundColor = baseColor,
            accentColor = ColorSelectedBg,
            showBadge = isFuture, // Brak kropki dla przeszłych/trwających
            onClick = { onClassClick(classEntity.id) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CurrentTimeIndicator() {
    var currentTime by remember { mutableStateOf(LocalTime.now(PolandZone)) }

    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now(PolandZone)
            delay(60_000)
        }
    }

    val minutesFromMidnight = currentTime.hour * 60 + currentTime.minute
    val topOffset = minutesFromMidnight.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = topOffset)
            .height(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        HorizontalDivider(
            color = ColorCurrentTime,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HourColWidth)
        )

        Box(
            modifier = Modifier
                .padding(start = HourColWidth - 5.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(ColorCurrentTime)
        )
    }
}