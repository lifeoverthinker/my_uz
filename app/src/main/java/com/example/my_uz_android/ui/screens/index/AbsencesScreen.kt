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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.EmptyStateMessage
import com.example.my_uz_android.ui.components.DashboardEmptyCard

@Composable
fun AbsencesScreen(
    viewModel: AbsencesViewModel,
    onAddAbsenceClick: (String?, String?) -> Unit,
    onEditAbsenceClick: (Int) -> Unit
) {
    val absencesState by viewModel.absencesState.collectAsStateWithLifecycle()

    var showLimitDialog by remember { mutableStateOf(false) }
    var limitEditSubject by remember { mutableStateOf("") }
    var limitEditType by remember { mutableStateOf("") }
    var limitEditValue by remember { mutableStateOf("2") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (absencesState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateMessage(
                    title = "100% frekwencji! ✅",
                    subtitle = "Bez ani jednej nieobecności!",
                    message = "Brawo! Nie opuściłeś żadnych zajęć w tym semestrze. Tak trzymaj!",
                    iconRes = R.drawable.students_rafiki, // Twoja nowa ilustracja
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Grupowanie po nazwie kierunku
            val groupedAbsences = absencesState.groupBy { it.courseName }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedAbsences.forEach { (courseName, subjects) ->

                    // Pokazujemy nagłówek TYLKO jeśli jest więcej niż 1 kierunek na liście
                    if (groupedAbsences.size > 1) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    top = if (courseName == groupedAbsences.keys.first()) 0.dp else 8.dp,
                                    bottom = 4.dp
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_graduation_hat),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = courseName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    items(subjects) { subjectGroup ->
                        AbsenceCard(
                            subjectData = subjectGroup,
                            onDeleteAbsence = { viewModel.deleteAbsence(it) },
                            onAddAbsenceClick = { type ->
                                onAddAbsenceClick(subjectGroup.subjectName, type)
                            },
                            onEditLimitClick = { type, currentLimit ->
                                limitEditSubject = subjectGroup.subjectName
                                limitEditType = type
                                limitEditValue = currentLimit.toString()
                                showLimitDialog = true
                            },
                            onEditAbsenceClick = { absence ->
                                onEditAbsenceClick(absence.id)
                            }
                        )
                    }
                }
            }
        }

        if (showLimitDialog) {
            Dialog(onDismissRequest = { showLimitDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Limit nieobecności",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Dla: ${ClassTypeUtils.getFullName(limitEditType)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = limitEditValue,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) limitEditValue = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            label = { Text("Maksymalna liczba") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showLimitDialog = false }) {
                                Text("Anuluj")
                            }
                            TextButton(
                                onClick = {
                                    val newLimit = limitEditValue.toIntOrNull() ?: 2
                                    viewModel.updateLimit(limitEditSubject, limitEditType, newLimit)
                                    showLimitDialog = false
                                }
                            ) {
                                Text("Zapisz")
                            }
                        }
                    }
                }
            }
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
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rot"
    )

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subjectData.subjectName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = contentColor,
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
            ) {
                subjectData.types.forEachIndexed { index, typeGroup ->
                    AbsenceTypeSection(
                        typeGroup = typeGroup,
                        onDelete = onDeleteAbsence,
                        onAddClick = { onAddAbsenceClick(typeGroup.classType) },
                        onEditLimit = { onEditLimitClick(typeGroup.classType, typeGroup.limit) },
                        onEditAbsence = onEditAbsenceClick
                    )

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

    val counterContainerColor = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val counterContentColor = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    val fullTypeName = ClassTypeUtils.getFullName(typeGroup.classType)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fullTypeName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Surface(
                color = counterContainerColor,
                shape = RoundedCornerShape(8.dp),
                onClick = onEditLimit
            ) {
                Text(
                    text = "$count/$limit",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = counterContentColor
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

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
                        style = MaterialTheme.typography.labelMedium.copy(color = primaryColor)
                    )
                }
            }
        }
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
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AbsencesScreenEmptyPreview() {
    com.example.my_uz_android.ui.theme.MyUZTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            com.example.my_uz_android.ui.components.EmptyStateMessage(
                title = "Czyste konto!",
                message = "Brak nieobecności do wyświetlenia.\nOby tak dalej! Jeśli jednak musisz, dodaj ją przyciskiem +",
                iconRes = R.drawable.college_students_rafiki,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}