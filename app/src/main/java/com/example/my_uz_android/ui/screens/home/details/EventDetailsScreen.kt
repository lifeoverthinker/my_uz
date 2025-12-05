package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.extendedColors

@Composable
fun EventDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.eventEntity

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.extendedColors.eventCardBackground

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 10) onBackClick()
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailIconBox(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (event != null) {
                // Tytuł
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    DetailIconBox {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = event.title,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 28.sp,
                            lineHeight = 36.sp,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = event.date,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = subTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details (BEZ DIVIDERA)
                if (event.description.isNotEmpty()) {
                    DetailSection(
                        label = "OPIS",
                        text = event.description,
                        iconRes = R.drawable.ic_menu_2,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                } else {
                    DetailSection(
                        label = "OPIS",
                        text = "Brak opisu",
                        iconRes = R.drawable.ic_menu_2,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
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
    iconColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        DetailIconBox {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = labelColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textColor,
                lineHeight = 22.sp
            )
        }
    }
}
