package com.example.my_uz_android.ui.screens.index.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.util.ClassTypeUtils

data class SubjectTypeState(
    val typeName: String,
    val average: Double? = null,
    val grades: List<GradeItem>
)

@Composable
fun ExpandableSubjectCard(
    subjectName: String,
    subjectCode: String,
    overallAverage: Double? = null,
    classTypes: List<SubjectTypeState>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onAddGradeClick: (String) -> Unit, // Przekazujemy nazwę typu (np. "Wykład")
    onTypeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    // Kolory z motywu
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val contentColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .clickable { onExpandClick() },
        color = cardBackgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subjectName,
                    style = TextStyle(
                        color = contentColor,
                        fontSize = 16.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W500,
                        letterSpacing = 0.15.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
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
                            style = TextStyle(
                                color = contentColor,
                                fontSize = 22.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.W400,
                                lineHeight = 28.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "średnia",
                            style = TextStyle(
                                color = subTextColor,
                                fontSize = 12.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.W400,
                                letterSpacing = 0.4.sp,
                                lineHeight = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                        tint = contentColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            // Lista typów zajęć
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = dividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    classTypes.forEachIndexed { index, typeState ->
                        // Przekazujemy typ zajęć (np. "Wykład") do onAddGradeClick
                        ClassTypeRow(
                            typeState = typeState.copy(
                                typeName = ClassTypeUtils.getFullName(typeState.typeName)
                            ),
                            onTypeClick = { onTypeClick(typeState.typeName) },
                            onAddGradeClick = { onAddGradeClick(typeState.typeName) }
                        )

                        if (index < classTypes.lastIndex) {
                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
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
        // Klikalny wiersz z nazwą typu, ocenami i średnią
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
                    style = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W500,
                        letterSpacing = 0.1.sp,
                        lineHeight = 20.sp
                    )
                )

                if (typeState.grades.isEmpty()) {
                    Text(
                        text = "Brak ocen",
                        style = TextStyle(
                            color = subTextColor,
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.W400,
                            letterSpacing = 0.4.sp,
                            lineHeight = 16.sp
                        )
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(typeState.grades) { grade ->
                            GradeBubble(
                                grade = grade,
                                onGradeClick = { }
                            )
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
                    style = TextStyle(
                        color = textColor,
                        fontSize = 22.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W400,
                        lineHeight = 28.sp
                    )
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Przejdź do szczegółów",
                    modifier = Modifier.size(24.dp),
                    tint = subTextColor
                )
            }
        }

        // Przycisk "Dodaj ocenę" - wiersz poniżej
        Surface(
            onClick = onAddGradeClick,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, primaryColor),
            // W Dark Mode tło przezroczyste lub surface, w Light Mode białe/surface
            color = Color.Transparent,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Dodaj ocenę",
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W500,
                        letterSpacing = 0.4.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableSubjectCardPreview() {
    MyUZTheme {
        val classTypes = listOf(
            SubjectTypeState(
                typeName = "WYK",
                average = 4.5,
                grades = listOf(
                    GradeItem(id = 1, value = "4.0"),
                    GradeItem(id = 2, value = "5.0"),
                    GradeItem(id = 3, value = "+")
                )
            ),
            SubjectTypeState(
                typeName = "CW",
                average = 3.5,
                grades = listOf(
                    GradeItem(id = 4, value = "3.0"),
                    GradeItem(id = 5, value = "+"),
                    GradeItem(id = 6, value = "4.0")
                )
            )
        )

        ExpandableSubjectCard(
            subjectName = "Inżynieria Oprogramowania",
            subjectCode = "IO-101",
            overallAverage = 4.0,
            classTypes = classTypes,
            isExpanded = true,
            onExpandClick = {},
            onAddGradeClick = {},
            onTypeClick = {}
        )
    }
}