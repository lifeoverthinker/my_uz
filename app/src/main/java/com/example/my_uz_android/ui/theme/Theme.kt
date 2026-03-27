package com.example.my_uz_android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * SEKCJA 1: Rozszerzenie Systemu Kolorów (Extended Colors)
 * * Wyjaśnienie: Material 3 posiada ograniczoną liczbę slotów na kolory.
 * Ta klasa pozwala nam dodać własne zmienne (np. specyficzne tła kart),
 * które automatycznie reagują na zmianę trybu Dark/Light.
 */
@Immutable
data class ExtendedColors(
    val classCardBackground: Color,
    val eventCardBackground: Color,
    val taskCardBackground: Color,
    val navBackground: Color,
    val navBorder: Color,
    val navActive: Color,
    val navInactive: Color,
    val homeTopBackground: Color,
    val homeHeaderBackground: Color,
    val homeContentBackground: Color,
    val buttonBackground: Color,
    val homeButtonBackground: Color,
    val iconText: Color,
    val grayInactive: Color = Color(0xFFBDBDBD)
)

// Zapewnia dostęp do rozszerzonych kolorów wewnątrz Composable
val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("No ExtendedColors provided")
}

/**
 * SEKCJA 2: Definicje Schematów Material 3
 * * Mapowanie kolorów z pliku Color.kt na standardowe sloty Material Theme.
 */
private fun lightScheme() = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

private fun darkScheme() = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

/**
 * SEKCJA 3: Główny Komponent Motywu (Entry Point)
 * * Zarządza przełączaniem trybów, kolorowaniem pasków systemowych
 * oraz dostarczaniem kolorów rozszerzonych do UI.
 */
@Composable
fun MyUZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Inicjalizacja kolorów niestandardowych zależnie od trybu
    val extended = if (darkTheme) {
        ExtendedColors(
            classCardBackground = card_class_dark,
            eventCardBackground = card_event_dark,
            taskCardBackground = card_task_dark,
            navBackground = nav_dark_background,
            navBorder = nav_dark_border,
            navActive = nav_dark_active,
            navInactive = nav_dark_inactive,
            homeTopBackground = home_top_background_dark,
            homeHeaderBackground = home_header_dark,
            homeContentBackground = home_content_dark,
            buttonBackground = button_background_dark,
            homeButtonBackground = home_button_background_dark,
            iconText = icon_text_dark
        )
    } else {
        ExtendedColors(
            classCardBackground = card_class_light,
            eventCardBackground = card_event_light,
            taskCardBackground = card_task_light,
            navBackground = nav_light_background,
            navBorder = nav_light_border,
            navActive = nav_light_active,
            navInactive = nav_light_inactive,
            homeTopBackground = home_top_background_light,
            homeHeaderBackground = home_header_light,
            homeContentBackground = home_content_light,
            buttonBackground = button_background_light,
            homeButtonBackground = home_button_background_light,
            iconText = icon_text_light
        )
    }

    val colorScheme = if (darkTheme) darkScheme() else lightScheme()
    val view = LocalView.current

    // Konfiguracja wyglądu paska stanu i nawigacji systemowej
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Udostępnienie obu systemów kolorów (M3 + Custom) w głąb aplikacji
    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// Skrót ułatwiający pobieranie kolorów w ekranach: extendedColors.nazwa
val extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current