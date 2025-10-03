import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// Klasa zawierająca definicje motywów aplikacji MyUZ
abstract class AppTheme {
  /// Motyw jasny aplikacji
  static ThemeData get lightTheme {
    return ThemeData(
      // Kolory podstawowe
      colorScheme: const ColorScheme(
        brightness: Brightness.light,
        primary: AppColors.myUZSysLightPrimary,
        onPrimary: AppColors.myUZSysLightOnPrimary,
        primaryContainer: AppColors.myUZSysLightPrimaryContainer,
        onPrimaryContainer: AppColors.myUZSysLightOnPrimaryContainer,
        secondary: AppColors.myUZSysLightSecondary,
        onSecondary: AppColors.myUZSysLightOnSecondary,
        secondaryContainer: AppColors.myUZSysLightSecondaryContainer,
        onSecondaryContainer: AppColors.myUZSysLightOnSecondaryContainer,
        tertiary: AppColors.myUZSysLightTertiary,
        onTertiary: AppColors.myUZSysLightOnTertiary,
        tertiaryContainer: AppColors.myUZSysLightTertiaryContainer,
        onTertiaryContainer: AppColors.myUZSysLightOnTertiaryContainer,
        error: AppColors.myUZSysLightError,
        onError: AppColors.myUZSysLightOnError,
        errorContainer: AppColors.myUZSysLightErrorContainer,
        onErrorContainer: AppColors.myUZSysLightOnErrorContainer,
        background: AppColors.myUZSysLightBackground,
        onBackground: AppColors.myUZSysLightOnBackground,
        surface: AppColors.myUZSysLightSurface,
        onSurface: AppColors.myUZSysLightOnSurface,
        surfaceVariant: AppColors.myUZSysLightSurfaceVariant,
        onSurfaceVariant: AppColors.myUZSysLightOnSurfaceVariant,
        outline: AppColors.myUZSysLightOutline,
        outlineVariant: AppColors.myUZSysLightOutlineVariant,
        shadow: AppColors.myUZSysLightShadow,
        scrim: AppColors.myUZSysLightScrim,
        inverseSurface: AppColors.myUZSysLightInverseSurface,
        onInverseSurface: AppColors.myUZSysLightInverseOnSurface,
        inversePrimary: AppColors.myUZSysLightInversePrimary,
        surfaceTint: AppColors.myUZSysLightSurfaceTint,
      ),

      // Style tekstu
      textTheme: const TextTheme(
        displayLarge: AppTextStyle.myUZDisplayLarge,
        displayMedium: AppTextStyle.myUZDisplayMedium,
        displaySmall: AppTextStyle.myUZDisplaySmall,
        headlineLarge: AppTextStyle.myUZHeadlineLarge,
        headlineMedium: AppTextStyle.myUZHeadlineMedium,
        headlineSmall: AppTextStyle.myUZHeadlineSmall,
        titleLarge: AppTextStyle.myUZTitleLarge,
        titleMedium: AppTextStyle.myUZTitleMedium,
        titleSmall: AppTextStyle.myUZTitleSmall,
        bodyLarge: AppTextStyle.myUZBodyLarge,
        bodyMedium: AppTextStyle.myUZBodyMedium,
        bodySmall: AppTextStyle.myUZBodySmall,
        labelLarge: AppTextStyle.myUZLabelLarge,
        labelMedium: AppTextStyle.myUZLabelMedium,
        labelSmall: AppTextStyle.myUZLabelSmall,
      ),

      // Ustawienia komponentów
      useMaterial3: true,

      // Konfiguracja AppBar
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.myUZSysLightSurface,
        foregroundColor: AppColors.myUZSysLightOnSurface,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: AppTextStyle.myUZTitleLarge.copyWith(
          color: AppColors.myUZSysLightOnSurface,
        ),
      ),

      // Konfiguracja BottomNavigationBar
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: AppColors.myUZSysLightSurface,
        selectedItemColor: AppColors.myUZSysLightPrimary,
        unselectedItemColor: AppColors.myUZSysLightOnSurfaceVariant,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
        selectedLabelStyle: AppTextStyle.myUZLabelMedium,
        unselectedLabelStyle: AppTextStyle.myUZLabelMedium,
      ),

