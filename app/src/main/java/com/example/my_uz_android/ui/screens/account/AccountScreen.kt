package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
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

    val isLoading by viewModel.isLoading.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    if (isLoading && userName.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = backgroundColor,
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
                    color = textColor
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

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(text = "Dane studiów")
                StudyCard(
                    fieldOfStudy = fieldOfStudy.ifBlank { "Brak danych" },
                    faculty = faculty.ifBlank { "Brak danych" },
                    group = selectedGroup ?: "-",
                    subgroups = selectedSubgroups,
                    mode = studyMode.ifBlank { "-" },
                    isAnonymous = isAnonymous
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Zarządzanie kontem")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountOptionItem(
                        iconRes = R.drawable.ic_user,
                        label = "Dane osobowe",
                        onClick = onPersonalDataClick
                    )
                    AccountOptionItem(
                        iconRes = R.drawable.ic_settings,
                        label = "Ustawienia",
                        onClick = onSettingsClick
                    )
                    AccountOptionItem(
                        iconRes = R.drawable.ic_info_circle,
                        label = "O aplikacji",
                        onClick = onAboutClick,
                        showDivider = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AccountOptionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
    ) {
        Column {
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            if (showDivider) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
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
                    contentDescription = "Gość",
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
                text = if (isAnonymous) "Użytkownik Gość" else userName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAnonymous) "Konto gościa" else userTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun StudyCard(
    fieldOfStudy: String,
    faculty: String,
    group: String,
    subgroups: Set<String>,
    mode: String,
    isAnonymous: Boolean
) {
    // Wymuszenie ciemnych kolorów tekstu, ponieważ tło (classCardBackground) jest teraz jasne/pastelowe w Dark Mode
    val contentColor = Color(0xFF1D192B)
    val labelColor = Color(0xFF494949)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.classCardBackground
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAnonymous) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = labelColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zaloguj się lub uzupełnij dane, aby widzieć informacje o studiach.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelColor
                    )
                }
            } else {
                Text(
                    text = fieldOfStudy,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                )

                StudyDetailRow(label = "Wydział", value = faculty, labelColor = labelColor, valueColor = contentColor)
                StudyDetailRow(label = "Grupa", value = group, labelColor = labelColor, valueColor = contentColor)

                val subgroupsText = if (subgroups.isNotEmpty()) subgroups.sorted().joinToString(", ") else "-"
                StudyDetailRow(label = "Podgrupy", value = subgroupsText, labelColor = labelColor, valueColor = contentColor)

                StudyDetailRow(label = "Tryb studiów", value = mode, labelColor = labelColor, valueColor = contentColor)
            }
        }
    }
}

@Composable
fun StudyDetailRow(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.outline,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
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