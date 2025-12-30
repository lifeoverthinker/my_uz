package com.example.my_uz_android.ui.components

import androidx.compose.animation.AnimatedVisibility
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
    // --- STYL Z FIGMY (Zmieniony/Light) ---
    // Kolory menu (opcje)
    val menuContainerColor = Color(0xFFEADDFF)
    val menuContentColor = Color(0xFF4F378A)

    // Kolory głównego FABa
    // Jeśli rozwijany (Home) i otwarty -> fioletowy (0xFF6750A4),
    // Jeśli zwinięty lub zwykły (inne ekrany) -> jasny fiolet (0xFFEADDFF)
    val fabContainerColor = if (isExpandable && isExpanded) Color(0xFF6750A4) else Color(0xFFEADDFF)

    // Kolor ikony głównego FABa
    // Jeśli rozwijany i otwarty -> biały (kontrast na ciemnym fiolecie) - zakładam standard M3
    // Jeśli zwinięty (inne ekrany) -> ciemny fiolet (0xFF4F378A) - zgodnie ze specyfikacją "dla pozostałych ekranów"
    val fabContentColor = if (isExpandable && isExpanded) Color.White else Color(0xFF4F378A)

    // Kształt z Figmy: 16.dp (dla zwiniętego) / 28.dp (dla rozwiniętego - opcjonalnie, w Twoim kodzie jest 28dp dla rozwiniętego FABa)
    val fabShape = if (isExpandable && isExpanded) RoundedCornerShape(28.dp) else RoundedCornerShape(16.dp)
    val menuShape = RoundedCornerShape(16.dp) // Kształt opcji menu

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
                    // Pojedynczy segment menu (zgodnie z kodem Figmy)
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
                            // Ikona
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
                .clip(fabShape)
                .background(fabContainerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMainFabClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // State-layer (padding 16.dp wewnątrz Boxa 56dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ikona (Box 24dp wewnątrz)
                Box(modifier = Modifier.size(24.dp)) {
                    if (isExpandable) {
                        // Ikona Plusa (obracana) - używamy Twojego ic_plus
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = if (isExpanded) "Zamknij" else "Otwórz",
                            tint = fabContentColor,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotation)
                        )
                    } else {
                        // Zwykła ikona (np. plus na innych ekranach)
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Akcja",
                            tint = fabContentColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}