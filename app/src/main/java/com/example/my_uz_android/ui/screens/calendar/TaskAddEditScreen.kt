package com.example.my_uz_android.ui.screens.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TaskDatePicker
import com.example.my_uz_android.ui.components.TaskTimePicker
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
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
    val context = LocalContext.current

    LaunchedEffect(uiState.isTaskSaved) {
        if (uiState.isTaskSaved) {
            Toast.makeText(context, "Zadanie zapisane", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    TaskAddEditContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSaveTask = viewModel::saveTask,
        onTitleChange = viewModel::updateTitle,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onSubjectChange = viewModel::updateSubject,
        onTypeChange = viewModel::updateType,
        onDescriptionChange = viewModel::updateDescription,
        onToggleSubjectModal = viewModel::toggleSubjectModal,
        modifier = modifier
    )
}

@Composable
fun TaskAddEditContent(
    uiState: TaskAddEditUiState,
    onNavigateBack: () -> Unit,
    onSaveTask: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
    onSubjectChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleSubjectModal: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val chipBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val chipTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    if (showDatePicker) {
        TaskDatePicker(
            onDateSelected = { timestamp ->
                timestamp?.let {
                    val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDateChange(date)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TaskTimePicker(
            initialTime = uiState.time ?: LocalTime.NOON,
            onTimeSelected = { time ->
                onTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (uiState.isSubjectModalVisible) {
        AlertDialog(
            onDismissRequest = { onToggleSubjectModal(false) },
            title = { Text("Wybierz przedmiot", fontFamily = InterFontFamily) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    uiState.availableSubjects.forEach { subject ->
                        TextButton(
                            onClick = { onSubjectChange(subject) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(text = subject, fontFamily = InterFontFamily, color = textColor, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        }
                        HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                    }
                    if (uiState.availableSubjects.isEmpty()) {
                        Text("Brak przedmiotów w planie.", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onToggleSubjectModal(false) }) { Text("Anuluj") } }
        )
    }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(id = R.drawable.ic_x_close), "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                // ZMIANA: CircleShape dla przycisku (Pigułka)
                Button(
                    onClick = { onSaveTask() },
                    enabled = uiState.title.isNotBlank(),
                    shape = CircleShape, // PIGUŁKA
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Zapisz", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                // 1. TYTUŁ (Pixel Perfect Matching Details)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.size(48.dp)) // Placeholder ikony
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                            BasicTextField(
                                value = uiState.title,
                                onValueChange = { onTitleChange(it) },
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
                                        Text("Dodaj tytuł", style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, color = textColor.copy(alpha = 0.4f)))
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp + 48.dp + 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.quickTitles) { title ->
                        Surface(
                            onClick = { onTitleChange(title) },
                            shape = RoundedCornerShape(12.dp),
                            color = if(uiState.title == title) primaryColor else chipBackgroundColor,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(text = title, style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = if(uiState.title == title) Color.White else chipTextColor))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 2. CZAS I DATA
                CommonRow(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cały dzień", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            Switch(
                                checked = uiState.isAllDay,
                                onCheckedChange = { isChecked -> onTimeChange(if (isChecked) null else LocalTime.NOON) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showDatePicker = true }.padding(vertical = 12.dp, horizontal = 4.dp)) {
                                Text(text = uiState.date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            }
                            if (!uiState.isAllDay) {
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { showTimePicker = true }
                                ) {
                                    Text(text = uiState.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "12:00", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold), color = primaryColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 3. PRZEDMIOT
                CommonRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Box(modifier = Modifier.fillMaxWidth().clickable { onToggleSubjectModal(true) }.padding(vertical = 12.dp)) {
                        Text(text = if (uiState.selectedSubject.isEmpty()) "Wybierz przedmiot" else uiState.selectedSubject, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 4. RODZAJ
                if (uiState.selectedSubject.isNotEmpty()) {
                    CommonRow(iconRes = R.drawable.ic_graduation_hat, iconTint = iconTint) {
                        var expandedType by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().clickable { expandedType = true }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (uiState.selectedType.isEmpty()) "Wybierz rodzaj" else uiState.selectedType, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                                Icon(painterResource(R.drawable.ic_chevron_down), null, tint = subTextColor, modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                                uiState.availableTypes.forEach { type ->
                                    DropdownMenuItem(text = { Text(type, fontFamily = InterFontFamily) }, onClick = { onTypeChange(type); expandedType = false })
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                }

                // 5. OPIS
                CommonRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = { onDescriptionChange(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = textColor, lineHeight = 24.sp),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField -> if (uiState.description.isEmpty()) Text("Dodaj opis", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = subTextColor)) else innerTextField() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun CommonRow(iconRes: Int, iconTint: Color, content: @Composable BoxScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(painterResource(id = iconRes), null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) { content() }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskAddEditPreview() {
    TaskAddEditContent(
        uiState = TaskAddEditUiState(title = "Test"),
        onNavigateBack = {}, onSaveTask = {}, onTitleChange = {}, onDateChange = {}, onTimeChange = {}, onSubjectChange = {}, onTypeChange = {}, onDescriptionChange = {}, onToggleSubjectModal = {}
    )
}