import 'package:flutter/material.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:my_uz/models/class_model.dart';

// Rezerwa po lewej aby kolumny dni wyrównały się z osią timeline:
// odpowiada pozycji pionowej linii w CalendarDayView: _labelWidth(48) + _labelFragmentGap(2) + _smallSegment(8) = 58
const double kCalendarLeftReserve = 58;

class CalendarView extends StatefulWidget {
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

  // publiczna metoda pomocnicza do generowania siatki 42 dni (6 wierszy x 7 dni)
  static List<DateTime> buildMonthDays(DateTime focus) {
    final firstOfMonth = DateTime(focus.year, focus.month, 1);
    final firstWeekday = firstOfMonth.weekday; // 1=pon
    final start = firstOfMonth.subtract(Duration(days: firstWeekday - 1));
    return List.generate(42, (i) => start.add(Duration(days: i)));
  }

  @override
  State<CalendarView> createState() => _CalendarViewState();
}

class _CalendarViewState extends State<CalendarView> {
  double _accumDx = 0.0;
  DateTime? _lastSwipeTime;
  static const int _swipeCooldownMs = 420;

  void _onPointerDown(PointerDownEvent ev) {
    _accumDx = 0.0;
  }

  void _onPointerMove(PointerMoveEvent ev) {
    _accumDx += ev.delta.dx;
  }

  void _trySwipe(VoidCallback cb) {
    final now = DateTime.now();
    if (_lastSwipeTime != null && now.difference(_lastSwipeTime!).inMilliseconds < _swipeCooldownMs) return;
    _lastSwipeTime = now;
    cb();
  }

  void _onPointerUp(PointerUpEvent ev) {
    // Threshold similar to earlier: 40px
    if (_accumDx.abs() < 40) return;
    if (_accumDx < 0) {
      if (widget.isWeekView) _trySwipe(() => widget.onNextWeek?.call());
      else _trySwipe(() => widget.onNextMonth?.call());
    } else {
      if (widget.isWeekView) _trySwipe(() => widget.onPrevWeek?.call());
      else _trySwipe(() => widget.onPrevMonth?.call());
    }
  }

