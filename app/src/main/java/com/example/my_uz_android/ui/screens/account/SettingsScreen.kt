package com.example.my_uz_android.ui.screens.account

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import com.example.my_uz_android.util.ClassTypeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val activeSettings = uiState.draftSettings ?: uiState.settings

    var showExportDialog by remember { mutableStateOf(false) }
    var exportSelection by remember { mutableStateOf(BackupDataType.values().toSet()) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Zapisano ustawienia", Toast.LENGTH_SHORT).show()
        }
    }

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
                isNavigationIconFilled = true, // Używamy stylu z kółkiem (jak w Terminarzu) dla spójności
                actions = {
                    // PRZYCISK ZAPISZ - widoczny po prawej stronie
                    TextButton(
                        onClick = { if (uiState.isModified) viewModel.saveSettings() },
                        enabled = uiState.isModified, // Aktywny tylko gdy są zmiany
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text(
                            text = "Zapisz",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp)
            ) {
                SettingsHeader("Wygląd")
                SettingsToggleItem(
                    iconRes = R.drawable.ic_palette,
                    title = "Ciemny motyw",
                    description = "Dostosuj jasność interfejsu",
                    isChecked = activeSettings?.isDarkMode == true,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )

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

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsHeader("Dane i synchronizacja")
                SettingsToggleItem(
                    iconRes = R.drawable.ic_check_circle_broken,
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
                        exportSelection = BackupDataType.values().toSet()
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
                Image(
                    painter = painterResource(R.drawable.settings_rafiki),
                    contentDescription = null,
                    modifier = Modifier.height(160.dp).fillMaxWidth().padding(horizontal = 48.dp),
                    alpha = 0.7f
                )

                Text(
                    text = "MyUZ v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp, bottom = 32.dp)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
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
            initialSelection = BackupDataType.values().toSet(),
            onDismiss = { viewModel.cancelImport() },
            onConfirm = { selection -> viewModel.confirmImport(selection) }
        )
    }
}

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
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!isChecked) }.padding(horizontal = 16.dp, vertical = 12.dp),
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(iconRes, isDestructive)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
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
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(colorSet.lightBg).border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) colorSet.lightAccent else Color.Transparent, shape = CircleShape).clickable { onColorSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = colorSet.lightAccent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun DataTypeSelectionDialog(title: String, confirmText: String, initialSelection: Set<BackupDataType>, onDismiss: () -> Unit, onConfirm: (Set<BackupDataType>) -> Unit) {
    var selection by remember { mutableStateOf(initialSelection) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BackupDataType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                            selection = if (selection.contains(type)) selection - type else selection + type
                        }.padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = selection.contains(type), onCheckedChange = {
                            selection = if (it) selection + type else selection - type
                        })
                        Text(type.displayName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selection) }) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

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
        } catch (e: Exception) { e.printStackTrace() }
    }
}