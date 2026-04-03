package com.example.my_uz_android.ui.screens.account

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.BackupDataType
import com.example.my_uz_android.data.models.ThemeMode
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.components.TopBarActionIcon
import com.example.my_uz_android.ui.screens.account.components.ThemeSelector
import com.example.my_uz_android.ui.theme.ClassColorPalette
import com.example.my_uz_android.util.ClassTypeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- MAIN SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val activeSettings = uiState.settings

    var showExportDialog by remember { mutableStateOf(false) }
    var exportSelection by remember { mutableStateOf(BackupDataType.entries.toSet()) }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "Ustawienia",
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onBackClick,
                isNavigationIconFilled = true,
                actions = {
                    AnimatedVisibility(
                        visible = uiState.isSaved,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TopBarActionIcon(
                            icon = R.drawable.ic_check,
                            onClick = {},
                            isFilled = true,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp)
                ) {
                    SettingsHeader("Wygląd")

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Motyw aplikacji",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val currentTheme = try {
                            ThemeMode.valueOf(activeSettings?.themeMode ?: ThemeMode.SYSTEM.name)
                        } catch (e: Exception) {
                            ThemeMode.SYSTEM
                        }

                        ThemeSelector(
                            selectedTheme = currentTheme,
                            onThemeSelected = { viewModel.setThemeMode(it) }
                        )
                    }

                    if (uiState.uniqueClassTypes.isNotEmpty()) {
                        SettingsHeader("Kolory zajęć")
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            uiState.uniqueClassTypes.forEach { classType ->
                                val safeClassType = classType.trim()
                                ClassColorPickerRow(
                                    classType = ClassTypeUtils.getFullName(safeClassType),
                                    selectedColorIndex = uiState.classColorMap[safeClassType] ?: 0,
                                    onColorSelected = { viewModel.updateClassColor(safeClassType, it) }
                                )
                            }
                        }
                    }

                    SettingsHeader("Powiadomienia")
                    SettingsToggleItem(
                        iconRes = R.drawable.ic_bell,
                        title = "Włącz powiadomienia",
                        description = "Główny przełącznik powiadomień",
                        isChecked = activeSettings?.notificationsEnabled == true,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )

                    if (activeSettings?.notificationsEnabled == true) {
                        SettingsToggleItem(
                            iconRes = R.drawable.ic_clock,
                            title = "Zajęcia",
                            description = "15 minut przed rozpoczęciem",
                            isChecked = activeSettings.notificationsClasses,
                            onCheckedChange = { viewModel.toggleClassesNotifications(it) }
                        )
                    }

                    SettingsHeader("Dane i synchronizacja")
                    SettingsToggleItem(
                        iconRes = R.drawable.ic_wifi_off,
                        title = "Tryb offline",
                        description = "Używaj tylko danych lokalnych",
                        isChecked = activeSettings?.offlineModeEnabled == true,
                        onCheckedChange = { viewModel.toggleOfflineMode(it) }
                    )

                    SettingsActionItem(
                        iconRes = R.drawable.ic_export,
                        title = "Eksportuj dane",
                        description = "Zapisz kopię do pliku JSON",
                        onClick = {
                            exportSelection = BackupDataType.entries.toSet()
                            showExportDialog = true
                        }
                    )

                    SettingsActionItem(
                        iconRes = R.drawable.ic_import,
                        title = "Importuj dane",
                        description = "Przywróć dane z pliku",
                        isDestructive = true,
                        onClick = { importLauncher.launch("application/json") }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "MyUZ v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 32.dp)
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        DataTypeSelectionDialog(
            title = "Eksportuj dane",
            confirmText = "Eksportuj",
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
        DataTypeSelectionDialog(
            title = "Importuj dane",
            confirmText = "Przywróć",
            initialSelection = BackupDataType.entries.toSet(),
            onDismiss = { viewModel.cancelImport() },
            onConfirm = { selection -> viewModel.confirmImport(selection) }
        )
    }
}

// --- COMPONENTS ---

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(iconRes: Int, title: String, description: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(iconRes)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsActionItem(iconRes: Int, title: String, description: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(iconRes, isDestructive)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsIconContainer(iconRes: Int, isError: Boolean = false) {
    val bgColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val iconColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
        Icon(painterResource(iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun ClassColorPickerRow(classType: String, selectedColorIndex: Int, onColorSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = classType, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ClassColorPalette.forEachIndexed { index, colorSet ->
                val isSelected = index == selectedColorIndex
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorSet.lightBg)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) colorSet.lightAccent else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = if (colorSet.lightBg.luminance() > 0.5f) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- DIALOGS ---

@Composable
fun DataTypeSelectionDialog(
    title: String,
    confirmText: String,
    initialSelection: Set<BackupDataType>,
    onDismiss: () -> Unit,
    onConfirm: (Set<BackupDataType>) -> Unit
) {
    var selection by remember { mutableStateOf(initialSelection) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, // Zgodne z udostępnianiem
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BackupDataType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selection = if (selection.contains(type)) selection - type else selection + type
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = selection.contains(type),
                            onCheckedChange = { isChecked ->
                                selection = if (isChecked) selection + type else selection - type
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selection) }) {
                Text(confirmText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

// --- UTILS ---

private suspend fun saveBackupToFile(context: Context, uri: Uri, viewModel: SettingsViewModel, selection: Set<BackupDataType>) {
    withContext(Dispatchers.IO) {
        try {
            val jsonString = viewModel.createBackupJson(selection)
            context.contentResolver.openOutputStream(uri)?.use { it.write(jsonString.toByteArray()) }
            withContext(Dispatchers.Main) { Toast.makeText(context, "Zapisano dane!", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_LONG).show() }
        }
    }
}

private suspend fun readBackupFileToPreview(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            content?.let { viewModel.previewBackupFile(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
