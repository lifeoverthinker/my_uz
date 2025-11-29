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

@Composable
fun ClassDetailsScreen(
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val classEntity = uiState.classItem

    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val classAccentColor = MaterialTheme.extendedColors.classCardBackground
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    // Karta szczegółów
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Odsłania pasek stanu (efekt karty)
            .padding(top = 8.dp) // Lekki odstęp od góry
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Belka do przeciągania
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 10) onBackClick() }
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(32.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(dividerColor))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    DetailIconBox(onClick = onBackClick) {
                        Icon(painter = painterResource(id = R.drawable.ic_x_close), contentDescription = "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (classEntity != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    val dayName = try {
                        DayOfWeek.of(classEntity.dayOfWeek)
                            .getDisplayName(TextStyle.FULL, Locale("pl"))
                            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    } catch (e: Exception) { "" }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                        DetailIconBox {
                            Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(classAccentColor))
                        }
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(text = classEntity.subjectName, fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, color = textColor, modifier = Modifier.padding(bottom = 4.dp))
                            Text(text = "$dayName, ${classEntity.startTime} – ${classEntity.endTime}", fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = subTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(start = 56.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSection(label = "TYP ZAJĘĆ", text = classEntity.classType, iconRes = R.drawable.ic_info_circle, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    if (!classEntity.room.isNullOrEmpty()) {
                        DetailSection(label = "LOKALIZACJA / SALA", text = classEntity.room, iconRes = R.drawable.ic_marker_pin, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }
                    if (!classEntity.teacherName.isNullOrEmpty()) {
                        DetailSection(label = "PROWADZĄCY", text = classEntity.teacherName, iconRes = R.drawable.ic_user, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).clickable(enabled = onClick != null) { onClick?.invoke() }, contentAlignment = Alignment.Center, content = content)
}

@Composable
private fun DetailSection(label: String, text: String, iconRes: Int, iconColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color, labelColor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Top) {
        DetailIconBox { Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp)) }
        Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
            Text(text = label, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = labelColor, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 2.dp))
            Text(text = text, fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = textColor, lineHeight = 22.sp)
        }
    }
}