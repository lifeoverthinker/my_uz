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

    val availableGroups = listOf("32INF-SP", "31INF-SM", "12MED-NP")
    var groupExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Najpierw padding systemowy
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), // Potem odstęp
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Konto studenta", style = MaterialTheme.typography.headlineMedium)
            }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tryb Ciemny", style = MaterialTheme.typography.titleMedium)
                        Text("Zmień motyw aplikacji", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings?.isDarkMode == true,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            Text("Ustawienia Planu", style = MaterialTheme.typography.titleLarge)

            ExposedDropdownMenuBox(
                expanded = groupExpanded,
                onExpandedChange = { groupExpanded = it }
            ) {
                OutlinedTextField(
                    value = settings?.selectedGroupCode ?: "Wybierz grupę",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Twoja grupa") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = groupExpanded,
                    onDismissRequest = { groupExpanded = false }
                ) {
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

            Column {
                Text("Podgrupy", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("L1", "L2", "L3").forEach { sub ->
                        FilterChip(
                            selected = settings?.selectedSubgroup == sub,
                            onClick = { viewModel.toggleSubgroup(sub) },
                            label = { Text(sub) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Wyloguj się")
            }
        }
    }
}