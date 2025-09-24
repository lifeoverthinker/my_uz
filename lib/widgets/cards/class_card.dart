import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// Karta zajęć wyświetlana na ekranie głównym i w kalendarzu
///
/// Wyświetla informacje o zajęciach, takie jak nazwa przedmiotu,
/// godzina i sala, wraz z inicjałem lub awatarem prowadzącego.
class ClassCard extends StatelessWidget {
  /// Tytuł zajęć
  final String title;

  /// Czas rozpoczęcia i zakończenia zajęć
  final String time;

  /// Sala, w której odbywają się zajęcia
  final String room;

  /// Inicjał lub skrót nazwy przedmiotu wyświetlany w kółku
  final String initial;

  /// Kolor tła karty
  final Color? backgroundColor;

  /// Kolor tła awatara
  final Color? avatarColor;

  /// Czy wyświetlać awatar
  final bool showAvatar;

  /// Czy wyświetlać wskaźnik statusu (kropkę)
  final bool showStatusDot;

  /// Kolor wskaźnika statusu
  final Color? statusDotColor;

  /// Konstruktor karty zajęć
  const ClassCard({
    super.key,
    required this.title,
    required this.time,
    required this.room,
    this.initial = 'A',
    this.backgroundColor,
    this.avatarColor,
    this.showAvatar = true,
    this.showStatusDot = false,
    this.statusDotColor,
  });

  /// Wariant karty zajęć dla ekranu kalendarza
  factory ClassCard.calendar({
    required String title,
    required String time,
    required String room,
    Color? statusDotColor,
  }) {
    return ClassCard(
      title: title,
      time: time,
      room: room,
      showAvatar: false,
      showStatusDot: true,
      statusDotColor: statusDotColor ?? AppColors.myUZSysLightTertiary,
    );
  }

  @override
  Widget build(BuildContext context) {
    // Pobranie motywu i stylów, aby uniknąć powtórzeń (zasada DRY)
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final textTheme = theme.textTheme;

    // Domyślny kolor tekstu dla godziny i sali
    final secondaryTextColor = colorScheme.onSurfaceVariant;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      // Figma: Container
      // color: #E8DEF8 (myUZSysLightSecondaryContainer)
      // borderRadius: 8
      decoration: ShapeDecoration(
        color: backgroundColor ?? colorScheme.secondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Figma: Text "Podstawy systemów dyskr..."
                // style: myUZTitleSmall
                // color: #1D192B (onSurface)
                // fontSize: 14, fontWeight: 500, letterSpacing: 0.1
                SizedBox(
                  width: showAvatar ? 192 : double.infinity,
                  child: Text(
                    title,
                    style: textTheme.titleSmall,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                const SizedBox(height: 8),

                // Informacje o czasie i sali
                Row(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    // Figma: Text "10:00 - 10:45"
                    // style: myUZBodySmall
                    // color: #494949 (onSurfaceVariant)
                    // fontSize: 12, fontWeight: 400, letterSpacing: 0.4
                    Text(
                      time,
                      style: textTheme.bodySmall?.copyWith(color: secondaryTextColor),
                    ),
                    const SizedBox(width: 16),

                    // Figma: Text "Sala 102"
                    // style: myUZBodySmall
                    // color: #494949 (onSurfaceVariant)
                    // fontSize: 12, fontWeight: 400, letterSpacing: 0.4
                    Text(
                      room,
                      style: textTheme.bodySmall?.copyWith(color: secondaryTextColor),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Rozdzielenie logiki dla awatara i kropki statusu dla czytelności (KISS)
          if (showAvatar) ...[
            const SizedBox(width: 16),
            _buildAvatar(colorScheme, textTheme),
          ] else if (showStatusDot) ...[
            const SizedBox(width: 8),
            _buildStatusDot(colorScheme),
          ],
        ],
      ),
    );
  }

  /// Buduje awatar z inicjałem
  Widget _buildAvatar(ColorScheme colorScheme, TextTheme textTheme) {
    return Container(
      width: 32,
      height: 32,
      // Figma: Avatar
      // color: #6750A4 (myUZSysLightPrimary)
      // shape: Oval
      decoration: ShapeDecoration(
        color: avatarColor ?? colorScheme.primary,
        shape: const OvalBorder(),
      ),
      child: Center(
        // Figma: Text "A"
        // style: myUZTitleMedium (z drobnymi różnicami w rodzinie czcionki)
        // color: #FFFBFE (onPrimary)
        // fontSize: 16, fontWeight: 500, letterSpacing: 0.15
        child: Text(
          initial,
          style: textTheme.titleMedium?.copyWith(
            color: colorScheme.onPrimary,
          ),
        ),
      ),
    );
  }

  /// Buduje wskaźnik statusu (kolorowa kropka)
  Widget _buildStatusDot(ColorScheme colorScheme) {
    return Container(
      width: 8,
      height: 8,
      // Figma: Status Dot
      // color: #7D5260 (myUZSysLightTertiary)
      // shape: Oval
      decoration: ShapeDecoration(
        color: statusDotColor ?? colorScheme.tertiary,
        shape: const OvalBorder(),
      ),
    );
  }
}