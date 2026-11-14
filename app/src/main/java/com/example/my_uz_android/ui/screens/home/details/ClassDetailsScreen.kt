package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.getAppBackgroundColor
import com.example.my_uz_android.util.ClassTypeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ClassDetailsScreen(
    classId: Int,
    onBackClick: () -> Unit,
    isTeacherPlan: Boolean = false,
    sharedCalendarViewModel: CalendarViewModel,
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendarUiState by sharedCalendarViewModel.uiState.collectAsState()

    LaunchedEffect(classId) {
        if (classId == -1) {
            viewModel.setTemporaryClass(calendarUiState.temporaryClassForDetails)
        }
    }

    if (uiState.isLoading) {
        DetailsLoadingState()
        return
    }

    val classEntity = uiState.classEntity
    if (classEntity == null) {
        DetailsEmptyState(
            title = stringResource(R.string.class_details_empty_title),
            description = stringResource(R.string.class_details_empty_message)
        )
        return
    }

    ClassDetailsContent(
        classEntity = classEntity,
        onBackClick = onBackClick,
        isTeacherPlan = isTeacherPlan
    )
}

@Composable
fun ClassDetailsContent(
    classEntity: ClassEntity,
    onBackClick: () -> Unit,
    isTeacherPlan: Boolean = false
) {
    val dateOrDayText = if (!classEntity.date.isNullOrBlank()) {
        try {
            val date = LocalDate.parse(classEntity.date)
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
            date.format(formatter).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            classEntity.date
        }
    } else {
        val day = DayOfWeek.of(if (classEntity.dayOfWeek == 0) 7 else classEntity.dayOfWeek)
        day.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }

    DetailsScaffold(onBackClick = onBackClick) {
        DetailsHeader(
            title = classEntity.subjectName,
            subtitle = "$dateOrDayText • ${classEntity.startTime} - ${classEntity.endTime}",
            squareColor = getAppBackgroundColor(0)
        )

        if (!classEntity.room.isNullOrBlank()) {
            DetailsInfoRow(
                iconRes = R.drawable.ic_marker_pin,
                label = stringResource(R.string.label_place),
                value = stringResource(R.string.label_room_format, classEntity.room ?: "")
            )
        }

        if (!isTeacherPlan) {
            if (!classEntity.teacherName.isNullOrBlank()) {
                DetailsInfoRow(
                    iconRes = R.drawable.ic_user,
                    label = stringResource(R.string.label_teacher),
                    value = classEntity.teacherName
                )
            }
        } else {
            val groupInfo = buildString {
                append(classEntity.groupCode)
                if (!classEntity.subgroup.isNullOrBlank()) append(" (Podgrupa: ${classEntity.subgroup})")
            }
            if (groupInfo.isNotBlank()) {
                DetailsInfoRow(
                    iconRes = R.drawable.ic_users,
                    label = stringResource(R.string.label_group),
                    value = groupInfo
                )
            }
        }

        DetailsInfoRow(
            iconRes = R.drawable.ic_stand,
            label = stringResource(R.string.label_class_type_details),
            value = ClassTypeUtils.getFullName(classEntity.classType)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassDetailsContentPreview() {
    MyUZTheme {
        ClassDetailsContent(
            classEntity = ClassEntity(
                id = 1,
                subjectName = "Algorytmy i Struktury Danych",
                classType = "W",
                room = "A-101",
                startTime = "08:15",
                endTime = "09:45",
                dayOfWeek = 1,
                date = "2026-04-02",
                groupCode = "INF-1A",
                subgroup = "1",
                teacherName = "dr Jan Kowalski",
                teacherEmail = "jan.kowalski@uczelnia.pl",
                teacherInstitute = "Instytut Informatyki",
                colorHex = "#3D84FF"
            ),
            onBackClick = {},
            isTeacherPlan = false
        )
    }
}