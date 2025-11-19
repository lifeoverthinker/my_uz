package com.example.my_uz_android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.AppFont

// --- KOLORY Z FIGMY / FLUTTERA ---
// Definiujemy je tutaj, aby idealnie pasowały do projektu,
// niezależnie od globalnego motywu aplikacji.
private val NavBackgroundColor = Color(0xFFFFFFFF) // Colors.white
private val NavBorderColor = Color(0xFFEDE6F3)     // Color(0xFFEDE6F3)
private val NavActiveColor = Color(0xFF381E72)     // Color(0xFF381E72)
private val NavInactiveColor = Color(0xFF787579)   // Color(0xFF787579)

// Definicja ekranów
sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    data object Main : Screen("main", "Główna", R.drawable.ic_home)
    data object Calendar : Screen("calendar", "Kalendarz", R.drawable.ic_calendar)
    data object Index : Screen("index", "Indeks", R.drawable.ic_graduation_hat)
    data object Account : Screen("account", "Konto", R.drawable.ic_user)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Main,
        Screen.Calendar,
        Screen.Index,
        Screen.Account
    )

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavBackgroundColor) // Białe tło
                    // Rysujemy linię TYLKO na górze (width 1px w Figmie to ok. 1dp w Androidzie)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = NavBorderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                    // Odsunięcie od dolnej krawędzi ekranu (na gesty/przyciski systemowe)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp) // Wysokość paska
                        .padding(bottom = 16.dp), // Padding dolny z Figmy
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = screen.iconResId),
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp), // Wymuszamy rozmiar 24x24
                                    // Ważne: Tintujemy ikonę na odpowiedni kolor
                                    tint = if (selected) NavActiveColor else NavInactiveColor
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    // Styl labelSmall z Type.kt ma: Inter, 11sp, w500, letterSpacing 0.5
                                    // co idealnie pasuje do Twojego kodu Fluttera.
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) NavActiveColor else NavInactiveColor
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
                            // Wyłączamy domyślne kolory Material3, bo sterujemy nimi ręcznie powyżej
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = NavActiveColor,
                                selectedTextColor = NavActiveColor,
                                unselectedIconColor = NavInactiveColor,
                                unselectedTextColor = NavInactiveColor
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) { PlaceholderScreen("Strona Główna") }
            composable(Screen.Calendar.route) { PlaceholderScreen("Kalendarz") }
            composable(Screen.Index.route) { PlaceholderScreen("Indeks Ocen") }
            composable(Screen.Account.route) { PlaceholderScreen("Konto Studenta") }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}