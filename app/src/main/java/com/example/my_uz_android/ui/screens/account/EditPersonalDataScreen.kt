package com.example.my_uz_android.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPersonalDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.settings.collectAsState()
    val groupSearchQuery by viewModel.groupSearchQuery.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val draftSubgroups by viewModel.draftSubgroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()

    var userName by remember { mutableStateOf(settings?.userName ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(settings) {
        if (settings != null && userName.isBlank()) {
            userName = settings!!.userName
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            // ZMIANA: TopAppBar zamiast CenterAlignedTopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = "Edycja danych",
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            ) {
                if (saveMessage != null) {
                    Text(
                        text = saveMessage!!,
                        color = if (saveMessage!!.contains("Error") || saveMessage!!.contains("Błąd")) MaterialTheme.colorScheme.error else primaryColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        // TODO: Implementacja aktualizacji imienia w VM
                        viewModel.saveChanges()
                        Toast.makeText(context, "Zapisano zmiany", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Zapisz",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sekcja 1: Dane studenta
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Dane studenta",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Imię i Nazwisko") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                )
            }

            // Sekcja 2: Grupa
            item {
                Text(
                    "Grupa dziekańska",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded && filteredGroups.isNotEmpty(),
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = groupSearchQuery,
                        onValueChange = {
                            viewModel.onSearchQueryChange(it)
                            expanded = true
                        },
                        placeholder = { Text("Szukaj grupy (np. 32INF)...") },
                        label = { Text("Kod grupy") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_search),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = {
                            if (groupSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        painterResource(R.drawable.ic_x_close),
                                        contentDescription = "Wyczyść",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && filteredGroups.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        filteredGroups.forEach { group ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        group,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily)
                                    )
                                },
                                onClick = {
                                    viewModel.selectGroup(group)
                                    expanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }

            // Sekcja 3: Podgrupy
            if (availableSubgroups.isNotEmpty()) {
                item {
                    Text(
                        "Wybierz podgrupy",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = primaryColor,
                            fontFamily = InterFontFamily
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

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
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}