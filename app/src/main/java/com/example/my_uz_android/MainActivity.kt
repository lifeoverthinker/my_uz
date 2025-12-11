package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ POPRAWKA: Używamy "container" zamiast "appContainer"
        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        setContent {
            val settings by settingsRepo.getSettingsStream().collectAsState(initial = null)
            val isDark = settings?.isDarkMode ?: false

            // ✅ POPRAWKA: Przekazujemy isDark do MyUZTheme
            MyUZTheme(darkTheme = isDark) {
                AppNavigation(startDestination = "landing")
            }
        }
    }
}
