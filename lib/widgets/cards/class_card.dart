import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// ClassCard – karta “Najbliższe zajęcia”
/// Figma height: 68 px (default fixed)
/// Możliwość trybu "hug" (auto wysokość) poprzez [hugHeight].
class ClassCard extends StatelessWidget {
  static const double _kHeightClass = 68;

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
  final bool hugHeight;
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
    this.hugHeight = false,
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
    // Zastąpienie deprecated textScaleFactor na textScaler
    final scale = MediaQuery.of(context).textScaler.scale(1.0);
    final bool adaptive = hugHeight || scale > 1.0;

    final content = Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? cs.secondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Teksty
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  title,
                  maxLines: hugHeight ? maxTitleLines : 1,
                  overflow: TextOverflow.ellipsis,
                  style: AppTextStyle.myUZLabelLarge.copyWith(
                    color: const Color(0xFF1D192B),
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 8),
                // Usunięto Flexible – w trybie hug wysokość nie jest sztywna.
                Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Icon(MyUz.clock, size: 16, color: const Color(0xFF4A4A4A)),
                    const SizedBox(width: 4),
                    Expanded(
                      child: Text(
                        time,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: AppTextStyle.myUZBodySmall
                            .copyWith(color: const Color(0xFF49454F)),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Text(
                        room,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: AppTextStyle.myUZBodySmall
                            .copyWith(color: const Color(0xFF49454F)),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(width: 16),
          // Awatar lub kropka statusu
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

    if (adaptive) {
      // Hug – pozwalamy rosnąć wg treści
      return Align(alignment: Alignment.topLeft, child: content);
    }

    // Stała wysokość – ale jako minHeight, żeby uniknąć overflow przy minimalnych różnicach renderingu
    return ConstrainedBox(
      constraints: const BoxConstraints(minHeight: _kHeightClass),
      child: content,
    );
  }
}
