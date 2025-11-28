package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event
    val iconTint = Color(0xFF444746)
    val eventAccentColor = Color(0xFF0F9D58) // Zielony akcent

    // PRZYWRÓCONO ZAOKRĄGLENIE
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // --- GEST ZAMYKANIA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 10) onBackClick()
                        }
                    }
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE0E0E0))
                    )
                }

                // Header (Tylko X)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    DetailIconBox(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_x_close),
                            contentDescription = "Zamknij",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (event != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- NAGŁÓWEK (Tytuł, Data i ZIELONY KWADRAT) ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // ZIELONY KWADRAT w boksie 48dp
                        DetailIconBox {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(eventAccentColor)
                            )
                        }

                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = event.title,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "${event.date}, ${event.timeRange}",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color(0xFF444746)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(start = 56.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SZCZEGÓŁY ---

                    if (event.location.isNotEmpty()) {
                        DetailSection(
                            label = "MIEJSCE",
                            text = event.location,
                            iconRes = R.drawable.ic_marker_pin,
                            iconColor = iconTint
                        )
                    }

                    if (event.description.isNotEmpty()) {
                        DetailSection(
                            label = "OPIS",
                            text = event.description,
                            iconRes = R.drawable.ic_menu_2,
                            iconColor = iconTint
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// Prywatne komponenty (ZMODYFIKOWANE)
@Composable
private fun DetailIconBox(
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun DetailSection(
    label: String,
    text: String,
    iconRes: Int,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top // WYRÓWNANIE DO GÓRY
    ) {
        DetailIconBox {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Zmniejszony padding górny
        Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF757575),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black,
                lineHeight = 22.sp
            )
        }
    }
}