package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.EventEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.getAppBackgroundColor

@Composable
fun EventDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        DetailsLoadingState()
        return
    }

    val event = uiState.eventEntity
    if (event == null) {
        DetailsEmptyState(
            title = stringResource(R.string.event_details_empty_title),
            description = stringResource(R.string.event_details_empty_message)
        )
        return
    }

    EventDetailsContent(
        event = event,
        onBackClick = onBackClick
    )
}

@Composable
fun EventDetailsContent(
    event: EventEntity,
    onBackClick: () -> Unit
) {
    DetailsScaffold(onBackClick = onBackClick) {
        DetailsHeader(
            title = event.title,
            subtitle = "${event.date} • ${event.timeRange}",
            squareColor = getAppBackgroundColor(2)
        )

        if (event.location.isNotBlank()) {
            DetailsInfoRow(
                iconRes = R.drawable.ic_marker_pin,
                label = stringResource(R.string.label_place),
                value = event.location
            )
        }

        if (event.description.isNotBlank()) {
            DetailsInfoRow(
                iconRes = R.drawable.ic_menu_2,
                label = stringResource(R.string.label_description),
                value = event.description,
                isMultiline = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailsContentPreview() {
    MyUZTheme {
        EventDetailsContent(
            event = EventEntity(
                id = 1,
                title = "Dzień Otwarty Wydziału",
                date = "Czwartek, 2 kwietnia 2026",
                timeRange = "10:00 - 14:00",
                location = "Budynek C, Aula Główna",
                description = "Spotkania z prowadzącymi i prezentacja kierunków."
            ),
            onBackClick = {}
        )
    }
}