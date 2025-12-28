package com.example.my_uz_android.ui.screens.calendar.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.components.ClassCardType
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private val PolandZone = ZoneId.of("Europe/Warsaw")
private val HourHeight = 60.dp

// Zmniejszona szerokość kolumny, aby usunąć nadmiar pustego miejsca po lewej
private val HourColWidth = 56.dp

private val ColorSelectedBg = Color(0xFF6750A4)
private val ColorTextDark = Color(0xFF4A4A4A)
private val ColorTextGray = Color(0xFF787579)
private val ColorDivider = Color(0xFFE0E0E0)
private val ColorCurrentTime = Color(0xFFEA4335)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScheduleView(
    calendarState: CalendarState,
    weekState: WeekCalendarState,
    selectedDate: LocalDate,
    isMonthView: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onToggleView: () -> Unit,
    classes: List<ClassEntity>,
    classColorMap: Map<String, Int>,
    onClassClick: (ClassEntity) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false
) {
    val classesForDay = remember(classes, selectedDate) {
        classes.filter { it.date == selectedDate.toString() }.sortedBy { it.startTime }
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        val now = LocalTime.now(PolandZone)
        val offsetMinutes = (now.hour * 60 + now.minute - 60).coerceAtLeast(0)
        scrollState.scrollTo(with(density) { offsetMinutes.dp.toPx() }.toInt())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp) // Ogólny padding strony 16dp
    ) {
        // --- NAGŁÓWEK ---
        if (showHeader) {
            val visibleMonth = if (isMonthView) {
                calendarState.firstVisibleMonth.yearMonth
            } else {
                val weekDays = weekState.firstVisibleWeek.days
                if (weekDays.isNotEmpty()) weekDays.first().date.let { YearMonth.from(it) } else YearMonth.now(PolandZone)
            }
            val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
            val monthTitle = "$monthName ${visibleMonth.year}"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { onToggleView() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D1B20)
                    )
                )
            }
        }
        // ----------------

        // Nagłówek dni tygodnia
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(HourColWidth))
            Box(modifier = Modifier.weight(1f)) {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY))
            }
        }

        AnimatedContent(targetState = isMonthView, label = "CalendarViewTransition") { targetIsMonthView ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(HourColWidth))
                Box(modifier = Modifier.weight(1f)) {
                    if (targetIsMonthView) {
                        HorizontalCalendar(
                            state = calendarState,
                            dayContent = { day ->
                                CalendarDay(
                                    date = day.date,
                                    isDateInMonth = day.position == DayPosition.MonthDate,
                                    isSelected = selectedDate == day.date,
                                    onClick = onDateSelected
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        WeekCalendar(
                            state = weekState,
                            dayContent = { day ->
                                CalendarDay(
                                    date = day.date,
                                    isDateInMonth = true,
                                    isSelected = selectedDate == day.date,
                                    onClick = onDateSelected
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // Rysowanie siatki godzin (0 do 23)
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(24) { index -> HourRow(hour = index) }
            }

            // Zajęcia
            Box(modifier = Modifier.matchParentSize().padding(start = HourColWidth)) {
                classesForDay.forEach { classEntity ->
                    ScheduledClassItem(
                        classEntity = classEntity,
                        selectedDate = selectedDate,
                        classColorMap = classColorMap,
                        onClassClick = onClassClick
                    )
                }
            }

            if (selectedDate == LocalDate.now(PolandZone)) CurrentTimeIndicator()
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f).height(24.dp),
                text = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pl"))
                    .replaceFirstChar { it.titlecase() }.take(1),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = ColorTextGray
            )
        }
    }
}

@Composable
fun CalendarDay(date: LocalDate, isDateInMonth: Boolean, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    val isToday = date == LocalDate.now(PolandZone)
    val textColor = if (isDateInMonth) {
        if (isSelected) Color.White else if (isToday) Color(0xFF6750A4) else ColorTextDark
    } else Color(0xFFE0E0E0)

    val circleSize = if (isSelected || isToday) 28.dp else 24.dp
    val circleColor = if (isSelected && isDateInMonth) ColorSelectedBg else Color.Transparent
    val borderModifier = if (isToday && !isSelected && isDateInMonth) Modifier.border(1.dp, ColorSelectedBg, CircleShape) else Modifier

    Box(
        modifier = Modifier.aspectRatio(1f).padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        fontSize = 16.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HourRow(hour: Int) {
    val rowHeight = HourHeight
    // Wyśrodkowanie tekstu w pionie względem linii
    val textYOffset = (-8).dp

    // Konfiguracja layoutu:
    // 1. Linia pionowa jest na `HourColWidth` (np. 56dp).
    // 2. Linia pozioma zaczyna się 8dp wcześniej (48dp).
    // 3. Tekst kończy się 8dp przed linią poziomą (40dp).
    val verticalLinePos = HourColWidth
    val horizontalLineStart = HourColWidth - 8.dp
    val textMaxWidth = horizontalLineStart - 8.dp // Zostawiamy 8dp odstępu między tekstem a linią

    Box(modifier = Modifier.fillMaxWidth().height(rowHeight)) {

        // 1. Linia Pozioma (Pomiń dla godziny 0)
        if (hour != 0) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalLineStart) // Kreska "odstaje" w lewo o 8dp
                    .align(Alignment.TopStart),
                thickness = 1.dp,
                color = ColorDivider
            )
        }

        // 2. Linia Pionowa
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .padding(start = verticalLinePos),
            thickness = 1.dp,
            color = ColorDivider
        )

        // 3. Tekst Godziny
        if (hour != 0) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = ColorTextGray,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily
                ),
                modifier = Modifier
                    .width(horizontalLineStart) // Kontener tekstu sięga do początku linii poziomej
                    .padding(end = 8.dp) // Wymuszamy 8dp odstępu od linii poziomej
                    .offset(y = textYOffset)
                    .align(Alignment.TopStart),
                textAlign = TextAlign.Start, // Wyrównanie do lewej (bez dodatkowego paddingu z lewej)
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun ScheduledClassItem(
    classEntity: ClassEntity,
    selectedDate: LocalDate,
    classColorMap: Map<String, Int>,
    onClassClick: (ClassEntity) -> Unit
) {
    val startParts = classEntity.startTime.split(":")
    val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
    val endParts = classEntity.endTime.split(":")
    val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
    val durationMinutes = endMinutes - startMinutes

    val topOffset = startMinutes.dp
    val height = durationMinutes.dp

    val now = LocalDateTime.now(PolandZone)
    val classStartDateTime = LocalDateTime.of(selectedDate, LocalTime.of(startParts[0].toInt(), startParts[1].toInt()))
    val isFuture = now.isBefore(classStartDateTime)

    val assignedColorIndex = classColorMap[classEntity.classType] ?: abs(classEntity.classType.hashCode()) % ClassColorPalette.size
    val colorSet = ClassColorPalette.getOrElse(assignedColorIndex) { ClassColorPalette[0] }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .offset(y = topOffset)
            .height(height)
    ) {
        ClassCard(
            classItem = classEntity,
            type = ClassCardType.CALENDAR,
            backgroundColor = colorSet.lightBg,
            accentColor = colorSet.lightAccent,
            showBadge = isFuture,
            onClick = { onClassClick(classEntity) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CurrentTimeIndicator() {
    var currentTime by remember { mutableStateOf(LocalTime.now(PolandZone)) }
    LaunchedEffect(Unit) { while (true) { currentTime = LocalTime.now(PolandZone); delay(60_000) } }
    val minutesFromMidnight = currentTime.hour * 60 + currentTime.minute
    val topOffset = minutesFromMidnight.dp

    Box(
        modifier = Modifier.fillMaxWidth().offset(y = topOffset).height(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        HorizontalDivider(
            color = ColorCurrentTime,
            thickness = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(start = HourColWidth)
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