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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.example.my_uz_android.ui.screens.notifications.NotificationsScreen
import com.example.my_uz_android.ui.screens.notifications.NotificationsViewModel

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar_check)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)

    data object GradeDetails : Screen("grade_details", "Szczegóły oceny", 0)
    data object AddEditGrade : Screen("add_grade", "Dodaj/Edytuj ocenę", 0)
    data object ClassDetails : Screen("class_details", "Szczegóły zajęć", 0) // <--- DODAJ TĘ LINIJKĘ
}
@Composable
fun AppNavigation(
    startDestination: String = "landing",
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(Screen.Main, Screen.Calendar, Screen.Index, Screen.Account)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // NAPRAWA: Wspólny ViewModel dla całego drzewa Kalendarza i Szuflady Ulubionych (Drawer)
    val sharedCalendarViewModel: CalendarViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    val calendarUiState by sharedCalendarViewModel.uiState.collectAsState()

    val showBottomBar = items.any { currentRoute?.startsWith(it.route) == true } ||
            currentRoute == "tasks" ||
            currentRoute == "schedule_search" ||
            currentRoute == "schedule_preview"

    val navBackgroundColor = MaterialTheme.extendedColors.navBackground
    val navBorderColor = MaterialTheme.extendedColors.navBorder
    val navActiveColor = MaterialTheme.extendedColors.navActive
    val navInactiveColor = MaterialTheme.extendedColors.navInactive

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                CalendarDrawerContent(
                    favorites = calendarUiState.favorites, // Pobieramy rzeczywiste ulubione!
                    selectedResourceId = calendarUiState.selectedResourceId,
                    currentScreen = currentRoute ?: "",
                    onMyPlanClick = {
                        scope.launch { drawerState.close() }
                        sharedCalendarViewModel.selectMyPlan()
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTasksClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("tasks") {
                            launchSingleTop = true
                        }
                    },
                    onFavoriteClick = { fav ->
                        scope.launch { drawerState.close() }
                        sharedCalendarViewModel.selectFavoritePlan(fav)
                        navController.navigate("schedule_preview")
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
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
                                val isCalendarSection = screen.route == "calendar" &&
                                        (currentRoute == "tasks" || currentRoute == "schedule_search" || currentRoute == "schedule_preview")

                                val selected = currentDestination?.hierarchy?.any {
                                    it.route?.startsWith(screen.route) == true
                                } == true || isCalendarSection

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
                                            restoreState = screen.route != Screen.Calendar.route
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
                        navController.navigate(Screen.Main.route) {
                            popUpTo("landing") {
                                inclusive = true
                            }
                        }
                    })
                }

                composable(Screen.Main.route) {
                    HomeScreen(
                        onClassClick = { classId -> navController.navigate("class_details/$classId") },
                        onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                        onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                        onAccountClick = { navController.navigate(Screen.Account.route) },
                        onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                        onNotificationsClick = { navController.navigate("notifications") }, // PODPIĘCIE EKRANU
                        onAddGradeClick = { navController.navigate("add_grade") },
                        onAddAbsenceClick = { navController.navigate("add_absence") },
                        onAddTaskClick = { navController.navigate("add_task") }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onSearchClick = { navController.navigate("schedule_search") },
                        onTasksClick = { navController.navigate("tasks") },
                        onAccountClick = { navController.navigate(Screen.Account.route) },
                        onClassClick = { classEntity -> navController.navigate("class_details/${classEntity.id}") },
                        onShowPreview = { navController.navigate("schedule_preview") },
                        viewModel = sharedCalendarViewModel // Podmiana na instancję ze wspólnego Scopa
                    )
                }

                composable("schedule_preview") {
                    SchedulePreviewScreen(
                        navController = navController,
                        viewModel = sharedCalendarViewModel, // Podmiana
                        onClassClick = { classEntity ->
                            sharedCalendarViewModel.setTemporaryClassForDetails(classEntity)
                            navController.navigate("class_details/-1")
                        }
                    )
                }

                composable("schedule_search") {
                    val searchViewModel: ScheduleSearchViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    ScheduleSearchScreen(
                        navController = navController,
                        searchViewModel = searchViewModel,
                        calendarViewModel = sharedCalendarViewModel // Podmiana
                    )
                }

                composable("tasks") {
                    TasksScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddTaskClick = { navController.navigate("add_task") },
                        onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                        onCalendarClick = { navController.popBackStack() },
                        onAccountClick = { navController.navigate(Screen.Account.route) }
                    )
                }

                composable(
                    route = "index?tab={tab}",
                    arguments = listOf(navArgument("tab") {
                        defaultValue = 0; type = NavType.IntType
                    })
                ) { backStackEntry ->
                    val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                    IndexScreen(
                        initialTab = initialTab,
                        onGradeDetailsClick = { gradeId -> navController.navigate("grade_details/$gradeId") },
                        onNavigateToClassTypeGrades = { subjectName, classType ->
                            navController.navigate("class_type_grades/$subjectName/$classType")
                        },
                        onAddGradeClick = { subject, classType ->
                            val route = if (subject != null && classType != null)
                                "add_grade?subject=$subject&classType=$classType" else "add_grade"
                            navController.navigate(route)
                        },
                        onAddAbsenceClick = { subject, classType ->
                            val route = if (subject != null && classType != null)
                                "add_absence?subject=$subject&classType=$classType" else "add_absence"
                            navController.navigate(route)
                        },
                        onEditAbsenceClick = { absenceId -> navController.navigate("absence_details/$absenceId") }
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

                composable("personal_data") {
                    PersonalDataScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { navController.navigate("edit_personal_data") })
                }
                composable("edit_personal_data") { EditPersonalDataScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("settings") { SettingsScreen(onBackClick = { navController.popBackStack() }) }
                composable("about_app") { AboutAppScreen(onBackClick = { navController.popBackStack() }) }

                composable(
                    route = "${Screen.ClassDetails.route}/{classId}?isTeacherPlan={isTeacherPlan}",
                    arguments = listOf(
                        navArgument("classId") { type = NavType.IntType },
                        navArgument("isTeacherPlan") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val isTeacherPlan = backStackEntry.arguments?.getBoolean("isTeacherPlan") ?: false

                    ClassDetailsScreen(
                        onBackClick = { navController.popBackStack() },
                        isTeacherPlan = isTeacherPlan
                    )
                }

                composable (
                        "event_details/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                ) { EventDetailsScreen(onBackClick = { navController.popBackStack() }) }

                composable(
                    "task_details/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                ) {
                    TaskDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onEditTask = { taskId -> navController.navigate("edit_task/$taskId") },
                        onDuplicateTask = { title, desc, subject, type, dueDate, isAllDay ->
                            navController.navigate("add_task?title=$title&desc=$desc&subject=$subject&type=$type&dueDate=$dueDate&isAllDay=$isAllDay")
                        }
                    )
                }

                composable(
                    route = "add_task?title={title}&desc={desc}&subject={subject}&type={type}&dueDate={dueDate}&isAllDay={isAllDay}",
                    arguments = listOf(
                        navArgument("title") { nullable = true; defaultValue = null },
                        navArgument("desc") { nullable = true; defaultValue = null },
                        navArgument("subject") { nullable = true; defaultValue = null },
                        navArgument("type") { nullable = true; defaultValue = null },
                        navArgument("dueDate") { type = NavType.LongType; defaultValue = 0L },
                        navArgument("isAllDay") { type = NavType.BoolType; defaultValue = false }
                    )
                ) { backStackEntry ->
                    TaskAddEditScreen(
                        taskId = null,
                        prefilledTitle = backStackEntry.arguments?.getString("title"),
                        prefilledDesc = backStackEntry.arguments?.getString("desc"),
                        prefilledSubject = backStackEntry.arguments?.getString("subject"),
                        prefilledType = backStackEntry.arguments?.getString("type"),
                        prefilledDate = backStackEntry.arguments?.getLong("dueDate")
                            .takeIf { it != 0L },
                        prefilledIsAllDay = backStackEntry.arguments?.getBoolean("isAllDay")
                            ?: false,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "edit_task/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                ) { backStackEntry ->
                    TaskAddEditScreen(
                        taskId = backStackEntry.arguments?.getInt("taskId"),
                        onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = "add_grade?gradeId={gradeId}&subject={subject}&classType={classType}&gradeValue={gradeValue}&weight={weight}&description={description}&comment={comment}",
                    arguments = listOf(
                        navArgument("gradeId") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("subject") { type = NavType.StringType; nullable = true },
                        navArgument("classType") { type = NavType.StringType; nullable = true },
                        navArgument("gradeValue") { type = NavType.StringType; nullable = true },
                        navArgument("weight") { type = NavType.StringType; nullable = true },
                        navArgument("description") { type = NavType.StringType; nullable = true },
                        navArgument("comment") { type = NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    AddEditGradeScreen(
                        gradeId = backStackEntry.arguments?.getInt("gradeId"),
                        prefilledSubject = backStackEntry.arguments?.getString("subject"),
                        prefilledClassType = backStackEntry.arguments?.getString("classType"),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "absence_details/{absenceId}",
                    arguments = listOf(navArgument("absenceId") { type = NavType.IntType })
                ) {
                    AbsenceDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("edit_absence/$id") },
                        onDuplicateAbsence = { subject, type, date ->
                            navController.navigate("add_absence?subject=$subject&classType=$type&date=$date")
                        }
                    )
                }

                composable(
                    route = Screen.GradeDetails.route + "/{gradeId}",
                    arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
                ) {
                    GradeDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("edit_grade/$id") },
                        onDuplicateGrade = { subject, classType, grade, weight, desc, comment ->
                            navController.navigate("add_grade?gradeId=0&subject=$subject&classType=$classType&gradeValue=$grade&weight=$weight&description=$desc&comment=$comment")
                        }
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
                    route = "edit_grade/{gradeId}",
                    arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
                ) { backStackEntry ->
                    AddEditGradeScreen(
                        gradeId = backStackEntry.arguments?.getInt("gradeId"),
                        prefilledSubject = null,
                        prefilledClassType = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "class_type_grades/{subjectName}/{classType}",
                    arguments = listOf(
                        navArgument("subjectName") { type = NavType.StringType },
                        navArgument("classType") { type = NavType.StringType })
                ) { backStackEntry ->
                    SubjectGradesScreen(
                        subjectName = backStackEntry.arguments?.getString("subjectName") ?: "",
                        classType = backStackEntry.arguments?.getString("classType") ?: "",
                        onNavigateBack = { navController.popBackStack() },
                        onGradeClick = { gradeId -> navController.navigate("grade_details/$gradeId") },
                        onAddGradeClick = {
                            navController.navigate(
                                "add_grade?subject=${
                                    backStackEntry.arguments?.getString(
                                        "subjectName"
                                    )
                                }&classType=${backStackEntry.arguments?.getString("classType")}"
                            )
                        }
                    )
                }

                composable(
                    route = "add_absence?subject={subject}&classType={classType}&date={date}",
                    arguments = listOf(
                        navArgument("subject") { type = NavType.StringType; nullable = true },
                        navArgument("classType") { type = NavType.StringType; nullable = true },
                        navArgument("date") { type = NavType.LongType; defaultValue = 0L }
                    )
                ) { backStackEntry ->
                    AddEditAbsenceScreen(
                        absenceId = null,
                        prefilledSubject = backStackEntry.arguments?.getString("subject"),
                        prefilledClassType = backStackEntry.arguments?.getString("classType"),
                        prefilledDate = backStackEntry.arguments?.getLong("date")
                            .takeIf { it != 0L },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "edit_absence/{absenceId}",
                    arguments = listOf(navArgument("absenceId") { type = NavType.IntType })
                ) { backStackEntry ->
                    AddEditAbsenceScreen(
                        absenceId = backStackEntry.arguments?.getInt("absenceId"),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("notifications") {
                    val notificationsViewModel: NotificationsViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    NotificationsScreen(
                        viewModel = notificationsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}