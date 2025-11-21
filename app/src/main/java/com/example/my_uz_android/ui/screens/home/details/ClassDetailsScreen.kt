package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ClassDetailsScreen(
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val classItem = uiState.classItem

    val backgroundColor = Color(0xFFF7F2F9)
    val onBackgroundColor = Color(0xFF1D192B)
    val purpleContainer = Color(0xFFE8DEF8) // Kolor kwadracika

    // --- Logika Swipe-to-Dismiss (Zwijanie palcem) ---
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun dismiss() {
        scope.launch {
            offsetY.animateTo(targetValue = 2000f)
            onBackClick()
        }
    }

    fun reset() {
        scope.launch {
            offsetY.animateTo(targetValue = 0f)
        }
    }

    MyUZTheme {
        // Tło pod spodem
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Biała karta "Szuflady"
            Surface(
                modifier = Modifier
                    .statusBarsPadding() // Uwzględnia pasek systemowy
                    .padding(top = 48.dp) // Odstęp od góry (6 * 8grid)
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) } // Obsługa przesuwania
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY.value > 150f) dismiss() else reset()
                            },
                            onDragCancel = { reset() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = offsetY.value + dragAmount
                                // Blokada ruchu w górę (tylko w dół)
                                scope.launch {
                                    offsetY.snapTo(newOffset.coerceAtLeast(0f))
                                }
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
                if (classItem != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. IKONA ZAMKNIĘCIA (X)
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

                        // 2. TYTUŁ, DATA I KWADRACIK
                        // Używamy Row z Alignment.CenterVertically, aby kwadracik był na środku względem tekstu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Odstęp między kolumną kwadracika a tekstem
                        ) {
                            // Kolumna lewa: Kontener na kwadracik (48x48, żeby pasował do ikon poniżej)
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Ozdobny kwadracik (20x20)
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(purpleContainer, RoundedCornerShape(4.dp))
                                )
                            }

                            // Kolumna prawa: Teksty
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp) // Mały odstęp między tytułem a datą
                            ) {
                                Text(
                                    text = classItem.subjectName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 20.sp, // Troszkę większy font dla tytułu
                                        fontWeight = FontWeight.W500,
                                        color = Color.Black,
                                        lineHeight = 26.sp
                                    )
                                )
                                Text(
                                    text = "${uiState.dayName}, 8 lip 2025 • ${classItem.startTime} - ${classItem.endTime}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.W400,
                                        color = Color.Black
                                    )
                                )
                            }
                        }

                        // 3. SZCZEGÓŁY (Pionowa lista)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Gęstsze ułożenie detali
                        ) {
                            // Typ zajęć
                            DetailRowItem(
                                iconRes = R.drawable.ic_stand,
                                text = classItem.classType
                            )

                            // Sala (Marker Pin)
                            DetailRowItem(
                                iconRes = R.drawable.ic_marker_pin,
                                text = classItem.room ?: "Brak sali"
                            )

                            // Prowadzący
                            DetailRowItem(
                                iconRes = R.drawable.ic_user,
                                text = classItem.teacherName ?: "Brak danych"
                            )
                        }
                    }
                } else {
                    // Loading
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = onBackgroundColor)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowItem(iconRes: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Kontener ikony: 48x48 -> 40x40 kółko (z kodu Fluttera)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    // W kodzie Fluttera: Clip.antiAlias (często sugeruje lekkie tło lub przygotowanie pod nie)
                    // Tutaj transparent, chyba że chcesz szare kółko jak w Google Calendar
                    .background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp) // Ikona 24dp
                )
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.W400
            )
        )
    }
}