import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// Dokładna kopia Google Calendar date picker
class ModalDatePicker {
  static Future<DateTime?> show(BuildContext context, {
    required DateTime initialDate,
    DateTime? firstDate,
    DateTime? lastDate,
    Locale? locale,
  }) {
    return showModalBottomSheet<DateTime>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(24)),
      ),
      builder: (context) => _GoogleCalendarDatePicker(
        initialDate: initialDate,
        firstDate: firstDate ?? DateTime(2020),
        lastDate: lastDate ?? DateTime(2100),
      ),
    );
  }

  /// Nowa wersja – dialog centrowany (Material 3 style) jak na zrzucie.
  static Future<DateTime?> showCenterDialog(BuildContext context, {
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
              color: Colors.white,
              borderRadius: BorderRadius.circular(24),
              child: _GoogleCalendarDatePicker(
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
}

class _GoogleCalendarDatePicker extends StatefulWidget {
  final DateTime initialDate;
  final DateTime firstDate;
  final DateTime lastDate;

  const _GoogleCalendarDatePicker({
    required this.initialDate,
    required this.firstDate,
    required this.lastDate,
  });

  @override
  State<_GoogleCalendarDatePicker> createState() => _GoogleCalendarDatePickerState();
}

class _GoogleCalendarDatePickerState extends State<_GoogleCalendarDatePicker> {
  late DateTime selectedDate;
  late DateTime displayMonth;
  late PageController _pageController;

  static const List<String> _monthNames = [
    'Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec',
    'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień'
  ];

  static const List<String> _monthShort = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];

  static const List<String> _dayNames = ['p', 'w', 'ś', 'c', 'p', 's', 'n'];

  @override
  void initState() {
    super.initState();
    selectedDate = widget.initialDate;
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

  void _navigateMonth(int delta) {
    final newMonth = DateTime(displayMonth.year, displayMonth.month + delta);
    if (newMonth.isBefore(widget.firstDate) ||
        newMonth.isAfter(DateTime(widget.lastDate.year, widget.lastDate.month + 1))) {
      return;
    }

    setState(() => displayMonth = newMonth);
    _pageController.animateToPage(
      _monthsBetween(widget.firstDate, newMonth),
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeOut,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.all(Radius.circular(24)),
        boxShadow: [
          BoxShadow(
            color: Color(0x1A000000),
            blurRadius: 10,
            offset: Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Header z małym tytułem i ikonką
          Container(
            padding: const EdgeInsets.fromLTRB(24, 16, 16, 8),
            child: Row(
              children: [
                Text(
                  'Wybierz datę',
                  style: AppTextStyle.myUZBodyMedium.copyWith(
                    color: const Color(0xFF5F6368),
                    fontWeight: FontWeight.w400,
                  ),
                ),
                const Spacer(),
                IconButton(
                  icon: Icon(
                    MyUz.edit_02,
                    size: 20,
                    color: const Color(0xFF5F6368),
                  ),
                  onPressed: () {},
                  iconSize: 20,
                  constraints: const BoxConstraints(minWidth: 40, minHeight: 40),
                ),
              ],
            ),
          ),

          // Główny tytuł daty - DUŻY
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            alignment: Alignment.centerLeft,
            child: Text(
              '${selectedDate.day} ${_monthShort[selectedDate.month - 1]} ${selectedDate.year}',
              style: AppTextStyle.myUZDisplaySmall.copyWith(
                color: const Color(0xFF202124),
                fontWeight: FontWeight.w400,
                fontSize: 32,
                height: 1.2,
              ),
            ),
          ),

          const SizedBox(height: 16),

          // Nawigacja miesiąca z dropdown i strzałkami
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Row(
              children: [
                // Dropdown miesiąc-rok
                InkWell(
                  onTap: () {},
                  borderRadius: BorderRadius.circular(8),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          '${_monthNames[displayMonth.month - 1]} ${displayMonth.year}',
                          style: AppTextStyle.myUZTitleMedium.copyWith(
                            color: const Color(0xFF202124),
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        const SizedBox(width: 4),
                        Icon(
                          MyUz.chevron_down,
                          size: 18,
                          color: const Color(0xFF5F6368),
                        ),
                      ],
                    ),
                  ),
                ),

                const Spacer(),

                // Strzałki nawigacji
                IconButton(
                  icon: Icon(
                    MyUz.chevron_left,
                    size: 24,
                    color: const Color(0xFF202124),
                  ),
                  onPressed: () => _navigateMonth(-1),
                  iconSize: 24,
                ),

                IconButton(
                  icon: Icon(
                    MyUz.chevron_right,
                    size: 24,
                    color: const Color(0xFF202124),
                  ),
                  onPressed: () => _navigateMonth(1),
                  iconSize: 24,
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // Etykiety dni tygodnia - małe litery jak Google
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Row(
              children: _dayNames.map((day) =>
                  Expanded(
                    child: Center(
                      child: Text(
                        day,
                        style: AppTextStyle.myUZBodySmall.copyWith(
                          color: const Color(0xFF5F6368),
                          fontWeight: FontWeight.w500,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  ),
              ).toList(),
            ),
          ),

          const SizedBox(height: 8),

          // Kalendarz - dokładnie jak Google
          SizedBox(
            height: 48 * 6,
            child: PageView.builder(
              controller: _pageController,
              onPageChanged: (page) {
                final months = _monthsBetween(widget.firstDate, DateTime(widget.firstDate.year, widget.firstDate.month));
                final targetMonth = DateTime(
                  widget.firstDate.year,
                  widget.firstDate.month + page - months,
                );
                setState(() => displayMonth = targetMonth);
              },
              itemBuilder: (context, pageIndex) {
                final months = _monthsBetween(widget.firstDate, DateTime(widget.firstDate.year, widget.firstDate.month));
                final month = DateTime(
                  widget.firstDate.year,
                  widget.firstDate.month + pageIndex - months,
                );
                return _buildMonthGrid(month);
              },
            ),
          ),

          // Przyciski akcji - dokładnie jak Google Calendar
          Container(
            padding: const EdgeInsets.all(24),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                // Anuluj
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  style: TextButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    minimumSize: Size.zero,
                  ),
                  child: Text(
                    'Anuluj',
                    style: AppTextStyle.myUZLabelLarge.copyWith(
                      color: AppColors.myUZSysLightPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),

                const SizedBox(width: 8),

                // OK
                TextButton(
                  onPressed: () => Navigator.of(context).pop(selectedDate),
                  style: TextButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    minimumSize: Size.zero,
                  ),
                  child: Text(
                    'OK',
                    style: AppTextStyle.myUZLabelLarge.copyWith(
                      color: AppColors.myUZSysLightPrimary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Padding na dole
          SizedBox(height: MediaQuery.of(context).padding.bottom + 8),
        ],
      ),
    );
  }

  Widget _buildMonthGrid(DateTime month) {
    final firstDay = DateTime(month.year, month.month, 1);
    final weekday = firstDay.weekday; // Mon=1 ... Sun=7
    final int daysToSubtract = (weekday + 6) % 7; // poniedziałek ->0, wt->1 ... niedz->6
    final startDate = firstDay.subtract(Duration(days: daysToSubtract));

    return GridView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 7,
        childAspectRatio: 1,
        mainAxisSpacing: 4,
        crossAxisSpacing: 4,
      ),
      itemCount: 42,
      itemBuilder: (context, index) {
        final date = startDate.add(Duration(days: index));
        final isCurrentMonth = date.month == month.month;
        final isSelected = date.day == selectedDate.day &&
            date.month == selectedDate.month &&
            date.year == selectedDate.year;
        final isToday = date.day == DateTime.now().day &&
            date.month == DateTime.now().month &&
            date.year == DateTime.now().year;

        return Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: isCurrentMonth ? () {
              setState(() => selectedDate = date);
            } : null,
            borderRadius: BorderRadius.circular(20),
            child: Container(
              decoration: BoxDecoration(
                color: isSelected
                    ? AppColors.myUZSysLightPrimary
                    : null,
                borderRadius: BorderRadius.circular(20),
                border: isToday && !isSelected
                    ? Border.all(
                  color: AppColors.myUZSysLightPrimary,
                  width: 1,
                )
                    : null,
              ),
              child: Center(
                child: Text(
                  '${date.day}',
                  style: AppTextStyle.myUZBodyMedium.copyWith(
                    color: isSelected
                        ? Colors.white
                        : isCurrentMonth
                        ? (isToday
                        ? AppColors.myUZSysLightPrimary
                        : const Color(0xFF202124))
                        : const Color(0xFF9AA0A6),
                    fontWeight: isSelected
                        ? FontWeight.w600
                        : FontWeight.w400,
                    fontSize: 14,
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
