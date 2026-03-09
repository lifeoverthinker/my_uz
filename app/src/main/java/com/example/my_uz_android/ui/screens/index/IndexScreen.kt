package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert // Używamy MoreVert (trzy kropki)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.FabOption
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
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
    var isFabExpanded by remember { mutableStateOf(false) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indeks",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Menu widoczne tylko, gdy użytkownik ma więcej niż 1 kierunek
                    if (uiState.userCourses.size > 1) {
                        Box {
                            IconButton(onClick = { isFilterExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Filtruj kierunki",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            DropdownMenu(
                                expanded = isFilterExpanded,
                                onDismissRequest = { isFilterExpanded = false }
                            ) {
                                uiState.userCourses.forEach { course ->
                                    val isSelected = uiState.selectedGroupCodes.contains(course.groupCode)
                                    DropdownMenuItem(
                                        text = { Text(course.fieldOfStudy ?: course.groupCode) },
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
                                            gradesViewModel.toggleGroupVisibility(course.groupCode)
                                            absencesViewModel.toggleGroupVisibility(course.groupCode)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                IndexTabs(
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
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