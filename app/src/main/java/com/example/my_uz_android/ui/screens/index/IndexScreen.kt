package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
import com.example.my_uz_android.ui.theme.MyUZTheme
import kotlinx.coroutines.launch

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
                },
                onAddClick = {
                    if (pagerState.currentPage == 0) {
                        onAddGradeClick(null, null)
                    } else {
                        onAddAbsenceClick(null, null)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // UX: Tytuł zawsze brzmi "Indeks".
    // Jeśli student ma więcej niż 1 kierunek, pokazujemy mu wybrany jako podtytuł (Subtitle)
    val subtitleText = if (userCourses.size > 1) {
        if (selectedGroupCodes.size == 1) {
            val selectedCourse = userCourses.find { it.groupCode == selectedGroupCodes.first() }
            selectedCourse?.fieldOfStudy ?: selectedCourse?.groupCode ?: "Wszystkie"
        } else if (selectedGroupCodes.isNotEmpty()) {
            "Wiele kierunków"
        } else {
            "Brak wybranych"
        }
    } else null // Dla 1 kierunku nie zaśmiecamy paska podtytułem

    TopAppBar(
        title = "Indeks",
        subtitle = subtitleText,
        navigationIcon = null, // Brak ikony powrotu na ekranie głównym
        isCenterAligned = false,
        actions = {
            // --- 1. PRZYCISK FILTROWANIA KIERUNKÓW (Po lewej stronie plusa) ---
            if (userCourses.size > 1) {
                Box {
                    TopBarActionIcon(
                        icon = R.drawable.ic_filter,
                        onClick = { isFilterExpanded = true }
                    )

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
                                            painter = painterResource(id = R.drawable.ic_check_square_broken),
                                            contentDescription = "Wybrane",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
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

            // --- 2. PRZYCISK DODAWANIA (Po prawej) ---
            TopBarActionIcon(
                icon = R.drawable.ic_plus,
                onClick = onAddClick
            )
        },
        // --- ZAKŁADKI INDEKSU WPIĘTE W TOP BAR ---
        bottomContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IndexTabs(
                    selectedTabIndex = currentTab,
                    onTabSelected = onTabSelected
                )
            }
        }
    )
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
            onToggleGroupVisibility = {},
            onAddClick = {}
        )
    }
}