import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';

// Rezerwa po lewej aby kolumny dni wyrównały się z osią timeline: 32(label) +16(gap) +8(segment)
const double kCalendarLeftReserve = 32 + 16 + 8; // 56

class CalendarView extends StatelessWidget {
  final DateTime focusedDay;
  final DateTime selectedDay;
  final bool isWeekView;
  final Map<DateTime, List<ClassModel>> classesByDay;
  final ValueChanged<DateTime> onDaySelected;
  final VoidCallback? onPrevWeek;
  final VoidCallback? onNextWeek;
  final VoidCallback? onPrevMonth;
  final VoidCallback? onNextMonth;
  const CalendarView({
    super.key,
    required this.focusedDay,
    required this.selectedDay,
    required this.isWeekView,
    required this.classesByDay,
    required this.onDaySelected,
    this.onPrevWeek,
    this.onNextWeek,
    this.onPrevMonth,
    this.onNextMonth,
  });

  @override
  Widget build(BuildContext context) {
    final days = _buildMonthDays(focusedDay); // zawsze 42 dni
    final monthGrid = _MonthGrid(
      days: days,
      focusedMonth: DateTime(focusedDay.year, focusedDay.month),
      selectedDay: selectedDay,
      classesByDay: classesByDay,
      onTap: onDaySelected,
      isWeekView: isWeekView,
    );
    Widget content = monthGrid;
    // Gesture: week -> zmiana tygodnia, month -> zmiana miesiąca
    content = GestureDetector(
      onHorizontalDragEnd: (details) {
        final v = details.primaryVelocity ?? 0;
        if (v.abs() < 140) return;
        if (isWeekView) {
          if (v < 0) { if (onNextWeek!=null) onNextWeek!(); } else { if (onPrevWeek!=null) onPrevWeek!(); }
        } else {
          if (v < 0) { if (onNextMonth!=null) onNextMonth!(); } else { if (onPrevMonth!=null) onPrevMonth!(); }
        }
      },
      child: monthGrid,
    );
    // Zewnętrzny padding usunięty – wyrównanie z nagłówkiem dni tygodnia (ten ma własny padding 16).
    return content;
  }

  List<DateTime> _buildMonthDays(DateTime focus) {
    final firstOfMonth = DateTime(focus.year, focus.month, 1);
    final firstWeekday = firstOfMonth.weekday; // 1=pon
    final start = firstOfMonth.subtract(Duration(days: firstWeekday - 1));
    return List.generate(42, (i) => start.add(Duration(days: i)));
  }
}

class _MonthGrid extends StatelessWidget {
  final List<DateTime> days; // 42 elementy
  final DateTime focusedMonth;
  final DateTime selectedDay;
  final Map<DateTime, List<ClassModel>> classesByDay;
  final ValueChanged<DateTime> onTap;
  final bool isWeekView; // decyduje o animacji wierszy
  const _MonthGrid({
    required this.days,
    required this.focusedMonth,
    required this.selectedDay,
    required this.classesByDay,
    required this.onTap,
    required this.isWeekView,
  });

