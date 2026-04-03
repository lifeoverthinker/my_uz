package com.example.my_uz_android.ui.screens.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditGradeScreenRoute(
    viewModel: AddEditGradeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    LaunchedEffect(isSaved) {
        if (isSaved) onNavigateBack()
    }

    var selectedQuickType by remember { mutableStateOf("") }

    val appGradeType = when (uiState.gradeType) {
        GradeType.STANDARD -> AppGradeType.SCALE
        GradeType.POINTS -> AppGradeType.POINTS
        GradeType.ACTIVITY -> AppGradeType.ACTIVITY
        GradeType.CUSTOM -> AppGradeType.SCALE
    }

    val subjectsList = uiState.availableSubjects.map { it.first }
    val classTypesList = uiState.availableSubjects
        .find { it.first == uiState.subjectName }
        ?.second
        .orEmpty()

    AddEditGradeScreen(
        isEditMode = uiState.gradeId > 0,
        selectedQuickType = selectedQuickType,
        onQuickTypeSelect = { type ->
            selectedQuickType = type
            viewModel.updateDescription(type)
            if (type.lowercase() == "aktywność") {
                viewModel.updateGradeType(GradeType.ACTIVITY)
            }
        },
        gradeType = appGradeType,
        onGradeTypeChange = { newType ->
            val mappedType = when (newType) {
                AppGradeType.SCALE -> GradeType.STANDARD
                AppGradeType.POINTS -> GradeType.POINTS
                AppGradeType.ACTIVITY -> GradeType.ACTIVITY
            }
            viewModel.updateGradeType(mappedType)
        },
        gradeValue = uiState.gradeValue?.toString() ?: "5.0",
        onGradeValueChange = { viewModel.updateGradeValue(it.toDoubleOrNull()) },
        pointsScored = uiState.customGradeValue,
        onPointsScoredChange = viewModel::updateCustomGradeValue,
        activityCount = uiState.customGradeValue.toIntOrNull() ?: 1,
        onActivityCountChange = { viewModel.updateCustomGradeValue(it.toString()) },
        weight = uiState.weight,
        onWeightChange = viewModel::updateWeight,
        selectedSubject = uiState.subjectName ?: "",
        onSubjectChange = { newSubject ->
            viewModel.updateSubjectName(newSubject)
            viewModel.updateClassType("")
        },
        subjectsList = subjectsList,
        selectedClassType = uiState.classType ?: "",
        onClassTypeChange = viewModel::updateClassType,
        classTypesList = classTypesList,
        selectedDateMillis = uiState.date,
        onDateChange = viewModel::updateDate,
        title = uiState.description,
        onTitleChange = viewModel::updateDescription,
        comment = uiState.comment,
        onCommentChange = viewModel::updateComment,
        onSaveClick = viewModel::saveGrade,
        onBackClick = onNavigateBack
    )
}

enum class AppGradeType {
    SCALE, POINTS, ACTIVITY
}

@Composable
private fun gradeAppOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary
)

private val GradeAppShape = RoundedCornerShape(16.dp)

