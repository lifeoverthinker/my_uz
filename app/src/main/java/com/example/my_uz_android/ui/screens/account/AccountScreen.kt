package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily

@OptIn(ExperimentalLayoutApi::class) // ✅ Naprawa błędu FlowRow
@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoaded by viewModel.isSettingsLoaded.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val groupSearchQuery by viewModel.groupSearchQuery.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val draftSelectedGroup by viewModel.draftSelectedGroup.collectAsState()

    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val draftSubgroups by viewModel.draftSubgroups.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()

    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    if (!isLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Konto",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = textColor
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. DANE STUDENTA (Tylko do odczytu)
            item {
                SectionHeader("Twoje dane")
                if (settings?.isAnonymous == true) {
                    Text(
                        text = "Tryb anonimowy (Gość)",
                        color = secondaryTextColor,
                        fontFamily = InterFontFamily
                    )
                } else {
                    InfoRow("Imię", settings?.userName ?: "-")
                    InfoRow("Wydział", settings?.faculty ?: "-")
                    InfoRow("Kierunek", settings?.fieldOfStudy ?: "-")
                    InfoRow("Tryb", settings?.studyMode ?: "-")
                }
            }

            // 2. SEMESTR
            item {
                SectionHeader("Twój Semestr")
                Text(
                    text = "Wybierz aktualny semestr, aby poprawnie liczyć średnią w indeksie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ✅ NAPRAWA: Zamiana zakresu 1..7 na listę (1..7).toList()
                    items((1..7).toList()) { sem ->
                        val isSelected = uiState.currentSemester == sem
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSemester(sem) },
                            label = { Text("Semestr $sem") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // 3. USTAWIENIA GRUPY (Edytowalne)
            item {
                SectionHeader("Ustawienia planu")

                // Wyszukiwarka grup
                OutlinedTextField(
                    value = groupSearchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Szukaj grupy (np. 32INF)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Wyniki wyszukiwania
                if (filteredGroups.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            filteredGroups.forEach { code ->
                                Text(
                                    text = code,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectGroup(code) }
                                        .padding(16.dp),
                                    fontWeight = if (code == draftSelectedGroup) FontWeight.Bold else FontWeight.Normal,
                                    color = if (code == draftSelectedGroup) primaryColor else textColor
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                // Wybrana grupa
                if (draftSelectedGroup != null) {
                    Text(
                        text = "Wybrana grupa: $draftSelectedGroup",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Podgrupy
                if (availableSubgroups.isNotEmpty()) {
                    Text(
                        "Wybierz podgrupy:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    // ✅ NAPRAWA: FlowRow jest eksperymentalne, dodano @OptIn na górze funkcji
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSubgroups.forEach { sub ->
                            val isSelected = draftSubgroups.contains(sub)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleSubgroup(sub) },
                                label = { Text(sub) },
                                leadingIcon = if (isSelected) {
                                    { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Zapisz zmiany w planie")
                    }
                }

                if (saveMessage != null) {
                    Text(
                        text = saveMessage!!,
                        color = if (saveMessage!!.contains("Error") || saveMessage!!.contains("Błąd")) MaterialTheme.colorScheme.error else primaryColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 4. WYGLĄD
            item {
                SectionHeader("Wygląd")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ciemny motyw", color = textColor, fontFamily = InterFontFamily)
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            // 5. INNE AKCJE
            item {
                SectionHeader("Inne")
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(painterResource(R.drawable.ic_x_close), null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wyloguj się / Resetuj dane")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontFamily = InterFontFamily
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = InterFontFamily)
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium, fontFamily = InterFontFamily)
    }
}