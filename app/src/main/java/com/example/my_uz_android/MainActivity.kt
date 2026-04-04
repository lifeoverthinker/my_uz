package com.example.my_uz_android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Główna aktywność aplikacji.
 *
 * Odpowiada za:
 * - ustawienie języka aplikacji (`pl`/`en`) na starcie i po zmianie w ustawieniach,
 * - uruchomienie nawigacji i motywu,
 * - obsługę splash screena i uprawnień powiadomień.
 */
class MainActivity : ComponentActivity() {
    @Volatile
    private var keepSplashVisible = true

    override fun attachBaseContext(newBase: Context) {
        val settingsRepo = (newBase.applicationContext as MyUZApplication).container.settingsRepository

        val languageCode = try {
            runBlocking {
                settingsRepo.getSettingsStream().map { it?.appLanguage }.first()
            }
        } catch (e: Exception) {
            null
        }

        val normalizedLanguage = normalizeLanguageCode(languageCode, Locale.getDefault().language)

        val locale = Locale(normalizedLanguage)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val contextWithLocale = newBase.createConfigurationContext(config)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val settingsRepo = (application as MyUZApplication).container.settingsRepository
        splashScreen.setKeepOnScreenCondition { keepSplashVisible }

        setContent {
            val appSettingsFlow = remember {
                settingsRepo.getSettingsStream().map { it ?: SettingsEntity() }
            }
            val appSettings by appSettingsFlow.collectAsState(initial = null)

            LaunchedEffect(appSettings) {
                if (appSettings != null) {
                    keepSplashVisible = false
                }
            }

            if (appSettings == null) {
                Box(modifier = Modifier.fillMaxSize())
                return@setContent
            }

            val settings = appSettings!!

            LaunchedEffect(settings.appLanguage) {
                val desiredLanguage = normalizeLanguageCode(settings.appLanguage, Locale.getDefault().language)
                val currentLanguage = resources.configuration.locales[0]?.language ?: Locale.getDefault().language

                if (currentLanguage != desiredLanguage) {
                    val locale = Locale(desiredLanguage)
                    Locale.setDefault(locale)

                    val newConfig = Configuration(resources.configuration)
                    newConfig.setLocale(locale)
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(newConfig, resources.displayMetrics)

                    recreate()
                }
            }

            val isSystemDark = isSystemInDarkTheme()
            val themeMode = settings.themeMode

            val useDarkTheme = when (themeMode) {
                ThemeMode.DARK.name -> true
                ThemeMode.LIGHT.name -> false
                else -> isSystemDark
            }

            MyUZTheme(darkTheme = useDarkTheme) {
                val startDestination = if (settings.isFirstRun) "landing" else Screen.Main.route

                RequestNotificationPermission()
                AppNavigation(
                    startDestination = startDestination,
                    deepLinkIntent = intent
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        triggerWidgetUpdate(this)
    }
}

/**
 * Normalizuje kod języka do dwóch wspieranych opcji aplikacji: `pl` i `en`.
 *
 * Dla legacy wartości (`system`, null, inne) wybór opiera się na języku urządzenia:
 * `en` pozostaje `en`, wszystko inne mapuje do `pl`.
 */
private fun normalizeLanguageCode(rawCode: String?, deviceLanguage: String): String = when (rawCode) {
    "pl" -> "pl"
    "en" -> "en"
    else -> if (deviceLanguage == "en") "en" else "pl"
}

@Composable
/**
 * Prosi użytkownika o zgodę na powiadomienia na Androidzie 13+.
 */
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