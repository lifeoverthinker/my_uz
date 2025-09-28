import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

class TaskCard extends StatelessWidget {
  final String title;
  final String description;
  final String? initial;
  final Color? backgroundColor;
  final Color? avatarColor;
  final bool showAvatar;

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
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? AppColors.myUZSysLightSecondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      width: double.infinity,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF1D192B)),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 8),
                Text(
                  description,
                  style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF4A4A4A)),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          if (showAvatar)
            Padding(
              padding: const EdgeInsets.only(left: 16),
              child: Container(
                width: 32,
                height: 32,
                decoration: ShapeDecoration(
                  color: avatarColor ?? colorScheme.primary,
                  shape: const OvalBorder(),
                ),
                child: Center(
                  child: Text(
                    initial ?? '',
                    style: AppTextStyle.myUZTitleMedium.copyWith(color: colorScheme.onPrimary),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
            )
          else
            const SizedBox.shrink(),
        ],
      ),
    );
  }
}