      // Konfiguracja przycisków
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.myUZSysLightPrimary,
          foregroundColor: AppColors.myUZSysLightOnPrimary,
          textStyle: AppTextStyle.myUZLabelLarge,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        ),
      ),

      // Konfiguracja kart
      cardTheme: const CardThemeData(
        color: AppColors.myUZSysLightSurfaceContainerLow,
        elevation: 1,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(16)),
        ),
        margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      ),
    );
  }

  /// Motyw ciemny aplikacji
  static ThemeData get darkTheme {
    return ThemeData(
      // Kolory podstawowe dla ciemnego motywu
      colorScheme: const ColorScheme(
        brightness: Brightness.dark,
        primary: AppColors.myUZSysDarkPrimary,
        onPrimary: AppColors.myUZSysDarkOnPrimary,
        primaryContainer: AppColors.myUZSysDarkPrimaryContainer,
        onPrimaryContainer: AppColors.myUZSysDarkOnPrimaryContainer,
        secondary: AppColors.myUZSysDarkSecondary,
        onSecondary: AppColors.myUZSysDarkOnSecondary,
        secondaryContainer: AppColors.myUZSysDarkSecondaryContainer,
        onSecondaryContainer: AppColors.myUZSysDarkOnSecondaryContainer,
        tertiary: AppColors.myUZSysDarkTertiary,
        onTertiary: AppColors.myUZSysDarkOnTertiary,
        tertiaryContainer: AppColors.myUZSysDarkTertiaryContainer,
        onTertiaryContainer: AppColors.myUZSysDarkOnTertiaryContainer,
        error: AppColors.myUZSysDarkError,
        onError: AppColors.myUZSysDarkOnError,
        errorContainer: AppColors.myUZSysDarkErrorContainer,
        onErrorContainer: AppColors.myUZSysDarkOnErrorContainer,
        background: AppColors.myUZSysDarkBackground,
        onBackground: AppColors.myUZSysDarkOnBackground,
        surface: AppColors.myUZSysDarkSurface,
        onSurface: AppColors.myUZSysDarkOnSurface,
        surfaceVariant: AppColors.myUZSysDarkSurfaceVariant,
        onSurfaceVariant: AppColors.myUZSysDarkOnSurfaceVariant,
        outline: AppColors.myUZSysDarkOutline,
        outlineVariant: AppColors.myUZSysDarkOutlineVariant,
        shadow: AppColors.myUZSysDarkShadow,
        scrim: AppColors.myUZSysDarkScrim,
        inverseSurface: AppColors.myUZSysDarkInverseSurface,
        onInverseSurface: AppColors.myUZSysDarkInverseOnSurface,
        inversePrimary: AppColors.myUZSysDarkInversePrimary,
        surfaceTint: AppColors.myUZSysDarkSurfaceTint,
      ),

      // Style tekstu
      textTheme: const TextTheme(
        displayLarge: AppTextStyle.myUZDisplayLarge,
        displayMedium: AppTextStyle.myUZDisplayMedium,
        displaySmall: AppTextStyle.myUZDisplaySmall,
        headlineLarge: AppTextStyle.myUZHeadlineLarge,
        headlineMedium: AppTextStyle.myUZHeadlineMedium,
        headlineSmall: AppTextStyle.myUZHeadlineSmall,
        titleLarge: AppTextStyle.myUZTitleLarge,
        titleMedium: AppTextStyle.myUZTitleMedium,
        titleSmall: AppTextStyle.myUZTitleSmall,
        bodyLarge: AppTextStyle.myUZBodyLarge,
        bodyMedium: AppTextStyle.myUZBodyMedium,
        bodySmall: AppTextStyle.myUZBodySmall,
        labelLarge: AppTextStyle.myUZLabelLarge,
        labelMedium: AppTextStyle.myUZLabelMedium,
        labelSmall: AppTextStyle.myUZLabelSmall,
      ),

      // Ustawienia komponentów dla ciemnego motywu
      useMaterial3: true,

      // Konfiguracja AppBar
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.myUZSysDarkSurface,
        foregroundColor: AppColors.myUZSysDarkOnSurface,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: AppTextStyle.myUZTitleLarge.copyWith(
          color: AppColors.myUZSysDarkOnSurface,
        ),
      ),

      // Konfiguracja BottomNavigationBar
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: AppColors.myUZSysDarkSurface,
        selectedItemColor: AppColors.myUZSysDarkPrimary,
        unselectedItemColor: AppColors.myUZSysDarkOnSurfaceVariant,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
        selectedLabelStyle: AppTextStyle.myUZLabelMedium,
        unselectedLabelStyle: AppTextStyle.myUZLabelMedium,
      ),

      // Konfiguracja przycisków
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.myUZSysDarkPrimary,
          foregroundColor: AppColors.myUZSysDarkOnPrimary,
          textStyle: AppTextStyle.myUZLabelLarge,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        ),
      ),

      // Konfiguracja kart
      cardTheme: const CardThemeData(
        color: AppColors.myUZSysDarkSurfaceContainerLow,
        elevation: 1,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(16)),
        ),
        margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      ),
    );
  }
}