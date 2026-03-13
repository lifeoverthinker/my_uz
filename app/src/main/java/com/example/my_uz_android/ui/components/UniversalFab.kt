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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    options: List<FabOption> = emptyList(),
    iconRes: Int = R.drawable.ic_plus
) {
    // --- KOLORY ---
    val fabContainerColor = MaterialTheme.colorScheme.primaryContainer
    val fabContentColor = MaterialTheme.colorScheme.onPrimaryContainer

    val menuContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val menuContentColor = MaterialTheme.colorScheme.onSecondaryContainer

    // --- ANIMACJA ---
    val targetCornerRadius = if (isExpandable && isExpanded) 28.dp else 16.dp
    val currentCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        label = "FabCornerAnimation"
    )

    val targetContainerColor = if (isExpandable && isExpanded) MaterialTheme.colorScheme.primary else fabContainerColor
    val currentContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        label = "FabContainerColorAnimation"
    )

    val targetContentColor = if (isExpandable && isExpanded) MaterialTheme.colorScheme.onPrimary else fabContentColor
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
        // --- MENU (Rozwijane opcje z nowoczesnym UI) ---
        AnimatedVisibility(
            visible = isExpandable && isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Etykieta typu Glassmorphism (półprzezroczyste tło, bez cienia)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 0.dp // Płaskie, nowoczesne
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        // Okrągły mini-FAB
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape) // Idealne koło zamiast zaokrąglonego kwadratu
                                .background(menuContainerColor)
                                .clickable { option.onClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = option.iconRes),
                                contentDescription = null,
                                tint = menuContentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- GŁÓWNY PRZYCISK FAB ---
        Surface(
            shape = RoundedCornerShape(currentCornerRadius),
            color = currentContainerColor,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMainFabClick
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isExpandable) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = if (isExpanded) "Zamknij" else "Otwórz",
                        tint = currentContentColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Akcja",
                        tint = currentContentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}