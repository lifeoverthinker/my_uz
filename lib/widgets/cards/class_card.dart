import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

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
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? colorScheme.secondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      width: double.infinity,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  title,
                  style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF1D192B)),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Icon(MyUz.clock, size: 16, color: const Color(0xFF4A4A4A)),
                    const SizedBox(width: 4),
                    Flexible(
                      child: Text(
                        time,
                        style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF4A4A4A)),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Flexible(
                      child: Text(
                        room,
                        style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF4A4A4A)),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
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
                color: avatarColor ?? colorScheme.primary,
                shape: const OvalBorder(),
              ),
              child: Center(
                child: Text(
                  initial,
                  style: AppTextStyle.myUZTitleMedium.copyWith(color: colorScheme.onPrimary),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            )
          else if (showStatusDot)
            Container(
              width: 8,
              height: 8,
              decoration: ShapeDecoration(
                color: statusDotColor ?? colorScheme.tertiary,
                shape: const OvalBorder(),
              ),
            )
          else
            const SizedBox.shrink(),
        ],
      ),
    );
  }
}
