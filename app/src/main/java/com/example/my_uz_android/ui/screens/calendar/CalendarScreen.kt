package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.CalendarTopAppBar
import com.example.my_uz_android.ui.components.PreviewTopAppBar
import com.example.my_uz_android.ui.components.SubgroupFilterDialog
import com.example.my_uz_android.ui.components.TeacherInfoDialog
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

    // Poprawka: użycie parametru 'initial' oraz jawne typowanie listy zadań
    val tasks: List<TaskEntity> by viewModel.tasks.collectAsState(initial = emptyList())

    val source = uiState.currentSource
    val isMyPlan = source is ScheduleSource.MyPlan

    val rawClasses by produceState(initialValue = emptyList<ClassEntity>(), key1 = source) {
        if (isMyPlan) {
            viewModel.myPlanClasses.collect { value = it }
        } else {
            viewModel.networkClasses.collect { value = it }
        }
    }

    val planName = uiState.selectedPlanName
    val isTeacher = if (!isMyPlan) {
        (source as? ScheduleSource.Favorite)?.type == "teacher" || (source as? ScheduleSource.Preview)?.type == "teacher"
    } else false

    val isFavorite = if (!isMyPlan) uiState.favorites.any { it.name == planName } else false

    val teacherData = remember(rawClasses) {
        if (isTeacher) {
            rawClasses.firstOrNull { !it.teacherEmail.isNullOrBlank() }
                ?: rawClasses.firstOrNull { !it.teacherInstitute.isNullOrBlank() }
                ?: rawClasses.firstOrNull { !it.teacherName.isNullOrBlank() }
        } else null
    }

    val availableSubgroups = remember(rawClasses, isMyPlan) {
        if (!isMyPlan && !isTeacher) rawClasses.map { it.subgroup ?: "" }.distinct().sorted() else emptyList()
    }

    var selectedSubgroups by remember(planName) { mutableStateOf<Set<String>?>(null) }
    val currentSelectedSubgroups = selectedSubgroups ?: availableSubgroups.toSet()

    val displayedClasses = remember(rawClasses, isMyPlan, isTeacher, currentSelectedSubgroups) {
        if (isMyPlan || isTeacher) {
            rawClasses
        } else {
            rawClasses.filter { currentSelectedSubgroups.contains(it.subgroup ?: "") }
        }
    }

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

    val calendarTitle = if (visibleMonth.year == YearMonth.now().year) monthName else "$monthName ${visibleMonth.year}"

    var showTeacherInfo by remember { mutableStateOf(false) }
    var showSubgroupFilter by remember { mutableStateOf(false) }

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
                    scope.launch { drawerState.close() }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (isMyPlan) {
                    CalendarTopAppBar(
                        title = calendarTitle,
                        isExpanded = isMonthView,
                        onNavigationClick = { scope.launch { drawerState.open() } },
                        onSearchClick = onSearchClick,
                        onAddClick = {
                            val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
                            selectedDate = today
                            scope.launch {
                                if (isMonthView) monthState.animateScrollToMonth(YearMonth.from(today))
                                else weekState.animateScrollToWeek(today)
                            }
                        },
                        onTitleClick = {
                            isMonthView = !isMonthView
                            scope.launch {
                                if (isMonthView) monthState.scrollToMonth(YearMonth.from(selectedDate))
                                else weekState.scrollToWeek(selectedDate)
                            }
                        }
                    )
                } else {
                    PreviewTopAppBar(
                        title = if (isTeacher) "Plan nauczyciela" else "Plan grupy",
                        subtitle = if (isTeacher) (teacherData?.teacherName ?: planName) else planName,
                        isFavorite = isFavorite,
                        onBackClick = { viewModel.selectMyPlan() },
                        onFavoriteClick = { viewModel.toggleFavorite(planName, if (isTeacher) "teacher" else "group") },
                        actionIcon = if (isTeacher) R.drawable.ic_info_circle else R.drawable.ic_filter_funnel,
                        onActionClick = {
                            if (isTeacher) showTeacherInfo = true else showSubgroupFilter = true
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
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
                    classes = displayedClasses,
                    tasks = tasks,
                    classColorMap = uiState.classColorMap,
                    onClassClick = onClassClick,
                    onTaskClick = { task -> onTasksClick() },
                    onToggleTaskCompletion = { task -> viewModel.toggleTaskCompletion(task) },
                    modifier = Modifier.padding(innerPadding),
                    showHeader = !isMyPlan
                )
            }
        }

        if (showTeacherInfo) {
            TeacherInfoDialog(
                onDismiss = { showTeacherInfo = false },
                fullName = teacherData?.teacherName ?: planName,
                department = teacherData?.teacherInstitute ?: "",
                email = teacherData?.teacherEmail ?: ""
            )
        }

        if (showSubgroupFilter) {
            SubgroupFilterDialog(
                subgroups = availableSubgroups,
                selectedSubgroups = currentSelectedSubgroups,
                onDismiss = { showSubgroupFilter = false },
                onSelectionChange = {
                    selectedSubgroups = it
                }
            )
        }
    }
}