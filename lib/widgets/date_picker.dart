import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// Nowa wersja – dialog centrowany (Material 3 style) zastępuje stary Date Picker.
abstract class ModalDatePicker {
  static Future<DateTime?> show(BuildContext context, {
    required DateTime initialDate,
    DateTime? firstDate,
    DateTime? lastDate,
  }) async {
    return showDialog<DateTime>(
      context: context,
      barrierDismissible: true,
      builder: (ctx) {
        return Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 360),
            child: Material(
              elevation: 4,
              color: Colors.transparent, // Kolor ustawiany wewnątrz _M3DatePicker
              borderRadius: BorderRadius.circular(28),
              child: _M3DatePicker(
                initialDate: initialDate,
                firstDate: firstDate ?? DateTime(2020),
                lastDate: lastDate ?? DateTime(2100),
              ),
            ),
          ),
        );
      },
    );
  }

// Usunięto starą metodę showModalBottomSheet (ModalDatePicker.show), pozostawiono tylko showCenterDialog,
// który teraz jest główną metodą show().
}

// Zmieniono nazwę klasy i przebudowano ją całkowicie na styl Material 3.
class _M3DatePicker extends StatefulWidget {
  final DateTime initialDate;
  final DateTime firstDate;
  final DateTime lastDate;

  const _M3DatePicker({
    required this.initialDate,
    required this.firstDate,
    required this.lastDate,
  });

  @override
  State<_M3DatePicker> createState() => _M3DatePickerState();
}

class _M3DatePickerState extends State<_M3DatePicker> {
  late DateTime selectedDate;
  late DateTime displayMonth;
  late PageController _pageController;

  // --- M3 COLORS & STYLES ---
  static const Color _kPrimary = Color(0xFF6750A4);
  static const Color _kOnPrimary = Colors.white;
  static const Color _kOnSurface = Color(0xFF1D1B20);
  static const Color _kOnSurfaceVariant = Color(0xFF49454F);
  static const Color _kOutlineVariant = Color(0xFFCAC4D0);
  static const Color _kSurfaceContainerHigh = Color(0xFFECE6F0);
  static const double _kDayTileSize = 40.0;
  static const double _kMonthNavHeight = 48.0;

