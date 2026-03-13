package com.example.my_uz_android.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    date: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialUtcTime = remember(date) {
        val localDate = Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    val dateState = rememberDatePickerState(initialSelectedDateMillis = initialUtcTime)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    dateState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                // Używamy pogrubionego labelLarge dla czytelności akcji
                Text("OK", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.outline))
            }
        },
        // Korzystamy z systemowych kształtów MD3
        shape = MaterialTheme.shapes.extraLarge,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        tonalElevation = 0.dp
    ) {
        androidx.compose.material3.DatePicker(
            state = dateState,
            showModeToggle = false, // Ukrywamy ikonę edycji ręcznej dla czystszego wyglądu "Calendar"
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.secondary, // Mniejszy nacisk na tytuł
                headlineContentColor = MaterialTheme.colorScheme.onSurface, // Mocny nagłówek z datą
                weekdayContentColor = MaterialTheme.colorScheme.outline, // Dni tygodnia bardziej subtelne
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}