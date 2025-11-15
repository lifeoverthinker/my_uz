import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// TaskCard – karta “Zadania”
/// Figma height: 84 px
/// Wyliczenie: 12 (padding top) + 20 (LabelLarge) + 8 + 32 (2×BodySmall) + 12 (padding bottom) = 84
class TaskCard extends StatelessWidget {
  static const double _kHeightTask = 84;
  // Opcjonalne hugging (domyślnie false by zachować look)
  final bool hugHeight;

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
    this.hugHeight = false,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final scale = MediaQuery.of(context).textScaleFactor;
    final adaptive = hugHeight || scale > 1.0;

    final inner = Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? AppColors.myUZSysLightSecondaryContainer,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Teksty
          Expanded(
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
          ),
          if (showAvatar)
            Padding(
              padding: const EdgeInsets.only(left: 16),
              child: Container(
                width: 32,
                height: 32,
                decoration: ShapeDecoration(
                  color: avatarColor ?? cs.primary,
                  shape: const OvalBorder(),
                ),
                alignment: Alignment.center,
                child: Text(
                  initial ?? '',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: AppTextStyle.myUZTitleMedium.copyWith(
                    color: cs.onPrimary,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            )
          else
            const SizedBox.shrink(),
        ],
      ),
    );

    if (adaptive) {
      return inner; // rośnie naturalnie
    }

    return ConstrainedBox(
      constraints: const BoxConstraints(minHeight: _kHeightTask),
      child: inner,
    );
  }
}