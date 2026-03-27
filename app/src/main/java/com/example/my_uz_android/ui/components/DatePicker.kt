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
                onClick = { dateState.selectedDateMillis?.let { onDateSelected(it) } }
            ) {
                Text("OK", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = MaterialTheme.colorScheme.outline)
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        androidx.compose.material3.DatePicker(
            state = dateState,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}