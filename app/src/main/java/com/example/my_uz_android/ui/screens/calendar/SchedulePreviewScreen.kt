package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.PreviewTopAppBar
import com.example.my_uz_android.ui.components.SubgroupFilterDialog
import com.example.my_uz_android.ui.components.TeacherInfoDialog
import com.example.my_uz_android.ui.screens.calendar.components.ScheduleView
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun SchedulePreviewScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (com.example.my_uz_android.data.models.ClassEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ZMIANA: Pobieramy zajęcia z uiState, a nie z nieistniejącego networkClasses
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
        classes.firstOrNull { !it.teacherEmail.isNullOrBlank() }
            ?: classes.firstOrNull { !it.teacherInstitute.isNullOrBlank() }
            ?: classes.firstOrNull { !it.teacherName.isNullOrBlank() }
    }

    var showTeacherInfo by remember { mutableStateOf(false) }
    var showSubgroupFilter by remember { mutableStateOf(false) }

    val availableSubgroups = remember(classes) {
        classes.map { it.subgroup ?: "" }.distinct().sorted()
    }

    var selectedSubgroups by remember(availableSubgroups) {
        mutableStateOf(availableSubgroups.toSet())
    }

    val filteredClasses = remember(classes, selectedSubgroups) {
        classes.filter { selectedSubgroups.contains(it.subgroup ?: "") }
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

    Scaffold(
        topBar = {
            PreviewTopAppBar(
                title = if (isTeacher) "Plan nauczyciela" else "Plan grupy",
                subtitle = if (isTeacher) (teacherData?.teacherName ?: planName) else planName,
                isFavorite = isFavorite,
                onBackClick = { navController.popBackStack() },
                onFavoriteClick = { viewModel.toggleFavorite(planName, if (isTeacher) "teacher" else "group") },
                actionIcon = if (isTeacher) R.drawable.ic_info_circle else R.drawable.ic_filter_funnel,
                onActionClick = {
                    if (isTeacher) showTeacherInfo = true else showSubgroupFilter = true
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    classes = filteredClasses,
                    tasks = emptyList(),
                    classColorMap = classColorMap,
                    onClassClick = onClassClick,
                    onTaskClick = {},
                    onToggleTaskCompletion = {},
                    onDeleteTask = {},
                    showHeader = true
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
                selectedSubgroups = selectedSubgroups,
                onDismiss = { showSubgroupFilter = false },
                onSelectionChange = { selectedSubgroups = it }
            )
        }
    }
}