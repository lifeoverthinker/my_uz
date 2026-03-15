package com.example.my_uz_android.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.ui.screens.account.SettingsViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BackupSettingsSection(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var jsonContentToSave by remember { mutableStateOf<String?>(null) }

    // ---------------------------------------------------------
    // LAUNCHER DO ZAPISU PLIKU (EKSPORT)
    // ---------------------------------------------------------
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { destinationUri ->
            jsonContentToSave?.let { content ->
                try {
                    context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                        Toast.makeText(context, "Kopia zapasowa zapisana!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Błąd zapisu pliku", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ---------------------------------------------------------
    // LAUNCHER DO ODCZYTU PLIKU (IMPORT)
    // ---------------------------------------------------------
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                // Odczytujemy zawartość wybranego pliku
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                reader.close()
                inputStream?.close()

                // Przekazujemy odczytany tekst do ViewModelu
                viewModel.importDatabase(
                    json = jsonString,
                    onSuccess = {
                        Toast.makeText(context, "Pomyślnie zaimportowano dane!", Toast.LENGTH_LONG).show()
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, "Błąd importu: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd odczytu pliku. Upewnij się, że to poprawny plik kopii zapasowej.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ---------------------------------------------------------
    // WIDOK (UI)
    // ---------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Kopia zapasowa",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Eksportuj lub importuj swoje oceny, zadania, plan zajęć i ustawienia.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PRZYCISK EKSPORTU
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.exportDatabase { json ->
                        jsonContentToSave = json
                        // Generujemy domyślną nazwę pliku z datą
                        val dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
                        createDocumentLauncher.launch("my_uz_backup_$dateString.json")
                    }
                }
            ) {
                Text("Eksportuj")
            }

            // PRZYCISK IMPORTU
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    // Odpalamy systemowe okno wyboru pliku (filtrujemy po JSON)
                    openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
                }
            ) {
                Text("Importuj")
            }
        }
    }
}