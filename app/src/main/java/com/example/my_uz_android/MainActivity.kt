package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.navigation.Screen
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        setContent {
            // Pobieramy ustawienia jako stan. Wartość początkowa to null.
            val settings by settingsRepo.getSettingsStream().collectAsState(initial = null)

            // Sprawdzamy, czy dane zostały już załadowane z bazy
            if (settings != null) {
                val isDark = settings!!.isDarkMode
                // Jeśli isFirstRun to true (lub null/błąd), idź do onboardingu ("landing").
                // Jeśli false, idź do ekranu głównego (Screen.Main.route).
                val startDest = if (settings!!.isFirstRun) "landing" else Screen.Main.route

                MyUZTheme(darkTheme = isDark) {
                    AppNavigation(startDestination = startDest)
                }
            } else {
                // (Opcjonalnie) Ekran ładowania, zanim odczytamy bazę danych,
                // żeby użytkownik nie zobaczył "mignięcia" onboardingu.
                MyUZTheme {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}