  @override
  Widget build(BuildContext context) {
    // final days = CalendarView.buildMonthDays(widget.focusedDay); // zawsze 42 dni (unused)

    // Używamy TableCalendar by mieć pewność, że dni miesiąca są poprawnie
    // generowane według kalendarza (bez duplikatów). Stylujemy każdą komórkę
    // przy pomocy lokalnego _DayCell, i wywołujemy callbacki page change.
    const rowHeight = 38.0;
    final targetRows = widget.isWeekView ? 1 : 6;

    return Listener(
      onPointerDown: _onPointerDown,
      onPointerMove: _onPointerMove,
      onPointerUp: _onPointerUp,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onHorizontalDragEnd: (details) {
          final v = details.primaryVelocity ?? 0;
          if (v.abs() < 40) return;
          if (v < 0) {
            if (widget.isWeekView) _trySwipe(() => widget.onNextWeek?.call()); else _trySwipe(() => widget.onNextMonth?.call());
          } else {
            if (widget.isWeekView) _trySwipe(() => widget.onPrevWeek?.call()); else _trySwipe(() => widget.onPrevMonth?.call());
          }
        },
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: AnimatedSize(
            duration: const Duration(milliseconds: 80),
            curve: Curves.easeInOutCubic,
            alignment: Alignment.topCenter,
            child: ClipRect(
              child: SizedBox(
                height: rowHeight * targetRows,
                child: Row(
                  children: [
                    const SizedBox(width: kCalendarLeftReserve),
                    Expanded(
                      child: Stack(
                        children: [
                          TableCalendar(
                            firstDay: DateTime(widget.focusedDay.year - 2),
                            lastDay: DateTime(widget.focusedDay.year + 2, 12, 31),
                            rowHeight: rowHeight,
                            daysOfWeekHeight: 0,
                            focusedDay: widget.focusedDay,
                            headerVisible: false,
                            daysOfWeekVisible: false,
                            calendarFormat: widget.isWeekView ? CalendarFormat.week : CalendarFormat.month,
                            startingDayOfWeek: StartingDayOfWeek.monday,
                            onDaySelected: (d, fd) => widget.onDaySelected(d),
                            onPageChanged: (newFocused) {
                              final now = DateTime.now();
                              if (_lastSwipeTime != null && now.difference(_lastSwipeTime!).inMilliseconds < _swipeCooldownMs) {
                                return;
                              }
                              if (newFocused.isAfter(widget.focusedDay)) {
                                if (widget.isWeekView) {
                                  _trySwipe(() => widget.onNextWeek?.call());
                                } else {
                                  _trySwipe(() => widget.onNextMonth?.call());
                                }
                              } else if (newFocused.isBefore(widget.focusedDay)) {
                                if (widget.isWeekView) {
                                  _trySwipe(() => widget.onPrevWeek?.call());
                                } else {
                                  _trySwipe(() => widget.onPrevMonth?.call());
                                }
                              }
                            },
                            calendarBuilders: CalendarBuilders(
                              defaultBuilder: (context, day, focused) {
                                return SizedBox(
                                  height: rowHeight,
                                  child: _DayCell(
                                    day: day,
                                    isSelected: day.year == widget.selectedDay.year && day.month == widget.selectedDay.month && day.day == widget.selectedDay.day,
                                    isOutside: day.month != widget.focusedDay.month,
                                    hasClasses: (widget.classesByDay[DateTime(day.year, day.month, day.day)] ?? const []).isNotEmpty,
                                    onTap: () => widget.onDaySelected(day),
                                    size: 32,
                                  ),
                                );
                              },
                              selectedBuilder: (context, day, focused) {
                                return SizedBox(
                                  height: rowHeight,
                                  child: _DayCell(
                                    day: day,
                                    isSelected: true,
                                    isOutside: day.month != widget.focusedDay.month,
                                    hasClasses: (widget.classesByDay[DateTime(day.year, day.month, day.day)] ?? const []).isNotEmpty,
                                    onTap: () => widget.onDaySelected(day),
                                    size: 32,
                                  ),
                                );
                              },
                              todayBuilder: (context, day, focused) {
                                final isSelected = day.year == widget.selectedDay.year && day.month == widget.selectedDay.month && day.day == widget.selectedDay.day;
                                return SizedBox(
                                  height: rowHeight,
                                  child: _DayCell(
                                    day: day,
                                    isSelected: isSelected,
                                    isOutside: day.month != widget.focusedDay.month,
                                    hasClasses: (widget.classesByDay[DateTime(day.year, day.month, day.day)] ?? const []).isNotEmpty,
                                    onTap: () => widget.onDaySelected(day),
                                    size: 32,
                                  ),
                                );
                              },
                            ),
                            calendarStyle: CalendarStyle(
                              outsideDaysVisible: true,
                              cellMargin: EdgeInsets.zero,
                              isTodayHighlighted: false,
                              todayDecoration: const BoxDecoration(
                                color: Colors.transparent,
                                shape: BoxShape.circle,
                              ),
                              selectedDecoration: const BoxDecoration(
                                color: Colors.transparent,
                                shape: BoxShape.circle,
                              ),
                              todayTextStyle: const TextStyle(),
                              selectedTextStyle: const TextStyle(),
                            ),
                          ),
                          // Przezroczysty detektor gestów na wierzchu, aby pewnie przechwycić flingi
                          Positioned.fill(
                            child: GestureDetector(
                              behavior: HitTestBehavior.translucent,
                              onHorizontalDragEnd: (details) {
                                final v = details.primaryVelocity ?? 0;
                                if (v.abs() < 40) return;
                                if (v < 0) {
                                  if (widget.isWeekView) _trySwipe(() => widget.onNextWeek?.call()); else _trySwipe(() => widget.onNextMonth?.call());
                                } else {
                                  if (widget.isWeekView) _trySwipe(() => widget.onPrevWeek?.call()); else _trySwipe(() => widget.onPrevMonth?.call());
                                }
                              },
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
        ),
      ),
    );
  }
}

class _MonthGrid extends StatelessWidget {
  final List<DateTime> days; // 42 elementy
  final DateTime focusedMonth;
  final DateTime focusedDay; // rzeczywisty fokus (używany w trybie tygodniowym)
  final DateTime selectedDay;
  final Map<DateTime, List<ClassModel>> classesByDay;
  final ValueChanged<DateTime> onTap;
  final bool isWeekView; // decyduje o animacji wierszy
  const _MonthGrid({
    required this.days,
    required this.focusedMonth,
    required this.focusedDay,
    required this.selectedDay,
    required this.classesByDay,
    required this.onTap,
    required this.isWeekView,
  });

  bool _isSameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      // contentWidth: szerokość dostępna wewnątrz paddingu (po obu stronach po 16px)
      final contentWidth = constraints.maxWidth - 32;
      // obszar od pionowej linii do końca frame'a to contentWidth - (kCalendarLeftReserve + 1)
      final available = (contentWidth - (kCalendarLeftReserve + 1)).clamp(0.0, double.infinity);
      final itemW = available / 7;
      const rowHeight = 38.0; // DayCell: 32 + 6 marg.
      final targetRows = isWeekView ? 1 : 6;

      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: AnimatedSize(
          duration: const Duration(milliseconds: 80),
          curve: Curves.easeInOutCubic,
          alignment: Alignment.topCenter,
          child: ClipRect(
            child: SizedBox(
              height: rowHeight * targetRows,
              child: Column(
                children: List.generate(6, (w) {
                  final visible = isWeekView ? (w == _weekIndexOf(focusedDay)) : (w == _weekIndexOf(selectedDay));
                  final opacityVisible = visible;
                  return _AnimatedMonthRow(
                    visible: visible,
                    opacityVisible: opacityVisible,
                    height: rowHeight,
                    child: Row(
                      children: [
                        const SizedBox(width: kCalendarLeftReserve),
                        for (int d = 0; d < 7; d++)
                          SizedBox(
                            width: itemW,
                            child: _DayCell(
                              day: days[w * 7 + d],
                              isSelected: _isSameDay(days[w * 7 + d], selectedDay),
                              isOutside: days[w * 7 + d].month != focusedMonth.month,
                              hasClasses: (classesByDay[DateTime(days[w * 7 + d].year, days[w * 7 + d].month, days[w * 7 + d].day)] ?? const []).isNotEmpty,
                              onTap: () => onTap(days[w * 7 + d]),
                              size: 32,
                            ),
                          ),
                      ],
                    ),
                  );
                }),
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

    final double diameter = size; // użyj przekazanego rozmiaru

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
            : (isToday ? cs.primary.withValues(alpha: 0.4) : Colors.transparent),
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
