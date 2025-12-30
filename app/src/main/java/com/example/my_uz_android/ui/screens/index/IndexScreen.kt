package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
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
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                IndexTabs(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        floatingActionButton = {
            // Prosty FAB z plusem
            UniversalFab(
                isExpandable = false,
                isExpanded = false,
                iconRes = R.drawable.ic_plus,
                onMainFabClick = {
                    when (selectedTab) {
                        0 -> onAddGradeClick(null, null)
                        1 -> onAddAbsenceClick(null, null)
                    }
                },
                options = emptyList()
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