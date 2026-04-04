package com.example.my_uz_android.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.ui.components.ClassCard
import com.example.my_uz_android.ui.theme.ClassColorPalette
import kotlin.math.abs
import com.example.my_uz_android.ui.components.DashboardEmptyCard
import com.example.my_uz_android.ui.components.DashboardActionEmptyCard
import com.example.my_uz_android.ui.theme.getAppBackgroundColor
import com.example.my_uz_android.ui.theme.getAppAccentColor
import androidx.compose.ui.res.stringResource

@Composable
fun UpcomingClasses(
    classes: List<ClassEntity>,
    isPlanSelected: Boolean,
    emptyMessage: String?,
    dayLabel: String?,
    classColorMap: Map<String, Int>,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClassClick: (Int) -> Unit,
    onSetupPlanClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = stringResource(R.string.upcoming_classes_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (dayLabel != null && isPlanSelected) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (!isPlanSelected) {
            DashboardActionEmptyCard(
                title = stringResource(R.string.setup_plan_title),
                message = stringResource(R.string.setup_plan_message),
                iconRes = R.drawable.ic_calendar_check,
                actionText = stringResource(R.string.setup_plan_button),
                onActionClick = onSetupPlanClick,
                containerColor = getAppBackgroundColor(0, isDarkMode),
                accentColor = getAppAccentColor(0, isDarkMode),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else if (classes.isEmpty()) {
            DashboardEmptyCard(
                title = stringResource(R.string.all_set_title),
                message = emptyMessage ?: stringResource(R.string.no_classes_today_tomorrow),
                iconRes = R.drawable.ic_calendar_check,
                containerColor = getAppBackgroundColor(0, isDarkMode),
                accentColor = getAppAccentColor(0, isDarkMode),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes) { classItem ->
                    val colorIndex = classColorMap[classItem.classType]
                        ?: (abs(classItem.classType.hashCode()) % ClassColorPalette.size)

                    val bgColor = getAppBackgroundColor(colorIndex, isDarkMode)
                    val accentColor = getAppAccentColor(colorIndex, isDarkMode)

                    ClassCard(
                        classItem = classItem,
                        backgroundColor = bgColor,
                        accentColor = accentColor,
                        onClick = { onClassClick(classItem.id) },
                        modifier = Modifier.width(264.dp)
                    )
                }
            }
        }
    }
}