package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.InterFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val draftName by viewModel.draftName.collectAsState()
    val draftSurname by viewModel.draftSurname.collectAsState()
    val draftRole by viewModel.draftRole.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(saveMessage) {
        if (saveMessage == "Zapisano") {
            onNavigateBack()
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Edytuj dane",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onNavigateBack,
                isCenterAligned = true,
                actions = {
                    TextButton(
                        onClick = { viewModel.savePersonalData() },
                        enabled = draftName.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Zapisz",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dane studenta",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = draftName,
                onValueChange = { viewModel.updateDraftName(it) },
                label = { Text("Imię") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_user), null, Modifier.size(24.dp))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = draftSurname,
                onValueChange = { viewModel.updateDraftSurname(it) },
                label = { Text("Nazwisko") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_user), null, Modifier.size(24.dp))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Forma zwrotu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleFilterChip(
                    label = "Student",
                    selected = draftRole == "STUDENT",
                    onClick = { viewModel.updateDraftRole("STUDENT") },
                    modifier = Modifier.weight(1f)
                )

                RoleFilterChip(
                    label = "Studentka",
                    selected = draftRole == "STUDENTKA",
                    onClick = { viewModel.updateDraftRole("STUDENTKA") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = modifier.height(48.dp),
        leadingIcon = if (selected) {
            { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}