  bool _isSameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      final available = constraints.maxWidth - kCalendarLeftReserve - 32; // odejmij padding 16*2
      final itemW = available / 7;
      const rowHeight = 38.0; // DayCell: 32 + 6 marg.
      final targetRows = isWeekView ? 1 : 6;
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: AnimatedSize(
          duration: const Duration(milliseconds: 340),
          curve: Curves.easeInOutCubic,
          alignment: Alignment.topCenter,
          child: ClipRect(
            child: SizedBox(
              height: rowHeight * targetRows,
              child: Column(
                children: [
                  for (int w = 0; w < 6; w++)
                    _AnimatedMonthRow(
                      visible: !isWeekView || w == _weekIndexOf(selectedDay),
                      opacityVisible: !isWeekView || w == _weekIndexOf(selectedDay),
                      height: rowHeight,
                      child: Row(
                        children: [
                          SizedBox(width: kCalendarLeftReserve),
                          for (int d = 0; d < 7; d++)
                            SizedBox(
                              width: itemW,
                              child: _DayCell(
                                day: days[w * 7 + d],
                                isSelected: _isSameDay(days[w * 7 + d], selectedDay),
                                isOutside: days[w * 7 + d].month != focusedMonth.month,
                                hasClasses: (classesByDay[DateTime(days[w * 7 + d].year, days[w * 7 + d].month, days[w * 7 + d].day)] ?? const []).isNotEmpty,
                                onTap: () => onTap(days[w * 7 + d]),
                                size: 34,
                              ),
                            ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      );
    });
  }

  int _weekIndexOf(DateTime day) {
    final first = DateTime(focusedMonth.year, focusedMonth.month, 1);
    final firstWeekday = first.weekday; // 1=pon
    final start = first.subtract(Duration(days: firstWeekday - 1));
    final diff = day.difference(start).inDays;
    return (diff / 7).floor();
  }
}

class _DayCell extends StatelessWidget {
  final DateTime day;
  final bool isSelected;
  final bool isOutside;
  final bool hasClasses;
  final VoidCallback onTap;
  final double size;
  const _DayCell({
    required this.day,
    required this.isSelected,
    required this.isOutside,
    required this.hasClasses,
    required this.onTap,
    required this.size,
  });

  bool get isToday {
    final now = DateTime.now();
    return day.year == now.year && day.month == now.month && day.day == now.day;
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final baseStyle = Theme.of(context).textTheme.bodyMedium;
    final today = DateTime.now();
    final isPast = day.isBefore(DateTime(today.year, today.month, today.day));

    Color textColor;
    if (isSelected) {
      textColor = cs.onPrimary;
    } else if (isOutside) {
      textColor = cs.onSurface.withValues(alpha: 0.35);
    } else if (isPast) {
      textColor = const Color(0xFFB0B0B0);
    } else {
      textColor = const Color(0xFF494949);
    }

    final double diameter = 32; // stała średnica dla kółka

    final TextStyle dayTextStyle = (baseStyle ?? const TextStyle()).copyWith(
      fontWeight: FontWeight.w600,
      fontSize: 16,
      color: textColor,
      height: 1,
    );

    Widget circleContent(String text) => AnimatedContainer(
      duration: const Duration(milliseconds: 160),
      width: diameter,
      height: diameter,
      decoration: BoxDecoration(
        color: isSelected
            ? cs.primary
            : (isToday ? cs.primary.withValues(alpha: 0.12) : Colors.transparent),
        borderRadius: BorderRadius.circular(diameter / 2),
        border: (isSelected || isToday) ? null : null,
      ),
      alignment: Alignment.center,
      child: Text(text, style: dayTextStyle),
    );

    final bool showCircle = isSelected || isToday;

    final child = showCircle
        ? circleContent(day.day.toString())
        : SizedBox(
            width: diameter,
            height: diameter,
            child: Center(
              child: Text(
                day.day.toString(),
                style: dayTextStyle,
              ),
            ),
          );

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: SizedBox(
        height: diameter + 6,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [child],
        ),
      ),
    );
  }
}

class _AnimatedMonthRow extends StatelessWidget {
  final bool visible; // czy w ogóle ma uczestniczyć (wysokość)
  final bool opacityVisible; // czy ma być w pełni widoczny
  final double height;
  final Widget child;
  const _AnimatedMonthRow({required this.visible, required this.opacityVisible, required this.height, required this.child});
  @override
  Widget build(BuildContext context) {
    return AnimatedOpacity(
      duration: const Duration(milliseconds: 260),
      curve: Curves.easeInOut,
      opacity: opacityVisible ? 1 : 0,
      child: SizedBox(
        height: visible ? height : 0, // jeśli niewidoczny -> wysokość 0, AnimatedSize animuje zmianę
        child: ClipRect(
          child: Align(
            alignment: Alignment.topCenter,
            heightFactor: visible ? 1 : 0,
            child: child,
          ),
        ),
      ),
    );
  }
}
