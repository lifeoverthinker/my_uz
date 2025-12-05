package com.example.my_uz_android.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current  // DODANE do Toast

    LaunchedEffect(Unit) {
        println("🟡🟡🟡 AccountScreen COMPOSED 🟡🟡🟡")
    }

    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.groupSearchQuery.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val selectedDraftSubgroups by viewModel.draftSubgroups.collectAsState()
    val draftGroup by viewModel.draftSelectedGroup.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()

    var groupExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ustawienia", style = MaterialTheme.typography.titleLarge) }
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

            Text(
                "Konfiguracja Planu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Wyszukiwarka grupy
            ExposedDropdownMenuBox(
                expanded = groupExpanded && filteredGroups.isNotEmpty(),
                onExpandedChange = { groupExpanded = it }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                        groupExpanded = true
                    },
                    label = { Text("Szukaj grupy (np. 32INF)") },
                    placeholder = { Text("Wpisz kod grupy") },
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_search), contentDescription = null, modifier = Modifier.size(24.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(painterResource(R.drawable.ic_x_close), contentDescription = "Wyczyść", modifier = Modifier.size(24.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                )

                ExposedDropdownMenu(
                    expanded = groupExpanded && filteredGroups.isNotEmpty(),
                    onDismissRequest = { groupExpanded = false }
                ) {
                    filteredGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = {
                                viewModel.selectGroup(group)
                                groupExpanded = false
                            }
                        )
                    }
                }
            }

            // Sekcja podgrup
            if (draftGroup != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Wybierz podgrupy:", style = MaterialTheme.typography.labelLarge)

                    if (isLoading && availableSubgroups.isEmpty()) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Pobieranie podgrup...", style = MaterialTheme.typography.bodySmall)
                    } else if (availableSubgroups.isEmpty()) {
                        Text(
                            "Brak podgrup dla tej grupy lub brak połączenia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSubgroups.forEach { sub ->
                                FilterChip(
                                    selected = selectedDraftSubgroups.contains(sub),
                                    onClick = { viewModel.toggleSubgroup(sub) },
                                    label = { Text(sub) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Przycisk Zapisz - Z DEBUGOWANIEM
            Button(
                onClick = {
                    println("🔵🔵🔵 SAVE BUTTON CLICKED 🔵🔵🔵")
                    Toast.makeText(context, "Zapisuję zmiany...", Toast.LENGTH_SHORT).show()
                    viewModel.saveChanges()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Zapisywanie...")
                } else {
                    Text("Zapisz zmiany")
                }
            }

            // Przycisk Wyloguj
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Zresetuj dane i wyloguj")
            }
        }
    }
}
