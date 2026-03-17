package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                SwipeToDismissBoxValue.EndToStart -> { onDelete(grade); false }
                SwipeToDismissBoxValue.StartToEnd -> { onDuplicate(grade); false }
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
                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                        else -> Color.Transparent
                    }, label = "swipe_bg"
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp),
                    contentAlignment = when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    val iconRes = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) R.drawable.ic_copy else R.drawable.ic_trash
                    Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            // Google Classroom Stream Style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onClick() }
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikona / Awatar po lewej
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (grade.isPoints) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val displayValue = if (grade.grade == -1.0) "+" else {
                        if (grade.grade % 1.0 == 0.0) grade.grade.toInt().toString() else grade.grade.toString()
                    }
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (grade.isPoints) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Tekst na środku
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.description.takeIf { !it.isNullOrBlank() } ?: (if (grade.isPoints) "Punkty" else "Ocena"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDate(grade.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Waga po prawej
                if (!grade.isPoints && grade.weight > 0 && grade.grade != -1.0) {
                    Text(
                        text = "Waga: ${if (grade.weight % 1.0 == 0.0) grade.weight.toInt() else grade.weight}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) // Zmiana na "10 paź 2024" (lepiej wygląda)
}