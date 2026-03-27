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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        // Pobieramy repozytorium ustawień z kontenera aplikacji
        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        setContent {
            // Reagujemy na wymogi Androida 13+ dotyczące powiadomień lokalnych
            RequestNotificationPermission()

            val settingsState by settingsRepo.getSettingsStream().collectAsState(initial = null)

            if (settingsState != null) {
                val currentSettings = settingsState ?: SettingsEntity()
                val isSystemDark = isSystemInDarkTheme()

                // Logika wyboru motywu (Dark/Light/System)
                val useDarkTheme = when (currentSettings.themeMode) {
                    ThemeMode.DARK.name -> true
                    ThemeMode.LIGHT.name -> false
                    else -> isSystemDark
                }

                // Decyzja o tym, czy pokazać Landing (pierwsze uruchomienie), czy od razu plan
                val startDest = if (currentSettings.isFirstRun) "landing" else Screen.Main.route

                MyUZTheme(darkTheme = useDarkTheme) {
                    AppNavigation(
                        startDestination = startDest,
                        deepLinkIntent = intent
                    )
                }
            } else {
                // Stan ładowania podczas inicjalizacji bazy danych przy starcie
                MyUZTheme(darkTheme = isSystemInDarkTheme()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Funkcja prosząca o uprawnienie POST_NOTIFICATIONS (Android 13+).
 * Bez tego użytkownik nie zobaczy powiadomień o zajęciach i zadaniach wysyłanych przez Worker.
 */
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Tu można dodać ewentualny log lub Snackbar, jeśli użytkownik odmówił zgody
        }

        LaunchedEffect(Unit) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
}