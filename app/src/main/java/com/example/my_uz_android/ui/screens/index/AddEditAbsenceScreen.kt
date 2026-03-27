package com.example.my_uz_android.ui.screens.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.*

// --- WRAPPER DLA NAWIGACJI ---
@Composable
fun AbsenceAddEditScreenRoute(
    viewModel: AddEditAbsenceViewModel,
    prefilledSubject: String? = null,
    prefilledClassType: String? = null,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(prefilledSubject, prefilledClassType) {
        if (uiState.id == 0 && uiState.subjectName == null) {
            viewModel.initNewAbsence(prefilledSubject, prefilledClassType)
        }
    }

    var localSubject by remember(uiState.subjectName) { mutableStateOf(uiState.subjectName ?: "") }
    var localClassType by remember(uiState.classType) { mutableStateOf(uiState.classType ?: "") }
    var isExcused by remember { mutableStateOf(true) }

    val subjectsList = uiState.availableSubjects.map { it.first }
    val classTypesList = uiState.availableSubjects.find { it.first == localSubject }?.second ?: emptyList()

    AbsenceAddEditScreen(
        isEditMode = uiState.id != 0,
        selectedSubject = localSubject,
        onSubjectChange = {
            localSubject = it
            viewModel.updateSubjectName(it)
            viewModel.updateClassType("")
        },
        subjectsList = subjectsList,
        selectedClassType = localClassType,
        onClassTypeChange = {
            localClassType = it
            viewModel.updateClassType(it)
        },
        classTypesList = classTypesList,
        dateMillis = uiState.date,
        onDateChange = { viewModel.updateDate(it) },
        isExcused = isExcused,
        onExcusedChange = { isExcused = it },
        reason = uiState.description,
        onReasonChange = { viewModel.updateDescription(it) },
        onSaveClick = { viewModel.saveAbsence() },
        onNavigateBack = onNavigateBack
    )
}

// --- WSPÓLNE STYLE ---
@Composable
private fun getAppTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
)

private val AppTextFieldShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)

@Composable
private fun ClickableFieldRow(
    label: String,
    value: String,
    iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTextFieldShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            thickness = 1.dp
        )
    }
}

// --- BEZSTANOWY WIDOK ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AbsenceAddEditScreen(
    isEditMode: Boolean,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    subjectsList: List<String>,
    selectedClassType: String,
    onClassTypeChange: (String) -> Unit,
    classTypesList: List<String>,
    dateMillis: Long,
    onDateChange: (Long) -> Unit,
    isExcused: Boolean,
    onExcusedChange: (Boolean) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edytuj nieobecność" else "Dodaj nieobecność") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Anuluj",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = selectedSubject.isNotBlank() && selectedClassType.isNotBlank()
                    ) {
                        Text("Zapisz", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. Data (Klikalny wiersz z Dividerem) ---
            ClickableFieldRow(
                label = "Data nieobecności",
                value = formatDate(dateMillis),
                iconRes = R.drawable.ic_calendar,
                onClick = { showDatePicker = true }
            )

            // --- 2. Przedmiot (Dropdown) ---
            AppDropdownMenu(
                label = "Przedmiot",
                selectedOption = selectedSubject,
                options = subjectsList,
                onOptionSelected = onSubjectChange,
                iconRes = R.drawable.ic_book_open
            )

            // --- 3. Rodzaj zajęć (Chipy) ---
            AnimatedVisibility(
                visible = selectedSubject.isNotEmpty() && classTypesList.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RODZAJ ZAJĘĆ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        classTypesList.forEach { type ->
                            FilterChip(
                                selected = selectedClassType == type,
                                onClick = { onClassTypeChange(type) },
                                label = { Text(ClassTypeUtils.getFullName(type)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // --- 4. Usprawiedliwienie (Switch z Ikoną) ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExcusedChange(!isExcused) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle_broken),
                            contentDescription = null,
                            tint = if (isExcused) androidx.compose.ui.graphics.Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Usprawiedliwiona",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isExcused,
                        onCheckedChange = onExcusedChange
                    )
                }
            }

            // --- 5. Powód / Opis (Filled TextField) ---
            TextField(
                value = reason,
                onValueChange = onReasonChange,
                label = { Text("Powód / Opis (opcjonalnie)") },
                placeholder = { Text("np. Wizyta u lekarza, zwolnienie lekarskie L4...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                colors = getAppTextFieldColors(),
                shape = AppTextFieldShape
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        DatePicker(
            date = dateMillis,
            onDateSelected = {
                onDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDropdownMenu(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    iconRes: Int
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = AppTextFieldShape
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL")).format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}