package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.MyUZTheme

// --- WRAPPER DLA NAWIGACJI ---
@Composable
fun EventDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.eventEntity != null) {
        EventDetailsContent(
            event = uiState.eventEntity!!,
            onBackClick = onBackClick
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nie znaleziono szczegółów wydarzenia.", color = MaterialTheme.colorScheme.error)
        }
    }
}

// --- BEZSTANOWY WIDOK ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsContent(
    event: EventEntity,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "",
                navigationIcon = R.drawable.ic_close,
                isNavigationIconFilled = true,
                onNavigationClick = onBackClick,
                actions = {
                    // Brak akcji Edytuj / Usuń - to globalne wydarzenia!
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Nagłówek ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp)) // Zielony dla wydarzeń
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${event.date} • ${event.timeRange}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Lokalizacja ---
            if (event.location.isNotBlank()) {
                DetailRow(
                    iconRes = R.drawable.ic_marker_pin,
                    label = "Miejsce",
                    value = event.location
                )
            }

            // --- Opis ---
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    iconRes = R.drawable.ic_menu_2,
                    label = null,
                    value = event.description,
                    isMultiline = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Komponent Pomocniczy dla Wierszy ---
@Composable
private fun DetailRow(
    iconRes: Int,
    label: String?,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = if (isMultiline) 4.dp else 0.dp).size(24.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value.ifEmpty { "-" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = valueColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventDetailsScreenPreview() {
    MyUZTheme {
        EventDetailsContent(
            event = EventEntity(
                id = 1,
                title = "Juwenalia 2026",
                description = "Największa impreza roku! Muzyka na żywo, food trucki i mnóstwo atrakcji studenckich na kampusie B.",
                date = "Piątek, 20 maja 2026",
                location = "Kampus B, Uniwersytet Zielonogórski",
                timeRange = "18:00 - 02:00"
            ),
            onBackClick = {}
        )
    }
}