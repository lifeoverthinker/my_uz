package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    initialTab: Int = 0, // DODANO: Obsługa startowej zakładki
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    onAddGradeClick: (String?, String?) -> Unit,
    onAddAbsenceClick: (String?, String?) -> Unit,
    onEditAbsenceClick: (Int) -> Unit
) {
    // Używamy PagerState, aby wspierać gesty przesuwania i synchronizację z initialTab
    val pagerState = rememberPagerState(
        initialPage = initialTab,
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Indeks",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
            // Dynamicznie dostosowujemy FAB do aktualnie widocznej zakładki w Pagerze
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
        // Użycie HorizontalPager zapewnia płynność i zachowanie stanu przy powrotach
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true // Pozwala przesuwać palcem między ocenami a nieobecnościami
        ) { page ->
            when (page) {
                0 -> GradesScreen(
                    onGradeDetailsClick = onGradeDetailsClick,
                    onNavigateToClassTypeGrades = onNavigateToClassTypeGrades,
                    onAddGradeClick = onAddGradeClick
                )
                1 -> {
                    val absencesViewModel: AbsencesViewModel = viewModel(factory = AppViewModelProvider.Factory)
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