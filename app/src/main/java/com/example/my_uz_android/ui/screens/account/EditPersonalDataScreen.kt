package com.example.my_uz_android.ui.screens.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.ui.AppViewModelProvider
import androidx.compose.foundation.layout.ExperimentalLayoutApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPersonalDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val userName by viewModel.userName.collectAsState()
    val userSurname by viewModel.userSurname.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()

    val groupSearchQuery by viewModel.groupSearchQuery.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val selectedSubgroups by viewModel.selectedSubgroups.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    var expandedGroupMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj dane") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_left),
                            contentDescription = "Wroc"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveChanges(onSuccess = onNavigateBack) }, enabled = !isLoading) {
                        Text("Zapisz")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Forma zwrotu",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilterChip(
                            selected = selectedGender == UserGender.STUDENT,
                            onClick = { viewModel.setGender(UserGender.STUDENT) },
                            label = {
                                Text("Student", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedGender == UserGender.STUDENTKA,
                            onClick = { viewModel.setGender(UserGender.STUDENTKA) },
                            label = {
                                Text("Studentka", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { viewModel.setUserName(it) },
                        label = { Text("Imie") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = userSurname,
                        onValueChange = { viewModel.setUserSurname(it) },
                        label = { Text("Nazwisko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium
                    )
                }

                HorizontalDivider()

                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedGroupMenu && filteredGroups.isNotEmpty(),
                        onExpandedChange = { expandedGroupMenu = !expandedGroupMenu }
                    ) {
                        OutlinedTextField(
                            value = groupSearchQuery,
                            onValueChange = {
                                viewModel.setGroupSearchQuery(it)
                                expandedGroupMenu = true
                            },
                            label = { Text("Kod grupy (np. 32INF-SP)") },
                            placeholder = { Text("Szukaj grupy...") },
                            trailingIcon = {
                                if (groupSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setGroupSearchQuery("") }) {
                                        Icon(Icons.Default.Close, "Wyczysc")
                                    }
                                } else {
                                    Icon(Icons.Default.Search, null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                            singleLine = true,
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                        )

                        ExposedDropdownMenu(
                            expanded = expandedGroupMenu && filteredGroups.isNotEmpty(),
                            onDismissRequest = { expandedGroupMenu = false }
                        ) {
                            filteredGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group) },
                                    onClick = {
                                        viewModel.selectGroup(group)
                                        expandedGroupMenu = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedGroup != null && availableSubgroups.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Wybierz podgrupy",
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableSubgroups.forEach { subgroup ->
                                FilterChip(
                                    selected = selectedSubgroups.contains(subgroup),
                                    onClick = { viewModel.toggleSubgroup(subgroup) },
                                    label = { Text(subgroup) },
                                    leadingIcon = if (selectedSubgroups.contains(subgroup)) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
