import 'package:flutter/material.dart';

class IndexTabs extends StatelessWidget {
  final int selectedIndex;
  final Function(int) onTabChanged;
  const IndexTabs({super.key, required this.selectedIndex, required this.onTabChanged});

  static const _selectedBg = Color(0xFFE9DEF8);
  static const _selectedText = Color(0xFF4B4358);
  static const _unselectedBorder = Color(0xFF7A757F);
  static const _unselectedText = Color(0xFF49454E);

  @override
  Widget build(BuildContext context) {
    // Zajmij całą dostępną szerokość (rodzic ma padding 16) i rozciągnij taby na równą szerokość.
    return Row(
      mainAxisSize: MainAxisSize.max,
      children: [
        Expanded(child: _buildTab('Oceny', 0, selectedIndex == 0)),
        const SizedBox(width: 16),
        Expanded(child: _buildTab('Nieobecności', 1, selectedIndex == 1)),
      ],
    );
  }

  Widget _buildTab(String text, int index, bool selected) {
    return GestureDetector(
      onTap: () => onTabChanged(index),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: ShapeDecoration(
          color: selected ? _selectedBg : Colors.transparent,
          shape: RoundedRectangleBorder(
            side: BorderSide(width: 1, color: selected ? Colors.transparent : _unselectedBorder),
            borderRadius: BorderRadius.circular(8),
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              text,
              style: const TextStyle(
                fontFamily: 'Inter',
                fontSize: 12,
                height: 1.33,
                letterSpacing: 0.40,
                fontWeight: FontWeight.w400,
              ).copyWith(color: selected ? _selectedText : _unselectedText),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}
