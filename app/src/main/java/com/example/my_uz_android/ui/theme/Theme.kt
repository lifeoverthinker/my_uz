package com.example.my_uz_android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val classCardBackground: Color = card_class_light,
    val eventCardBackground: Color = card_event_light,
    val taskCardBackground: Color = card_task_light,
    val navBackground: Color = nav_light_background,
    val navBorder: Color = nav_light_border,
    val navActive: Color = nav_light_active,
    val navInactive: Color = nav_light_inactive,
    val homeTopBackground: Color = home_top_background_light,
    val homeHeaderBackground: Color = home_header_light,
    val homeContentBackground: Color = home_content_light,
    val buttonBackground: Color = button_background_light,
    val homeButtonBackground: Color = home_button_background_light, // ✅ DODANO
    val iconText: Color = icon_text_light,
    val grayInactive: Color = Color(0xFFBDBDBD)
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

private fun lightScheme(): ColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint
)

private fun darkScheme(): ColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint
)

@Composable
fun MyUZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
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
            homeButtonBackground = home_button_background_dark, // ✅ Ustawienie dla Dark Mode
            iconText = icon_text_dark,
            grayInactive = Color(0xFFBDBDBD)
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
            homeButtonBackground = home_button_background_light, // ✅ Ustawienie dla Light Mode (E8DEF8)
            iconText = icon_text_light,
            grayInactive = Color(0xFFBDBDBD)
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme()
        else -> lightScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Domyślne kolory pasków (tło aplikacji)
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()

                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = !darkTheme
                insets.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current