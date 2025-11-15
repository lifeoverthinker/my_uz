import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// Small circular menu button used across calendar / tasks screens.
/// Visual appearance matches the circle used in CalendarScreen.
class TopMenuButton extends StatelessWidget {
  final VoidCallback onTap;
  final double size;
  final Color? iconColor;
  final Color? backgroundColor;

  const TopMenuButton({
    super.key,
    required this.onTap,
    this.size = 48,
    this.iconColor,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      customBorder: const CircleBorder(),
      onTap: onTap,
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(color: backgroundColor ?? const Color(0xFFF7F2F9), shape: BoxShape.circle),
        alignment: Alignment.center,
        child: Icon(MyUz.menu_01, size: 24, color: iconColor ?? const Color(0xFF1D192B)),
      ),
    );
  }
}