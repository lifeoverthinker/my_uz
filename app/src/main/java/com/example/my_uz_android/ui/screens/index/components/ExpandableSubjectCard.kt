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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily
import com.example.my_uz_android.util.ClassTypeUtils

// UWAGA: Usunięto definicję data class GradeItem, ponieważ znajduje się ona już w pliku GradeBubble.kt
// Dzięki temu, że oba pliki są w tym samym pakiecie, ten plik automatycznie "widzi" tamtą klasę.

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
    onAddGradeClick: (String) -> Unit,
    onTypeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .clickable { onExpandClick() },
        color = Color(0xFFF8F1FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = subjectName,
                        style = TextStyle(
                            color = Color(0xFF1D1B20),
                            fontSize = 16.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.W500,
                            letterSpacing = 0.15.sp,
                            lineHeight = 24.sp
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.width(45.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = overallAverage?.let { String.format("%.1f", it) } ?: "-",
                                style = TextStyle(
                                    color = Color(0xFF1D1B20),
                                    fontSize = 22.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.W400,
                                    lineHeight = 28.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(modifier = Modifier.width(45.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "średnia",
                                style = TextStyle(
                                    color = Color(0xFF1D1B20),
                                    fontSize = 12.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.W400,
                                    letterSpacing = 0.4.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(arrowRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Rozwiń",
                            tint = Color(0xFF1D1B20)
                        )
                    }
                }
            }

            // --- LISTA TYPÓW ZAJĘĆ ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFCAC4D0), thickness = 1.dp, modifier = Modifier.fillMaxWidth())

                    classTypes.forEachIndexed { index, typeState ->
                        ClassTypeRow(
                            typeState = typeState.copy(typeName = ClassTypeUtils.getFullName(typeState.typeName)),
                            onTypeClick = { onTypeClick(typeState.typeName) },
                            onAddGradeClick = { onAddGradeClick(typeState.typeName) }
                        )

                        if (index < classTypes.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFCAC4D0),
                                thickness = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun ExpandableSubjectCardPreview() {
    val classTypes = listOf(
        SubjectTypeState(
            typeName = "WYK",
            average = 4.5,
            grades = listOf(
                GradeItem(id = 1, value = 4.0),
                GradeItem(id = 2, value = 5.0)
            )
        ),
        SubjectTypeState(
            typeName = "CW",
            average = 3.5,
            grades = listOf(
                GradeItem(id = 3, value = 3.0),
                GradeItem(id = 4, value = 4.0)
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

@Preview
@Composable
fun ClassTypeRowPreview() {
    val typeState = SubjectTypeState(
        typeName = "Wykład",
        average = 4.7,
        grades = listOf(
            GradeItem(id = 1, value = 5.0),
            GradeItem(id = 2, value = 4.5),
            GradeItem(id = 3, value = 4.0)
        )
    )
    ClassTypeRow(typeState = typeState, onTypeClick = {}, onAddGradeClick = {})
}

@Composable
fun ClassTypeRow(
    typeState: SubjectTypeState,
    onTypeClick: () -> Unit,
    onAddGradeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTypeClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        color = Color(0xFF1D1B20),
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
                            color = Color(0xFF1D1B20),
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
                            // GradeBubble pochodzi z GradeBubble.kt i przyjmuje GradeItem
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
                    style = TextStyle(
                        color = Color(0xFF1D1B20),
                        fontSize = 18.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W500,
                        lineHeight = 24.sp
                    )
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Przejdź do szczegółów",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF1D1B20)
                )
            }
        }

        Surface(
            onClick = { onAddGradeClick() },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF4F3D74)),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF4F3D74),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Dodaj ocenę",
                    style = TextStyle(
                        color = Color(0xFF4F3D74),
                        fontSize = 12.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.W400,
                        letterSpacing = 0.4.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}