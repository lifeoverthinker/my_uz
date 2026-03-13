package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.MyUZTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onPersonalDataClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val userName by viewModel.userName.collectAsState()
    val userSurname by viewModel.userSurname.collectAsState()
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()

    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedSubgroups by viewModel.selectedSubgroups.collectAsState()
    val faculty by viewModel.faculty.collectAsState()
    val fieldOfStudy by viewModel.fieldOfStudy.collectAsState()
    val studyMode by viewModel.studyMode.collectAsState()
    val availableDirections by viewModel.availableDirections.collectAsState()
    val activeDirection by viewModel.activeDirection.collectAsState()
    val directionToFieldMap by viewModel.directionToFieldMap.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    AccountScreenContent(
        userName = userName,
        userSurname = userSurname,
        isAnonymous = isAnonymous,
        selectedGender = selectedGender,
        selectedGroup = selectedGroup,
        selectedSubgroups = selectedSubgroups,
        faculty = faculty,
        fieldOfStudy = fieldOfStudy,
        studyMode = studyMode,
        availableDirections = availableDirections,
        activeDirection = activeDirection,
        directionToFieldMap = directionToFieldMap,
        isLoading = isLoading,
        onSettingsClick = onSettingsClick,
        onPersonalDataClick = onPersonalDataClick,
        onAboutClick = onAboutClick,
        onDirectionSelected = viewModel::setActiveDirection
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreenContent(
    userName: String,
    userSurname: String,
    isAnonymous: Boolean,
    selectedGender: UserGender?,
    selectedGroup: String?,
    selectedSubgroups: Set<String>,
    faculty: String,
    fieldOfStudy: String,
    studyMode: String,
    availableDirections: List<String>,
    activeDirection: String?,
    directionToFieldMap: Map<String, String>,
    isLoading: Boolean,
    onSettingsClick: () -> Unit,
    onPersonalDataClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDirectionSelected: (String) -> Unit
) {
    if (isLoading && userName.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "Konto",
                navigationIcon = null,
                isCenterAligned = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileSection(
                userName = "$userName $userSurname".trim(),
                userTitle = selectedGender?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                    ?: "Student",
                isAnonymous = isAnonymous
            )

            StudyDirectionsSection(
                fieldOfStudy = fieldOfStudy.ifBlank { "Brak danych" },
                faculty = faculty.ifBlank { "Brak danych" },
                mode = studyMode.ifBlank { "-" },
                subgroups = selectedSubgroups,
                fallbackGroup = selectedGroup,
                availableDirections = availableDirections,
                activeDirection = activeDirection,
                directionToFieldMap = directionToFieldMap,
                isAnonymous = isAnonymous,
                onDirectionSelected = onDirectionSelected
            )

            // ZARZĄDZANIE KONTEM - Spójne wypełnione karty (Filled Cards)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionTitle(text = "Zarządzanie kontem")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        AccountOptionItem(
                            iconRes = R.drawable.ic_user,
                            label = "Dane osobowe",
                            onClick = onPersonalDataClick
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        AccountOptionItem(
                            iconRes = R.drawable.ic_settings,
                            label = "Ustawienia",
                            onClick = onSettingsClick
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        AccountOptionItem(
                            iconRes = R.drawable.ic_info_circle,
                            label = "O aplikacji",
                            onClick = onAboutClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StudyDirectionsSection(
    fieldOfStudy: String,
    faculty: String,
    mode: String,
    subgroups: Set<String>,
    fallbackGroup: String?,
    availableDirections: List<String>,
    activeDirection: String?,
    directionToFieldMap: Map<String, String>,
    isAnonymous: Boolean,
    onDirectionSelected: (String) -> Unit
) {
    val fallbackDirections = if (availableDirections.isNotEmpty()) {
        availableDirections
    } else {
        listOfNotNull(fallbackGroup).filter { it.isNotBlank() }
    }

    val selectedDirection = activeDirection
        ?.takeIf { fallbackDirections.contains(it) }
        ?: fallbackDirections.firstOrNull()
        ?: "-"

    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(text = "Dane studiów")

            if (!isAnonymous && fallbackDirections.size > 1) {
                Box {
                    AssistChip(
                        onClick = { isDropdownExpanded = true },
                        label = { Text(selectedDirection) },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Zmień kierunek")
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp)
                    )

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        fallbackDirections.forEach { direction ->
                            val directionName = directionToFieldMap[direction] ?: fieldOfStudy
                            DropdownMenuItem(
                                text = { Text("$directionName | $direction") },
                                onClick = {
                                    onDirectionSelected(direction)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Oddychająca, wypelniona karta w stylu Classroom
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (isAnonymous) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uzupełnij profil, aby zobaczyć dane studiów.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "KIERUNEK",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = fieldOfStudy,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StudyTwoColumnItem(
                            label = "Tryb",
                            value = mode,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        StudyTwoColumnItem(
                            label = "Grupa",
                            value = selectedDirection,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    StudySingleColumnItem(
                        label = "Wydział",
                        value = faculty
                    )

                    StudySingleColumnItem(
                        label = "Podgrupy",
                        value = if (subgroups.isNotEmpty()) subgroups.toList().sorted()
                            .joinToString(", ") else "-"
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyTwoColumnItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StudySingleColumnItem(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AccountOptionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun ProfileSection(userName: String, userTitle: String, isAnonymous: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isAnonymous || userName.isBlank()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Gość",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = getInitials(userName),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isAnonymous) "Użytkownik Gość" else userName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAnonymous) "Konto gościa" else userTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

fun getInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> {
            val first = parts.first().take(1).uppercase()
            val last = parts.last().take(1).uppercase()
            "$first$last"
        }
    }
}

// ==========================================
// PREVIEW
// ==========================================
@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    MyUZTheme {
        AccountScreenContent(
            userName = "Jan",
            userSurname = "Kowalski",
            isAnonymous = false,
            selectedGender = UserGender.STUDENT,
            selectedGroup = "11-INF-ZI-S",
            selectedSubgroups = setOf("L1", "C2"),
            faculty = "Wydział Informatyki, Elektrotechniki i Automatyki",
            fieldOfStudy = "Informatyka",
            studyMode = "Stacjonarne",
            availableDirections = listOf("11-INF-ZI-S", "12-MAT-S"),
            activeDirection = "11-INF-ZI-S",
            directionToFieldMap = mapOf(
                "11-INF-ZI-S" to "Informatyka",
                "12-MAT-S" to "Matematyka"
            ),
            isLoading = false,
            onSettingsClick = {},
            onPersonalDataClick = {},
            onAboutClick = {},
            onDirectionSelected = {}
        )
    }
}