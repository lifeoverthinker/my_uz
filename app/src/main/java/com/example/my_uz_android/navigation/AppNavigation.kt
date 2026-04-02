package com.example.my_uz_android.navigation

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.my_uz_android.ui.components.Fab
import com.example.my_uz_android.ui.screens.account.*
import com.example.my_uz_android.ui.screens.calendar.*
import com.example.my_uz_android.ui.screens.calendar.components.CalendarDrawerContent
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchScreen
import com.example.my_uz_android.ui.screens.calendar.search.ScheduleSearchViewModel
import com.example.my_uz_android.ui.screens.calendar.tasks.*
import com.example.my_uz_android.ui.screens.home.HomeScreen
import com.example.my_uz_android.ui.screens.home.details.*
import com.example.my_uz_android.ui.screens.index.*
import com.example.my_uz_android.ui.screens.notifications.NotificationsScreen
import com.example.my_uz_android.ui.screens.notifications.NotificationsViewModel
import com.example.my_uz_android.ui.screens.onboarding.LandingScreen
import com.example.my_uz_android.ui.theme.extendedColors
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar_check)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)

    data object GradeDetails : Screen("grade_details", "Szczegóły oceny", 0)
    data object AddEditGrade : Screen("add_grade", "Dodaj/Edytuj ocenę", 0)
    data object ClassDetails : Screen("class_details", "Szczegóły zajęć", 0)
}

