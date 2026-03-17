package com.example.my_uz_android.ui.screens.calendar.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
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
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.components.ClassCardType
import com.example.my_uz_android.ui.screens.calendar.DaySwipeDirection
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView(
    calendarState: CalendarState,
    weekState: WeekCalendarState,
    selectedDate: LocalDate,
    isMonthView: Boolean,
    swipeDirection: DaySwipeDirection? = null,
    onDateSelected: (LocalDate) -> Unit,
    onToggleView: () -> Unit,
    classes: List<ClassEntity>,
    tasks: List<TaskEntity>,
    classColorMap: Map<String, Int>,
    onClassClick: (ClassEntity) -> Unit,
    isTeacherPlan: Boolean = false,
    availableDirections: List<String> = emptyList(),
    selectedDirections: Set<String> = emptySet(),
    onDirectionToggled: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    showHeader: Boolean = false
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(classes, selectedDate) {
        delay(50)

        val isToday = selectedDate == LocalDate.now(PolandZone)
        val classesForToday = classes.filter { it.date == selectedDate.toString() }

        val targetMinute = if (isToday) {
            val now = LocalTime.now(PolandZone)
            (now.hour * 60 + now.minute - 60).coerceAtLeast(0)
        } else if (classesForToday.isNotEmpty()) {
            val firstClassMinute = classesForToday.minOfOrNull {
                try {
                    val parts = it.startTime.split(":")
                    parts[0].toInt() * 60 + parts[1].toInt()
                } catch (e: Exception) {
                    8 * 60
                }
            } ?: (8 * 60)
            (firstClassMinute - 60).coerceAtLeast(0)
        } else {
            8 * 60
        }

        scrollState.animateScrollTo(with(density) { targetMinute.dp.toPx() }.toInt())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showHeader) {
            val visibleMonth = if (isMonthView) {
                calendarState.firstVisibleMonth.yearMonth
            } else {
                val weekDays = weekState.firstVisibleWeek.days
                if (weekDays.isNotEmpty()) weekDays.first().date.let { YearMonth.from(it) }
                else YearMonth.now(PolandZone)
            }

            val monthName = visibleMonth.month.getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale("pl"))
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

        if (availableDirections.isNotEmpty()) {
            CalendarFilterRow(
                availableDirections = availableDirections,
                selectedDirections = selectedDirections,
                onDirectionToggled = onDirectionToggled
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.width(HourColWidth))
            Box(modifier = Modifier.weight(1f)) {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.width(HourColWidth))
            Box(modifier = Modifier.weight(1f)) {
                if (isMonthView) {
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            val dayTasks = tasks.filter {
                                val taskDate = Instant.ofEpochMilli(it.dueDate).atZone(PolandZone).toLocalDate()
                                taskDate == day.date
                            }
                            CalendarDay(
                                date = day.date,
                                isDateInMonth = day.position == DayPosition.MonthDate,
                                isSelected = selectedDate == day.date,
                                tasksForDay = dayTasks,
                                onClick = { onDateSelected(it) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    WeekCalendar(
                        state = weekState,
                        dayContent = { day ->
                            val dayTasks = tasks.filter {
                                val taskDate = Instant.ofEpochMilli(it.dueDate).atZone(PolandZone).toLocalDate()
                                taskDate == day.date
                            }
                            CalendarDay(
                                date = day.date,
                                isDateInMonth = true,
                                isSelected = selectedDate == day.date,
                                tasksForDay = dayTasks,
                                onClick = { onDateSelected(it) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedDate,
            transitionSpec = {
                val isNext = swipeDirection != DaySwipeDirection.PREVIOUS
                val enterOffset: (Int) -> Int = { fullWidth -> if (isNext) fullWidth else -fullWidth }
                val exitOffset: (Int) -> Int = { fullWidth -> if (isNext) -fullWidth else fullWidth }

                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = enterOffset
                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = exitOffset
                    ) + fadeOut(animationSpec = tween(300))
                )
            },
            label = "DayOnlyTransition",
            modifier = Modifier.fillMaxSize()
        ) { animatedDate ->
            val animatedClassesForDay = remember(classes, animatedDate) {
                classes.filter { it.date == animatedDate.toString() }.sortedBy { it.startTime }
            }

            DayTimelineContent(
                selectedDate = animatedDate,
                classesForDay = animatedClassesForDay,
                classColorMap = classColorMap,
                onClassClick = onClassClick,
                isTeacherPlan = isTeacherPlan,
                scrollState = scrollState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarFilterRow(
    availableDirections: List<String>,
    selectedDirections: Set<String>,
    onDirectionToggled: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableDirections) { direction ->
            val isSelected = selectedDirections.contains(direction)
            FilterChip(
                selected = isSelected,
                onClick = { onDirectionToggled(direction, !isSelected) },
                label = {
                    Text(
                        text = direction,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun DayTimelineContent(
    selectedDate: LocalDate,
    classesForDay: List<ClassEntity>,
    classColorMap: Map<String, Int>,
    onClassClick: (ClassEntity) -> Unit,
    isTeacherPlan: Boolean,
    scrollState: androidx.compose.foundation.ScrollState
) {
    if (classesForDay.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            com.example.my_uz_android.ui.components.EmptyStateMessage(
                title = "Brak zajęć",
                message = "W tym dniu nie masz zaplanowanych żadnych zajęć.",
                iconRes = R.drawable.calendar_rafiki
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    repeat(24) { index -> HourRow(hour = index) }
                }

                BoxWithConstraints(modifier = Modifier.matchParentSize().padding(start = 56.dp)) {
                    val eventLayouts = remember(classesForDay) {
                        com.example.my_uz_android.util.calculateEventLayouts(classesForDay)
                    }

                    eventLayouts.forEach { layoutInfo ->
                        ScheduledClassItem(
                            classEntity = layoutInfo.classEntity,
                            selectedDate = selectedDate,
                            classColorMap = classColorMap,
                            colIndex = layoutInfo.colIndex,
                            totalCols = layoutInfo.totalCols,
                            maxWidthDp = maxWidth,
                            onClassClick = onClassClick,
                            isTeacherPlan = isTeacherPlan
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

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f).height(24.dp),
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
    tasksForDay: List<TaskEntity> = emptyList(),
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

            if (tasksForDay.isNotEmpty() && isDateInMonth) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    tasksForDay.take(4).forEach { task ->
                        val dotColor = when (task.priority) {
                            2 -> MaterialTheme.colorScheme.error
                            1 -> Color(0xFFF57C00)
                            else -> Color(0xFF388E3C)
                        }

                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
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
    colIndex: Int,
    totalCols: Int,
    maxWidthDp: androidx.compose.ui.unit.Dp,
    onClassClick: (ClassEntity) -> Unit,
    isTeacherPlan: Boolean = false
) {
    val startParts = classEntity.startTime.split(":")
    val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
    val endParts = classEntity.endTime.split(":")
    val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
    val durationMinutes = endMinutes - startMinutes

    val topOffset = startMinutes.dp
    val height = durationMinutes.dp

    val columnWidth = maxWidthDp / totalCols
    val startXOffset = columnWidth * colIndex

    val now = LocalDateTime.now(PolandZone)
    val classStartDateTime = LocalDateTime.of(selectedDate, LocalTime.of(startParts[0].toInt(), startParts[1].toInt()))
    val isFuture = now.isBefore(classStartDateTime)

    val assignedColorIndex = classColorMap[classEntity.classType] ?: abs(classEntity.classType.hashCode()) % ClassColorPalette.size
    val colorSet = ClassColorPalette.getOrElse(assignedColorIndex) { ClassColorPalette[0] }

    Box(
        modifier = Modifier
            .width(columnWidth)
            .padding(bottom = 2.dp, end = 4.dp)
            .offset(x = startXOffset, y = topOffset)
            .height(height)
    ) {
        ClassCard(
            classItem = classEntity,
            type = ClassCardType.CALENDAR,
            backgroundColor = colorSet.lightBg,
            accentColor = colorSet.lightAccent,
            showBadge = isFuture,
            isTeacherPlan = isTeacherPlan,
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