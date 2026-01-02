package com.example.my_uz_android.ui.screens.calendar.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.components.ClassCardType
import com.example.my_uz_android.ui.components.TaskCard
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private val PolandZone = ZoneId.of("Europe/Warsaw")
private val HourHeight = 60.dp
private val HourColWidth = 56.dp

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
    tasks: List<TaskEntity>, // ✅ Lista zadań
    classColorMap: Map<String, Int>,
    onClassClick: (ClassEntity) -> Unit,
    onTaskClick: (TaskEntity) -> Unit, // ✅ Kliknięcie w zadanie
    onToggleTaskCompletion: (TaskEntity) -> Unit, // ✅ Zmiana statusu
    modifier: Modifier = Modifier,
    showHeader: Boolean = false
) {
    val classesForDay = remember(classes, selectedDate) {
        classes.filter { it.date == selectedDate.toString() }.sortedBy { it.startTime }
    }

    // Filtrujemy zadania dla wybranego dnia
    val tasksForDay = remember(tasks, selectedDate) {
        tasks.filter {
            Instant.ofEpochMilli(it.dueDate).atZone(PolandZone).toLocalDate() == selectedDate
        }
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- NAGŁÓWEK (Miesiąc/Rok) ---
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
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Nagłówek dni tygodnia
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.width(HourColWidth))
            Box(modifier = Modifier.weight(1f)) {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY))
            }
        }

        AnimatedContent(targetState = isMonthView, label = "CalendarTransition", modifier = Modifier.padding(horizontal = 16.dp)) { targetIsMonth ->
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

        Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- SEKCJA ZADAŃ (Nad godzinami) ---
                if (tasksForDay.isNotEmpty()) {
                    Column(modifier = Modifier.padding(start = HourColWidth + 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
                        Text(
                            text = "ZADANIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        tasksForDay.forEach { task ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd) {
                                        onToggleTaskCompletion(task)
                                        false // ✅ Zwracamy false, żeby karta wróciła na miejsce (toggle)
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Icon(
                                            painter = painterResource(if (task.isCompleted) R.drawable.ic_x_close else R.drawable.ic_check_square_broken),
                                            contentDescription = null,
                                            tint = if (task.isCompleted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                },
                                enableDismissFromEndToStart = false,
                                content = {
                                    // ✅ Naprawa: TaskCard nie ma parametru onClick, więc opakowujemy go w Box
                                    Box(modifier = Modifier.clickable { onTaskClick(task) }) {
                                        TaskCard(
                                            task = task,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                // --- SIATKA GODZINOWA ---
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f).height(24.dp),
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl"))
                    .replaceFirstChar { it.titlecase() }.take(1),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarDay(date: LocalDate, isDateInMonth: Boolean, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    val isToday = date == LocalDate.now(PolandZone)
    val primaryColor = MaterialTheme.colorScheme.primary

    val textColor = if (isDateInMonth) {
        if (isSelected) MaterialTheme.colorScheme.onPrimary
        else if (isToday) primaryColor
        else MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.outlineVariant

    val circleSize = if (isSelected || isToday) 28.dp else 24.dp
    val circleColor = if (isSelected && isDateInMonth) primaryColor else Color.Transparent
    val borderModifier = if (isToday && !isSelected && isDateInMonth) Modifier.border(1.dp, primaryColor, CircleShape) else Modifier

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

    Box(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
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

    val indicatorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier.fillMaxWidth().offset(y = topOffset).height(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        HorizontalDivider(
            color = indicatorColor,
            thickness = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(start = HourColWidth)
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