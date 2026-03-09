package com.example.my_uz_android.ui.screens.account

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.UserCourseEntity
import com.example.my_uz_android.data.models.UserGender
import com.example.my_uz_android.ui.AppViewModelProvider
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
        onNavigateBack = {
            viewModel.cancelEdit() // Oczyszcza szkice przy powrocie strzałką
            onNavigateBack()
        },
        onSaveChanges = { viewModel.saveChanges(onNavigateBack) },
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
    onNavigateBack: () -> Unit,
    onSaveChanges: () -> Unit,
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
                title = { Text("Edytuj profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.ic_chevron_left), "Wróć", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(onClick = onSaveChanges, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Zapisz", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Twoje kierunki studiów", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                selectedGroup?.let { gCode ->
                    val isMainLoading = loadingSubgroupsFor.contains(gCode)

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
                                    Text(gCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                                            label = { Text(sub, style = MaterialTheme.typography.bodySmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                additionalUserCourses.forEach { course ->
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
                                                label = { Text(sub, style = MaterialTheme.typography.bodySmall) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

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
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ==========================================
// PREVIEW
// ==========================================
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
                UserCourseEntity(id = 1, groupCode = "12-MAT-S", fieldOfStudy = "Wczytywanie...")
            ),
            additionalSubgroupsMap = mapOf(),
            filteredGroups = emptyList(),
            isLoading = false,
            loadingSubgroupsFor = setOf("12-MAT-S"),
            fieldOfStudy = "Informatyka",
            onNavigateBack = {},
            onSaveChanges = {},
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