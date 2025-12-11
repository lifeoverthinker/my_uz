package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.AbsenceEntity
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAbsenceScreen(
    availableClasses: List<ClassEntity>,
    initialSubject: String? = null,
    initialType: String? = null,
    existingAbsence: AbsenceEntity? = null,
    onSave: (String, String, Long, String?) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("Nieobecność") }
    var selectedSubject by remember { mutableStateOf(existingAbsence?.subjectName ?: initialSubject ?: "") }
    var selectedType by remember { mutableStateOf(existingAbsence?.classType ?: initialType ?: "") }
    var description by remember { mutableStateOf(existingAbsence?.description ?: "") }

    var selectedDate by remember {
        mutableStateOf(
            if (existingAbsence != null)
                Instant.ofEpochMilli(existingAbsence.date).atZone(ZoneId.systemDefault()).toLocalDate()
            else
                LocalDate.now()
        )
    }

    var showSubjectModal by remember { mutableStateOf(false) }
    var showTypeModal by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val uniqueSubjects = remember(availableClasses) {
        availableClasses.map { it.subjectName }.distinct().sorted()
    }
    val availableTypes = remember(selectedSubject, availableClasses) {
        availableClasses
            .filter { it.subjectName == selectedSubject }
            .map { it.classType }
            .distinct()
            .sorted()
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val isFormValid = selectedSubject.isNotBlank() && selectedType.isNotBlank()

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER (Zgodny z TaskAddEditScreen) ---
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
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Button(
                    onClick = {
                        val dateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onSave(selectedSubject, selectedType, dateMillis, description.ifBlank { null })
                    },
                    enabled = isFormValid,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Zapisz", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- TYTUŁ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.size(48.dp)) // Spacer pod ikonę (dla wyrównania)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = textColor
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                // --- DATA ---
                AbsenceCommonRow(iconRes = R.drawable.ic_calendar, iconTint = iconTint) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateLong(selectedDate),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = textColor
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // --- PRZEDMIOT ---
                AbsenceCommonRow(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSubjectModal = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSubject.isBlank()) "Wybierz przedmiot" else selectedSubject,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (selectedSubject.isBlank()) subTextColor else textColor
                        )
                        Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = subTextColor, modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(color = dividerColor)

                // --- RODZAJ ---
                val isTypeEnabled = selectedSubject.isNotBlank()
                AbsenceCommonRow(
                    iconRes = R.drawable.ic_graduation_hat,
                    iconTint = if (isTypeEnabled) iconTint else iconTint.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isTypeEnabled) { showTypeModal = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val typeText = if (selectedType.isBlank()) "Rodzaj zajęć" else ClassTypeUtils.getFullName(selectedType)
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (!isTypeEnabled) subTextColor.copy(alpha = 0.4f) else if (selectedType.isBlank()) subTextColor else textColor
                        )
                        Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = if (isTypeEnabled) subTextColor else subTextColor.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(color = dividerColor)

                // --- OPIS ---
                AbsenceCommonRow(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = textColor, lineHeight = 24.sp),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (description.isEmpty()) {
                                    Text("Dodaj opis...", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = subTextColor))
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // --- USUWANIE ---
                if (existingAbsence != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black), // ✅ CZARNY KOSZ I TEKST
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_trash), // ✅ TWOJA IKONA KOSZA
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Usuń nieobecność", fontFamily = InterFontFamily, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // --- Modale (Bez zmian) ---
        if (showDatePicker) {
            val dateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            DatePicker(
                date = dateMillis,
                onDateSelected = { newMillis -> selectedDate = Instant.ofEpochMilli(newMillis).atZone(ZoneId.systemDefault()).toLocalDate(); showDatePicker = false },
                onDismiss = { showDatePicker = false }
            )
        }
        if (showSubjectModal) {
            Dialog(onDismissRequest = { showSubjectModal = false }) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                        items(uniqueSubjects) { subject ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { selectedSubject = subject; selectedType = ""; showSubjectModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedSubject == subject, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(subject, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = textColor))
                            }
                        }
                    }
                }
            }
        }
        if (showTypeModal) {
            Dialog(onDismissRequest = { showTypeModal = false }) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                        items(availableTypes) { type ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { selectedType = type; showTypeModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == type, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(ClassTypeUtils.getFullName(type), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily, color = textColor))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Komponent wiersza (spójny z TaskAddEdit)
@Composable
fun AbsenceCommonRow(iconRes: Int, iconTint: Color, content: @Composable BoxScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { content() }
    }
}

private fun formatDateLong(date: LocalDate): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))
        date.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) }
    } catch (e: Exception) {
        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}