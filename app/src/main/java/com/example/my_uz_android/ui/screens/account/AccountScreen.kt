package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
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
                isCenterAligned = false,
                isNavigationIconFilled = true
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
                userTitle = selectedGender?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Student",
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

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionTitle(text = "Zarządzanie kontem")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    AccountOptionItem(
                        iconRes = R.drawable.ic_user,
                        label = "Dane osobowe",
                        onClick = onPersonalDataClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    AccountOptionItem(
                        iconRes = R.drawable.ic_settings,
                        label = "Ustawienia",
                        onClick = onSettingsClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    AccountOptionItem(
                        iconRes = R.drawable.ic_info_circle,
                        label = "O aplikacji",
                        onClick = onAboutClick
                    )
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (isAnonymous) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Uzupełnij profil, aby zobaczyć dane studiów.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "KIERUNEK",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = fieldOfStudy,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StudyItem(label = "TRYB", value = mode, modifier = Modifier.weight(1f))
                        StudyItem(label = "GRUPA", value = selectedDirection, modifier = Modifier.weight(1f))
                    }

                    StudyItem(label = "WYDZIAŁ", value = faculty)

                    StudyItem(
                        label = "PODGRUPY",
                        value = if (subgroups.isNotEmpty()) subgroups.toList().sorted().joinToString(", ") else "-"
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AccountOptionItem(iconRes: Int, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = getInitials(userName),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isAnonymous) "Użytkownik Gość" else userName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAnonymous) "Konto gościa" else userTitle,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
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
