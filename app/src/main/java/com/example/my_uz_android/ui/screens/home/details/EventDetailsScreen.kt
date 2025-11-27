package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.MyUZTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event

    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun dismiss() {
        scope.launch {
            offsetY.animateTo(targetValue = 2000f)
            onBackClick()
        }
    }

    fun reset() {
        scope.launch { offsetY.animateTo(targetValue = 0f) }
    }

    MyUZTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = { if (offsetY.value > 150f) dismiss() else reset() },
                            onDragCancel = { reset() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = offsetY.value + dragAmount
                                scope.launch { offsetY.snapTo(newOffset.coerceAtLeast(0f)) }
                            }
                        )
                    }
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                if (event != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        // UCHWYT
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }

                        // GÓRNY PASEK
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { dismiss() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_x_close),
                                    contentDescription = "Zamknij",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row {
                                IconButton(onClick = { /* Edycja */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit_2),
                                        contentDescription = "Edytuj",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = { /* Menu */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dots_vertical),
                                        contentDescription = "Więcej",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // TREŚĆ
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(18.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            RoundedCornerShape(6.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${event.date} • ${event.timeRange}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                            EventDetailRow(
                                iconRes = R.drawable.ic_marker_pin,
                                text = event.location
                            )
                            EventDetailRow(
                                iconRes = R.drawable.ic_menu_2,
                                text = event.description
                            )
                            EventDetailRow(
                                iconRes = R.drawable.ic_calendar,
                                text = "Kalendarz główny"
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailRow(iconRes: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}