package com.example.my_uz_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily

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
    iconRes: Int = R.drawable.ic_plus,
    options: List<FabOption> = emptyList()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    // Kolory kontenera opcji (zgodne ze snippetem: Primary Container)
    val optionContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onOptionContainerColor = MaterialTheme.colorScheme.onPrimaryContainer

    // Animacje
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 16.dp, // 28dp to pełne koło dla 56dp
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "cornerRadius"
    )

    val fabColor by animateColorAsState(
        targetValue = if (isExpanded) surfaceColor else primaryColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "fabColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isExpanded) primaryColor else onPrimaryColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding() // ✅ Gwarancja równej wysokości na wszystkich ekranach
    ) {
        // Overlay
        if (isExpandable && isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f)) // Lekko przezroczyste tło
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onMainFabClick()
                    }
            )
        }

        // Kontener FAB i Opcji
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // OPCJE (Renderowane tylko gdy expanded)
            if (isExpandable && isExpanded) {
                // Wyświetlamy opcje. Snippet sugerował pigułki z tekstem.
                options.forEach { option ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Surface(
                            onClick = option.onClick,
                            shape = RoundedCornerShape(28.dp), // Pigułka
                            color = optionContainerColor,
                            shadowElevation = 4.dp,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End // Tekst do prawej (wg snippetu)
                            ) {
                                // Ikonka (opcjonalna w snippecie, ale UXowo dobra)
                                /*
                                Icon(
                                    painter = painterResource(id = option.iconRes),
                                    contentDescription = null,
                                    tint = onOptionContainerColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                */

                                // Tekst
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.15.sp
                                    ),
                                    color = onOptionContainerColor
                                )
                            }
                        }
                    }
                }
            }

            // GŁÓWNY FAB
            FloatingActionButton(
                onClick = onMainFabClick,
                containerColor = fabColor,
                shape = RoundedCornerShape(cornerRadius),
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.size(56.dp)
            ) {
                if (isExpandable) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Zamknij" else "Otwórz",
                        tint = iconColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Akcja",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}