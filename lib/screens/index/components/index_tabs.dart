import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';

class IndexTabs extends StatelessWidget {
  final int selectedIndex;
  final Function(int) onTabChanged;
  const IndexTabs({
    super.key,
    required this.selectedIndex,
    required this.onTabChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _buildTab('Oceny', 0)),
        const SizedBox(width: 16),
        Expanded(child: _buildTab('Nieobecności', 1)),
      ],
    );
  }

  Widget _buildTab(String text, int index) {
    final bool selected = selectedIndex == index;
    return GestureDetector(
      onTap: () => onTabChanged(index),
      child: Container(
        height: 40,
        decoration: BoxDecoration(
          color: selected ? AppColors.myUZSysLightPrimaryContainer : AppColors.myUZWhite,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: selected ? Colors.transparent : Colors.black12,
            width: 1,
          ),
        ),
        alignment: Alignment.center,
        child: Text(
          text,
          style: TextStyle(
            color: Colors.black87,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
  }
}

