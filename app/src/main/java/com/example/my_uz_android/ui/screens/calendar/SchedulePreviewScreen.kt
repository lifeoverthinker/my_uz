package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.PreviewTopAppBar
import com.example.my_uz_android.ui.components.SubgroupFilterDialog
import com.example.my_uz_android.ui.components.TeacherInfoDialog
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
import kotlin.math.abs

@Composable
fun SchedulePreviewScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (ClassEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val classes = uiState.visibleClasses
    val classColorMap = uiState.classColorMap
    val planName = uiState.selectedPlanName
    val isFavorite = uiState.favorites.any { it.name == planName }

    val type = when (val source = uiState.currentSource) {
        is ScheduleSource.Preview -> source.type
        is ScheduleSource.Favorite -> source.type
        else -> "group"
    }
    val isTeacher = type == "teacher"

    val teacherData = remember(classes) {
        classes.firstOrNull {
            !it.teacherEmail.isNullOrBlank() || !it.teacherInstitute.isNullOrBlank()
        } ?: classes.firstOrNull { it.teacherName?.isNotBlank() == true }
    }

    SchedulePreviewScreenContent(
        classes = classes,
        classColorMap = classColorMap,
        planName = planName,
        isFavorite = isFavorite,
        isTeacher = isTeacher,
        teacherData = teacherData,
        isLoading = uiState.isLoading,
        onBackClick = { navController.popBackStack() },
        onFavoriteClick = {
            viewModel.toggleFavorite(
                planName,
                if (isTeacher) "teacher" else "group"
            )
        },
        onClassClick = { classEntity ->
            viewModel.setTemporaryClassForDetails(classEntity)
            navController.navigate("class_details/-1?isTeacherPlan=$isTeacher")
        }
    )
}

@Composable
fun SchedulePreviewScreenContent(
    classes: List<ClassEntity>,
    classColorMap: Map<String, Int>,
    planName: String,
    isFavorite: Boolean,
    isTeacher: Boolean,
    teacherData: ClassEntity?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onClassClick: (ClassEntity) -> Unit
) {
    var showTeacherInfo by remember { mutableStateOf(false) }
    var showSubgroupFilter by remember { mutableStateOf(false) }

    val availableSubgroups = remember(classes) {
        classes.map { it.subgroup ?: "" }.distinct().sorted()
    }

    // Fix filtrów: Inicjalizacja podgrup dopiero gdy dane faktycznie dotrą
    var selectedSubgroups by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(availableSubgroups) {
        if (selectedSubgroups.isEmpty() && availableSubgroups.isNotEmpty()) {
            selectedSubgroups = availableSubgroups.toSet()
        }
    }

    val filteredClasses = remember(classes, selectedSubgroups) {
        if (selectedSubgroups.isEmpty()) classes
        else classes.filter { selectedSubgroups.contains(it.subgroup ?: "") }
    }

    val currentDate = remember { LocalDate.now(ZoneId.of("Europe/Warsaw")) }
    val currentMonth = remember { YearMonth.now(ZoneId.of("Europe/Warsaw")) }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }
    val firstDayOfWeek = DayOfWeek.MONDAY

    var selectedDate by remember { mutableStateOf(currentDate) }
    var isMonthView by remember { mutableStateOf(false) }
    var swipeDirection by remember { mutableStateOf<DaySwipeDirection?>(null) }

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
    val density = LocalDensity.current

    fun navigateDay(offsetDays: Long, direction: DaySwipeDirection) {
        val newDate = selectedDate.plusDays(offsetDays)
        if (newDate == selectedDate) return

        swipeDirection = direction
        selectedDate = newDate

        scope.launch {
            if (isMonthView) {
                monthState.scrollToMonth(YearMonth.from(newDate))
            } else {
                weekState.scrollToWeek(newDate)
            }
        }
    }

    Scaffold(
        topBar = {
            PreviewTopAppBar(
                title = if (isTeacher) "Plan nauczyciela" else "Plan grupy",
                subtitle = if (isTeacher) (teacherData?.teacherName ?: planName) else planName,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onFavoriteClick = onFavoriteClick,
                actionIcon = if (isTeacher) R.drawable.ic_info_circle else R.drawable.ic_filter,
                onActionClick = {
                    if (isTeacher) showTeacherInfo = true else showSubgroupFilter = true
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Crossfade(
            targetState = isLoading,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
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
                },
            label = "ScheduleLoadingTransition"
        ) { loading ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else if (classes.isEmpty()) {
                // Tymczasowy, bezpieczny "Empty State" bez użycia zewnętrznego komponentu
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info_circle),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Brak zajęć",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "W wybranym planie nie znaleziono żadnych zaplanowanych zajęć.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                ScheduleView(
                    calendarState = monthState,
                    weekState = weekState,
                    selectedDate = selectedDate,
                    isMonthView = isMonthView,
                    swipeDirection = swipeDirection,
                    onDateSelected = { date ->
                        swipeDirection = null
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
                    classes = filteredClasses,
                    tasks = emptyList(),
                    classColorMap = classColorMap,
                    onClassClick = onClassClick,
                    isTeacherPlan = isTeacher, // Przekazujemy flagę do widoku planu
                    showHeader = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showTeacherInfo) {
        TeacherInfoDialog(
            onDismiss = { showTeacherInfo = false },
            fullName = teacherData?.teacherName ?: planName,
            department = teacherData?.teacherInstitute ?: "Brak informacji o jednostce",
            email = teacherData?.teacherEmail ?: "Brak adresu e-mail"
        )
    }

    if (showSubgroupFilter) {
        SubgroupFilterDialog(
            subgroups = availableSubgroups,
            selectedSubgroups = selectedSubgroups,
            onDismiss = { showSubgroupFilter = false },
            onSelectionChange = { selectedSubgroups = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulePreviewScreenPreview() {
    MyUZTheme {
        SchedulePreviewScreenContent(
            classes = listOf(
                ClassEntity(
                    id = 1,
                    subjectName = "Programowanie Obiektowe",
                    classType = "Wykład",
                    teacherName = "Dr inż. Jan Kowalski",
                    room = "A-2 101",
                    startTime = "08:15",
                    endTime = "09:45",
                    date = LocalDate.now().toString(),
                    dayOfWeek = 1,
                    groupCode = "12IN",
                    subgroup = "L1"
                )
            ),
            classColorMap = emptyMap(),
            planName = "Dr inż. Jan Kowalski",
            isFavorite = false,
            isTeacher = true,
            teacherData = null,
            isLoading = false,
            onBackClick = {},
            onFavoriteClick = {},
            onClassClick = {}
        )
    }
}