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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.ThemeMode
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.navigation.Screen
import com.example.my_uz_android.ui.theme.MyUZTheme
import com.example.my_uz_android.widget.triggerWidgetUpdate
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    @Volatile
    private var keepSplashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val settingsRepo = (application as MyUZApplication).container.settingsRepository

        splashScreen.setKeepOnScreenCondition { keepSplashVisible }

        setContent {
            val startupSettings by produceState<SettingsEntity?>(initialValue = null) {
                value = try {
                    settingsRepo.getSettingsStream().firstOrNull() ?: SettingsEntity()
                } catch (_: Exception) {
                    SettingsEntity()
                }
            }

            keepSplashVisible = startupSettings == null

            val isSystemDark = isSystemInDarkTheme()
            val themeMode = startupSettings?.themeMode ?: ThemeMode.SYSTEM.name

            val useDarkTheme = when (themeMode) {
                ThemeMode.DARK.name -> true
                ThemeMode.LIGHT.name -> false
                else -> isSystemDark
            }

            MyUZTheme(darkTheme = useDarkTheme) {
                if (startupSettings != null) {
                    val startDestination = if (startupSettings?.isFirstRun == true) {
                        "landing"
                    } else {
                        Screen.Main.route
                    }

                    RequestNotificationPermission()
                    AppNavigation(
                        startDestination = startDestination,
                        deepLinkIntent = intent
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        triggerWidgetUpdate(this)
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