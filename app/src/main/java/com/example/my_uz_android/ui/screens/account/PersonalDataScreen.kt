package com.example.my_uz_android.ui.screens.account

/**
 * Ekran wyświetlający dane personalne i przypisane kierunki studiów.
 * Zaprojektowany zgodnie ze standardem Material Design 3 (karty, listy, wyraźna hierarchia).
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val hasMainGroup = !selectedGroup.isNullOrBlank()
    val sortedSubgroups = if (hasMainGroup) selectedSubgroups.toList().sorted() else emptyList()
    val hasAdditionalCourses = additionalCourses.isNotEmpty()
    val hasAnyStudyData = hasMainGroup || sortedSubgroups.isNotEmpty() || hasAdditionalCourses

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.personal_data_title),
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Text(
                text = stringResource(R.string.personal_data_student_profile),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("$userName $userSurname", style = MaterialTheme.typography.bodyLarge) },
                        overlineContent = { Text(stringResource(R.string.personal_data_name_surname)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_user),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = {
                            Text(
                                selectedGender?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: stringResource(R.string.default_user_title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        overlineContent = { Text(stringResource(R.string.personal_data_gender_title)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Text(
                text = stringResource(R.string.personal_data_saved_groups),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = MaterialTheme.shapes.large
            ) {
                if (!hasAnyStudyData) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.personal_data_no_data_msg), style = MaterialTheme.typography.bodyMedium) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info_circle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                } else {
                    if (hasMainGroup) {
                        ListItem(
                            headlineContent = { Text(selectedGroup.orEmpty(), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)) },
                            overlineContent = { Text(stringResource(R.string.personal_data_main_group)) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        if (sortedSubgroups.isNotEmpty()) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.personal_data_selected_subgroups),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sortedSubgroups.forEach { subgroup ->
                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text(subgroup) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                            border = null,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (hasAdditionalCourses) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.personal_data_additional_groups), style = MaterialTheme.typography.labelLarge) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        additionalCourses.forEachIndexed { index, course ->
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(course.groupCode, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(course.fieldOfStudy ?: stringResource(R.string.no_data), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                val subgroups = course.selectedSubgroup?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.sorted()
                                if (!subgroups.isNullOrEmpty()) {
                                    Text(
                                        text = stringResource(R.string.personal_data_subgroups_format, subgroups.joinToString(", ")),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            if (index < additionalCourses.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
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
                UserCourseEntity(id = 1, groupCode = "12-MAT-S", fieldOfStudy = "Matematyka stosowana", selectedSubgroup = "L1,W")
            ),
            onNavigateBack = {},
            onNavigateToEdit = {}
        )
    }
}