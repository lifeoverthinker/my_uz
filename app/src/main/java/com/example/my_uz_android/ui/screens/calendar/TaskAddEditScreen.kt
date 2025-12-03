package com.example.my_uz_android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
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

    // Kolory
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val taskAccentColor = MaterialTheme.extendedColors.classCardBackground

    LaunchedEffect(uiState.isTaskSaved) {
        if (uiState.isTaskSaved) onNavigateBack()
    }

    // Modal DatePicker
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

    // Modal Przedmiotu
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
            confirmButton = { TextButton(onClick = { viewModel.toggleSubjectModal(false) }) { Text("Anuluj") } }
        )
    }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(id = R.drawable.ic_x_close), "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                Button(
                    onClick = { viewModel.saveTask() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
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
                // 1. TYTUŁ (Użycie BasicTextField dla idealnego wyrównania z TaskDetails)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // Padding tylko z góry, boki obsłużone niżej
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ikona/Placeholder (48dp + 16dp paddingu zewnętrznego = 64dp od lewej, jak w details)
                    Box(modifier = Modifier.padding(start = 16.dp).size(48.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(taskAccentColor))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Input Tytułu
                    Box(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            textStyle = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp, // Match details
                                color = textColor
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "Dodaj tytuł",
                                        style = TextStyle(
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 28.sp,
                                            lineHeight = 36.sp,
                                            color = textColor.copy(alpha = 0.5f)
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
                Spacer(modifier = Modifier.height(16.dp))

                // 2. CZAS I DATA
                AddEditRow(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Cały dzień
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateTime(if (uiState.isAllDay) java.time.LocalTime.NOON else null) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cały dzień", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor, modifier = Modifier.weight(1f))
                            Switch(
                                checked = uiState.isAllDay,
                                onCheckedChange = { isChecked -> viewModel.updateTime(if (isChecked) null else java.time.LocalTime.NOON) }
                            )
                        }

                        // Data
                        Text(
                            text = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("pl"))),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = textColor,
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(vertical = 4.dp)
                        )

                        // Godzina
                        if (!uiState.isAllDay) {
                            Text(
                                text = uiState.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "12:00",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = textColor,
                                modifier = Modifier.fillMaxWidth().clickable { /* TODO: TimePicker */ }.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp)) // Divider wcięty
                Spacer(modifier = Modifier.height(16.dp))

                // 3. PRZEDMIOT
                AddEditRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSubjectModal(true) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState.selectedSubject.isEmpty()) {
                            Text("Wybierz przedmiot", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = subTextColor)
                        } else {
                            Text(uiState.selectedSubject, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                        }
                    }
                }

                // 4. RODZAJ
                if (uiState.selectedSubject.isNotEmpty()) {
                    AddEditRow(iconRes = R.drawable.ic_graduation_hat, iconTint = iconTint) {
                        var expandedType by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (uiState.selectedType.isEmpty()) "Wybierz rodzaj" else uiState.selectedType,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                                color = if (uiState.selectedType.isEmpty()) subTextColor else textColor,
                                modifier = Modifier.fillMaxWidth().clickable { expandedType = true }.padding(vertical = 12.dp)
                            )
                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // 5. OPIS (Naprawione kolory dla TextField)
                AddEditRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    TextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text("Dodaj opis", fontFamily = InterFontFamily, color = subTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = textColor),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, // FIX: Używamy IndicatorColor zamiast BorderColor
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditRow(
    iconRes: Int?,
    iconTint: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp), // Padding poziomy tutaj
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (iconRes != null) {
                Icon(painterResource(id = iconRes), null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(top = 10.dp)) { // Wyrównanie do środka ikonki
            content()
        }
    }
}