  // Polskie nazwy
  static const List<String> _monthNames = [
    'Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec',
    'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień'
  ];
  static const List<String> _monthShort = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];
  // P, W, Ś, C, P, S, N (Pon, Wt, Śr, Czw, Pt, Sob, Niedz)
  static const List<String> _dayNames = ['P', 'W', 'Ś', 'C', 'P', 'S', 'N'];
  // --- KONIEC M3 COLORS & STYLES ---


  @override
  void initState() {
    super.initState();
    selectedDate = DateTime(widget.initialDate.year, widget.initialDate.month, widget.initialDate.day);
    displayMonth = DateTime(widget.initialDate.year, widget.initialDate.month);
    _pageController = PageController(
      initialPage: _monthsBetween(widget.firstDate, displayMonth),
    );
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  int _monthsBetween(DateTime from, DateTime to) {
    return (to.year - from.year) * 12 + to.month - from.month;
  }

  void _onMonthPageChanged(int pageIndex) {
    final months = _monthsBetween(widget.firstDate, DateTime(widget.firstDate.year, widget.firstDate.month));
    final targetMonth = DateTime(
      widget.firstDate.year,
      widget.firstDate.month + pageIndex - months,
    );
    setState(() => displayMonth = targetMonth);
  }

  void _navigateMonth(int delta) {
    final targetPage = _pageController.page!.round() + delta;
    _pageController.animateToPage(
      targetPage,
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeOut,
    );
    // onPageChanged zaktualizuje displayMonth
  }

  String _formatSelectedDate(DateTime date) {
    // Format: Wt, Sie 17
    final plWeekdayShort = _dayNames[date.weekday == 7 ? 6 : date.weekday - 1]; // Niedz to 7, a nasza lista ma 7 elementów (0-6)
    final plMonthShort = _monthShort[date.month - 1];
    return '$plWeekdayShort, $plMonthShort ${date.day}';
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 360,
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        color: _kSurfaceContainerHigh,
        borderRadius: BorderRadius.circular(28),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Nagłówek (Header)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.only(top: 16, left: 24, right: 12, bottom: 12),
            decoration: const BoxDecoration(
              border: Border(
                bottom: BorderSide(width: 1, color: _kOutlineVariant),
              ),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Wybierz dzień
                      Text(
                        'Wybierz dzień',
                        style: AppTextStyle.myUZLabelLarge.copyWith(
                          color: _kOnSurfaceVariant,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 12),
                      // Wt, Sie 17
                      Text(
                        _formatSelectedDate(selectedDate),
                        style: AppTextStyle.myUZDisplaySmall.copyWith(
                          color: _kOnSurface,
                          fontWeight: FontWeight.w400,
                          fontSize: 32, // Zgodnie ze specyfikacją
                        ),
                      ),
                    ],
                  ),
                ),
                // Ikona edycji
                IconButton(
                  icon: Icon(MyUz.edit_02, size: 24, color: _kOnSurface),
                  onPressed: () {}, // Funkcjonalność do zaimplementowania (może w innej wersji)
                  constraints: const BoxConstraints(minWidth: 48, minHeight: 48),
                ),
              ],
            ),
          ),

          // Nawigacja miesiąca
          Container(
            width: double.infinity,
            padding: const EdgeInsets.only(top: 4, left: 16, right: 12, bottom: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Dropdown miesiąc-rok
                InkWell(
                  onTap: () {}, // Dropdown do zaimplementowania
                  borderRadius: BorderRadius.circular(100),
                  child: Container(
                    padding: const EdgeInsets.only(top: 10, left: 8, right: 4, bottom: 10),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          '${_monthNames[displayMonth.month - 1]} ${displayMonth.year}',
                          textAlign: TextAlign.center,
                          style: AppTextStyle.myUZLabelLarge.copyWith(
                            color: _kOnSurfaceVariant,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Icon(
                          MyUz.chevron_down,
                          size: 18,
                          color: _kOnSurfaceVariant,
                        ),
                      ],
                    ),
                  ),
                ),
                // Strzałki nawigacji
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(MyUz.chevron_left, size: 24, color: _kOnSurface),
                      onPressed: () => _navigateMonth(-1),
                      iconSize: 24,
                      constraints: const BoxConstraints(minWidth: _kMonthNavHeight, minHeight: _kMonthNavHeight),
                    ),
                    IconButton(
                      icon: const Icon(MyUz.chevron_right, size: 24, color: _kOnSurface),
                      onPressed: () => _navigateMonth(1),
                      iconSize: 24,
                      constraints: const BoxConstraints(minWidth: _kMonthNavHeight, minHeight: _kMonthNavHeight),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Etykiety dni tygodnia (Mon-Sun)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: SizedBox(
              height: _kMonthNavHeight,
              child: Row(
                children: _dayNames.map((day) =>
                    Expanded(
                      child: Center(
                        child: Text(
                          day,
                          style: AppTextStyle.myUZBodyLarge.copyWith( // Zgodnie ze specyfikacją
                            color: _kOnSurface,
                            fontWeight: FontWeight.w400,
                            fontSize: 16,
                          ),
                        ),
                      ),
                    ),
                ).toList(),
              ),
            ),
          ),

          // Kalendarz - PageView
          SizedBox(
            height: _kMonthNavHeight * 6, // 6 wierszy po 48px
            child: PageView.builder(
              controller: _pageController,
              onPageChanged: _onMonthPageChanged,
              itemBuilder: (context, pageIndex) {
                // Obliczenie miesiąca na podstawie indexu PageView
                final months = _monthsBetween(widget.firstDate, DateTime(widget.firstDate.year, widget.firstDate.month));
                final month = DateTime(
                  widget.firstDate.year,
                  widget.firstDate.month + pageIndex - months,
                );
                return _buildMonthGrid(month);
              },
            ),
          ),

          // Przyciski akcji
          Container(
            width: double.infinity,
            padding: const EdgeInsets.only(top: 4, left: 12, right: 12, bottom: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Przycisk Clear (Wyczyść)
                TextButton(
                  onPressed: () => Navigator.of(context).pop(null), // Zwraca null
                  style: TextButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                    minimumSize: Size.zero,
                  ),
                  child: Text(
                    'Wyczyść',
                    style: AppTextStyle.myUZLabelLarge.copyWith(
                      color: _kPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                Expanded(
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      // Przycisk Anuluj
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(),
                        style: TextButton.styleFrom(
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                          minimumSize: Size.zero,
                        ),
                        child: Text(
                          'Anuluj',
                          style: AppTextStyle.myUZLabelLarge.copyWith(
                            color: _kPrimary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Przycisk OK
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(selectedDate),
                        style: TextButton.styleFrom(
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                          minimumSize: Size.zero,
                        ),
                        child: Text(
                          'OK',
                          style: AppTextStyle.myUZLabelLarge.copyWith(
                            color: _kPrimary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          // Usunięto dolny padding na SafeArea, ponieważ dialog jest centrowany.
        ],
      ),
    );
  }

  Widget _buildMonthGrid(DateTime month) {
    // Mon=1 ... Sun=7. Chcemy, by poniedziałek był pierwszy (index 0).
    final firstDay = DateTime(month.year, month.month, 1);
    final weekday = firstDay.weekday;
    // (1-1 + 7) % 7 = 0 dla poniedziałku (który jest indexem 0)
    final int daysToSubtract = (weekday - 1 + 7) % 7;
    final startDate = firstDay.subtract(Duration(days: daysToSubtract));

    final now = DateTime.now();

    return GridView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 7,
        childAspectRatio: 1,
        mainAxisSpacing: 0,
        crossAxisSpacing: 0,
      ),
      itemCount: 42,
      itemBuilder: (context, index) {
        final date = startDate.add(Duration(days: index));
        final isCurrentMonth = date.month == month.month;
        final isSelected = date.day == selectedDate.day &&
            date.month == selectedDate.month &&
            date.year == selectedDate.year;
        final isToday = date.day == now.day &&
            date.month == now.month &&
            date.year == now.year;

        // Okrągły kształt dla każdego dnia
        return Container(
          width: _kMonthNavHeight,
          height: _kMonthNavHeight,
          alignment: Alignment.center,
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: isCurrentMonth ? () {
                setState(() => selectedDate = date);
              } : null,
              borderRadius: BorderRadius.circular(100),
              child: Container(
                width: _kDayTileSize,
                height: _kDayTileSize,
                decoration: BoxDecoration(
                  color: isSelected
                      ? _kPrimary
                      : Colors.transparent,
                  borderRadius: BorderRadius.circular(100),
                  border: isToday && !isSelected
                      ? Border.all(
                    color: _kPrimary,
                    width: 1,
                  )
                      : null,
                ),
                child: Center(
                  child: Text(
                    '${date.day}',
                    style: AppTextStyle.myUZBodyLarge.copyWith(
                      fontSize: 16,
                      fontWeight: isSelected ? FontWeight.w500 : FontWeight.w400,
                      color: isSelected
                          ? _kOnPrimary
                          : isCurrentMonth
                          ? (isToday ? _kPrimary : _kOnSurface)
                          : _kOnSurfaceVariant.withOpacity(0.4), // Nieaktywne dni z poprzedniego/następnego miesiąca
                    ),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}