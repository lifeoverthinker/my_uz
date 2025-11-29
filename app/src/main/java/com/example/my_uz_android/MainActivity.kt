package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.my_uz_android.data.repositories.SettingsRepository
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MyUZApplication).container
        val settingsRepository = appContainer.settingsRepository

        setContent {
            // Obserwujemy ustawienia w czasie rzeczywistym
            val settings by settingsRepository.getSettingsStream().collectAsState(initial = null)

            // Jeśli settings są null (jeszcze nie załadowane), użyj systemowego
            val isDarkTheme = settings?.isDarkMode ?: isSystemInDarkTheme()

            MyUZTheme(
                darkTheme = isDarkTheme
            ) {
                AppNavigation()
            }
        }
    }
}