@Composable
private fun GradeClickableFieldRow(
    label: String,
    value: String,
    iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(GradeAppShape)
            .clickable(onClick = onClick),
        shape = GradeAppShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditGradeScreen(
    isEditMode: Boolean,
    selectedQuickType: String,
    onQuickTypeSelect: (String) -> Unit,
    gradeType: AppGradeType,
    onGradeTypeChange: (AppGradeType) -> Unit,
    gradeValue: String,
    onGradeValueChange: (String) -> Unit,
    pointsScored: String,
    onPointsScoredChange: (String) -> Unit,
    activityCount: Int,
    onActivityCountChange: (Int) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    subjectsList: List<String>,
    selectedClassType: String,
    onClassTypeChange: (String) -> Unit,
    classTypesList: List<String>,
    selectedDateMillis: Long?,
    onDateChange: (Long) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = if (isEditMode) "Edytuj wpis" else "Dodaj wpis",
                navigationIcon = R.drawable.ic_close,
                isNavigationIconFilled = true,
                onNavigationClick = onBackClick,
                actions = {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .heightIn(min = 48.dp),
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SZYBKI WYBÓR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val quickTypes = listOf(
                    "Kolokwium",
                    "Egzamin",
                    "Aktywność",
                    "Wejściówka",
                    "Projekt",
                    "Prezentacja",
                    "Zadanie domowe",
                    "Odpowiedź ustna"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTypes) { type ->
                        FilterChip(
                            selected = selectedQuickType == type,
                            onClick = { onQuickTypeSelect(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Tytuł") },
                placeholder = { Text("np. Kolokwium 1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = gradeAppOutlinedTextFieldColors(),
                shape = GradeAppShape
            )

            GradeClickableFieldRow(
                label = "Data",
                value = formatGradeAddEditDate(selectedDateMillis) ?: "Wybierz datę",
                iconRes = R.drawable.ic_calendar,
                onClick = { showDatePicker = true }
            )

            GradeAppDropdownMenu(
                label = "Przedmiot",
                selectedOption = selectedSubject,
                options = subjectsList,
                onOptionSelected = onSubjectChange
            )

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

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = gradeType == AppGradeType.SCALE,
                            onClick = { onGradeTypeChange(AppGradeType.SCALE) },
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        ) { Text("Skala") }

                        SegmentedButton(
                            selected = gradeType == AppGradeType.POINTS,
                            onClick = { onGradeTypeChange(AppGradeType.POINTS) },
                            shape = RoundedCornerShape(0.dp)
                        ) { Text("Punkty") }

                        SegmentedButton(
                            selected = gradeType == AppGradeType.ACTIVITY,
                            onClick = { onGradeTypeChange(AppGradeType.ACTIVITY) },
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        ) { Text("Plusy") }
                    }

                    when (gradeType) {
                        AppGradeType.SCALE -> {
                            val gradeOptions = listOf("5.0", "4.5", "4.0", "3.5", "3.0", "2.5", "2.0")
                            GradeAppDropdownMenu(
                                label = "Wybierz ocenę",
                                selectedOption = gradeValue,
                                options = gradeOptions,
                                onOptionSelected = onGradeValueChange
                            )
                        }

                        AppGradeType.POINTS -> {
                            OutlinedTextField(
                                value = pointsScored,
                                onValueChange = onPointsScoredChange,
                                label = { Text("Liczba punktów") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = gradeAppOutlinedTextFieldColors(),
                                shape = GradeAppShape
                            )
                        }

                        AppGradeType.ACTIVITY -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledIconButton(
                                    onClick = { if (activityCount > 0) onActivityCountChange(activityCount - 1) },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_minus),
                                        contentDescription = "Odejmij",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = activityCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                FilledIconButton(
                                    onClick = { onActivityCountChange(activityCount + 1) },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_plus),
                                        contentDescription = "Dodaj",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = gradeType == AppGradeType.SCALE,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = onWeightChange,
                            label = { Text("Waga (opcjonalnie)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.ic_scales),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = gradeAppOutlinedTextFieldColors(),
                            shape = GradeAppShape
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Opis (opcjonalnie)") },
                placeholder = { Text("Dodatkowe informacje, uwagi...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                colors = gradeAppOutlinedTextFieldColors(),
                shape = GradeAppShape
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePicker(
            date = selectedDateMillis ?: System.currentTimeMillis(),
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
private fun GradeAppDropdownMenu(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = gradeAppOutlinedTextFieldColors(),
            shape = GradeAppShape
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

private fun formatGradeAddEditDate(millis: Long?): String? {
    if (millis == null) return null
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL"))
        .format(Date(millis))
        .replaceFirstChar { it.uppercase() }
}

@Preview(showBackground = true)
@Composable
private fun AddEditGradeScreenPreview() {
    MyUZTheme {
        AddEditGradeScreen(
            isEditMode = false,
            selectedQuickType = "Kolokwium",
            onQuickTypeSelect = {},
            gradeType = AppGradeType.SCALE,
            onGradeTypeChange = {},
            gradeValue = "4.5",
            onGradeValueChange = {},
            pointsScored = "18",
            onPointsScoredChange = {},
            activityCount = 2,
            onActivityCountChange = {},
            weight = "2",
            onWeightChange = {},
            selectedSubject = "Matematyka",
            onSubjectChange = {},
            subjectsList = listOf("Matematyka", "Fizyka", "Programowanie"),
            selectedClassType = "W",
            onClassTypeChange = {},
            classTypesList = listOf("W", "L", "C"),
            selectedDateMillis = System.currentTimeMillis(),
            onDateChange = {},
            title = "Kolokwium 1",
            onTitleChange = {},
            comment = "Krótki komentarz do oceny",
            onCommentChange = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}