@Composable
fun AppNavigation(
    startDestination: String = "landing",
    navController: NavHostController = rememberNavController(),
    deepLinkIntent: android.content.Intent? = null
) {
    LaunchedEffect(deepLinkIntent) {
        if (deepLinkIntent?.data?.toString() == "myuz://add_task") {
            navController.navigate("tasks") {
                launchSingleTop = true
            }
        }
    }

    val items = listOf(Screen.Main, Screen.Calendar, Screen.Index, Screen.Account)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sharedCalendarViewModel: CalendarViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    val calendarUiState by sharedCalendarViewModel.uiState.collectAsStateWithLifecycle()

    val showBottomBar = items.any { currentRoute?.startsWith(it.route) == true } ||
            currentRoute == "tasks" ||
            currentRoute == "schedule_search" ||
            currentRoute == "schedule_preview"

    val navBackgroundColor = extendedColors.navBackground
    val navBorderColor = extendedColors.navBorder
    val navActiveColor = extendedColors.navActive
    val navInactiveColor = extendedColors.navInactive

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                CalendarDrawerContent(
                    favorites = calendarUiState.favorites,
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
                        navController.navigate("tasks") { launchSingleTop = true }
                    },
                    onFavoriteClick = { fav ->
                        scope.launch { drawerState.close() }
                        sharedCalendarViewModel.selectFavoritePlan(fav)
                        navController.navigate("schedule_preview")
                    },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
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
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeOut(tween(300))
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
                    },
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
                    }
                ) {
                    composable("landing") {
                        LandingScreen(onNavigateToHome = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo("landing") { inclusive = true }
                            }
                        })
                    }

                    composable(Screen.Main.route) {
                        HomeScreen(
                            onClassClick = { classId -> navController.navigate("class_details/$classId") },
                            onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                            onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                            onAccountClick = { navController.navigate(Screen.Account.route) },
                            onSetupPlanClick = { navController.navigate("personal_data") },
                            onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                            onNotificationsClick = { navController.navigate("notifications") },
                            onAddGradeClick = { navController.navigate("add_grade") },
                            onAddAbsenceClick = { navController.navigate("add_absence") },
                            onAddTaskClick = { navController.navigate("add_task") }
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

                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSearchClick = { navController.navigate("schedule_search") },
                            onTasksClick = { navController.navigate("tasks") },
                            onAccountClick = { navController.navigate(Screen.Account.route) },
                            onClassClick = { classEntity -> navController.navigate("class_details/${classEntity.id}") },
                            onShowPreview = { navController.navigate("schedule_preview") },
                            viewModel = sharedCalendarViewModel
                        )
                    }

                    composable("schedule_preview") {
                        SchedulePreviewScreen(
                            navController = navController,
                            viewModel = sharedCalendarViewModel,
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
                            calendarViewModel = sharedCalendarViewModel
                        )
                    }

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
                        val classId = backStackEntry.arguments?.getInt("classId") ?: 0

                        ClassDetailsScreen(
                            classId = classId,
                            onBackClick = { navController.popBackStack() },
                            isTeacherPlan = isTeacherPlan,
                            sharedCalendarViewModel = sharedCalendarViewModel
                        )
                    }

                    composable(
                        "event_details/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                    ) {
                        EventDetailsScreen(onBackClick = { navController.popBackStack() })
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
                        "task_details/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                    ) {
                        val detailsViewModel: TaskDetailsViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)

                        TaskDetailsScreenRoute(
                            viewModel = detailsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onEditClick = { taskId -> navController.navigate("edit_task/$taskId") },
                            onDuplicateClick = {
                                val task = detailsViewModel.uiState.value.task
                                task?.let {
                                    val title = Uri.encode(it.title)
                                    val desc = Uri.encode(it.description ?: "")
                                    val subject = Uri.encode(it.subjectName ?: "")
                                    val type = Uri.encode(it.classType ?: "")
                                    navController.navigate(
                                        "add_task?title=$title&desc=$desc&subject=$subject&type=$type&dueDate=${it.dueDate}&isAllDay=${it.isAllDay}"
                                    )
                                }
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
                    ) {
                        val addEditViewModel: TaskAddEditViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        TaskAddEditScreenRoute(
                            viewModel = addEditViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "edit_task/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                    ) {
                        val editViewModel: TaskAddEditViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        TaskAddEditScreenRoute(
                            viewModel = editViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Index.route) {
                        IndexScreen(
                            onSubjectClick = { subjectName ->
                                navController.navigate("subject_grades/${Uri.encode(subjectName)}")
                            },
                            onAddGradeClick = { navController.navigate("add_grade") },
                            onAddAbsenceClick = { navController.navigate("add_absence") },
                            onEditAbsenceClick = { id -> navController.navigate("absence_details/$id") },
                            onAddGradeSpecificClick = { subj, type ->
                                navController.navigate("add_grade?subject=${Uri.encode(subj)}&classType=${Uri.encode(type)}")
                            },
                            onAddAbsenceSpecificClick = { subj, type ->
                                navController.navigate("add_absence?subject=${Uri.encode(subj)}&classType=${Uri.encode(type)}")
                            }
                        )
                    }

                    composable("subject_grades/{subjectName}") { backStackEntry ->
                        val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                        SubjectGradesScreen(
                            subjectName = subjectName,
                            onBackClick = { navController.popBackStack() },
                            onGradeClick = { gradeId -> navController.navigate("grade_details/$gradeId") },
                            onAddGradeClick = { subj, type ->
                                navController.navigate("add_grade?subject=${Uri.encode(subj)}&classType=${Uri.encode(type)}")
                            }
                        )
                    }

                    composable(
                        route = Screen.GradeDetails.route + "/{gradeId}",
                        arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
                    ) {
                        val detailsViewModel: GradeDetailsViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        GradeDetailsScreenRoute(
                            viewModel = detailsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onEditGrade = { id -> navController.navigate("edit_grade/$id") },
                            onDuplicateGrade = {
                                val grade = detailsViewModel.uiState.value.grade
                                grade?.let {
                                    navController.navigate(
                                        "add_grade?subject=${Uri.encode(it.subjectName)}" +
                                                "&classType=${Uri.encode(it.classType)}" +
                                                "&description=${Uri.encode(it.description ?: "")}" +
                                                "&weight=${it.weight}"
                                    )
                                }
                            }
                        )
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
                    ) {
                        val addEditViewModel: AddEditGradeViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        AddEditGradeScreenRoute(
                            viewModel = addEditViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "edit_grade/{gradeId}",
                        arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
                    ) {
                        val editViewModel: AddEditGradeViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        AddEditGradeScreenRoute(
                            viewModel = editViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "absence_details/{absenceId}",
                        arguments = listOf(navArgument("absenceId") { type = NavType.IntType })
                    ) {
                        val detailsViewModel: AbsenceDetailsViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        AbsenceDetailsScreenRoute(
                            viewModel = detailsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onEditClick = { id -> navController.navigate("edit_absence/$id") },
                            onDuplicateClick = {
                                val absence = detailsViewModel.uiState.value.absence
                                absence?.let {
                                    navController.navigate(
                                        "add_absence?subject=${Uri.encode(it.subjectName ?: "")}&classType=${Uri.encode(it.classType ?: "")}"
                                    )
                                }
                            }
                        )
                    }

                    composable(
                        route = "add_absence?subject={subject}&classType={classType}",
                        arguments = listOf(
                            navArgument("subject") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("classType") { type = NavType.StringType; nullable = true; defaultValue = null }
                        )
                    ) { backStackEntry ->
                        val addEditViewModel: AddEditAbsenceViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        AbsenceAddEditScreenRoute(
                            viewModel = addEditViewModel,
                            prefilledSubject = backStackEntry.arguments?.getString("subject"),
                            prefilledClassType = backStackEntry.arguments?.getString("classType"),
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "edit_absence/{absenceId}",
                        arguments = listOf(navArgument("absenceId") { type = NavType.IntType })
                    ) {
                        val addEditViewModel: AddEditAbsenceViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)
                        AbsenceAddEditScreenRoute(
                            viewModel = addEditViewModel,
                            prefilledSubject = null,
                            prefilledClassType = null,
                            onNavigateBack = { navController.popBackStack() }
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
                            onNavigateToEdit = { navController.navigate("edit_personal_data") }
                        )
                    }

                    composable("edit_personal_data") {
                        EditPersonalDataScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable("settings") {
                        SettingsScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("about_app") {
                        AboutAppScreen(onBackClick = { navController.popBackStack() })
                    }
                }

                if (currentRoute == Screen.Main.route) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        Fab(
                            onAddGrade = { navController.navigate("add_grade") },
                            onAddAbsence = { navController.navigate("add_absence") },
                            onAddTask = { navController.navigate("add_task") }
                        )
                    }
                }
            }
        }
    }
}