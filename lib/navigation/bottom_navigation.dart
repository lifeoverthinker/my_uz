import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';

/// Widget nawigacji dolnej dla aplikacji MyUZ
class MyUZBottomNavigation extends StatelessWidget {
  /// Indeks aktualnie wybranej zakładki
  final int currentIndex;

  /// Callback wywoływany przy zmianie zakładki
  final ValueChanged<int> onTap;

  /// Konstruktor widgetu nawigacji
  const MyUZBottomNavigation({
    Key? key,
    required this.currentIndex,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Kolory z Figmy dla nawigacji
    const Color inactiveColor = Color(0xFF787579);
    const Color activeColor = Color(0xFF381E72);
    const Color borderColor = Color(0xFFEDE6F3);

    return Container(
      width: 360, // Szerokość z Figmy (360)
      padding: const EdgeInsets.only(bottom: 16), // Padding tylko dolny 16px z Figmy
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(
          top: BorderSide(
            width: 1,
            color: borderColor, // #EDE6F3 z Figmy
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildNavItem(0, MyUz.home_02, 'Główna', inactiveColor, activeColor),
          _buildNavItem(1, MyUz.calendar, 'Kalendarz', inactiveColor, activeColor),
          _buildNavItem(2, MyUz.book_open_01, 'Indeks', inactiveColor, activeColor),
          _buildNavItem(3, MyUz.user_01, 'Konto', inactiveColor, activeColor),
        ],
      ),
    );
  }

  /// Buduje pojedynczy element nawigacji
  Widget _buildNavItem(int index, IconData icon, String label, Color inactiveColor, Color activeColor) {
    final bool isSelected = currentIndex == index;
    final Color color = isSelected ? activeColor : inactiveColor;

    return Expanded(
      child: InkWell(
        onTap: () => onTap(index),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8), // Padding góra-dół 8px z Figmy
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: 24, // Rozmiar ikony 24x24 z Figmy
                color: color,
              ),
              Text(
                label,
                style: AppTextStyle.myUZLabelSmall.copyWith(
                  color: color, // Kolor zależny od aktywności (#787579 lub #381E72)
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}