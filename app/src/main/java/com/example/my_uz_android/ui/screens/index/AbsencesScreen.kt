package com.example.my_uz_android.ui.screens.index

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsencesScreen(
    viewModel: AbsencesViewModel,
    showAddSheet: Boolean,
    onDismissSheet: () -> Unit
) {
    val absencesState by viewModel.absencesState.collectAsStateWithLifecycle()
    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()

    var preselectedSubject by remember { mutableStateOf<String?>(null) }
    var preselectedType by remember { mutableStateOf<String?>(null) }
    var absenceToEdit by remember { mutableStateOf<AbsenceEntity?>(null) }

    var showLimitDialog by remember { mutableStateOf(false) }
    var limitEditSubject by remember { mutableStateOf("") }
    var limitEditType by remember { mutableStateOf("") }
    var limitEditValue by remember { mutableStateOf("2") }

    var internalShowSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (absencesState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Brak nieobecności.\nDodaj pierwszą przyciskiem +",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(absencesState) { subjectGroup ->
                    AbsenceCard(
                        subjectData = subjectGroup,
                        onDeleteAbsence = { viewModel.deleteAbsence(it) },
                        onAddAbsenceClick = { type ->
                            preselectedSubject = subjectGroup.subjectName
                            preselectedType = type
                            absenceToEdit = null
                            internalShowSheet = true
                        },
                        onEditLimitClick = { type, currentLimit ->
                            limitEditSubject = subjectGroup.subjectName
                            limitEditType = type
                            limitEditValue = currentLimit.toString()
                            showLimitDialog = true
                        },
                        onEditAbsenceClick = { absence ->
                            preselectedSubject = absence.subjectName
                            preselectedType = absence.classType
                            absenceToEdit = absence
                            internalShowSheet = true
                        }
                    )
                }
            }
        }

        LaunchedEffect(showAddSheet) {
            if (showAddSheet) {
                preselectedSubject = null
                preselectedType = null
                absenceToEdit = null
                internalShowSheet = true
            }
        }

        if (internalShowSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    internalShowSheet = false
                    onDismissSheet()
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                AddEditAbsenceScreen(
                    availableClasses = availableClasses,
                    initialSubject = preselectedSubject,
                    initialType = preselectedType,
                    existingAbsence = absenceToEdit,
                    onSave = { subject, type, date, description ->
                        if (absenceToEdit != null) {
                            viewModel.deleteAbsence(absenceToEdit!!)
                            viewModel.addAbsence(subject, type, date, description)
                        } else {
                            viewModel.addAbsence(subject, type, date, description)
                        }
                        internalShowSheet = false
                        onDismissSheet()
                    },
                    onDelete = {
                        if (absenceToEdit != null) {
                            viewModel.deleteAbsence(absenceToEdit!!)
                        }
                        internalShowSheet = false
                        onDismissSheet()
                    },
                    onCancel = {
                        internalShowSheet = false
                        onDismissSheet()
                    }
                )
            }
        }

        if (showLimitDialog) {
            AlertDialog(
                onDismissRequest = { showLimitDialog = false },
                title = { Text("Limit nieobecności") },
                text = {
                    Column {
                        Text("Dla: ${ClassTypeUtils.getFullName(limitEditType)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = limitEditValue,
                            onValueChange = { if (it.all { char -> char.isDigit() }) limitEditValue = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            label = { Text("Maksymalna liczba") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newLimit = limitEditValue.toIntOrNull() ?: 2
                            viewModel.updateLimit(limitEditSubject, limitEditType, newLimit)
                            showLimitDialog = false
                        }
                    ) { Text("Zapisz") }
                },
                dismissButton = {
                    TextButton(onClick = { showLimitDialog = false }) { Text("Anuluj") }
                }
            )
        }
    }
}

@Composable
fun AbsenceCard(
    subjectData: SubjectAbsences,
    onDeleteAbsence: (AbsenceEntity) -> Unit,
    onAddAbsenceClick: (String) -> Unit,
    onEditLimitClick: (String, Int) -> Unit,
    onEditAbsenceClick: (AbsenceEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rot")

    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .background(cardBackgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subjectData.subjectName,
                style = TextStyle(
                    color = contentColor,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W500,
                    letterSpacing = 0.15.sp,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Zwiń" else "Rozwiń",
                modifier = Modifier.rotate(rotationState),
                tint = subTextColor
            )
        }

        if (expanded) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(16.dp)
                // Usunięto verticalArrangement, zarządzamy odstępami ręcznie w pętli
            ) {
                subjectData.types.forEachIndexed { index, typeGroup ->
                    AbsenceTypeSection(
                        typeGroup = typeGroup,
                        onDelete = onDeleteAbsence,
                        onAddClick = { onAddAbsenceClick(typeGroup.classType) },
                        onEditLimit = { onEditLimitClick(typeGroup.classType, typeGroup.limit) },
                        onEditAbsence = onEditAbsenceClick
                    )

                    // Dodajemy Divider tylko jeśli to NIE jest ostatni element
                    if (index < subjectData.types.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AbsenceTypeSection(
    typeGroup: AbsenceTypeGroup,
    onDelete: (AbsenceEntity) -> Unit,
    onAddClick: () -> Unit,
    onEditLimit: () -> Unit,
    onEditAbsence: (AbsenceEntity) -> Unit
) {
    val count = typeGroup.absences.size
    val limit = typeGroup.limit
    val isLimitReached = count >= limit

    val counterContainerColor = if (isLimitReached) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val counterContentColor = if (isLimitReached) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    val fullTypeName = ClassTypeUtils.getFullName(typeGroup.classType)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // --- HEADER SEKCJI ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fullTypeName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )

            // Subtelny licznik z ołówkiem (do edycji limitu)
            Surface(
                color = counterContainerColor,
                shape = RoundedCornerShape(8.dp),
                onClick = onEditLimit
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$count/$limit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = counterContentColor
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edytuj limit",
                        tint = counterContentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // --- LISTA NIEOBECNOŚCI ---
        if (typeGroup.absences.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                typeGroup.absences.forEach { absence ->
                    AbsenceDateItem(
                        absence = absence,
                        onDelete = onDelete,
                        onClick = { onEditAbsence(absence) }
                    )
                }
            }
        } else {
            Text(
                text = "Brak wpisów",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- PRZYCISK DODAJ (Wyrównany do prawej) ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                onClick = onAddClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, primaryColor),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Dodaj nieobecność",
                        style = TextStyle(
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.W500,
                            letterSpacing = 0.4.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Usunięto Divider z końca tej funkcji – teraz zarządza nim AbsenceCard
    }
}

@Composable
fun AbsenceDateItem(
    absence: AbsenceEntity,
    onDelete: (AbsenceEntity) -> Unit,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault())

    val dateStr = dateFormatter.format(Instant.ofEpochMilli(absence.date))
    val desc = absence.description

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = { onDelete(absence) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = "Usuń",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}