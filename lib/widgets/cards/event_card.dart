import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

class EventCard extends StatelessWidget {
  final String title;
  final String description;
  final Color? backgroundColor;

  const EventCard({
    super.key,
    required this.title,
    required this.description,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? const Color(0xFFDAF5D7),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      width: double.infinity,
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
    );
  }
}
