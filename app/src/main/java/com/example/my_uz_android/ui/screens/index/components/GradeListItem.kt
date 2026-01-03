package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.GradeEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeListItem(
    grade: GradeEntity,
    onClick: () -> Unit,
    onDelete: (GradeEntity) -> Unit,
    onDuplicate: (GradeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete(grade)
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDuplicate(grade)
                    false
                }
                else -> false
            }
        }
    )

    Column(modifier = modifier) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        else -> Color.Transparent
                    }, label = "swipe_bg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                        .padding(horizontal = 24.dp),
                    contentAlignment = when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    val iconRes = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                        R.drawable.ic_copy else R.drawable.ic_trash

                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                            MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onClick() }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SEKCJA 1: Ocena / Punkty
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Box(
                        modifier = Modifier
                            .height(32.dp) // Zmienione z size() na height() + minWidth dla punktów
                            .defaultMinSize(minWidth = 32.dp)
                            .background(
                                if(grade.isPoints) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                            .padding(horizontal = 8.dp), // Padding dla szerszych liczb
                        contentAlignment = Alignment.Center
                    ) {
                        val displayValue = if (grade.grade == -1.0) "+" else {
                            if (grade.grade % 1.0 == 0.0) grade.grade.toInt().toString()
                            else grade.grade.toString()
                        }

                        Text(
                            text = if(grade.isPoints) "$displayValue pkt" else displayValue,
                            style = MaterialTheme.typography.labelLarge,
                            color = if(grade.isPoints) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // SEKCJA 2: Opis
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = grade.description ?: (if (grade.isPoints) "Punkty" else "Ocena"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                // SEKCJA 3: Data i Waga
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDate(grade.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Pokaż wagę tylko jeśli to NIE są punkty i waga jest inna niż 1
                    if (!grade.isPoints && grade.weight.toDouble() != 1.0) {
                        Text(
                            text = "waga: ${grade.weight.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}