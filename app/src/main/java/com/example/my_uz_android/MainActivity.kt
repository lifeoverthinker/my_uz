package com.example.my_uz_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
            RequestNotificationPermission()

            // FIX: null to poprawny stan (np. świeża instalacja), więc dajemy domyślne ustawienia.
            val settingsState by settingsRepo
                .getSettingsStream()
                .collectAsState(initial = null)

            val currentSettings = settingsState ?: SettingsEntity()
            val isSystemDark = isSystemInDarkTheme()

            val useDarkTheme = when (currentSettings.themeMode) {
                ThemeMode.DARK.name -> true
                ThemeMode.LIGHT.name -> false
                else -> isSystemDark
            }

            val startDest = if (currentSettings.isFirstRun) "landing" else Screen.Main.route

            MyUZTheme(darkTheme = useDarkTheme) {
                AppNavigation(
                    startDestination = startDest,
                    deepLinkIntent = intent
                )
            }
        }
    }
}

/**
 * Prośba o uprawnienie POST_NOTIFICATIONS (Android 13+).
 */
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
}