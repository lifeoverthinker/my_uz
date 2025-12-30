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
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.navigation.Screen
import com.example.my_uz_android.ui.theme.MyUZTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        setContent {
            val settings by settingsRepo.getSettingsStream()
                .map { it ?: SettingsEntity() }
                .collectAsState(initial = null)

            if (settings != null) {
                val currentSettings = settings!!
                val isDark = currentSettings.isDarkMode
                val startDest = if (currentSettings.isFirstRun) "landing" else Screen.Main.route

                MyUZTheme(darkTheme = isDark) {
                    AppNavigation(startDestination = startDest)
                }
            } else {
                MyUZTheme {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}