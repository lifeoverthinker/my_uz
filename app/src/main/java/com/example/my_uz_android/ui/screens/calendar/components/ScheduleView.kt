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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
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
import java.time.*
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs

private val PolandZone = ZoneId.of("Europe/Warsaw")
private val HourHeight = 60.dp
private val HourColWidth = 56.dp
private val ColumnSpacing = 4.dp // Usunęłam zduplikowaną zmienną

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
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
    // 1. Filtrujemy i sortujemy zajęcia dla wybranego dnia
    val classesForDay = remember(classes, selectedDate) {
        classes.filter { it.date == selectedDate.toString() }.sortedBy { it.startTime }
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // 2. Automatyczne przewijanie do pierwszych zajęć (Smart Scroll) - NAPRAWIONE
    LaunchedEffect(classesForDay) {
        val firstClassMinute = classesForDay.minOfOrNull {
            val parts = it.startTime.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } ?: (8 * 60) // Jeśli brak zajęć, też 8:00

        // Przewiń o godzinę wcześniej, żeby było widać kontekst
        val targetMinute = (firstClassMinute - 60).coerceAtLeast(0)
        scrollState.scrollTo(with(density) { targetMinute.dp.toPx() }.toInt())
    }

    // Od tego miejsca Twój kod w dół zostaje bez zmian
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- NAGĹĂ“WEK MIESIÄ„CA ---
        if (showHeader) {
            val visibleMonth = if (isMonthView) {
                calendarState.firstVisibleMonth.yearMonth
            } else {
                val weekDays = weekState.firstVisibleWeek.days
                if (weekDays.isNotEmpty()) weekDays.first().date.let { YearMonth.from(it) }
                else YearMonth.now(PolandZone)
            }

            val monthName =
                visibleMonth.month.getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale("pl"))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }
            val monthTitle = "$monthName ${visibleMonth.year}"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { onToggleView() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthTitle,
                        style = TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        painter = painterResource(if (isMonthView) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // --- NAGĹĂ“WKI DNI TYGODNIA ---
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.width(HourColWidth))
            Box(modifier = Modifier.weight(1f)) {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY))
            }
        }

        // --- KALENDARZ (MIESIÄ„C / TYDZIEĹ) ---
        AnimatedContent(
            targetState = isMonthView,
            label = "CalendarTransition",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { targetIsMonth ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(HourColWidth))
                Box(modifier = Modifier.weight(1f)) {
                    if (targetIsMonth) {
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

        // --- SIATKA ZAJÄÄ† (SCROLLOWALNA) ---
        Box(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {

                // TĹ‚o: Linie godzinowe
                Column(modifier = Modifier.fillMaxWidth()) {
                    repeat(24) { index -> HourRow(hour = index) }
                }

                // ZawartoĹ›Ä‡: Karty zajÄ™Ä‡
                Box(modifier = Modifier
                    .matchParentSize()
                    .padding(start = HourColWidth)) {
                    classesForDay.forEach { classEntity ->
                        ScheduledClassItem(
                            classEntity = classEntity,
                            selectedDate = selectedDate,
                            classColorMap = classColorMap,
                            onClassClick = onClassClick
                        )
                    }
                }

                // WskaĹşnik aktualnego czasu
                if (selectedDate == LocalDate.now(PolandZone)) {
                    CurrentTimeIndicator()
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                text = dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale("pl"))
                    .replaceFirstChar { it.titlecase() }.take(1),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isDateInMonth: Boolean,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now(PolandZone)
    val primaryColor = MaterialTheme.colorScheme.primary

    val textColor = if (isDateInMonth) {
        if (isSelected) MaterialTheme.colorScheme.onPrimary
        else if (isToday) primaryColor
        else MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.outlineVariant

    val circleSize = if (isSelected || isToday) 28.dp else 24.dp
    val circleColor = if (isSelected && isDateInMonth) primaryColor else Color.Transparent
    val borderModifier = if (isToday && !isSelected && isDateInMonth) Modifier.border(
        1.dp,
        primaryColor,
        CircleShape
    ) else Modifier

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
    val textYOffset = (-8).dp
    val verticalLinePos = HourColWidth
    val horizontalLineStart = HourColWidth - 8.dp
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(rowHeight)) {
        if (hour != 0) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalLineStart)
                    .align(Alignment.TopStart),
                thickness = 1.dp,
                color = dividerColor
            )
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .padding(start = verticalLinePos),
            thickness = 1.dp,
            color = dividerColor
        )

        if (hour != 0) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .width(horizontalLineStart)
                    .padding(end = 8.dp)
                    .offset(y = textYOffset)
                    .align(Alignment.TopStart),
                textAlign = TextAlign.Start,
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
    val classStartDateTime =
        LocalDateTime.of(selectedDate, LocalTime.of(startParts[0].toInt(), startParts[1].toInt()))
    val isFuture = now.isBefore(classStartDateTime)

    val assignedColorIndex = classColorMap[classEntity.classType]
        ?: abs(classEntity.classType.hashCode()) % ClassColorPalette.size
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
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now(PolandZone)
            delay(60_000)
        }
    }

    val minutesFromMidnight = currentTime.hour * 60 + currentTime.minute
    val topOffset = minutesFromMidnight.dp
    val indicatorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = topOffset)
            .height(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        HorizontalDivider(
            color = indicatorColor,
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
                .background(indicatorColor)
        )
    }
}
