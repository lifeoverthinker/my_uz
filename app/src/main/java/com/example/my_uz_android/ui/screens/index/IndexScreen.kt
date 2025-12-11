package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.FabOption
import com.example.my_uz_android.ui.components.UniversalFab
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun IndexScreen(
    onGradeDetailsClick: (Int) -> Unit,
    onNavigateToClassTypeGrades: (String, String) -> Unit,
    // ✅ ZMIANA: Parametry dla dodawania oceny
    onAddGradeClick: (String?, String?) -> Unit,
    onAddAbsenceClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = "Indeks",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                IndexTabs(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        floatingActionButton = {
            // Opcje FAB (Główny przycisk na dole)
            // Tu wywołujemy onAddGradeClick(null, null), bo to dodawanie "ogólne"
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
                            onAddAbsenceClick()
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
                    // ✅ Przekazujemy callback dalej
                    onAddGradeClick = onAddGradeClick
                )
                1 -> AbsencesScreen()
            }
        }
    }
}

@Composable
private fun AbsencesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "Nieobecności - w przygotowaniu",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}