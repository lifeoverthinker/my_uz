package com.example.my_uz_android.ui.screens.account

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.MyUZTheme

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
    val selectedSubgroups by viewModel.mainSelectedSubgroups.collectAsState()
    val fieldOfStudy by viewModel.mainFieldOfStudy.collectAsState()
    val additionalUserCourses by viewModel.additionalUserCourses.collectAsState()
    val additionalSubgroupsMap by viewModel.additionalSubgroupsMap.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingSubgroupsFor by viewModel.loadingSubgroupsFor.collectAsState()
    val isSavedFeedback by viewModel.isSavedFeedback.collectAsState()

    EditPersonalDataScreenContent(
        userName = userName,
        userSurname = userSurname,
        selectedGender = selectedGender,
        groupSearchQuery = groupSearchQuery,
        selectedGroup = selectedGroup,
        availableSubgroups = availableSubgroups,
        selectedSubgroups = selectedSubgroups,
        additionalUserCourses = additionalUserCourses,
        additionalSubgroupsMap = additionalSubgroupsMap,
        filteredGroups = filteredGroups,
        isLoading = isLoading,
        loadingSubgroupsFor = loadingSubgroupsFor,
        fieldOfStudy = fieldOfStudy,
        isSavedFeedback = isSavedFeedback,
        onNavigateBack = {
            viewModel.exitEditMode()
            onNavigateBack()
        },
        onUserNameChange = viewModel::setUserName,
        onUserSurnameChange = viewModel::setUserSurname,
        onGenderSelect = viewModel::setGender,
        onGroupSearchQueryChange = viewModel::setGroupSearchQuery,
        onGroupSelect = viewModel::selectGroup,
        onClearMainGroup = viewModel::clearMainGroup,
        onToggleSubgroup = viewModel::toggleMainSubgroup,
        onRemoveAdditionalCourse = viewModel::removeAdditionalCourse,
        onUpdateAdditionalSubgroup = viewModel::updateAdditionalSubgroup
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPersonalDataScreenContent(
    userName: String,
    userSurname: String,
    selectedGender: UserGender?,
    groupSearchQuery: String,
    selectedGroup: String?,
    availableSubgroups: List<String>,
    selectedSubgroups: Set<String>,
    additionalUserCourses: List<UserCourseEntity>,
    additionalSubgroupsMap: Map<String, List<String>>,
    filteredGroups: List<String>,
    isLoading: Boolean,
    loadingSubgroupsFor: Set<String>,
    fieldOfStudy: String,
    isSavedFeedback: Boolean,
    onNavigateBack: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onUserSurnameChange: (String) -> Unit,
    onGenderSelect: (UserGender) -> Unit,
    onGroupSearchQueryChange: (String) -> Unit,
    onGroupSelect: (String) -> Unit,
    onClearMainGroup: () -> Unit,
    onToggleSubgroup: (String) -> Unit,
    onRemoveAdditionalCourse: (UserCourseEntity) -> Unit,
    onUpdateAdditionalSubgroup: (UserCourseEntity, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var expandedMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "Edytuj profil",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onNavigateBack,
                isNavigationIconFilled = true,
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        AnimatedVisibility(visible = isSavedFeedback, enter = fadeIn(), exit = fadeOut()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Zapisano",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Forma zwrotu", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = selectedGender == UserGender.STUDENT,
                            onClick = { onGenderSelect(UserGender.STUDENT) },
                            label = { Text("Student", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedGender == UserGender.STUDENTKA,
                            onClick = { onGenderSelect(UserGender.STUDENTKA) },
                            label = { Text("Studentka", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = onUserNameChange,
                        label = { Text("Imię") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = userSurname,
                        onValueChange = onUserSurnameChange,
                        label = { Text("Nazwisko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text(
                    "Twoje kierunki studiów",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            if (selectedGroup != null) {
                item {
                    val isMainLoading = loadingSubgroupsFor.contains(selectedGroup)

                    OutlinedCard(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(fieldOfStudy.ifBlank { "Główny kierunek" }, style = MaterialTheme.typography.titleSmall)
                                    Text(selectedGroup, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = onClearMainGroup) {
                                    Icon(Icons.Default.Delete, contentDescription = "Usuń kierunek główny", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            if (isMainLoading) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Pobieranie podgrup...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            } else if (availableSubgroups.isNotEmpty()) {
                                Text("Podgrupy:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                FlowRow(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    availableSubgroups.forEach { sub ->
                                        FilterChip(
                                            selected = selectedSubgroups.contains(sub),
                                            onClick = { onToggleSubgroup(sub) },
                                            label = { Text(sub, style = MaterialTheme.typography.bodySmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(additionalUserCourses) { course ->
                val isAdditionalLoading = loadingSubgroupsFor.contains(course.groupCode)

                OutlinedCard(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                    ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(course.fieldOfStudy ?: "Dodatkowy kierunek", style = MaterialTheme.typography.titleSmall)
                                Text(course.groupCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onRemoveAdditionalCourse(course) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (isAdditionalLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Pobieranie podgrup...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        } else {
                            val available = additionalSubgroupsMap[course.groupCode] ?: emptyList()
                            if (available.isNotEmpty()) {
                                Text("Podgrupy:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                FlowRow(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    available.forEach { sub ->
                                        val current = course.selectedSubgroup?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                                        FilterChip(
                                            selected = current.contains(sub),
                                            onClick = { onUpdateAdditionalSubgroup(course, sub) },
                                            label = { Text(sub, style = MaterialTheme.typography.bodySmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedMenu && filteredGroups.isNotEmpty(),
                    onExpandedChange = { expandedMenu = !expandedMenu }
                ) {
                    OutlinedTextField(
                        value = groupSearchQuery,
                        onValueChange = { onGroupSearchQueryChange(it); expandedMenu = true },
                        label = { Text(if (selectedGroup == null) "Znajdź główny kierunek..." else "Dodaj kolejny kierunek...") },
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null, Modifier.size(24.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMenu && filteredGroups.isNotEmpty(),
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        filteredGroups.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    onGroupSelect(g)
                                    expandedMenu = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPersonalDataScreenPreview() {
    MyUZTheme {
        EditPersonalDataScreenContent(
            userName = "Jan",
            userSurname = "Kowalski",
            selectedGender = UserGender.STUDENT,
            groupSearchQuery = "",
            selectedGroup = "11-INF-ZI-S",
            availableSubgroups = listOf("L1", "L2", "C1", "C2"),
            selectedSubgroups = setOf("L1", "C2"),
            additionalUserCourses = listOf(
                UserCourseEntity(id = 1, groupCode = "12-MAT-S", fieldOfStudy = "Wczytywanie...", selectedSubgroup = "C1")
            ),
            additionalSubgroupsMap = mapOf(
                "12-MAT-S" to listOf("L1", "C1", "C2")
            ),
            filteredGroups = emptyList(),
            isLoading = false,
            loadingSubgroupsFor = setOf(),
            fieldOfStudy = "Informatyka",
            isSavedFeedback = true,
            onNavigateBack = {},
            onUserNameChange = {},
            onUserSurnameChange = {},
            onGenderSelect = {},
            onGroupSearchQueryChange = {},
            onGroupSelect = {},
            onClearMainGroup = {},
            onToggleSubgroup = {},
            onRemoveAdditionalCourse = {},
            onUpdateAdditionalSubgroup = { _, _ -> }
        )
    }
}
