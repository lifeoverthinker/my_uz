package com.example.my_uz_android.ui.screens.account

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.example.my_uz_android.util.ClassTypeUtils // [Dodano import]
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onBackClick: () -> Unit = onNavigateBack,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportSelection by remember { mutableStateOf(BackupDataType.values().toSet()) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { scope.launch { saveBackupToFile(context, it, viewModel, exportSelection) } }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { scope.launch { readBackupFileToPreview(context, it, viewModel) } }
    }

    LaunchedEffect(uiState.importMessage) {
        uiState.importMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Ustawienia",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sekcja: Wygląd
                SettingsSection(title = "Wygląd") {
                    SettingsSwitchItem(
                        iconRes = R.drawable.ic_palette,
                        title = "Ciemny motyw",
                        isChecked = uiState.settings?.isDarkMode == true,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }

                // Sekcja: Kolory zajęć
                if (uiState.uniqueClassTypes.isNotEmpty()) {
                    SettingsSection(title = "Kolory zajęć") {
                        uiState.uniqueClassTypes.forEach { classType ->
                            val colorIndex = uiState.classColorMap[classType]
                                ?: kotlin.math.abs(classType.hashCode()) % ClassColorPalette.size

                            ClassColorPickerItem(
                                // [ZMIANA] Użycie getFullName do wyświetlania pełnej nazwy
                                classType = ClassTypeUtils.getFullName(classType),
                                selectedColorIndex = colorIndex,
                                onColorSelected = { newIndex ->
                                    viewModel.updateClassColor(classType, newIndex)
                                }
                            )
                        }
                    }
                }

                // Sekcja: Aplikacja i Dane
                SettingsSection(title = "Aplikacja i Dane") {
                    SettingsSwitchItem(
                        iconRes = R.drawable.ic_check_circle_broken,
                        title = "Tryb Offline",
                        subtitle = "Wymusza korzystanie z lokalnej bazy",
                        isChecked = uiState.settings?.offlineModeEnabled == true,
                        onCheckedChange = { viewModel.toggleOfflineMode(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsActionItem(
                        iconRes = R.drawable.ic_share,
                        title = "Eksportuj dane",
                        subtitle = "Wybierz dane do zapisu w pliku JSON",
                        onClick = {
                            exportSelection = BackupDataType.values().toSet()
                            showExportDialog = true
                        }
                    )

                    SettingsActionItem(
                        iconRes = R.drawable.ic_info_circle,
                        title = "Importuj dane",
                        subtitle = "Wczytaj kopię zapasową z pliku",
                        isDestructive = true,
                        onClick = { importLauncher.launch("application/json") }
                    )
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Ustawienia zostały zapisane", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Zapisz ustawienia", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "MyUZ v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // --- Dialogi ---
    if (showExportDialog) {
        DataTypeSelectionDialog(
            title = "Eksportuj dane",
            confirmText = "Zapisz do pliku",
            initialSelection = exportSelection,
            onDismiss = { showExportDialog = false },
            onConfirm = { selection ->
                exportSelection = selection
                showExportDialog = false
                exportLauncher.launch("myuz_backup_${System.currentTimeMillis()}.json")
            }
        )
    }

    if (uiState.showImportDialog && uiState.backupPreview != null) {
        val availableTypes = remember(uiState.backupPreview) {
            val preview = uiState.backupPreview!!
            val types = mutableSetOf<BackupDataType>()
            if (preview.settings != null) types.add(BackupDataType.SETTINGS)
            if (preview.classes.isNotEmpty()) types.add(BackupDataType.CLASSES)
            if (preview.tasks.isNotEmpty()) types.add(BackupDataType.TASKS)
            if (preview.grades.isNotEmpty()) types.add(BackupDataType.GRADES)
            if (preview.absences.isNotEmpty()) types.add(BackupDataType.ABSENCES)
            if (preview.events.isNotEmpty()) types.add(BackupDataType.EVENTS)
            types
        }

        DataTypeSelectionDialog(
            title = "Importuj dane",
            confirmText = "Przywróć wybrane",
            initialSelection = availableTypes,
            availableTypes = availableTypes,
            onDismiss = { viewModel.cancelImport() },
            onConfirm = { selection -> viewModel.confirmImport(selection) }
        )
    }
}

// --- Komponenty pomocnicze ---

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsActionItem(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ClassColorPickerItem(
    classType: String,
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = classType,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ClassColorPalette.forEachIndexed { index, colorSet ->
                val isSelected = index == selectedColorIndex
                val borderColor = if (isSelected) MaterialTheme.colorScheme.onSurface else colorSet.lightAccent.copy(alpha = 0.3f)
                val borderWidth = if (isSelected) 2.dp else 1.dp

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorSet.lightBg)
                        .border(borderWidth, borderColor, CircleShape)
                        .clickable { onColorSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = colorSet.lightAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DataTypeSelectionDialog(
    title: String,
    confirmText: String,
    initialSelection: Set<BackupDataType>,
    availableTypes: Set<BackupDataType> = BackupDataType.values().toSet(),
    onDismiss: () -> Unit,
    onConfirm: (Set<BackupDataType>) -> Unit
) {
    var selection by remember { mutableStateOf(initialSelection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    text = "Wybierz elementy:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                BackupDataType.values().forEach { type ->
                    val isAvailable = availableTypes.contains(type)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isAvailable) {
                                selection = if (selection.contains(type)) selection - type else selection + type
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selection.contains(type),
                            onCheckedChange = { isChecked ->
                                selection = if (isChecked) selection + type else selection - type
                            },
                            enabled = isAvailable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAvailable) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selection) },
                enabled = selection.isNotEmpty()
            ) {
                Text(confirmText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

private suspend fun saveBackupToFile(
    context: Context,
    uri: Uri,
    viewModel: SettingsViewModel,
    selection: Set<BackupDataType>
) {
    withContext(Dispatchers.IO) {
        try {
            val jsonString = viewModel.createBackupJson(selection)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            withContext(Dispatchers.Main) { Toast.makeText(context, "Zapisano wybrane dane!", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
}

private suspend fun readBackupFileToPreview(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) { stringBuilder.append(line); line = reader.readLine() }
                }
            }
            viewModel.previewBackupFile(stringBuilder.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}