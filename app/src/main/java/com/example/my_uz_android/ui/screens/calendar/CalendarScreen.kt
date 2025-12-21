package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// --- STALE ROZMIARY I KOLORY ---
private val HourColWidth = 48.dp
private val LineOverlap = 8.dp
private val TextGap = 5.dp
private val ColorSelectedBg = Color(0xff6750a4)
private val ColorTextUnselected = Color(0xff4a4a4a)
private val ColorTextGray = Color(0xff787579)
private val ColorDivider = Color(0xffe0e0e0)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAccountClick: () -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = firstDayOfWeek) }

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    onAddClick = { /* TODO: Dodaj notatkę */ },
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
                    .padding(horizontal = 16.dp) // JEDYNY PADDING BOCZNY
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // --- 1. NAGŁÓWEK DNI TYGODNIA ---
                // Przesuwamy go w prawo o szerokość kolumny godzin, żeby pasował do siatki
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(HourColWidth)) // Pusty box nad godzinami
                    Box(modifier = Modifier.weight(1f)) {
                        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                    }
                }

                // --- 2. KALENDARZ (DNI MIESIĄCA) ---
                // Również przesunięty, żeby dni były nad odpowiednimi kolumnami siatki
                AnimatedContent(
                    targetState = isMonthView,
                    label = "CalendarViewTransition"
                ) { targetIsMonthView ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Jeśli widok miesięczny - kalendarz zajmuje całość (standardowy wygląd)
                        // Jeśli widok tygodniowy - przesuwamy go o szerokość godzin, żeby pasował do siatki
                        if (!targetIsMonthView) {
                            Spacer(modifier = Modifier.width(HourColWidth))
                        }

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
                                    // W widoku miesięcznym nie potrzebujemy dopasowania do siatki godzin
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

                // Linia oddzielająca nagłówek od siatki (widoczna tylko w widoku tygodnia pod dniami)
                if(!isMonthView) {
                    // Ta linia jest już rysowana przez siatkę jako pierwsza linia (godzina 0:00)
                    // więc tutaj możemy dać tylko mały odstęp
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    HorizontalDivider(color = ColorDivider, thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))
                }

                // --- 3. SIATKA GODZINOWA ---
                // Wyświetlamy tylko w widoku tygodniowym (standard w aplikacjach)
                if (!isMonthView) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // 25 elementów: 0..23 to godziny, 24 to linia zamykająca
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(25) { index ->
                            if (index == 24) {
                                // Ostatnia linia zamykająca (po 23:00)
                                HourRow(hour = 0, isLastLine = true)
                            } else {
                                HourRow(hour = index, isLastLine = false)
                            }
                        }
                    }
                } else {
                    // W widoku miesięcznym lista zajęć (jak wcześniej)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        items(3) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Zajęcia w dniu ${selectedDate}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<java.time.DayOfWeek>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
    val isToday = date == LocalDate.now()

    val textColor = if (isDateInMonth) {
        if (isSelected) Color.White
        else if (isToday) Color(0xFF6750A4)
        else ColorTextUnselected
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
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brak dodatkowego Spacera na górze, aby trzymać się ściśle layoutu
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
    // Wysokość wiersza to 60dp, chyba że to ostatnia linia zamykająca (wtedy 0dp wysokości samej komórki, tylko linia)
    val rowHeight = if (isLastLine) 1.dp else 60.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
    ) {
        // 1. Kolumna Czasu
        Box(
            modifier = Modifier
                .width(HourColWidth)
                .fillMaxHeight(),
        ) {
            // Tekst wyświetlamy tylko jeśli to nie jest 0:00 (początek dnia) i nie jest to linia zamykająca
            // Zgodnie z prośbą: "od 00 do 1:00 nie bylo podanej godziny 00"
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
                        .padding(end = 12.dp) // Odstęp od linii pionowej
                        .offset(y = (-5).dp) // Zgodnie z prośbą: offset 5dp do góry od linii
                )
            }

            // Pozioma "krótka" kreska (krzyżyk) wychodząca w lewo
            HorizontalDivider(
                modifier = Modifier
                    .width(LineOverlap) // 8dp
                    .align(Alignment.TopEnd),
                thickness = 1.dp,
                color = ColorDivider
            )
        }

        // 2. Pionowa Linia (przez całą wysokość)
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = ColorDivider
        )

        // 3. Obszar Siatki (Linia pozioma + Miejsce na zajęcia)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Główna linia pozioma
            HorizontalDivider(
                modifier = Modifier.align(Alignment.TopStart),
                thickness = 1.dp,
                color = ColorDivider
            )
        }
    }
}