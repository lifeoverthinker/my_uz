package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TaskDatePicker
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskAddEditScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskAddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // --- KOLORYSTYKA ---
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val taskAccentColor = MaterialTheme.extendedColors.classCardBackground
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(uiState.isTaskSaved) {
        if (uiState.isTaskSaved) onNavigateBack()
    }

    // --- MODALE ---

    // 1. Modal DatePicker
    if (showDatePicker) {
        TaskDatePicker(
            onDateSelected = { timestamp ->
                timestamp?.let {
                    val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    viewModel.updateDate(date)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // 2. Modal Wyboru Przedmiotu (AlertDialog)
    if (uiState.isSubjectModalVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSubjectModal(false) },
            title = { Text("Wybierz przedmiot", fontFamily = InterFontFamily) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    uiState.availableSubjects.forEach { subject ->
                        TextButton(
                            onClick = { viewModel.updateSubject(subject) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(
                                text = subject,
                                fontFamily = InterFontFamily,
                                color = textColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        }
                        HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                    }
                    if (uiState.availableSubjects.isEmpty()) {
                        Text("Brak przedmiotów w planie.", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleSubjectModal(false) }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // --- UI GŁÓWNE ---
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- NAGŁÓWEK (Zamknij / Zapisz) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Button(
                    onClick = { viewModel.saveTask() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Zapisz", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            // --- FORMULARZ ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. TYTUŁ (Layout identyczny jak w TaskDetailsScreen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Globalny margines
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Ikona (Kwadrat akcentu)
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(taskAccentColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Pole Tekstowe Tytułu
                    Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            textStyle = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = textColor
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "Tytuł zadania",
                                        style = TextStyle(
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 28.sp,
                                            lineHeight = 36.sp,
                                            color = textColor.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 2. CZAS I DATA
                CommonRow(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Switch "Cały dzień"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Logika przełączania czasu
                                    viewModel.updateTime(if (uiState.isAllDay) java.time.LocalTime.NOON else null)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cały dzień",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = uiState.isAllDay,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateTime(if (isChecked) null else java.time.LocalTime.NOON)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = primaryColor
                                )
                            )
                        }

                        // Wybór Daty
                        Text(
                            text = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("pl"))),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        )

                        // Wybór Godziny (tylko jeśli nie "Cały dzień")
                        if (!uiState.isAllDay) {
                            Text(
                                text = uiState.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "12:00",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = primaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Otwórz TimePicker */ }
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 3. PRZEDMIOT
                CommonRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSubjectModal(true) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState.selectedSubject.isEmpty()) {
                            Text(
                                "Wybierz przedmiot",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = subTextColor
                            )
                        } else {
                            Text(
                                uiState.selectedSubject,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = textColor
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 4. RODZAJ ZAJĘĆ (Widoczny tylko po wybraniu przedmiotu)
                if (uiState.selectedSubject.isNotEmpty()) {
                    CommonRow(iconRes = R.drawable.ic_graduation_hat, iconTint = iconTint) {
                        var expandedType by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedType = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (uiState.selectedType.isEmpty()) "Wybierz rodzaj" else uiState.selectedType,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                    color = if (uiState.selectedType.isEmpty()) subTextColor else textColor
                                )
                                // Dodana strzałka dla lepszego UX
                                Icon(
                                    painterResource(R.drawable.ic_chevron_down),
                                    contentDescription = null,
                                    tint = subTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                uiState.availableTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontFamily = InterFontFamily) },
                                        onClick = {
                                            viewModel.updateType(type)
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                }

                // 5. OPIS
                CommonRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = InterFontFamily,
                                color = textColor,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.description.isEmpty()) {
                                    Text(
                                        "Dodaj opis",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = InterFontFamily,
                                            color = subTextColor
                                        )
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Dodatkowy odstęp na dole, by klawiatura nie zasłaniała treści
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * Komponent pomocniczy dla wierszy formularza.
 * Zapewnia identyczne wcięcia i wyrównanie jak w TaskDetailsScreen.
 */
@Composable
fun CommonRow(
    iconRes: Int,
    iconTint: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp), // Marginesy i oddech wertykalny
        verticalAlignment = Alignment.Top
    ) {
        // Kolumna Ikony (stała szerokość 48dp)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp)) // Odstęp 12dp (zgodny z Details)

        // Kontener treści
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}