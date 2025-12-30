package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.FabOption
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.index.components.IndexTabs

@Composable
fun IndexScreen(
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    onAddGradeClick: (String?, String?) -> Unit,
    onAddAbsenceClick: (String?, String?) -> Unit,
    onEditAbsenceClick: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
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
                    // ✅ POPRAWKA: Ujednolicenie z ekranem Konta (było headlineLarge)
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                IndexTabs(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        floatingActionButton = {
            val fabOptions = when (selectedTab) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
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