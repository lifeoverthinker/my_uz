package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.calendar.components.ScheduleView
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private val ColorTitle = Color(0xff1d1b20)
private val ColorSubtitle = Color(0xff49454f)
private val ColorIconDefault = Color(0xff49454f)
private val ColorHeartActive = Color(0xff6750a4)

@Composable
fun SchedulePreviewScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClassClick: (com.example.my_uz_android.data.models.ClassEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val classes by viewModel.networkClasses.collectAsState()
    val classColorMap = uiState.classColorMap

    val title = uiState.selectedPlanName
    val isFavorite = uiState.favorites.any { it.name == title }

    val type = when (val source = uiState.currentSource) {
        is ScheduleSource.Preview -> source.type
        is ScheduleSource.Favorite -> source.type
        else -> "group"
    }
    val isGroup = type == "group"

    // --- Stan kalendarza (niezależny dla podglądu) ---
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
    // ------------------------------------------------

    Scaffold(
        topBar = {
            if (isGroup) {
                GroupPreviewAppBar(
                    title = "Plan grupy",
                    subtitle = title,
                    isFavorite = isFavorite,
                    onBackClick = { navController.popBackStack() },
                    onFilterClick = { },
                    onFavoriteClick = { viewModel.toggleFavorite(title, "group") }
                )
            } else {
                TeacherPreviewAppBar(
                    title = "Plan nauczyciela",
                    subtitle = title,
                    isFavorite = isFavorite,
                    onBackClick = { navController.popBackStack() },
                    onInfoClick = { },
                    onFavoriteClick = { viewModel.toggleFavorite(title, "teacher") }
                )
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorHeartActive)
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
                classes = classes,
                classColorMap = classColorMap,
                onClassClick = onClassClick,
                modifier = Modifier.padding(innerPadding),
                showHeader = true // ✅ Pokazujemy wyrazisty nagłówek miesiąca
            )
        }
    }
}

// ... (Funkcje GroupPreviewAppBar i TeacherPreviewAppBar pozostają bez zmian z poprzedniego kroku)
@Composable
fun GroupPreviewAppBar(
    title: String,
    subtitle: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(48.dp).clickable(onClick = onBackClick), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Back", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontWeight = FontWeight(400), fontSize = 22.sp, lineHeight = 28.sp),
                text = title,
                color = ColorTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                text = subtitle,
                color = ColorSubtitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        Row(modifier = Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clickable(onClick = onFilterClick), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_filter_funnel), contentDescription = "Filtruj", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
            }
            Box(modifier = Modifier.size(48.dp).clickable(onClick = onFavoriteClick), contentAlignment = Alignment.Center) {
                if (isFavorite) {
                    Icon(painterResource(R.drawable.ic_heart_filled), contentDescription = "Ulubione", modifier = Modifier.size(24.dp), tint = ColorHeartActive)
                } else {
                    Icon(painterResource(R.drawable.ic_heart), contentDescription = "Dodaj do ulubionych", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
                }
            }
        }
    }
}

@Composable
fun TeacherPreviewAppBar(
    title: String,
    subtitle: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(48.dp).clickable(onClick = onBackClick), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Back", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontWeight = FontWeight(400), fontSize = 22.sp, lineHeight = 28.sp),
                text = title,
                color = ColorTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                text = subtitle,
                color = ColorSubtitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        Row(modifier = Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clickable(onClick = onInfoClick), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_info_circle), contentDescription = "Info", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
            }
            Box(modifier = Modifier.size(48.dp).clickable(onClick = onFavoriteClick), contentAlignment = Alignment.Center) {
                if (isFavorite) {
                    Icon(painterResource(R.drawable.ic_heart_filled), contentDescription = "Ulubione", modifier = Modifier.size(24.dp), tint = ColorHeartActive)
                } else {
                    Icon(painterResource(R.drawable.ic_heart), contentDescription = "Dodaj do ulubionych", modifier = Modifier.size(24.dp), tint = ColorIconDefault)
                }
            }
        }
    }
}