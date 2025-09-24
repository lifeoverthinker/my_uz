import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// Karta zadań wyświetlana na ekranie głównym i w kalendarzu
///
/// Wyświetla informacje o zadaniu, takie jak tytuł i opis,
/// oraz opcjonalnie inicjał lub awatar przedmiotu.
class TaskCard extends StatelessWidget {
  /// Tytuł zadania
  final String title;

  /// Opis zadania
  final String description;

  /// Inicjał lub skrót nazwy przedmiotu wyświetlany w kółku
  final String? initial;

  /// Kolor tła karty
  final Color? backgroundColor;

  /// Kolor tła awatara
  final Color? avatarColor;

  /// Czy wyświetlać awatar
  final bool showAvatar;

  /// Konstruktor karty zadań
  const TaskCard({
    super.key,
    required this.title,
    required this.description,
    this.initial,
    this.backgroundColor,
    this.avatarColor,
    this.showAvatar = true,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? AppColors.myUZSysLightSecondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Row(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Tytuł zadania
                      SizedBox(
                        width: showAvatar && initial != null ? 192 : double.infinity,
                        child: Text(
                          title,
                          style: AppTextStyle.myUZTitleSmall.copyWith(
                            color: const Color(0xFF222222),
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      const SizedBox(height: 8),

                      // Opis zadania
                      SizedBox(
                        width: showAvatar && initial != null ? 192 : double.infinity,
                        child: Text(
                          description,
                          style: AppTextStyle.myUZBodySmall.copyWith(
                            color: const Color(0xFF494949),
                          ),
                          overflow: TextOverflow.ellipsis,
                          maxLines: 2,
                        ),
                      ),
                    ],
                  ),
                ),

                // Awatar (opcjonalny)
                if (showAvatar && initial != null) ...[
                  const SizedBox(width: 16),
                  _buildAvatar(),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  /// Buduje awatar z inicjałem
  Widget _buildAvatar() {
    return Container(
      width: 32,
      height: 32,
      decoration: ShapeDecoration(
        color: avatarColor ?? AppColors.myUZSysLightTertiary,
        shape: const OvalBorder(),
      ),
      child: Center(
        child: Text(
          initial!,
          style: AppTextStyle.myUZTitleMedium.copyWith(
            color: AppColors.myUZSysLightOnTertiary,
          ),
        ),
      ),
    );
  }
}