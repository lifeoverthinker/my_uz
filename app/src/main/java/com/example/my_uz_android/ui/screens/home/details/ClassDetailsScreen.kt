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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.screens.calendar.CalendarViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme
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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.classEntity != null) {
        ClassDetailsContent(
            classEntity = uiState.classEntity!!,
            onBackClick = onBackClick,
            isTeacherPlan = isTeacherPlan
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest), contentAlignment = Alignment.Center) {
            Text("Nie znaleziono szczegółów zajęć.", color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailsContent(
    classEntity: ClassEntity,
    onBackClick: () -> Unit,
    isTeacherPlan: Boolean = false
) {
    val dateOrDayText = if (!classEntity.date.isNullOrBlank()) {
        try {
            val date = LocalDate.parse(classEntity.date)
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl", "PL"))
            date.format(formatter).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            classEntity.date
        }
    } else {
        val day = DayOfWeek.of(if (classEntity.dayOfWeek == 0) 7 else classEntity.dayOfWeek)
        day.getDisplayName(TextStyle.FULL, Locale("pl", "PL")).replaceFirstChar { it.uppercase() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = "",
                    navigationIcon = R.drawable.ic_close,
                    isNavigationIconFilled = true,
                    onNavigationClick = onBackClick,
                    actions = { },
                    containerColor = Color.Transparent
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Nagłówek ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF9C27B0), RoundedCornerShape(4.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column {
                        Text(
                            text = classEntity.subjectName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dateOrDayText • ${classEntity.startTime} - ${classEntity.endTime}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Miejsce (Sala) ---
                if (!classEntity.room.isNullOrBlank()) {
                    DetailRow(
                        iconRes = R.drawable.ic_marker_pin,
                        label = "Miejsce",
                        value = "Sala ${classEntity.room}"
                    )
                }

                if (!isTeacherPlan) {
                    if (!classEntity.teacherName.isNullOrBlank()) {
                        val teacherDetails = buildString {
                            append(classEntity.teacherName)
                            if (!classEntity.teacherEmail.isNullOrBlank()) append("\n${classEntity.teacherEmail}")
                            if (!classEntity.teacherInstitute.isNullOrBlank()) append("\n${classEntity.teacherInstitute}")
                        }
                        DetailRow(
                            iconRes = R.drawable.ic_user,
                            label = "Prowadzący",
                            value = teacherDetails,
                            isMultiline = true
                        )
                    }
                } else {
                    val groupInfo = buildString {
                        append(classEntity.groupCode)
                        if (!classEntity.subgroup.isNullOrBlank()) append(" (Podgrupa: ${classEntity.subgroup})")
                    }
                    if (groupInfo.isNotBlank()) {
                        DetailRow(
                            iconRes = R.drawable.ic_users,
                            label = "Grupa",
                            value = groupInfo
                        )
                    }
                }

                DetailRow(
                    iconRes = R.drawable.ic_stand,
                    label = "Rodzaj zajęć",
                    value = ClassTypeUtils.getFullName(classEntity.classType)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int,
    label: String?,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = if (isMultiline) 4.dp else 0.dp).size(24.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value.ifEmpty { "-" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = valueColor
            )
        }
    }
}
