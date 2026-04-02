// java/com/example/my_uz_android/ui/screens/index/IndexScreen.kt

package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
    onSubjectClick: (String) -> Unit,
    onAddGradeClick: () -> Unit,
    onAddAbsenceClick: () -> Unit,
    onEditAbsenceClick: (Int) -> Unit,
    onAddGradeSpecificClick: (String, String) -> Unit = { _, _ -> },
    onAddAbsenceSpecificClick: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    // Zielony komentarz:
    // Wspólny poziom VM zapewnia spójny filtr kierunków między zakładkami Oceny/Nieobecności.
    val gradesViewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val absencesViewModel: AbsencesViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val gradesState by gradesViewModel.uiState.collectAsStateWithLifecycle()

    var showFilterMenu by remember { mutableStateOf(false) }
    val multiplePlans = gradesState.userCourses.size > 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Indeks",
                navigationIcon = null,
                actions = {
                    if (multiplePlans) {
                        Box {
                            TopBarActionIcon(
                                icon = R.drawable.ic_filter,
                                // Zielony komentarz:
                                // Zgodnie z UX: ikona filtra ma zawsze mieć koło tła.
                                isFilled = true,
                                onClick = { showFilterMenu = true }
                            )

                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                gradesState.userCourses.forEach { course ->
                                    val normalizedCode = course.groupCode.trim().uppercase()
                                    val isSelected = gradesState.selectedGroupCodes.contains(normalizedCode)

                                    DropdownMenuItem(
                                        text = { Text(course.fieldOfStudy ?: course.groupCode) },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_check),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        onClick = {
                                            // Zielony komentarz:
                                            // Jeden klik synchronizuje filtr w obu VM,
                                            // żeby Oceny i Nieobecności pokazywały ten sam zestaw kierunków.
                                            gradesViewModel.toggleGroupVisibility(course.groupCode)
                                            absencesViewModel.toggleGroupVisibility(course.groupCode)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    TopBarActionIcon(
                        icon = R.drawable.ic_plus,
                        isFilled = true,
                        onClick = {
                            if (selectedTabIndex == 0) onAddGradeClick() else onAddAbsenceClick()
                        }
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (selectedTabIndex == 0) {
                GradesScreen(
                    viewModel = gradesViewModel,
                    onSubjectClick = onSubjectClick,
                    onAddGradeClick = onAddGradeSpecificClick
                )
            } else {
                AbsencesScreen(
                    viewModel = absencesViewModel,
                    onAddAbsenceClick = onAddAbsenceSpecificClick,
                    onEditAbsenceClick = onEditAbsenceClick
                )
            }
        }
    }
}

@Composable
private fun FilterTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Oceny", "Nieobecności").forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(index) }
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                        } else {
                            Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}