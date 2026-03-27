package com.example.my_uz_android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.my_uz_android.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. Model danych dla akcji
data class FabAction(
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
fun UniversalFab(
    onAddGrade: () -> Unit,
    onAddAbsence: () -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lista akcji - łatwa do rozbudowy
    val actions = remember(onAddGrade, onAddAbsence, onAddTask) {
        listOf(
            FabAction("Dodaj ocenę", R.drawable.ic_trophy, onAddGrade),
            FabAction("Dodaj nieobecność", R.drawable.ic_calendar_minus, onAddAbsence),
            FabAction("Dodaj zadanie", R.drawable.ic_book_open, onAddTask)
        )
    }

    var isDialogOpen by remember { mutableStateOf(false) }
    var fabPaddingBottom by remember { mutableStateOf(0.dp) }
    var fabPaddingEnd by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    // Główny wyzwalacz (Static FAB)
    if (!isDialogOpen) {
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isDialogOpen = true
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .size(56.dp)
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    fabPaddingBottom = with(density) { (view.height - bounds.bottom).toDp() }
                    fabPaddingEnd = with(density) { (view.width - bounds.right).toDp() }
                }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Otwórz menu dodawania")
        }
    }

    if (isDialogOpen) {
        ExpandedFabDialog(
            actions = actions,
            fabPaddingBottom = fabPaddingBottom,
            fabPaddingEnd = fabPaddingEnd,
            onDismiss = { isDialogOpen = false }
        )
    }
}

@Composable
private fun ExpandedFabDialog(
    actions: List<FabAction>,
    fabPaddingBottom: androidx.compose.ui.unit.Dp,
    fabPaddingEnd: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }

    // Animacja wejścia
    LaunchedEffect(Unit) { isVisible = true }

    val closeWithAnimation = {
        coroutineScope.launch {
            isVisible = false
            delay(200) // Czas wyjścia
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { closeWithAnimation() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        // Używamy transition do synchronizacji wszystkich parametrów
        val transition = updateTransition(targetState = isVisible, label = "FabMenuTransition")

        val scrimAlpha by transition.animateFloat(label = "ScrimAlpha") { if (it) 0.6f else 0f }
        val fabRadius by transition.animateDp(label = "FabRadius") { if (it) 28.dp else 16.dp }
        val rotation by transition.animateFloat(label = "IconRotation") { if (it) 45f else 0f }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { closeWithAnimation() }
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = fabPaddingBottom, end = fabPaddingEnd)
            ) {
                // Menu Items
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 72.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    actions.forEachIndexed { index, action ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(150, delayMillis = index * 30)) +
                                    slideInHorizontally { it / 2 },
                            exit = fadeOut(tween(100)) + slideOutHorizontally { it / 2 }
                        ) {
                            FabMenuItem(
                                label = action.label,
                                iconRes = action.iconRes,
                                onClick = {
                                    action.onClick()
                                    closeWithAnimation()
                                }
                            )
                        }
                    }
                }

                // Morphing FAB (z rotacją ikony zamiast Crossfade dla płynniejszego efektu)
                Surface(
                    onClick = { closeWithAnimation() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(56.dp),
                    shape = RoundedCornerShape(fabRadius),
                    color = if (isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isVisible) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(label: String, iconRes: Int, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}