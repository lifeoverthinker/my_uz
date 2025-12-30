package com.example.my_uz_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.extendedColors

data class FabOption(
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
fun UniversalFab(
    onMainFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    options: List<FabOption> = emptyList(),
    iconRes: Int = R.drawable.ic_plus
) {
    // --- STAŁE KOLORY DLA MENU (OPCJE) ---
    val menuContainerColor = Color(0xFFEADDFF)
    val menuContentColor = Color(0xFF4F378A)
    val menuShape = RoundedCornerShape(16.dp)

    // --- ANIMACJA STANU GŁÓWNEGO FABA ---
    // Logika: Jeśli jest rozwinięty (tylko na Home), ma inny styl.
    // W przeciwnym razie (zwinięty Home LUB inne ekrany) ma styl domyślny (jasny, 16dp).

    val targetCornerRadius = if (isExpandable && isExpanded) 28.dp else 16.dp
    val currentCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        label = "FabCornerAnimation"
    )

    val targetContainerColor = if (isExpandable && isExpanded) Color(0xFF6750A4) else Color(0xFFEADDFF)
    val currentContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        label = "FabContainerColorAnimation"
    )

    // Kolor ikony: Biały gdy rozwinięty, Ciemny (z Theme lub Hex) gdy zwinięty
    val collapsedIconColor = MaterialTheme.extendedColors.iconText // Używamy koloru z motywu (np. 0xFF1D192B) dla spójności z innymi przyciskami
    val targetContentColor = if (isExpandable && isExpanded) Color.White else collapsedIconColor
    val currentContentColor by animateColorAsState(
        targetValue = targetContentColor,
        label = "FabContentColorAnimation"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "FabRotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // --- MENU (Rozwijane opcje) ---
        AnimatedVisibility(
            visible = isExpandable && isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(menuShape)
                            .background(menuContainerColor)
                            .clickable { option.onClick() }
                    ) {
                        Row(
                            modifier = Modifier
                                .height(56.dp)
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ikona opcji
                            Box(modifier = Modifier.size(24.dp)) {
                                Icon(
                                    painter = painterResource(id = option.iconRes),
                                    contentDescription = null,
                                    tint = menuContentColor,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Tekst
                            Text(
                                text = option.label,
                                style = TextStyle(
                                    fontWeight = FontWeight(500),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                ),
                                color = menuContentColor,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // --- GŁÓWNY PRZYCISK FAB ---
        Box(
            modifier = Modifier
                .size(56.dp)
                // Najpierw clip (kształt), potem background (kolor)
                .clip(RoundedCornerShape(currentCornerRadius))
                .background(currentContainerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMainFabClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    if (isExpandable) {
                        // Ikona Plusa (obracana)
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = if (isExpanded) "Zamknij" else "Otwórz",
                            tint = currentContentColor,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotation)
                        )
                    } else {
                        // Zwykła ikona (statyczna)
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Akcja",
                            tint = currentContentColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}