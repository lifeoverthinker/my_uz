package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.theme.MyUZTheme

@Composable
fun PersonalDataScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val userName by viewModel.userName.collectAsState()
    val userSurname by viewModel.userSurname.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedSubgroups by viewModel.mainSelectedSubgroups.collectAsState()
    val additionalCourses by viewModel.additionalUserCourses.collectAsState()

    PersonalDataScreenContent(
        userName = userName,
        userSurname = userSurname,
        selectedGender = selectedGender,
        selectedGroup = selectedGroup,
        selectedSubgroups = selectedSubgroups,
        additionalCourses = additionalCourses,
        onNavigateBack = onNavigateBack,
        onNavigateToEdit = onNavigateToEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonalDataScreenContent(
    userName: String,
    userSurname: String,
    selectedGender: UserGender?,
    selectedGroup: String?,
    selectedSubgroups: Set<String>,
    additionalCourses: List<UserCourseEntity>,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val sortedSubgroups = selectedSubgroups.toList().sorted()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "Dane osobowe",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onNavigateBack,
                isNavigationIconFilled = true,
                actions = {
                    TopBarActionIcon(
                        icon = R.drawable.ic_edit,
                        onClick = onNavigateToEdit,
                        isFilled = true,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PersonalDataGroup(title = "Profil studenta") {
                DataItem(label = "Imię i nazwisko", value = "$userName $userSurname")
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DataItem(
                    label = "Płeć / Forma zwrotu",
                    value = selectedGender?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                        ?: "Student"
                )
            }

            PersonalDataGroup(title = "Zapisane grupy") {
                DataItem(label = "Grupa główna", value = selectedGroup ?: "Brak przypisanej grupy")

                if (sortedSubgroups.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Wybrane podgrupy",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sortedSubgroups.forEach { subgroup ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = subgroup,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                if (additionalCourses.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Grupy dodatkowe",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        additionalCourses.forEachIndexed { index, course ->
                            Column(modifier = Modifier.padding(bottom = if (index < additionalCourses.size - 1) 16.dp else 0.dp)) {
                                Text(
                                    text = course.groupCode,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = course.fieldOfStudy ?: "Brak danych",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                val subgroups =
                                    course.selectedSubgroup?.split(",")?.map { it.trim() }
                                        ?.filter { it.isNotBlank() }?.sorted()
                                if (!subgroups.isNullOrEmpty()) {
                                    Text(
                                        text = "Podgrupy: ${subgroups.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalDataGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        OutlinedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth(),
            content = { Column(content = content) }
        )
    }
}

@Composable
fun DataItem(label: String, value: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalDataScreenPreview() {
    MyUZTheme {
        PersonalDataScreenContent(
            userName = "Jan",
            userSurname = "Kowalski",
            selectedGender = UserGender.STUDENT,
            selectedGroup = "11-INF-ZI-S",
            selectedSubgroups = setOf("L1", "C2"),
            additionalCourses = listOf(
                UserCourseEntity(
                    id = 1,
                    groupCode = "12-MAT-S",
                    fieldOfStudy = "Matematyka stosowana",
                    selectedSubgroup = "L1,W"
                ),
                UserCourseEntity(
                    id = 2,
                    groupCode = "14-FIZ-N",
                    fieldOfStudy = "Fizyka techniczna",
                    selectedSubgroup = ""
                )
            ),
            onNavigateBack = {},
            onNavigateToEdit = {}
        )
    }
}
