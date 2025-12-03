package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.settings.collectAsState()
    val availableGroups by viewModel.availableGroups.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()

    var groupExpanded by remember { mutableStateOf(false) }

    // Obliczanie wybranych podgrup do wyświetlenia
    val selectedSubgroupsList = remember(settings?.selectedSubgroup) {
        settings?.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ustawienia", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    // Opcjonalnie: Przycisk wstecz jeśli potrzebny w tym miejscu
                    // IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sekcja Wyglądu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tryb Ciemny", style = MaterialTheme.typography.titleMedium)
                        Text("Dostosuj wygląd aplikacji", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = settings?.isDarkMode == true,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            HorizontalDivider()

            Text("Konfiguracja Planu", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Wybór Grupy (Dynamiczny)
            ExposedDropdownMenuBox(
                expanded = groupExpanded,
                onExpandedChange = { groupExpanded = it }
            ) {
                OutlinedTextField(
                    value = settings?.selectedGroupCode ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupa Dziekańska") },
                    placeholder = { Text("Wybierz grupę") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = groupExpanded,
                    onDismissRequest = { groupExpanded = false }
                ) {
                    if (availableGroups.isEmpty()) {
                        DropdownMenuItem(text = { Text("Ładowanie...") }, onClick = { })
                    } else {
                        availableGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    viewModel.updateGroup(group)
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Wybór Podgrup (Dynamiczny)
            if (availableSubgroups.isNotEmpty() || selectedSubgroupsList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Podgrupy (np. laboratoria):", style = MaterialTheme.typography.labelLarge)

                    // Używamy FlowRow (dostępne w nowszym Compose) lub prostego Row z scrollowaniem
                    // Tutaj prosta wersja Row z zawijaniem (FlowRow wymaga eksperymentalnego API w starszych wersjach)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Jeśli dopiero pobraliśmy podgrupy, pokazujemy je.
                        // Jeśli nie, pokazujemy chociaż te zapisane (jako fallback)
                        val displayList = if(availableSubgroups.isNotEmpty()) availableSubgroups else selectedSubgroupsList

                        displayList.forEach { sub ->
                            FilterChip(
                                selected = selectedSubgroupsList.contains(sub),
                                onClick = { viewModel.toggleSubgroup(sub) },
                                label = { Text(sub) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Zresetuj dane i wyloguj")
            }
        }
    }
}