package com.example.my_uz_android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.account.*
import com.example.my_uz_android.ui.screens.calendar.*
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchScreen
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchViewModel
import com.example.my_uz_android.ui.screens.calendar.tasks.TaskAddEditScreen
import com.example.my_uz_android.ui.screens.calendar.tasks.TasksScreen
import com.example.my_uz_android.ui.screens.home.HomeScreen
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.EventDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsScreen
import com.example.my_uz_android.ui.screens.index.*
import com.example.my_uz_android.ui.screens.onboarding.LandingScreen
import com.example.my_uz_android.ui.theme.extendedColors

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar_check)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)

    // DODANE OBIEKTY DLA TRAS SZCZEGÓŁOWYCH
    data object GradeDetails : Screen("grade_details", "Szczegóły oceny", 0)
    data object AddEditGrade : Screen("add_grade", "Dodaj/Edytuj ocenę", 0)
}

@Composable
fun AppNavigation(
    startDestination: String = "landing",
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(Screen.Main, Screen.Calendar, Screen.Index, Screen.Account)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = items.any { it.route == currentRoute } || currentRoute == "tasks"

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
                            val isCalendarActive = screen.route == "calendar" && currentRoute == "tasks"
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true || isCalendarActive

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
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            composable("landing") {
                LandingScreen(onNavigateToHome = {
                    navController.navigate(Screen.Main.route) { popUpTo("landing") { inclusive = true } }
                })
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
                val calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
                CalendarScreen(
                    onSearchClick = { navController.navigate("schedule_search") },
                    onTasksClick = { navController.navigate("tasks") },
                    onAccountClick = { navController.navigate(Screen.Account.route) },
                    onClassClick = { classEntity -> navController.navigate("class_details/${classEntity.id}") },
                    onShowPreview = { navController.navigate("schedule_preview") },
                    viewModel = calendarViewModel
                )
            }

            composable("schedule_preview") {
                val parentEntry = remember(navController.currentBackStackEntry) {
                    navController.getBackStackEntry(Screen.Calendar.route)
                }
                val calendarViewModel: CalendarViewModel = viewModel(parentEntry, factory = AppViewModelProvider.Factory)

                SchedulePreviewScreen(
                    navController = navController,
                    viewModel = calendarViewModel,
                    onClassClick = { classEntity ->
                        calendarViewModel.setTemporaryClassForDetails(classEntity)
                        navController.navigate("class_details/-1")
                    }
                )
            }

            composable("schedule_search") {
                val parentEntry = remember(navController.currentBackStackEntry) {
                    navController.getBackStackEntry(Screen.Calendar.route)
                }
                val calendarViewModel: CalendarViewModel = viewModel(parentEntry, factory = AppViewModelProvider.Factory)
                val searchViewModel: ScheduleSearchViewModel = viewModel(factory = AppViewModelProvider.Factory)

                ScheduleSearchScreen(
                    navController = navController,
                    searchViewModel = searchViewModel,
                    calendarViewModel = calendarViewModel
                )
            }

            composable("tasks") {
                TasksScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddTaskClick = { navController.navigate("add_task") },
                    onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                    onCalendarClick = {
                        navController.popBackStack()
                    },
                    onAccountClick = { navController.navigate(Screen.Account.route) }
                )
            }

            composable(Screen.Index.route) {
                IndexScreen(
                    onGradeDetailsClick = { gradeId -> navController.navigate("grade_details/$gradeId") },
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
                    onEditAbsenceClick = { absenceId -> navController.navigate("edit_absence/$absenceId") }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    onBackClick = { },
                    onPersonalDataClick = { navController.navigate("personal_data") },
                    onSettingsClick = { navController.navigate("settings") },
                    onAboutClick = { navController.navigate("about_app") }
                )
            }

            composable("personal_data") { PersonalDataScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToEdit = { navController.navigate("edit_personal_data") }) }
            composable("edit_personal_data") { EditPersonalDataScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("settings") { SettingsScreen(onBackClick = { navController.popBackStack() }) }
            composable("about_app") { AboutAppScreen(onBackClick = { navController.popBackStack() }) }
            composable("class_details/{classId}", arguments = listOf(navArgument("classId") { type = NavType.IntType })) { ClassDetailsScreen(onBackClick = { navController.popBackStack() }) }
            composable("event_details/{eventId}", arguments = listOf(navArgument("eventId") { type = NavType.IntType })) { EventDetailsScreen(onBackClick = { navController.popBackStack() }) }
            composable("task_details/{taskId}", arguments = listOf(navArgument("taskId") { type = NavType.IntType })) { TaskDetailsScreen(onNavigateBack = { navController.popBackStack() }, onEditTask = { taskId -> navController.navigate("edit_task/$taskId") }) }
            composable("add_task") { TaskAddEditScreen(taskId = null, onNavigateBack = { navController.popBackStack() }) }
            composable(route = "edit_task/{taskId}", arguments = listOf(navArgument("taskId") { type = NavType.IntType })) { backStackEntry -> TaskAddEditScreen(taskId = backStackEntry.arguments?.getInt("taskId"), onNavigateBack = { navController.popBackStack() }) }

            // ZMODYFIKOWANA TRASA DLA DODAWANIA I DUPLIKACJI OCENY
            composable(
                route = "add_grade?gradeId={gradeId}&subject={subject}&classType={classType}&gradeValue={gradeValue}&weight={weight}&description={description}&comment={comment}",
                arguments = listOf(
                    navArgument("gradeId") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("subject") { type = NavType.StringType; nullable = true },
                    navArgument("classType") { type = NavType.StringType; nullable = true },
                    navArgument("gradeValue") { type = NavType.StringType; nullable = true },
                    navArgument("weight") { type = NavType.StringType; nullable = true }, // Zmienione na String
                    navArgument("description") { type = NavType.StringType; nullable = true },
                    navArgument("comment") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                AddEditGradeScreen(
                    gradeId = backStackEntry.arguments?.getInt("gradeId"),
                    // ViewModel sam wyciągnie resztę z SavedStateHandle, ale przekazujemy te podstawowe dla pewności
                    prefilledSubject = backStackEntry.arguments?.getString("subject"),
                    prefilledClassType = backStackEntry.arguments?.getString("classType"),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("add_absence") { AddEditAbsenceScreen(absenceId = null, prefilledSubject = null, prefilledClassType = null, onNavigateBack = { navController.popBackStack() }) }

            composable(
                route = Screen.GradeDetails.route + "/{gradeId}",
                arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
            ) {
                GradeDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate("edit_grade/$id")
                    },
                    onDuplicateGrade = { subject, classType, grade, weight, desc, comment ->
                        navController.navigate(
                            "add_grade?gradeId=0&subject=$subject&classType=$classType&gradeValue=$grade&weight=$weight&description=$desc&comment=$comment"
                        )
                    }
                )
            }
            composable(route = "edit_grade/{gradeId}", arguments = listOf(navArgument("gradeId") { type = NavType.IntType })) { backStackEntry -> AddEditGradeScreen(gradeId = backStackEntry.arguments?.getInt("gradeId"), prefilledSubject = null, prefilledClassType = null, onNavigateBack = { navController.popBackStack() }) }
            composable(route = "class_type_grades/{subjectName}/{classType}", arguments = listOf(navArgument("subjectName") { type = NavType.StringType }, navArgument("classType") { type = NavType.StringType })) { backStackEntry -> SubjectGradesScreen(subjectName = backStackEntry.arguments?.getString("subjectName") ?: "", classType = backStackEntry.arguments?.getString("classType") ?: "", onNavigateBack = { navController.popBackStack() }, onGradeClick = { gradeId -> navController.navigate("grade_details/$gradeId") }, onAddGradeClick = { navController.navigate("add_grade?subject=${backStackEntry.arguments?.getString("subjectName")}&classType=${backStackEntry.arguments?.getString("classType")}") }) }
            composable(route = "add_absence?subject={subject}&classType={classType}", arguments = listOf(navArgument("subject") { type = NavType.StringType; nullable = true }, navArgument("classType") { type = NavType.StringType; nullable = true })) { backStackEntry -> AddEditAbsenceScreen(absenceId = null, prefilledSubject = backStackEntry.arguments?.getString("subject"), prefilledClassType = backStackEntry.arguments?.getString("classType"), onNavigateBack = { navController.popBackStack() }) }
            composable(route = "edit_absence/{absenceId}", arguments = listOf(navArgument("absenceId") { type = NavType.IntType })) { backStackEntry -> AddEditAbsenceScreen(absenceId = backStackEntry.arguments?.getInt("absenceId"), onNavigateBack = { navController.popBackStack() }) }
        }
    }
}