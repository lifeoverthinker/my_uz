package com.example.my_uz_android.ui.screens.index

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.DatePicker
import com.example.my_uz_android.ui.screens.calendar.tasks.FormRow
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAbsenceScreen(
    absenceId: Int?,
    prefilledSubject: String? = null,
    prefilledClassType: String? = null,
    prefilledDate: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditAbsenceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(isSaved) {
        if (isSaved) {
            Toast.makeText(context, "Operacja zakończona pomyślnie", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(absenceId, prefilledSubject, prefilledClassType, prefilledDate) {
        if (absenceId != null && absenceId != 0) {
            viewModel.loadAbsence(absenceId)
        } else {
            if (uiState.id == 0 && uiState.subjectName == null) {
                viewModel.initNewAbsence(prefilledSubject, prefilledClassType)
                if (prefilledDate != null) viewModel.updateDate(prefilledDate)
            }
        }
    }

    Dialog(
        onDismissRequest = onNavigateBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.ic_x_close), "Zamknij")
                    }
                    Button(
                        onClick = { viewModel.saveAbsence() },
                        enabled = !uiState.subjectName.isNullOrBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        ) { paddingValues ->
            AddEditAbsenceContent(
                uiState = uiState,
                onSubjectChange = viewModel::updateSubjectName,
                onClassTypeChange = viewModel::updateClassType,
                onDescriptionChange = viewModel::updateDescription,
                onDateChange = viewModel::updateDate,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun AddEditAbsenceContent(
    uiState: AddEditAbsenceUiState,
    onSubjectChange: (String?) -> Unit,
    onClassTypeChange: (String?) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubjectModal by remember { mutableStateOf(false) }
    var showTypeModal by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isTypeSelectionEnabled = !uiState.subjectName.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(40.dp))
            Text(
                text = if(uiState.id != 0) "Edytuj nieobecność" else "Nowa nieobecność",
                style = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))

        FormRow(iconRes = R.drawable.ic_calendar, onClick = { showDatePicker = true }) {
            val date = Instant.ofEpochMilli(uiState.date).atZone(ZoneId.systemDefault()).toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("pl"))
            Text(text = date.format(formatter), style = MaterialTheme.typography.bodyLarge)
        }

        FormRow(iconRes = R.drawable.ic_book_open, onClick = { showSubjectModal = true }) {
            Text(
                text = uiState.subjectName ?: "Wybierz przedmiot",
                style = MaterialTheme.typography.bodyLarge,
                color = if (uiState.subjectName == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }

        FormRow(
            iconRes = R.drawable.ic_graduation_hat,
            onClick = if (isTypeSelectionEnabled) { { showTypeModal = true } } else null
        ) {
            val typeText = uiState.classType?.let { ClassTypeUtils.getFullName(it) } ?: "Rodzaj zajęć (opcjonalne)"
            Text(
                text = typeText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (!isTypeSelectionEnabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
            )
        }

        FormRow(iconRes = R.drawable.ic_menu_2) {
            BasicTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (uiState.description.isEmpty()) Text("Dodaj opis (opcjonalnie)", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        DatePicker(
            date = uiState.date,
            onDateSelected = { onDateChange(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showSubjectModal) {
        Dialog(onDismissRequest = { showSubjectModal = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                    item { Text("Wybierz przedmiot", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(24.dp)) }
                    items(uiState.availableSubjects) { (subject, _) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSubjectChange(subject); onClassTypeChange(null); showSubjectModal = false }.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            RadioButton(selected = uiState.subjectName == subject, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(subject)
                        }
                    }
                }
            }
        }
    }

    if (showTypeModal) {
        val availableTypes = uiState.availableSubjects.find { it.first == uiState.subjectName }?.second ?: emptyList()
        Dialog(onDismissRequest = { showTypeModal = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
                    item { Text("Wybierz rodzaj", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(24.dp)) }
                    items(availableTypes) { type ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onClassTypeChange(type); showTypeModal = false }.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            RadioButton(selected = uiState.classType == type, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(ClassTypeUtils.getFullName(type))
                        }
                    }
                }
            }
        }
    }
}