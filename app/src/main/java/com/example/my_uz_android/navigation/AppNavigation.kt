package com.example.my_uz_android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.screens.account.AccountScreen
import com.example.my_uz_android.ui.screens.account.EditPersonalDataScreen
import com.example.my_uz_android.ui.screens.account.PersonalDataScreen
import com.example.my_uz_android.ui.screens.account.SettingsScreen // ✅ Dodano import
import com.example.my_uz_android.ui.screens.calendar.TaskAddEditScreen
import com.example.my_uz_android.ui.screens.calendar.TasksScreen
import com.example.my_uz_android.ui.screens.home.HomeScreen
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.EventDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsScreen
import com.example.my_uz_android.ui.screens.index.AddEditAbsenceScreen
import com.example.my_uz_android.ui.screens.index.AddEditGradeScreen
import com.example.my_uz_android.ui.screens.index.GradeDetailsScreen
import com.example.my_uz_android.ui.screens.index.IndexScreen
import com.example.my_uz_android.ui.screens.index.SubjectGradesScreen
import com.example.my_uz_android.ui.screens.onboarding.LandingScreen
import com.example.my_uz_android.ui.theme.extendedColors

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)
}

@Composable
fun AppNavigation(
    startDestination: String
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Main, Screen.Calendar, Screen.Index, Screen.Account)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = items.any { it.route == currentRoute }

    val navBackgroundColor = MaterialTheme.extendedColors.navBackground
    val navBorderColor = MaterialTheme.extendedColors.navBorder
    val navActiveColor = MaterialTheme.extendedColors.navActive
    val navInactiveColor = MaterialTheme.extendedColors.navInactive

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(navBackgroundColor)
                        .drawBehind {
                            drawLine(
                                color = navBorderColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        items.forEach { screen ->
                            val selected =
                                currentDestination?.hierarchy?.any { it.route == screen.route } == true

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(id = screen.iconResId),
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (selected) navActiveColor else navInactiveColor
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) navActiveColor else navInactiveColor
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    selectedIconColor = navActiveColor,
                                    selectedTextColor = navActiveColor,
                                    unselectedIconColor = navInactiveColor,
                                    unselectedTextColor = navInactiveColor
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("landing") {
                LandingScreen(
                    onFinishOnboarding = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo("landing") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Main.route) {
                HomeScreen(
                    onClassClick = { classId -> navController.navigate("class_details/$classId") },
                    onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                    onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                    onAccountClick = { navController.navigate(Screen.Account.route) },
                    onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                    onAddGradeClick = { navController.navigate("add_grade") },
                    onAddAbsenceClick = { navController.navigate("add_absence") },
                    onAddTaskClick = { navController.navigate("add_task") }
                )
            }

            composable(Screen.Calendar.route) {
                TasksScreen(
                    onAddTaskClick = { navController.navigate("add_task") },
                    onTaskClick = { task -> navController.navigate("task_details/${task.id}") }
                )
            }

            composable(Screen.Index.route) {
                IndexScreen(
                    onGradeDetailsClick = { gradeId ->
                        navController.navigate("grade_details/$gradeId")
                    },
                    onNavigateToClassTypeGrades = { subjectName, classType ->
                        navController.navigate("class_type_grades/$subjectName/$classType")
                    },
                    onAddGradeClick = { subject, classType ->
                        if (subject != null && classType != null) {
                            navController.navigate("add_grade?subject=$subject&classType=$classType")
                        } else {
                            navController.navigate("add_grade")
                        }
                    },
                    onAddAbsenceClick = { subject, classType ->
                        if (subject != null && classType != null) {
                            navController.navigate("add_absence?subject=$subject&classType=$classType")
                        } else {
                            navController.navigate("add_absence")
                        }
                    },
                    onEditAbsenceClick = { absenceId ->
                        navController.navigate("edit_absence/$absenceId")
                    }
                )
            }

            // ✅ ZAKTUALIZOWANA NAWIGACJA KONTA
            composable(Screen.Account.route) {
                AccountScreen(
                    onBackClick = { },
                    onLogoutClick = {
                        navController.navigate("landing") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onPersonalDataClick = {
                        navController.navigate("personal_data")
                    },
                    onSettingsClick = { // ✅ Obsługa kliknięcia w ustawienia
                        navController.navigate("settings")
                    }
                )
            }

            // ✅ EKRAN PODGLĄDU DANYCH OSOBOWYCH
            composable("personal_data") {
                PersonalDataScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate("edit_personal_data") }
                )
            }

            // ✅ EKRAN EDYCJI DANYCH OSOBOWYCH
            composable("edit_personal_data") {
                EditPersonalDataScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ✅ EKRAN USTAWIEŃ (NOWY)
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- DETALE I INNE EKRANY ---
            composable(
                "class_details/{classId}",
                arguments = listOf(navArgument("classId") { type = NavType.IntType })
            ) {
                ClassDetailsScreen(onBackClick = { navController.popBackStack() })
            }

            composable(
                "event_details/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) {
                EventDetailsScreen(onBackClick = { navController.popBackStack() })
            }

            composable(
                "task_details/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) {
                TaskDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditTask = { taskId -> navController.navigate("edit_task/$taskId") }
                )
            }

            composable("add_task") {
                TaskAddEditScreen(
                    taskId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "edit_task/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId")
                TaskAddEditScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "add_grade?subject={subject}&classType={classType}",
                arguments = listOf(
                    navArgument("subject") { nullable = true },
                    navArgument("classType") { nullable = true }
                )
            ) { backStackEntry ->
                val subject = backStackEntry.arguments?.getString("subject")
                val classType = backStackEntry.arguments?.getString("classType")

                AddEditGradeScreen(
                    gradeId = null,
                    prefilledSubject = subject,
                    prefilledClassType = classType,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("add_absence") {
                AddEditAbsenceScreen(
                    absenceId = null,
                    prefilledSubject = null,
                    prefilledClassType = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "grade_details/{gradeId}",
                arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val gradeId = backStackEntry.arguments?.getInt("gradeId")
                GradeDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("edit_grade/$gradeId") }
                )
            }

            composable(
                route = "edit_grade/{gradeId}",
                arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val gradeId = backStackEntry.arguments?.getInt("gradeId")
                AddEditGradeScreen(
                    gradeId = gradeId,
                    prefilledSubject = null,
                    prefilledClassType = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "class_type_grades/{subjectName}/{classType}",
                arguments = listOf(
                    navArgument("subjectName") { type = NavType.StringType },
                    navArgument("classType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                val classType = backStackEntry.arguments?.getString("classType") ?: ""

                SubjectGradesScreen(
                    subjectName = subjectName,
                    classType = classType,
                    onNavigateBack = { navController.popBackStack() },
                    onGradeClick = { gradeId ->
                        navController.navigate("grade_details/$gradeId")
                    },
                    onAddGradeClick = {
                        navController.navigate("add_grade?subject=$subjectName&classType=$classType")
                    }
                )
            }

            composable(
                route = "add_absence?subject={subject}&classType={classType}",
                arguments = listOf(
                    navArgument("subject") { nullable = true },
                    navArgument("classType") { nullable = true }
                )
            ) { backStackEntry ->
                val subject = backStackEntry.arguments?.getString("subject")
                val classType = backStackEntry.arguments?.getString("classType")

                AddEditAbsenceScreen(
                    absenceId = null,
                    prefilledSubject = subject,
                    prefilledClassType = classType,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "edit_absence/{absenceId}",
                arguments = listOf(navArgument("absenceId") { type = NavType.IntType })
            ) { backStackEntry ->
                val absenceId = backStackEntry.arguments?.getInt("absenceId")
                AddEditAbsenceScreen(
                    absenceId = absenceId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}