import 'package:flutter/material.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/theme/app_colors.dart';

class TaskInfoTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const TaskInfoTile({super.key, required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 20, color: AppColors.myUZSysLightPrimary),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onSurfaceVariant)),
              const SizedBox(height: 4),
              Text(value, style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface)),
            ],
          ),
        ),
      ],
    );
  }
}

