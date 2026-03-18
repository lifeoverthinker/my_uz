package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.ThemeMode
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.navigation.Screen
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        setContent {
            val settingsState by settingsRepo.getSettingsStream().collectAsState(initial = null)

            if (settingsState != null) {
                val currentSettings = settingsState ?: SettingsEntity()
                val isSystemDark = isSystemInDarkTheme()

                // --- THEME SELECTION LOGIC ---
                val useDarkTheme = when (currentSettings.themeMode) {
                    ThemeMode.DARK.name -> true
                    ThemeMode.LIGHT.name -> false
                    else -> isSystemDark
                }

                val startDest = if (currentSettings.isFirstRun) "landing" else Screen.Main.route

                MyUZTheme(darkTheme = useDarkTheme) {
                    AppNavigation(startDestination = startDest)
                }
            } else {
                MyUZTheme(darkTheme = isSystemInDarkTheme()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}