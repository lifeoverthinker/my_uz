import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// ClassCard – karta “Najbliższe zajęcia”
/// Figma height: 68 px (default fixed)
/// Możliwość trybu "hug" (auto wysokość) poprzez [hugHeight].
class ClassCard extends StatelessWidget {
  final String title;
  final String time;
  final String room;
  final String initial;
  final Color? backgroundColor;
  final Color? avatarColor;
  final bool showAvatar;
  final bool showStatusDot;
  final Color? statusDotColor;
  /// Jeśli true – zachowuje się jak Figma "Hug" (wysokość dopasowana do treści),
  /// w przeciwnym wypadku stałe 68 px.
  final bool hugHeight; // pozostawione dla zgodności, zawsze traktowane jako true
  /// Gdy hugHeight = true można pozwolić tytułowi na 2 linie (opcjonalnie).
  final int maxTitleLines;

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
    this.hugHeight = true, // default teraz true
    this.maxTitleLines = 1,
  });

  factory ClassCard.calendar({
    required String title,
    required String time,
    required String room,
    Color? statusDotColor,
    bool hugHeight = false,
  }) {
    return ClassCard(
      title: title,
      time: time,
      room: room,
      showAvatar: false,
      showStatusDot: true,
      statusDotColor: statusDotColor ?? AppColors.myUZSysLightTertiary,
      hugHeight: hugHeight,
      maxTitleLines: hugHeight ? 2 : 1,
    );
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return LayoutBuilder(
      builder: (context, constraints) {
        final maxH = constraints.maxHeight;
        // compact jeśli karta dostaje ciasny wysokościowy constraint (np. 44)
        final bool compact = maxH.isFinite && maxH <= 48;
        final double vPad = compact ? 0 : 8; // normalnie 8 (Figma), w kompakt 0
        final double hPad = 12; // stały padding poziomy
        final double gap = compact ? 4 : 8; // odstęp tytuł -> wiersz info

        return Container(
          padding: EdgeInsets.fromLTRB(hPad, vPad, hPad, vPad),
          decoration: ShapeDecoration(
            color: backgroundColor ?? cs.secondaryContainer,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      maxLines: maxTitleLines,
                      overflow: TextOverflow.ellipsis,
                      style: AppTextStyle.myUZLabelLarge.copyWith(
                        color: const Color(0xFF1D192B),
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    SizedBox(height: gap),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Icon(MyUz.clock, size: 16, color: const Color(0xFF4A4A4A)),
                        const SizedBox(width: 4),
                        // Pełny czas – nie skracamy
                        Text(
                          time,
                          maxLines: 1,
                          overflow: TextOverflow.visible,
                          softWrap: false,
                          style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F)),
                        ),
                        const SizedBox(width: 16),
                        // Sala może się skracać
                        Expanded(
                          child: Text(
                            room,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F)),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              if (showAvatar)
                Container(
                  width: 32,
                  height: 32,
                  decoration: ShapeDecoration(
                    color: avatarColor ?? cs.primary,
                    shape: const OvalBorder(),
                  ),
                  alignment: Alignment.center,
                  child: Text(
                    initial,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: AppTextStyle.myUZTitleMedium.copyWith(
                      color: cs.onPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                )
              else if (showStatusDot)
                Container(
                  width: 8,
                  height: 8,
                  decoration: ShapeDecoration(
                    color: statusDotColor ?? cs.tertiary,
                    shape: const OvalBorder(),
                  ),
                )
              else
                const SizedBox.shrink(),
            ],
          ),
        );
      },
    );
  }
}
