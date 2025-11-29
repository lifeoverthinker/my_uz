package com.example.my_uz_android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.example.my_uz_android.ui.screens.calendar.TasksScreen
import com.example.my_uz_android.ui.screens.home.HomeScreen
import com.example.my_uz_android.ui.screens.home.details.ClassDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.EventDetailsScreen
import com.example.my_uz_android.ui.screens.home.details.TaskDetailsScreen
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.ui.theme.extendedColors

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Main, Screen.Calendar, Screen.Index, Screen.Account)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = items.any { it.route == currentRoute }

    MyUZTheme {
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
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.Transparent,
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
            // POPRAWKA: Aplikujemy padding TYLKO od dołu.
            // Góra jest obsługiwana przez poszczególne ekrany (statusBarsPadding),
            // co pozwala na rysowanie tła pod paskiem stanu.
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable(Screen.Main.route) {
                    HomeScreen(
                        onClassClick = { classId -> navController.navigate("class_details/$classId") },
                        onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                        onTaskClick = { taskId -> navController.navigate("task_details/$taskId") },
                        onAccountClick = { navController.navigate(Screen.Account.route) },
                        onCalendarClick = { navController.navigate(Screen.Calendar.route) }
                    )
                }

                composable(Screen.Calendar.route) {
                    TasksScreen(
                        onBackClick = { },
                        onTaskClick = { taskId -> navController.navigate("task_details/$taskId") }
                    )
                }

                composable(Screen.Index.route) { PlaceholderScreen("Indeks Ocen") }

                composable(Screen.Account.route) {
                    AccountScreen(
                        onBackClick = { },
                        onLogoutClick = {
                            navController.navigate("landing") {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "class_details/{classId}",
                    arguments = listOf(navArgument("classId") { type = NavType.IntType }),
                    enterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    exitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) },
                    popEnterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    popExitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) }
                ) {
                    ClassDetailsScreen(onBackClick = { navController.popBackStack() })
                }

                composable(
                    route = "event_details/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
                    enterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    exitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) },
                    popEnterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    popExitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) }
                ) {
                    EventDetailsScreen(onBackClick = { navController.popBackStack() })
                }

                composable(
                    route = "task_details/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
                    enterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    exitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) },
                    popEnterTransition = { scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(300)) },
                    popExitTransition = { scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(300)) }
                ) { backStackEntry ->
                    TaskDetailsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onEditTask = { }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}