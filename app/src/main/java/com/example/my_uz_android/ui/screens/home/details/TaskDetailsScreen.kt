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
import androidx.compose.runtime.*
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task

    // KOLORY Z MOTYWU
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val taskAccentColor = MaterialTheme.extendedColors.classCardBackground

    // ZMIANA: Używamy surfaceContainerLowest dla czystej bieli w Light Mode
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor, // Dynamiczne tło (Białe w Light / Ciemne w Dark)
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 10) onNavigateBack() }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailIconBox(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_x_close), contentDescription = "Zamknij", tint = textColor, modifier = Modifier.size(24.dp))
                    }

                    if (task != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailIconBox(onClick = { onEditTask(task.id) }) {
                                Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "Edytuj", tint = textColor, modifier = Modifier.size(24.dp))
                            }

                            Box {
                                DetailIconBox(onClick = { showMenu = true }) {
                                    Icon(painter = painterResource(id = R.drawable.ic_dots_vertical), contentDescription = "Opcje", tint = textColor, modifier = Modifier.size(24.dp))
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh) // Tło menu pozostawiamy lekko wyróżnione
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Duplikuj", fontFamily = InterFontFamily, color = textColor) },
                                        onClick = {
                                            viewModel.duplicateTask(task)
                                            showMenu = false
                                            onNavigateBack()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Usuń", fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            viewModel.deleteTask()
                                            showMenu = false
                                            onNavigateBack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (task != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                        DetailIconBox {
                            Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(taskAccentColor))
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = task.title, fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, color = textColor, modifier = Modifier.padding(bottom = 4.dp))

                            val dateString = if (task.dueDate > 0) {
                                try {
                                    Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("pl")))
                                } catch (e: Exception) { "Brak terminu" }
                            } else "Brak terminu"

                            Text(text = dateString, fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = subTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(start = 56.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    if (!task.description.isNullOrEmpty()) {
                        DetailSection(label = "OPIS", text = task.description, iconRes = R.drawable.ic_menu_2, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                    }
                    DetailSection(label = "STATUS", text = if (task.isCompleted) "Zakończone" else "W toku", iconRes = if (task.isCompleted) R.drawable.ic_check_circle_broken else R.drawable.ic_info_circle, iconColor = iconTint, textColor = textColor, labelColor = subTextColor)
                }

                Button(
                    onClick = { viewModel.toggleTaskCompletion(task) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(text = if (task.isCompleted) "Oznacz jako nieukończone" else "Oznacz jako ukończone", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            } else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nie znaleziono zadania", color = textColor) }
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