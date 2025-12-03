package com.example.my_uz_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.my_uz_android.ui.theme.InterFontFamily
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimePicker(
    initialTime: LocalTime = LocalTime.NOON,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    // Kolor tła (Biały w light / Ciemny w dark)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = surfaceColor, // WYMUSZENIE KOLORU SURFACE
            tonalElevation = 0.dp, // Brak cienia tonalnego (fioletu)
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(shape = RoundedCornerShape(28.dp), color = surfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz godzinę",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant, // Tło tarczy (lekko szare)
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = surfaceColor, // Tło kontenera
                        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        periodSelectorUnselectedContainerColor = surfaceColor,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj", fontFamily = InterFontFamily, fontWeight = FontWeight.Medium)
                    }
                    TextButton(
                        onClick = {
                            val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            onTimeSelected(selectedTime)
                        }
                    ) {
                        Text("OK", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}