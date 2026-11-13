package com.example.my_uz_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MyUZ_Black = Color(0xFF1D192B)
val MyUZ_White = Color(0xFFFFFFFF)
val MyUZ_Icon_Bg_Light = Color(0xFFF7F2F9)
val MyUZ_Badge_Red = Color(0xFFB3261E)

val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)
val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)
val md_theme_light_error = MyUZ_Badge_Red
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)
val md_theme_light_outline = Color(0xFF79747E)
val md_theme_light_background = MyUZ_White
val md_theme_light_onBackground = Color(0xFF1D1B20)
val md_theme_light_surface = MyUZ_White
val md_theme_light_onSurface = MyUZ_Black
val md_theme_light_surfaceVariant = Color(0xFFE7E0EB)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
val md_theme_light_inverseSurface = Color(0xFF313033)
val md_theme_light_inverseOnSurface = Color(0xFFF4EFF4)
val md_theme_light_inversePrimary = Color(0xFFD0BCFF)
val md_theme_light_outlineVariant = Color(0xFFCAC4D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF8F1FA)
val md_theme_light_surfaceContainer = Color(0xFFF3EDF7)
val md_theme_light_surfaceContainerHigh = Color(0xFFECE6F0)
val md_theme_light_surfaceContainerHighest = Color(0xFFE6E0E9)

val md_theme_dark_primary = Color(0xFFCBB8FF)
val md_theme_dark_onPrimary = Color(0xFF2B1768)
val md_theme_dark_primaryContainer = Color(0xFF4F3A9B)
val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)
val md_theme_dark_secondary = Color(0xFFCFC7E8)
val md_theme_dark_onSecondary = Color(0xFF332D4A)
val md_theme_dark_secondaryContainer = Color(0xFF3F3A57)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)
val md_theme_dark_tertiary = Color(0xFFF2B7CB)
val md_theme_dark_onTertiary = Color(0xFF4A2133)
val md_theme_dark_tertiaryContainer = Color(0xFF5E3447)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E4)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_outline = Color(0xFF8B84A8)
val md_theme_dark_background = Color(0xFF0F0C18)
val md_theme_dark_onBackground = Color(0xFFECE8F7)
val md_theme_dark_surface = Color(0xFF0F0C18)
val md_theme_dark_onSurface = Color(0xFFECE8F7)
val md_theme_dark_surfaceVariant = Color(0xFF2D2842)
val md_theme_dark_onSurfaceVariant = Color(0xFFBDB6D3)
val md_theme_dark_inverseSurface = Color(0xFFE8E4F2)
val md_theme_dark_inverseOnSurface = Color(0xFF2F2B3D)
val md_theme_dark_inversePrimary = Color(0xFF6750A4)
val md_theme_dark_outlineVariant = Color(0xFF3A3452)
val md_theme_dark_scrim = Color(0xFF000000)

val md_theme_dark_surfaceContainerLowest = Color(0xFF0F0C18)
val md_theme_dark_surfaceContainerLow = Color(0xFF151222)
val md_theme_dark_surfaceContainer = Color(0xFF1B172B)
val md_theme_dark_surfaceContainerHigh = Color(0xFF231F36)
val md_theme_dark_surfaceContainerHighest = Color(0xFF2D2842)

val nav_light_background = MyUZ_White
val nav_light_border = Color(0xFFEDE6F3)
val nav_light_active = Color(0xFF381E72)
val nav_light_inactive = Color(0xFF787579)

val nav_dark_background = Color(0xFF151222)
val nav_dark_border = Color(0xFF2D2842)
val nav_dark_active = Color(0xFFCBB8FF)
val nav_dark_inactive = Color(0xFFA9A2C0)

// Tło górnej części ekranu głównego
val home_top_background_light = MyUZ_Icon_Bg_Light
val home_top_background_dark = Color(0xFF1A1630)

data class AppColorSet(
    val background: Color,
    val content: Color,
    val accent: Color
)

val ColorSetPurple = AppColorSet(
    background = Color(0xFFD6C8F5),
    content = Color(0xFF2A1B5C),
    accent = Color(0xFF6750A4)
)

val ColorSetBlue = AppColorSet(
    background = Color(0xFFB9E0EE),
    content = Color(0xFF0E3342),
    accent = Color(0xFF2F7A94)
)

val ColorSetGreen = AppColorSet(
    background = Color(0xFFBFE6B8),
    content = Color(0xFF1B3D17),
    accent = Color(0xFF3F8A36)
)

val ColorSetYellow = AppColorSet(
    background = Color(0xFFEFE3A0),
    content = Color(0xFF3F3400),
    accent = Color(0xFF8A7A12)
)

val ColorSetOrange = AppColorSet(
    background = Color(0xFFFFCDB5),
    content = Color(0xFF4A2008),
    accent = Color(0xFFC2571A)
)

val ColorSetPink = AppColorSet(
    background = Color(0xFFFBC3D6),
    content = Color(0xFF5A1230),
    accent = Color(0xFFB83B6B)
)

val AppColorPalette = listOf(
    ColorSetPurple,
    ColorSetBlue,
    ColorSetGreen,
    ColorSetYellow,
    ColorSetOrange,
    ColorSetPink
)

val ClassColorPalette = AppColorPalette

val CardClassColors = ColorSetPurple
val CardEventColors = ColorSetGreen
val CardTaskColors = ColorSetBlue

fun getAppColorSet(index: Int): AppColorSet =
    AppColorPalette[(index and 0x7FFFFFFF) % AppColorPalette.size]

fun getAppBackgroundColor(index: Int): Color = getAppColorSet(index).background
fun getAppContentColor(index: Int): Color = getAppColorSet(index).content
fun getAppAccentColor(index: Int): Color = getAppColorSet(index).accent

@Composable
fun tileContentColor(
    tileBackground: Color,
    fallback: Color = MaterialTheme.colorScheme.onSurface
): Color = AppColorPalette.firstOrNull { it.background == tileBackground }?.content ?: fallback

@Composable
fun tileSecondaryContentColor(
    tileBackground: Color,
    fallback: Color = MaterialTheme.colorScheme.onSurfaceVariant
): Color = AppColorPalette.firstOrNull { it.background == tileBackground }
    ?.content?.copy(alpha = 0.8f) ?: fallback

// Przeciążenia dla starych wywołań - parametr isDark jest ignorowany.
// Po podmianie wywołań w całej aplikacji można je usunąć.
@Deprecated("Kolory kafelków nie zależą od trybu. Użyj getAppBackgroundColor(index).")
@Suppress("UNUSED_PARAMETER")
fun getAppBackgroundColor(index: Int, isDark: Boolean): Color = getAppBackgroundColor(index)

@Deprecated("Kolory kafelków nie zależą od trybu. Użyj getAppAccentColor(index).")
@Suppress("UNUSED_PARAMETER")
fun getAppAccentColor(index: Int, isDark: Boolean): Color = getAppAccentColor(index)

// Dobiera indeks koloru dla przedmiotu (z mapy użytkownika lub z hasha nazwy)
fun getClassColorIndex(subjectName: String?, userColorMap: Map<String, Int> = emptyMap()): Int {
    if (subjectName == null) return 0
    return userColorMap[subjectName] ?: ((subjectName.hashCode() and 0x7FFFFFFF) % AppColorPalette.size)
}

// Kolor kropki priorytetu zadania (2 = wysoki, 1 = średni, 0 = niski)
@Composable
fun taskPriorityDotColor(priority: Int): Color {
    return when (priority) {
        2 -> MaterialTheme.colorScheme.error
        1 -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }
}