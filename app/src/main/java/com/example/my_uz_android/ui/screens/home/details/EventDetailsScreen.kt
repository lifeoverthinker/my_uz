package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val backgroundColor = Color(0xFFF7F2F9)
    val onBackgroundColor = Color(0xFF1D192B)
    val greenContainer = Color(0xFFDAF5D7)

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
                .background(backgroundColor)
        ) {
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 48.dp)
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
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        spotColor = Color(0x26000000)
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                if (event != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. IKONA ZAMKNIĘCIA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                onClick = { dismiss() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_x_close),
                                    contentDescription = "Zamknij",
                                    tint = onBackgroundColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // 2. TYTUŁ I DATA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Box 48x48
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(greenContainer, RoundedCornerShape(4.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black,
                                        lineHeight = 28.sp
                                    )
                                )
                                Text(
                                    text = "${event.date} • ${event.timeRange}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        color = Color(0xFF494949)
                                    )
                                )
                            }
                        }

                        // 3. SZCZEGÓŁY
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Lokalizacja
                            EventDetailRow(
                                iconRes = R.drawable.ic_marker_pin,
                                text = event.location
                            )
                            // Opis (Menu 2)
                            EventDetailRow(
                                iconRes = R.drawable.ic_menu_2,
                                text = event.description
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = onBackgroundColor)
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
        // Box 48x48
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF494949),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                color = Color(0xFF1D192B),
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}