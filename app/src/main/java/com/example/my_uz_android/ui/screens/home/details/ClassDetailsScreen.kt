package com.example.my_uz_android.ui.screens.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.extendedColors
import com.example.my_uz_android.ui.theme.MyUZTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import com.example.my_uz_android.util.ClassTypeUtils

@Composable
fun ClassDetailsScreen(
    onBackClick: () -> Unit,
    isTeacherPlan: Boolean = false, // <-- NOWY PARAMETR STERUJĄCY LOGIKĄ
    viewModel: ClassDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

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
    // TŁO ZEWNĘTRZNE: Szare/przyciemnione, dokładnie jak to robi Google Calendar
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    // TŁO KARTY: Czysta biel (w jasnym motywie)
    val surfaceColor = MaterialTheme.colorScheme.surface

    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.extendedColors.classCardBackground

    // Zewnętrzny Box tworzy szare tło aplikacji na samej górze (pod paskiem powiadomień)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // Główna biała karta z zaokrągleniami
        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Uchwyt do przeciągania (Drag handle) w stylu Google
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                // Header z przyciskiem zamknięcia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount > 10) onBackClick()
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailIconBox(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_x_close),
                            contentDescription = "Zamknij",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (classEntity != null) {
                    val dayName = try {
                        DayOfWeek.of(classEntity.dayOfWeek)
                            .getDisplayName(TextStyle.FULL, Locale("pl"))
                            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    } catch (e: Exception) {
                        ""
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DetailIconBox {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = classEntity.subjectName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Text(
                                text = "$dayName, ${classEntity.startTime} – ${classEntity.endTime}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = subTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSection(
                        label = "TYP",
                        text = ClassTypeUtils.getFullName(classEntity.classType),
                        iconRes = R.drawable.ic_info_circle,
                        iconColor = iconTint,
                        textColor = textColor,
                        labelColor = subTextColor
                    )

                    if (!classEntity.room.isNullOrEmpty()) {
                        DetailSection(
                            label = "SALA",
                            text = classEntity.room,
                            iconRes = R.drawable.ic_map,
                            iconColor = iconTint,
                            textColor = textColor,
                            labelColor = subTextColor
                        )
                    }

                    // --- ŚCISŁY PODZIAŁ LOGIKI ---
                    if (isTeacherPlan) {
                        // PLAN NAUCZYCIELA -> TYLKO GRUPY I PODGRUPY
                        if (classEntity.groupCode.isNotEmpty()) {
                            val groupsText = if (!classEntity.subgroup.isNullOrEmpty()) {
                                "${classEntity.groupCode}, podgrupa: ${classEntity.subgroup}"
                            } else {
                                classEntity.groupCode
                            }
                            DetailSection(
                                label = "GRUPY I PODGRUPY",
                                text = groupsText,
                                iconRes = R.drawable.ic_users,
                                iconColor = iconTint,
                                textColor = textColor,
                                labelColor = subTextColor
                            )
                        }
                    } else {
                        // PLAN STUDENTA (GRUPY) -> TYLKO PROWADZĄCY
                        if (!classEntity.teacherName.isNullOrEmpty()) {
                            DetailSection(
                                label = "PROWADZĄCY",
                                text = classEntity.teacherName,
                                iconRes = R.drawable.ic_user,
                                iconColor = iconTint,
                                textColor = textColor,
                                labelColor = subTextColor
                            )
                        }
                    }

                } else if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nie znaleziono zajęć",
                                style = MaterialTheme.typography.bodyLarge,
                                color = subTextColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onBackClick) {
                                Text("Powrót")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailIconBox(onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun DetailSection(
    label: String,
    text: String,
    iconRes: Int,
    iconColor: Color,
    textColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        DetailIconBox {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        }
    }
}

// ZŁOTA ZASADA: Previews
@Preview(showBackground = true, name = "Szczegóły - Plan Studenta")
@Composable
fun StudentClassDetailsPreview() {
    MyUZTheme {
        ClassDetailsContent(
            classEntity = ClassEntity(
                id = 1,
                subjectName = "Programowanie Obiektowe",
                classType = "L",
                startTime = "10:15",
                endTime = "11:45",
                dayOfWeek = 2,
                date = "2024-05-15",
                groupCode = "31-INF-L",
                subgroup = "L2",
                teacherName = "Dr. Jan Kowalski",
                room = "A-2 105"
            ),
            isLoading = false,
            isTeacherPlan = false, // Wyświetli tylko prowadzącego
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Szczegóły - Plan Nauczyciela")
@Composable
fun TeacherClassDetailsPreview() {
    MyUZTheme {
        ClassDetailsContent(
            classEntity = ClassEntity(
                id = 1,
                subjectName = "Programowanie Obiektowe",
                classType = "L",
                startTime = "10:15",
                endTime = "11:45",
                dayOfWeek = 2,
                date = "2024-05-15",
                groupCode = "31-INF-L",
                subgroup = "L2",
                teacherName = "Dr. Jan Kowalski",
                room = "A-2 105"
            ),
            isLoading = false,
            isTeacherPlan = true, // Wyświetli tylko grupy
            onBackClick = {}
        )
    }
}