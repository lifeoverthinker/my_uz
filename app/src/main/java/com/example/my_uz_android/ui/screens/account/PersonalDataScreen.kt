package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonalDataScreen(
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit = {},
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val draftName by viewModel.draftName.collectAsState()
    val draftSurname by viewModel.draftSurname.collectAsState()

    val availableGroups by viewModel.filteredGroups.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val draftSubgroups by viewModel.draftSubgroups.collectAsState()
    val groupSearchQuery by viewModel.groupSearchQuery.collectAsState()

    val saveMessage by viewModel.saveMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var expandedGroupDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(saveMessage) {
        if (saveMessage != null && saveMessage!!.contains("Zapisano")) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dane osobowe",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = "Wróć",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveChanges()
                        focusManager.clearFocus()
                    }) {
                        Text("Zapisz", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        snackbarHost = {
            if(saveMessage != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearSaveMessage() }) { Text("OK") }
                    },
                    modifier = Modifier.padding(16.dp)
                ) { Text(saveMessage!!) }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Twoje dane", style = MaterialTheme.typography.labelLarge, color = primaryColor)

            // Imię
            OutlinedTextField(
                value = draftName,
                onValueChange = { viewModel.updateDraftName(it) },
                label = { Text("Imię") },
                placeholder = { Text("Np. Jan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape = RoundedCornerShape(8.dp) // Styl jak w onboardingu
            )

            // Nazwisko
            OutlinedTextField(
                value = draftSurname,
                onValueChange = { viewModel.updateDraftSurname(it) },
                label = { Text("Nazwisko") },
                placeholder = { Text("Np. Kowalski") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Uczelnia", style = MaterialTheme.typography.labelLarge, color = primaryColor)

            // Wyszukiwarka grup
            ExposedDropdownMenuBox(
                expanded = expandedGroupDropdown,
                onExpandedChange = { expandedGroupDropdown = !expandedGroupDropdown }
            ) {
                OutlinedTextField(
                    value = groupSearchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                        expandedGroupDropdown = true
                    },
                    label = { Text("Kod grupy") },
                    placeholder = { Text("Np. 32INF-SP") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroupDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                if (availableGroups.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedGroupDropdown,
                        onDismissRequest = { expandedGroupDropdown = false }
                    ) {
                        availableGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    viewModel.selectGroup(group)
                                    expandedGroupDropdown = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }

            // Pasek ładowania
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Podgrupy
            if (availableSubgroups.isNotEmpty()) {
                Text("Podgrupy (opcjonalne):", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableSubgroups.forEach { subgroup ->
                        val isSelected = draftSubgroups.contains(subgroup)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleSubgroup(subgroup) },
                            label = { Text(subgroup) },
                            leadingIcon = if (isSelected) {
                                { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}