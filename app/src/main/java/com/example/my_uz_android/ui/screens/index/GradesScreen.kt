package com.example.my_uz_android.ui.screens.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.util.ClassTypeUtils
import com.example.my_uz_android.ui.components.EmptyStateFigma
import androidx.compose.ui.res.stringResource

@Composable
fun GradesScreen(
    viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSubjectClick: (String, String) -> Unit,
    onAddGradeClick: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.subjects.isEmpty()) {
            EmptyStateFigma(
                title = stringResource(R.string.subject_grades_empty_title),
                subtitle = stringResource(R.string.subject_grades_empty_subtitle),
                message = stringResource(R.string.subject_grades_empty_message),
                iconRes = R.drawable.grades_rafiki,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                illustrationSize = 221.7868.dp,
                containerHeight = 582.dp
            )
        } else {
            val isMultiCourse = uiState.userCourses.size > 1
            val groupedByCourse = remember(uiState.subjects, isMultiCourse) {
                if (!isMultiCourse) {
                    listOf("single" to uiState.subjects)
                } else {
                    uiState.subjects
                        .groupBy { it.courseName.ifBlank { "Inne / Dawne" } }
                        .toList()
                        .sortedBy { it.first.lowercase() }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    val isMultiCourseBanner = uiState.courseAverages.size > 1

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        if (!isMultiCourseBanner) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.current_semester_average),
                                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = uiState.average?.let { String.format("%.1f", it) } ?: "-",
                                    style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 24.sp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.current_semester_averages),
                                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                uiState.courseAverages.forEachIndexed { index, (courseName, avg) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = courseName.ifBlank { stringResource(R.string.other_courses) },
                                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = avg?.let { String.format("%.1f", it) } ?: "-",
                                            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    if (index < uiState.courseAverages.lastIndex) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                groupedByCourse.forEach { (courseName, subjects) ->
                    if (isMultiCourse) {
                        item {
                            Text(
                                text = if (courseName == "single") "" else courseName.ifBlank { stringResource(R.string.other_courses) },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    items(subjects, key = { it.uniqueKey }) { subject ->
                        ExpandableSubjectGradeCard(
                            subject = subject,
                            onGradeListClick = { type -> onSubjectClick(subject.uniqueKey, type) },
                            onAddGradeClick = { type -> onAddGradeClick(subject.name, type) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpandableSubjectGradeCard(
    subject: SubjectState,
    onGradeListClick: (String) -> Unit,
    onAddGradeClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "card_rotation")
    val cardBackground = MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBackground, RoundedCornerShape(8.dp))
            .then(if (expanded) Modifier.padding(bottom = 16.dp) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subject.name,
                    style = TextStyle(fontWeight = FontWeight(500), fontSize = 16.sp, lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = subject.average?.let { String.format("%.1f", it) } ?: "-",
                            style = TextStyle(fontWeight = FontWeight(400), fontSize = 22.sp, lineHeight = 28.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.average_label_small),
                            style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = stringResource(R.string.expand_collapse_content_description),
                        modifier = Modifier.size(24.dp).rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                subject.types.forEachIndexed { typeIndex, classType ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGradeListClick(classType.name) },
                            horizontalArrangement = Arrangement.spacedBy(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = ClassTypeUtils.getFullName(classType.name),
                                    style = TextStyle(fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (classType.grades.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.no_grades),
                                        style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        classType.grades.forEach { grade ->
                                            val displayVal = if (grade.grade == -1.0) "+" else {
                                                if (grade.grade % 1.0 == 0.0) grade.grade.toInt().toString() else grade.grade.toString()
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = displayVal,
                                                    style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = classType.average?.let { String.format("%.1f", it) } ?: "-",
                                    style = TextStyle(fontWeight = FontWeight(400), fontSize = 22.sp, lineHeight = 28.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Styl CTA: prostokąt zaokrąglony 8dp + kolor bordera = kolor tekstu
                        Row(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAddGradeClick(classType.name) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_plus),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.add_grade),
                                style = TextStyle(fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (typeIndex < subject.types.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}