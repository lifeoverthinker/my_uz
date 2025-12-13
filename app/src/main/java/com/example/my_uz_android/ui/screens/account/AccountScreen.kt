package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import java.util.Locale

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onPersonalDataClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.settings.collectAsState()
    val isLoaded by viewModel.isSettingsLoaded.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    if (!isLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            // Nagłówek "Konto" zgodnie z Figmą
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Konto",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal, // Figma: W400
                        fontSize = 24.sp
                    ),
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
            // 1. Profil (Avatar + Imię)
            ProfileSection(
                userName = settings?.userName ?: "Student",
                subtitle = if (settings?.isAnonymous == true) "Gość" else "Student"
            )

            // 2. Dane studiów
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(text = "Dane studiów")

                // Lista kart kierunków
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StudyCard(
                            fieldOfStudy = settings?.fieldOfStudy ?: "Brak kierunku",
                            faculty = settings?.faculty ?: "Brak wydziału",
                            group = settings?.selectedGroupCode ?: "-",
                            subgroup = settings?.selectedSubgroup?.takeIf { it.isNotBlank() } ?: "-",
                            mode = settings?.studyMode ?: "-"
                        )
                    }
                }
            }

            // 3. Zarządzanie kontem
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Zarządzanie kontem")

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

// --- Komponenty ---

@Composable
fun ProfileSection(userName: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary), // Figma: 0xFF68548E
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getInitials(userName),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onPrimary // Biały
                )
            )
        }

        // Tekst
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium, // Figma: W500
                    fontSize = 16.sp,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.onBackground // Figma: 0xFF1D1B20
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium, // Figma: W500
                    fontSize = 14.sp,
                    letterSpacing = 0.1.sp,
                    color = MaterialTheme.colorScheme.outline // Figma: 0xFF7A757F
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
    subgroup: String,
    mode: String
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.classCardBackground // Figma: 0xFFE9DEF8
        ),
        modifier = Modifier.width(290.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nazwa kierunku
            Text(
                text = fieldOfStudy,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = 0.1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            // Szczegóły
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StudyDetailRow(label = "Wydział", value = faculty)
                StudyDetailRow(label = "Grupa", value = group)
                StudyDetailRow(label = "Podgrupa", value = subgroup)
                StudyDetailRow(label = "Tryb studiów", value = mode)
            }
        }
    }
}

@Composable
fun StudyDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 0.4.sp,
                color = MaterialTheme.colorScheme.outline // Figma: 0xFF7A757F
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 0.4.sp,
                color = MaterialTheme.colorScheme.onBackground // Figma: 0xFF1D1B20
            ),
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium, // Figma: W500
            fontSize = 16.sp,
            letterSpacing = 0.15.sp,
            color = MaterialTheme.colorScheme.onBackground // Figma: Black
        )
    )
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal, // Figma: W400
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
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

// Zaktualizowana funkcja do inicjałów
fun getInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    return when {
        parts.isEmpty() -> ""
        // Jeśli jest tylko jedno słowo (np. samo imię), zwróć 1 literę
        parts.size == 1 -> parts[0].take(1).uppercase()
        // Jeśli jest więcej słów, zwróć pierwszą literę pierwszego i ostatniego słowa (Imię + Nazwisko)
        else -> {
            val first = parts.first().take(1).uppercase()
            val last = parts.last().take(1).uppercase()
            "$first$last"
        }
    }
}