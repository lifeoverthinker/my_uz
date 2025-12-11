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
import com.example.my_uz_android.ui.theme.extendedColors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import com.example.my_uz_android.util.ClassTypeUtils

@Composable
fun ClassDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val classEntity = uiState.classEntity

    // ✅ Kolory z Theme (dla Dark Mode)
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.extendedColors.classCardBackground

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

            if (classEntity != null) {
                val dayName = try {
                    DayOfWeek.of(classEntity.dayOfWeek)
                        .getDisplayName(TextStyle.FULL, Locale("pl"))
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                } catch (e: Exception) {
                    ""
                }

                // Tytuł i Data (zgodnie z Twoim wzorem)
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
                            text = classEntity.subjectName,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 28.sp,
                            lineHeight = 36.sp,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "$dayName, ${classEntity.startTime} – ${classEntity.endTime}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = subTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details (BEZ DIVIDERA)
                DetailSection(
                    label = "TYP",
                    text = ClassTypeUtils.getFullName(classEntity.classType),
                    iconRes = R.drawable.ic_info_circle,
                    iconColor = iconTint,
                    textColor = textColor,
                    labelColor = subTextColor
                )

                if (!classEntity.room.isNullOrEmpty()) {
                    DetailSection(
                        label = "SALA",
                        text = classEntity.room,
                        iconRes = R.drawable.ic_map, // lub ic_marker_pin jeśli wolisz
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }

                if (!classEntity.teacherName.isNullOrEmpty()) {
                    DetailSection(
                        label = "PROWADZĄCY",
                        text = classEntity.teacherName,
                        iconRes = R.drawable.ic_user,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }
            } else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Obsługa błędu
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nie znaleziono zajęć",
                            style = MaterialTheme.typography.bodyLarge,
                            color = subTextColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Powrót")
                        }
                    }
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
    iconColor: Color,
    textColor: Color,
    labelColor: Color
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