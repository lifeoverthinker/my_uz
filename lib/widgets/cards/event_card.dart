import 'package:flutter/material.dart';
import 'package:my_uz/theme/text_style.dart';

/// EventCard – karta “Wydarzenia”
/// Figma height: 84 px (analogiczna struktura do TaskCard)
/// 12 + 20 + 8 + 32 + 12 = 84
class EventCard extends StatelessWidget {
  static const double _kHeightEvent = 84;

  final String title;
  final String description;
  final Color? backgroundColor;
  final bool hugHeight;

  const EventCard({
    super.key,
    required this.title,
    required this.description,
    this.backgroundColor,
    this.hugHeight = false,
  });

  @override
  Widget build(BuildContext context) {
    final scale = MediaQuery.of(context).textScaleFactor;
    final adaptive = hugHeight || scale > 1.0;

    final inner = Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? const Color(0xFFDAF5D7),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            title,
            maxLines: hugHeight ? 2 : 1,
            overflow: TextOverflow.ellipsis,
            style: AppTextStyle.myUZLabelLarge.copyWith(
              color: const Color(0xFF1D192B),
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            description,
            maxLines: hugHeight ? 3 : 2,
            overflow: TextOverflow.ellipsis,
            style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F)),
          ),
        ],
      ),
    );

    if (adaptive) return inner;

    return ConstrainedBox(
      constraints: const BoxConstraints(minHeight: _kHeightEvent),
      child: inner,
    );
  }
}