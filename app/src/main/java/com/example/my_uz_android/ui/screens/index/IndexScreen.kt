package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
import kotlinx.coroutines.launch

@Composable
fun IndexScreen(
    initialTab: Int = 0,
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    onAddGradeClick: (String?, String?) -> Unit,
    onAddAbsenceClick: (String?, String?) -> Unit,
    onEditAbsenceClick: (Int) -> Unit,
    calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialTab) { 2 }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            IndexTopBar(
                userCourses = uiState.userCourses,
                selectedGroupCodes = uiState.selectedGroupCodes,
                currentTab = pagerState.currentPage,
                onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                onToggleGroupVisibility = { calendarViewModel.toggleGroupVisibility(it) },
                onAddClick = {
                    if (pagerState.currentPage == 0) onAddGradeClick(null, null)
                    else onAddAbsenceClick(null, null)
                }
            )
        }
    ) { padding -> // POPRAWIONE: Klamra była źle zamknięta w poprzedniej wersji
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding),
            userScrollEnabled = true
        ) { page ->
            if (page == 0) {
                val gradesViewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                GradesScreen(
                    viewModel = gradesViewModel,
                    onGradeDetailsClick = onGradeDetailsClick,
                    onNavigateToClassTypeGrades = onNavigateToClassTypeGrades,
                    onAddGradeClick = onAddGradeClick
                )
            } else {
                val absencesViewModel: AbsencesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                AbsencesScreen(
                    viewModel = absencesViewModel,
                    onEditAbsenceClick = onEditAbsenceClick,
                    onAddAbsenceClick = onAddAbsenceClick
                )
            }
        }
    } // ZAMKNIĘCIE Scaffold
} // ZAMKNIĘCIE funkcji IndexScreen

@Composable
fun IndexTopBar(
    userCourses: List<UserCourseEntity>,
    selectedGroupCodes: Set<String>,
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onToggleGroupVisibility: (String) -> Unit,
    onAddClick: () -> Unit
) {
    var isFilterExpanded by remember { mutableStateOf(false) }

    val subtitleText = if (userCourses.size > 1 && selectedGroupCodes.size == 1) {
        userCourses.find { it.groupCode == selectedGroupCodes.first() }?.fieldOfStudy
    } else null

    TopAppBar(
        title = "Indeks",
        subtitle = subtitleText,
        navigationIcon = null,
        actions = {
            if (userCourses.size > 1) {
                Box {
                    TopBarActionIcon(
                        icon = R.drawable.ic_filter,
                        isFilled = true,
                        onClick = { isFilterExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isFilterExpanded,
                        onDismissRequest = { isFilterExpanded = false }
                    ) {
                        userCourses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.fieldOfStudy ?: course.groupCode) },
                                onClick = { onToggleGroupVisibility(course.groupCode) },
                                trailingIcon = {
                                    if (selectedGroupCodes.contains(course.groupCode))
                                        Icon(painterResource(R.drawable.ic_check_square_broken), null, Modifier.size(20.dp))
                                }
                            )
                        }
                    }
                }
            }
            TopBarActionIcon(
                icon = R.drawable.ic_plus,
                isFilled = true,
                onClick = onAddClick
            )
        },
        bottomContent = {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                IndexTabs(selectedTabIndex = currentTab, onTabSelected = onTabSelected)
            }
        }
    )
}