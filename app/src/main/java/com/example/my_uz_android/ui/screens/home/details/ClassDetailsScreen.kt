package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private fun getDayName(dayOfWeek: Int): String {
    return try {
        DayOfWeek.of(dayOfWeek)
            .getDisplayName(TextStyle.FULL, Locale("pl"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun ClassDetailsScreen(
    classId: Int,
    onBackClick: () -> Unit,
    isTeacherPlan: Boolean = false,
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sharedCalendarViewModel: CalendarViewModel? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(classId) {
        if (classId == -1 && sharedCalendarViewModel != null) {
            val tempClass = sharedCalendarViewModel.uiState.value.temporaryClassForDetails
            if (tempClass != null) {
                viewModel.setTemporaryClass(tempClass)
            }
        }
    }

    ClassDetailsContent(
        classEntity = uiState.classEntity,
        isLoading = uiState.isLoading,
        isTeacherPlan = isTeacherPlan,
        onBackClick = onBackClick
    )
}

@Composable
fun ClassDetailsContent(
    classEntity: ClassEntity?,
    isLoading: Boolean,
    isTeacherPlan: Boolean,
    onBackClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.extendedColors.classCardBackground

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_close),
                        contentDescription = "Zamknij",
                        tint = textColor
                    )
                }
            }
        }
    ) { paddingValues ->
        if (classEntity != null) {
            val dayName = getDayName(classEntity.dayOfWeek)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = classEntity.subjectName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dayName, ${classEntity.startTime} – ${classEntity.endTime}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = subTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionRow(
                    iconRes = R.drawable.ic_info_circle,
                    text = ClassTypeUtils.getFullName(classEntity.classType),
                    label = "Typ",
                    iconColor = iconTint,
                    textColor = textColor,
                    labelColor = subTextColor
                )

                if (!classEntity.room.isNullOrEmpty()) {
                    DetailSectionRow(
                        iconRes = R.drawable.ic_map,
                        text = classEntity.room,
                        label = "Sala",
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }

                if (isTeacherPlan && classEntity.groupCode.isNotEmpty()) {
                    DetailSectionRow(
                        iconRes = R.drawable.ic_users,
                        text = buildString {
                            append(classEntity.groupCode)
                            if (!classEntity.subgroup.isNullOrEmpty()) append("\nPodgrupa: ${classEntity.subgroup}")
                        },
                        label = "Grupy / Kierunek",
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                } else if (!isTeacherPlan && !classEntity.teacherName.isNullOrEmpty()) {
                    DetailSectionRow(
                        iconRes = R.drawable.ic_user,
                        text = buildString {
                            append(classEntity.teacherName)
                            if (!classEntity.teacherEmail.isNullOrEmpty()) append("\n${classEntity.teacherEmail}")
                            if (!classEntity.teacherInstitute.isNullOrEmpty()) append("\n${classEntity.teacherInstitute}")
                        },
                        label = "Prowadzący",
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        } else if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun DetailSectionRow(
    iconRes: Int,
    text: String,
    label: String,
    iconColor: Color,
    textColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = labelColor)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Szczegóły - Plan Studenta")
@Composable
fun StudentClassDetailsPreview() {
    MyUZTheme {
        ClassDetailsContent(
            classEntity = ClassEntity(
                id = 1,
                subjectName = "Algorytmy i Struktury Danych",
                classType = "W",
                startTime = "08:15",
                endTime = "09:45",
                dayOfWeek = 1,
                date = "2024-10-14",
                groupCode = "31-INF-S",
                subgroup = null,
                teacherName = "Prof. dr hab. inż. Jan Kowalski",
                teacherEmail = "j.kowalski@iie.uz.zgora.pl",
                teacherInstitute = "Instytut Informatyki",
                room = "A-2 105"
            ),
            isLoading = false,
            isTeacherPlan = false,
            onBackClick = {}
        )
    }
}
