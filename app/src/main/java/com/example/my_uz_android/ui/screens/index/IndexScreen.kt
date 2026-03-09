package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.FabOption
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
import com.example.my_uz_android.ui.theme.MyUZTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource

@Composable
fun IndexScreen(
    initialTab: Int = 0,
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    onAddGradeClick: (String?, String?) -> Unit,
    onAddAbsenceClick: (String?, String?) -> Unit,
    onEditAbsenceClick: (Int) -> Unit
) {
    val gradesViewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val absencesViewModel: AbsencesViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val uiState by gradesViewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = initialTab,
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IndexTopBar(
                userCourses = uiState.userCourses,
                selectedGroupCodes = uiState.selectedGroupCodes,
                currentTab = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                onToggleGroupVisibility = { groupCode ->
                    gradesViewModel.toggleGroupVisibility(groupCode)
                    absencesViewModel.toggleGroupVisibility(groupCode)
                }
            )
        },
        floatingActionButton = {
            val fabOptions = when (pagerState.currentPage) {
                0 -> listOf(
                    FabOption(
                        label = "Dodaj ocenę",
                        iconRes = R.drawable.ic_trophy,
                        onClick = {
                            isFabExpanded = false
                            onAddGradeClick(null, null)
                        }
                    )
                )

                1 -> listOf(
                    FabOption(
                        label = "Dodaj nieobecność",
                        iconRes = R.drawable.ic_calendar_minus,
                        onClick = {
                            isFabExpanded = false
                            onAddAbsenceClick(null, null)
                        }
                    )
                )

                else -> emptyList()
            }

            UniversalFab(
                isExpandable = fabOptions.size > 1,
                isExpanded = isFabExpanded,
                onMainFabClick = {
                    if (fabOptions.size > 1) {
                        isFabExpanded = !isFabExpanded
                    } else {
                        fabOptions.firstOrNull()?.onClick?.invoke()
                    }
                },
                options = fabOptions
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> GradesScreen(
                    viewModel = gradesViewModel,
                    onGradeDetailsClick = onGradeDetailsClick,
                    onNavigateToClassTypeGrades = onNavigateToClassTypeGrades,
                    onAddGradeClick = onAddGradeClick
                )

                1 -> {
                    AbsencesScreen(
                        viewModel = absencesViewModel,
                        onAddAbsenceClick = onAddAbsenceClick,
                        onEditAbsenceClick = onEditAbsenceClick
                    )
                }
            }
        }
    }
}

// Wydzielony TopBar, by móc używać na nim @Preview bez inicjowania ViewModeli
@Composable
fun IndexTopBar(
    userCourses: List<UserCourseEntity>,
    selectedGroupCodes: Set<String>,
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onToggleGroupVisibility: (String) -> Unit
) {
    var isFilterExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Indeks",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface // Poprawiony kontrast
            )

            if (userCourses.size > 1) {
                Box {
                    IconButton(onClick = { isFilterExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter_funnel), // <-- TWOJA IKONA
                            contentDescription = "Filtruj kierunki",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = isFilterExpanded,
                        onDismissRequest = { isFilterExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        userCourses.forEach { course ->
                            val isSelected = selectedGroupCodes.contains(course.groupCode)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = course.fieldOfStudy ?: course.groupCode,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Wybrane",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    onToggleGroupVisibility(course.groupCode)
                                }
                            )
                        }
                    }
                }
            }
        }

        IndexTabs(
            selectedTabIndex = currentTab,
            onTabSelected = onTabSelected
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndexTopBarPreview() {
    MyUZTheme {
        IndexTopBar(
            userCourses = listOf(
                UserCourseEntity(
                    id = 1,
                    groupCode = "12IN",
                    fieldOfStudy = "Informatyka",
                    semester = 1
                ),
                UserCourseEntity(
                    id = 2,
                    groupCode = "34MA",
                    fieldOfStudy = "Matematyka",
                    semester = 3
                )
            ),
            selectedGroupCodes = setOf("12IN"),
            currentTab = 0,
            onTabSelected = {},
            onToggleGroupVisibility = {}
        )
    }
}