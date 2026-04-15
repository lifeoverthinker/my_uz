package com.example.my_uz_android.ui.screens.account

/**
 * Ekran ustawień aplikacji.
 * Pozwala użytkownikowi na zmianę motywu, kolorów zajęć, języka aplikacji,
 * zarządzanie powiadomieniami oraz dostęp do funkcji kopii zapasowej.
 */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.util.Locale

/**
 * Trasa ekranu ustawień spinająca nawigację z `SettingsViewModel`.
 */
@Composable
fun SettingsScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    SettingsScreen(
        onBackClick = onNavigateBack,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Główny ekran ustawień aplikacji.
 *
 * Pokazuje sekcje wyglądu, języka, powiadomień oraz backupu danych.
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.settings_screen_title),
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // --- WYGLĄD I JĘZYK ---
                    SettingsSection(title = stringResource(R.string.settings_appearance_section)) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.settings_theme_label),
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.settings_language_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LanguageSelector(
                                currentLanguage = normalizeAppLanguageForUi(activeSettings?.appLanguage),
                                onLanguageSelected = { viewModel.setAppLanguage(it) }
                            )
                        }
                    }

                    // --- PRZYWRÓCONE: KOLORY ZAJĘĆ ---
                    if (uiState.uniqueClassTypes.isNotEmpty()) {
                        SettingsSection(title = stringResource(R.string.settings_class_colors_section)) {
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
                    }

                    // --- POWIADOMIENIA ---
                    SettingsSection(title = stringResource(R.string.settings_notifications_section)) {
                        SettingsToggleItem(
                            iconRes = R.drawable.ic_bell,
                            title = stringResource(R.string.settings_notifications_push),
                            description = stringResource(R.string.settings_notifications_push_desc),
                            isChecked = activeSettings?.notificationsEnabled == true,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )

                        if (activeSettings?.notificationsEnabled == true) {
                            SettingsToggleItem(
                                iconRes = R.drawable.ic_clock,
                                title = stringResource(R.string.settings_notifications_classes_title),
                                description = stringResource(R.string.settings_notifications_classes_desc),
                                isChecked = activeSettings.notificationsClasses,
                                onCheckedChange = { viewModel.toggleClassesNotifications(it) }
                            )
                        }
                    }

                    // --- KOPIA ZAPASOWA ---
                    SettingsSection(title = stringResource(R.string.settings_backup_section)) {
                        SettingsActionItem(
                            iconRes = R.drawable.ic_export,
                            title = stringResource(R.string.btn_export),
                            description = stringResource(R.string.settings_backup_export_desc),
                            onClick = {
                                exportSelection = BackupDataType.entries.toSet()
                                showExportDialog = true
                            }
                        )

                        SettingsActionItem(
                            iconRes = R.drawable.ic_import,
                            title = stringResource(R.string.btn_import),
                            description = stringResource(R.string.settings_backup_import_desc),
                            isDestructive = true,
                            onClick = { importLauncher.launch("application/json") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
            title = stringResource(R.string.settings_export_data_title),
            confirmText = stringResource(R.string.btn_export),
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
            title = stringResource(R.string.settings_import_data_title),
            confirmText = stringResource(R.string.settings_restore),
            initialSelection = BackupDataType.entries.toSet(),
            onDismiss = { viewModel.cancelImport() },
            onConfirm = { selection -> viewModel.confirmImport(selection) }
        )
    }
}

// --- KOMPONENTY UI ---

@Composable
/**
 * Sekcja ustawień z nagłówkiem i wspólnym kontenerem wizualnym.
 */
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) { content() }
        }
    }
}

@Composable
/**
 * Mały przełącznik języka aplikacji (tylko `pl` i `en`).
 */
fun LanguageSelector(currentLanguage: String, onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        "pl" to stringResource(R.string.settings_language_polish),
        "en" to stringResource(R.string.settings_language_english)
    )
    Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        languages.forEach { (code, label) ->
            val isSelected = currentLanguage == code
            FilterChip(
                selected = isSelected,
                onClick = { onLanguageSelected(code) },
                label = { Text(label, modifier = Modifier.padding(horizontal = 4.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
/**
 * Element listy ustawień z przełącznikiem boolean.
 */
fun SettingsToggleItem(iconRes: Int, title: String, description: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
/**
 * Element akcji w ustawieniach uruchamiający jednorazowe działanie.
 */
fun SettingsActionItem(iconRes: Int, title: String, description: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
/**
 * Wiersz wyboru koloru dla typu zajęć.
 */
fun ClassColorPickerRow(classType: String, selectedColorIndex: Int, onColorSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = classType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
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
                            tint = if (colorSet.lightBg.luminance() > 0.5f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Dialog wyboru typów danych do eksportu/importu.
 */
fun DataTypeSelectionDialog(title: String, confirmText: String, initialSelection: Set<BackupDataType>, onDismiss: () -> Unit, onConfirm: (Set<BackupDataType>) -> Unit) {
    var selection by remember { mutableStateOf(initialSelection) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                BackupDataType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { selection = if (selection.contains(type)) selection - type else selection + type }
                    ) {
                        Checkbox(checked = selection.contains(type), onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = type.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selection) }) { Text(confirmText, style = MaterialTheme.typography.labelLarge) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), style = MaterialTheme.typography.labelLarge) } }
    )
}

/**
 * Zapisuje wybrane dane backupu do wskazanego pliku JSON.
 */
private suspend fun saveBackupToFile(context: Context, uri: Uri, viewModel: SettingsViewModel, selection: Set<BackupDataType>) {
    withContext(Dispatchers.IO) {
        try {
            val jsonString = viewModel.createBackupJson(selection)
            context.contentResolver.openOutputStream(uri)?.use { it.write(jsonString.toByteArray()) }
            withContext(Dispatchers.Main) { Toast.makeText(context, "Zapisano dane!", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
}

/**
 * Odczytuje plik backupu i przekazuje jego zawartość do podglądu importu.
 */
private suspend fun readBackupFileToPreview(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            content?.let { viewModel.previewBackupFile(it) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Nie można odczytać pliku.", Toast.LENGTH_LONG).show() }
        }
    }
}

/**
 * Normalizuje język ustawień do dwóch wspieranych wartości `pl`/`en` na potrzeby UI.
 */
private fun normalizeAppLanguageForUi(rawCode: String?): String = when (rawCode) {
    "pl" -> "pl"
    "en" -> "en"
    else -> if (Locale.getDefault().language == "en") "en" else "pl"
}
