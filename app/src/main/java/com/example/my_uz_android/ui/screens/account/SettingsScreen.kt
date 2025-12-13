package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.my_uz_android.ui.screens.account.components.ThemeSelector
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.util.ClassTypeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val classTypes = uiState.uniqueClassTypes

    // 1. Definicja palety kolorów z Twoich plików (Color.kt via Theme.kt)
    // Kolejność determinuje jaki kolor dostanie kolejny typ zajęć
    val extendedColors = MaterialTheme.extendedColors
    val palette = listOf(
        MaterialTheme.colorScheme.primary,      // Fioletowy
        extendedColors.customGreen,             // Zielony
        extendedColors.customOrange,            // Pomarańczowy
        extendedColors.customBlue,              // Niebieski
        extendedColors.customRed,               // Czerwony
        MaterialTheme.colorScheme.tertiary,     // Różowy/Tertiary
        MaterialTheme.colorScheme.secondary     // Szary/Secondary
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ustawienia",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_left),
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // --- SEKCJA: MOTYW ---
            item {
                SettingsSection(title = "Motyw") {
                    // Tryb ciemny
                    SettingsSwitchRow(
                        title = "Tryb ciemny",
                        subtitle = "Włącz ciemny motyw aplikacji",
                        isChecked = settings?.isDarkMode ?: false,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Motyw kolorystyczny
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Motyw kolorystyczny",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        // Przekazujemy paletę kolorów z motywu do selektora
                        ThemeSelector(
                            colors = palette.take(6), // Bierzemy pierwsze 6 jako główne motywy
                            selectedColor = null, // TODO: Podpiąć pod DB w przyszłości
                            onColorSelected = { /* TODO */ }
                        )
                    }
                }
            }

            // --- SEKCJA: KOLORY TYPÓW ZAJĘĆ (DYNAMICZNA) ---
            item {
                SettingsSection(title = "Kolory typów zajęć") {
                    if (classTypes.isEmpty()) {
                        Text(
                            text = "Brak zajęć w planie. Pobierz plan, aby zobaczyć typy zajęć.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Iterujemy po typach i dynamicznie przypisujemy kolor
                        classTypes.forEachIndexed { index, typeAbbr ->
                            val color = palette[index % palette.size] // Cykliczne dobieranie koloru
                            val fullName = ClassTypeUtils.getFullName(typeAbbr)

                            ClassColorRow(
                                name = fullName.ifBlank { typeAbbr },
                                color = color
                            )
                        }
                    }
                }
            }

            // --- SEKCJA: POWIADOMIENIA ---
            item {
                SettingsSection(title = "Powiadomienia") {
                    SettingsSwitchRow(
                        title = "Zbliżające się zajęcia",
                        subtitle = "15 min. przed rozpoczęciem",
                        isChecked = settings?.notificationsEnabled ?: true,
                        onCheckedChange = {
                            settings?.let { current ->
                                viewModel.updateSettings(current.copy(notificationsEnabled = it))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchRow(
                        title = "Zbliżające się zadanie",
                        subtitle = "Dzień przed terminem",
                        isChecked = true, // Placeholder
                        onCheckedChange = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary, // Kolor z motywu
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
fun ClassColorRow(
    name: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    }
}