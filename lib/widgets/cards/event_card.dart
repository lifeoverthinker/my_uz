import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// Karta wydarzeń wyświetlana na ekranie głównym
///
/// Wyświetla informacje o wydarzeniu, takie jak tytuł i opis.
/// Zazwyczaj ma zielone tło, aby odróżnić ją od innych kart.
class EventCard extends StatelessWidget {
  /// Tytuł wydarzenia
  final String title;

  /// Opis wydarzenia
  final String description;

  /// Kolor tła karty
  final Color? backgroundColor;

  /// Konstruktor karty wydarzeń
  const EventCard({
    super.key,
    required this.title,
    required this.description,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        // Domyślnie zielone tło dla wydarzeń
        color: backgroundColor ?? const Color(0xFFDAF5D7),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
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
                // Tytuł wydarzenia
                SizedBox(
                  width: double.infinity,
                  child: Text(
                    title,
                    style: AppTextStyle.myUZTitleSmall.copyWith(
                      color: const Color(0xFF222222),
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                const SizedBox(height: 8),

                // Opis wydarzenia
                SizedBox(
                  width: double.infinity,
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
        ],
      ),
    );
  }
}