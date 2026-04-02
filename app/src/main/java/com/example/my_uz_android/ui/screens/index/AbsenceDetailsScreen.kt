package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.getAppAccentColor
import com.example.my_uz_android.ui.theme.getClassColorIndex
import com.example.my_uz_android.util.ClassTypeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AbsenceDetailsScreenRoute(
    viewModel: AbsenceDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDuplicateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val absence = uiState.absence

    if (uiState.isLoading || absence == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    AbsenceDetailsScreen(
        subjectName = absence.subjectName ?: "",
        classType = ClassTypeUtils.getFullName(absence.classType),
        dateMillis = absence.date,
        isExcused = absence.isExcused,
        reason = absence.description ?: "",
        onNavigateBack = onNavigateBack,
        onEditClick = { onEditClick(absence.id) },
        onDeleteClick = { viewModel.deleteAbsence(onSuccess = onNavigateBack) },
        onDuplicateClick = onDuplicateClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceDetailsScreen(
    subjectName: String,
    classType: String,
    dateMillis: Long,
    isExcused: Boolean,
    reason: String,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val colorIndex = getClassColorIndex(subjectName)
    val accentColor = getAppAccentColor(colorIndex, isDark)

    // Zielony komentarz:
    // Zostawiamy kolor statusu 1:1 względem obecnego UX (zielony + akcent dla nieusprawiedliwionej).
    val statusText = if (isExcused) "Usprawiedliwiona" else "Nieusprawiedliwiona"
    val statusColor = if (isExcused) Color(0xFF388E3C) else getAppAccentColor(5, isDark)
    val statusIcon = if (isExcused) R.drawable.ic_check_circle_broken else R.drawable.ic_close

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = "",
                    navigationIcon = R.drawable.ic_close,
                    isNavigationIconFilled = true,
                    onNavigationClick = onNavigateBack,
                    containerColor = Color.Transparent,
                    actions = {
                        TopBarActionIcon(
                            icon = R.drawable.ic_edit,
                            isFilled = true,
                            onClick = onEditClick
                        )
                        Box {
                            TopBarActionIcon(
                                icon = R.drawable.ic_dots_vertical,
                                isFilled = true,
                                onClick = { showMenu = true }
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplikuj") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_copy),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Usuń", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_trash),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = accentColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column {
                        Text(
                            text = subjectName.ifBlank { "Nieznany przedmiot" },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatAbsenceDetailsDate(dateMillis),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(
                    iconRes = statusIcon,
                    iconTint = statusColor,
                    label = "Status",
                    value = statusText,
                    valueColor = statusColor,
                    isValueHighlight = true
                )

                DetailRow(
                    iconRes = R.drawable.ic_stand,
                    label = "Rodzaj zajęć",
                    value = classType
                )

                if (reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        iconRes = R.drawable.ic_menu_2,
                        label = null,
                        value = reason,
                        isMultiline = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false },
                    itemType = "nieobecność"
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    label: String?,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isValueHighlight: Boolean = false,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = if (isMultiline) 4.dp else 0.dp)
                .size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Column {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value.ifEmpty { "-" },
                style = if (isValueHighlight) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                },
                color = valueColor
            )
        }
    }
}

private fun formatAbsenceDetailsDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL"))
        .format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

@Preview(showBackground = true)
@Composable
fun AbsenceDetailsScreenPreview() {
    MyUZTheme {
        AbsenceDetailsScreen(
            subjectName = "Architektura Komputerów",
            classType = "Laboratorium",
            dateMillis = System.currentTimeMillis(),
            isExcused = true,
            reason = "Zwolnienie lekarskie dostarczone do dziekanatu w dniu 12.05.",
            onNavigateBack = {},
            onEditClick = {},
            onDeleteClick = {},
            onDuplicateClick = {}
        )
    }
}