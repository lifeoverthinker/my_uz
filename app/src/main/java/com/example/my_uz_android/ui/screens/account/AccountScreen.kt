package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.extendedColors

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

    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading && userName.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Konto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
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
                isAnonymous = isAnonymous,
                onDirectionSelected = viewModel::setActiveDirection
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Zarzadzanie kontem")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountOptionItem(
                        iconRes = R.drawable.ic_user,
                        label = "Dane osobowe",
                        onClick = onPersonalDataClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    AccountOptionItem(
                        iconRes = R.drawable.ic_settings,
                        label = "Ustawienia",
                        onClick = onSettingsClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
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
    isAnonymous: Boolean,
    onDirectionSelected: (String) -> Unit
) {
    val contentColor = Color(0xFF1D192B)
    val labelColor = Color(0xFF494949)

    val fallbackDirections = if (availableDirections.isNotEmpty()) {
        availableDirections
    } else {
        listOfNotNull(fallbackGroup).filter { it.isNotBlank() }
    }

    val selectedDirection = activeDirection
        ?.takeIf { fallbackDirections.contains(it) }
        ?: fallbackDirections.firstOrNull()
        ?: "-"

    val chipScroll = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dane studiow",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!isAnonymous) {
                Text(
                    text = "Liczba kierunkow: ${fallbackDirections.size.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (!isAnonymous && fallbackDirections.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fallbackDirections.forEach { direction ->
                    FilterChip(
                        selected = direction == selectedDirection,
                        onClick = { onDirectionSelected(direction) },
                        label = {
                            Text(
                                text = "$fieldOfStudy | $direction",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.extendedColors.classCardBackground,
                            selectedLabelColor = contentColor
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.extendedColors.classCardBackground
            )
        ) {
            if (isAnonymous) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = labelColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uzupelnij profil, aby zobaczyc dane studiow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelColor
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "KIERUNEK",
                        style = MaterialTheme.typography.labelLarge,
                        color = labelColor
                    )
                    Text(
                        text = fieldOfStudy,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = contentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StudyTwoColumnItem(
                            label = "Tryb",
                            value = mode,
                            modifier = Modifier.weight(1f),
                            labelColor = labelColor,
                            valueColor = contentColor
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        StudyTwoColumnItem(
                            label = "Grupa",
                            value = selectedDirection,
                            modifier = Modifier.weight(1f),
                            labelColor = labelColor,
                            valueColor = contentColor
                        )
                    }

                    StudySingleColumnItem(
                        label = "Wydzial",
                        value = faculty,
                        labelColor = labelColor,
                        valueColor = contentColor
                    )
                    StudySingleColumnItem(
                        label = "Podgrupy",
                        value = if (subgroups.isNotEmpty()) subgroups.sorted().joinToString(", ") else "-",
                        labelColor = labelColor,
                        valueColor = contentColor
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
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

@Composable
private fun StudySingleColumnItem(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

@Composable
fun AccountOptionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
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
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (isAnonymous || userName.isBlank()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Gosc",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(
                    text = getInitials(userName),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isAnonymous) "Uzytkownik Gosc" else userName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAnonymous) "Konto goscia" else userTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onBackground
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
