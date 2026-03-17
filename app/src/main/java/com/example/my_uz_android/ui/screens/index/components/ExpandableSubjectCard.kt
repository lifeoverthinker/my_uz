package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.GradeEntity
import com.example.my_uz_android.util.ClassTypeUtils

data class SubjectTypeState(
    val typeName: String,
    val average: Double? = null,
    val grades: List<GradeEntity>
)

@Composable
fun ExpandableSubjectCard(
    subjectName: String,
    subjectCode: String,
    overallAverage: Double? = null,
    classTypes: List<SubjectTypeState>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onAddGradeClick: (String) -> Unit,
    onTypeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val contentColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        color = cardBackgroundColor,
        shadowElevation = 0.dp, // BRAK CIENIA - Google M3 Style
        shape = RoundedCornerShape(16.dp) // Większe zaokrąglenia
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = contentColor,
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text(
                            text = overallAverage?.let { String.format("%.1f", it) } ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "średnia",
                            style = MaterialTheme.typography.bodySmall,
                            color = subTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp).rotate(arrowRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

                    classTypes.forEachIndexed { index, typeState ->
                        ClassTypeRow(
                            typeState = typeState.copy(typeName = ClassTypeUtils.getFullName(typeState.typeName)),
                            onTypeClick = { onTypeClick(typeState.typeName) },
                            onAddGradeClick = { onAddGradeClick(typeState.typeName) }
                        )

                        if (index < classTypes.lastIndex) {
                            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ClassTypeRow(
    typeState: SubjectTypeState,
    onTypeClick: () -> Unit,
    onAddGradeClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTypeClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = typeState.typeName,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor
                )

                if (typeState.grades.isEmpty()) {
                    Text(
                        text = "Brak wpisów",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(typeState.grades) { grade ->
                            GradeBubble(grade = grade)
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = typeState.average?.let { String.format("%.1f", it) } ?: "-",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Przejdź do szczegółów",
                    modifier = Modifier.size(24.dp),
                    tint = subTextColor
                )
            }
        }

        // Czysty, bezramkowy przycisk "Dodaj wpis" zamiast zarysowanego
        TextButton(
            onClick = onAddGradeClick,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp, bottom = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Dodaj wpis", style = MaterialTheme.typography.labelLarge)
        }
    }
}