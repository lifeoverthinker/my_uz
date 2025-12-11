package com.example.my_uz_android.ui.screens.index

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddEditGradeScreen(
    gradeId: Int?,
    prefilledSubject: String? = null,
    prefilledClassType: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditGradeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(prefilledSubject, prefilledClassType) {
        if (gradeId == null) {
            prefilledSubject?.let { viewModel.updateSubjectName(it) }
            prefilledClassType?.let { viewModel.updateClassType(it) }
        }
    }

    AddEditGradeContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSaveGrade = {
            viewModel.saveGrade {
                Toast.makeText(context, "Ocena zapisana", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
        },
        onSubjectChange = viewModel::updateSubjectName,
        onClassTypeChange = viewModel::updateClassType,
        onGradeTypeChange = viewModel::updateGradeType,
        onGradeValueChange = viewModel::updateGradeValue,
        onCustomGradeChange = viewModel::updateCustomGradeValue,
        onWeightChange = viewModel::updateWeight,
        onDescriptionChange = viewModel::updateDescription,
        onCommentChange = viewModel::updateComment, // ✅
        onDateChange = viewModel::updateDate,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGradeContent(
    uiState: AddEditGradeUiState,
    onNavigateBack: () -> Unit,
    onSaveGrade: () -> Unit,
    onSubjectChange: (String?) -> Unit,
    onClassTypeChange: (String?) -> Unit,
    onGradeTypeChange: (GradeType) -> Unit,
    onGradeValueChange: (Double?) -> Unit,
    onCustomGradeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCommentChange: (String) -> Unit, // ✅
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val quickTitles = listOf(
        "Kolokwium", "Egzamin", "Kartkówka", "Wejściówka", "Projekt", "Aktywność"
    )

    var showSubjectModal by remember { mutableStateOf(false) }
    var showTypeModal by remember { mutableStateOf(false) }
    var showGradeModal by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val standardGrades = listOf(2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)
    val isTypeSelectionEnabled = !uiState.subjectName.isNullOrEmpty()

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.fillMaxSize().statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER
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
                    Icon(painter = painterResource(id = R.drawable.ic_x_close), contentDescription = "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                }

                Button(
                    onClick = onSaveGrade,
                    enabled = uiState.subjectName != null,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(stringResource(R.string.btn_save), fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. TYTUŁ (Duży na górze)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.size(48.dp)) // Spacer
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        BasicTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            textStyle = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, color = textColor),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.description.isEmpty()) {
                                    Text(
                                        text = "Dodaj tytuł",
                                        style = TextStyle(fontFamily = InterFontFamily, fontSize = 28.sp, color = textColor.copy(alpha = 0.4f))
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                // QUICK CHIPS
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 76.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTitles) { title ->
                        val isSelected = uiState.description == title
                        Surface(
                            onClick = { onDescriptionChange(title) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primaryColor else Color.Transparent,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, dividerColor) else null,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(text = title, style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else textColor))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor)

                // 2. DATA
                CommonRowGrade(iconRes = R.drawable.ic_clock, iconTint = iconTint) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = formatDateLong(uiState.date),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = textColor
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 3. PRZEDMIOT
                CommonRowGrade(iconRes = R.drawable.ic_book_open, iconTint = iconTint) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showSubjectModal = true }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.subjectName ?: "Wybierz przedmiot",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (uiState.subjectName == null) subTextColor else textColor
                        )
                        Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = subTextColor, modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 4. RODZAJ ZAJĘĆ
                CommonRowGrade(iconRes = R.drawable.ic_graduation_hat, iconTint = if (isTypeSelectionEnabled) iconTint else iconTint.copy(alpha = 0.4f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = isTypeSelectionEnabled) { showTypeModal = true }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val typeText = uiState.classType?.let { ClassTypeUtils.getFullName(it) } ?: "Rodzaj zajęć"
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
                            color = if (!isTypeSelectionEnabled) subTextColor.copy(alpha = 0.4f) else if (uiState.classType == null) subTextColor else textColor
                        )
                        Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = if (isTypeSelectionEnabled) subTextColor else subTextColor.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 5. OCENA
                CommonRowGrade(iconRes = R.drawable.ic_trophy, iconTint = iconTint) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showGradeModal = true }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val gradeText = when (uiState.gradeType) {
                            GradeType.STANDARD -> uiState.gradeValue?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "Wybierz ocenę"
                            GradeType.CUSTOM -> uiState.customGradeValue.ifBlank { "Wpisz wartość" }
                            GradeType.ACTIVITY -> "Aktywność +"
                        }
                        Text(text = gradeText, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = if (uiState.gradeValue == null && uiState.gradeType != GradeType.ACTIVITY) subTextColor else textColor)
                        Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = subTextColor, modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 6. WAGA
                CommonRowGrade(iconRes = R.drawable.ic_scales, iconTint = iconTint) {
                    OutlinedTextField(
                        value = uiState.weight,
                        onValueChange = onWeightChange,
                        label = { Text("Waga", fontFamily = InterFontFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = dividerColor)

                // 7. OPIS (DODATKOWY KOMENTARZ) - ✅ TERAZ DZIAŁA!
                CommonRowGrade(iconRes = R.drawable.ic_menu_2, iconTint = iconTint) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        BasicTextField(
                            value = uiState.comment,
                            onValueChange = onCommentChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = InterFontFamily,
                                color = textColor,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            decorationBox = { innerTextField ->
                                if (uiState.comment.isEmpty()) {
                                    Text(
                                        text = "Dodaj opis",
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

                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // --- Dialogi (Bez zmian) ---
        if (showDatePicker) {
            DatePicker(
                date = uiState.date,
                onDateSelected = { millis ->
                    onDateChange(millis)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // --- Modale (Przedmiot, Typ, Ocena) ---
        // ... (Tu jest kod modali, który już masz poprawny)
        if (showSubjectModal) {
            Dialog(onDismissRequest = { showSubjectModal = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                        item { Text(text = "Wybierz przedmiot", style = MaterialTheme.typography.titleLarge.copy(fontFamily = InterFontFamily), color = textColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
                        items(uiState.availableSubjects) { (subject, _) ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onSubjectChange(subject); onClassTypeChange(null); showSubjectModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = uiState.subjectName == subject, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(subject, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            }
                        }
                    }
                }
            }
        }

        if (showTypeModal) {
            val selectedSubject = uiState.availableSubjects.find { it.first == uiState.subjectName }
            val availableTypes = selectedSubject?.second ?: emptyList()
            Dialog(onDismissRequest = { showTypeModal = false }) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                        item { Text(text = "Wybierz rodzaj zajęć", style = MaterialTheme.typography.titleLarge.copy(fontFamily = InterFontFamily), color = textColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
                        items(availableTypes) { type ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onClassTypeChange(type); showTypeModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = uiState.classType == type, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(ClassTypeUtils.getFullName(type), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            }
                        }
                    }
                }
            }
        }

        if (showGradeModal) {
            Dialog(onDismissRequest = { showGradeModal = false }) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                        item { Text(text = "Wybierz ocenę", style = MaterialTheme.typography.titleLarge.copy(fontFamily = InterFontFamily), color = textColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
                        items(standardGrades) { grade ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onGradeTypeChange(GradeType.STANDARD); onGradeValueChange(grade); showGradeModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = uiState.gradeValue == grade && uiState.gradeType == GradeType.STANDARD, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (grade % 1.0 == 0.0) grade.toInt().toString() else grade.toString(), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth().clickable { onGradeTypeChange(GradeType.ACTIVITY); onCustomGradeChange("+"); showGradeModal = false }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = uiState.gradeType == GradeType.ACTIVITY, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Aktywność +", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily), color = textColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommonRowGrade(
    iconRes: Int,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

private fun formatDateLong(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))
    return date.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) }
}