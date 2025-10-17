import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';

// Rezerwa po lewej aby kolumny dni wyrównały się z osią timeline:
// odpowiada pozycji pionowej linii w CalendarDayView: _labelWidth(48) + _labelFragmentGap(4) + _smallSegment(8) = 60
const double kCalendarLeftReserve = 60;

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
  final bool enableSwipe; // Dodano parametr
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
    this.enableSwipe = true, // domyślnie true
  });

  @override
  State<CalendarView> createState() => _CalendarViewState();

  // Public static helper used by tests: buduje 42 dni dla miesiąca zaczynając od poniedziałku
  static List<DateTime> buildMonthDays(DateTime focus) {
    final firstOfMonth = DateTime(focus.year, focus.month, 1);
    final firstWeekday = firstOfMonth.weekday; // 1=pon
    final start = firstOfMonth.subtract(Duration(days: firstWeekday - 1));
    return List.generate(42, (i) => start.add(Duration(days: i)));
  }
}

class _CalendarViewState extends State<CalendarView> {
  // pointer-based gesture tracking (works even when child widgets have GestureDetectors)
  double? _startX;
  int? _startTimeMillis;
  double? _lastX;
  int? _lastTimeMillis;
  double? _startY;
  double? _lastY;
  static const double _dragThreshold = 40; // minimalna odległość px (bardziej czuły)
  static const double _velocityThreshold = 250; // px/s (bardziej czuły)
  DateTime? _lastWeekMonday;
  // ignore: unused_field
  int _slideDir = 0; // -1 = left (next), 1 = right (prev), 0 = none

  void _onPointerDown(PointerDownEvent e) {
    _startX = e.position.dx;
    _startY = e.position.dy;
    _lastX = _startX;
    final t = DateTime.now().millisecondsSinceEpoch;
    _startTimeMillis = t;
    _lastTimeMillis = t;
  }

  void _onPointerMove(PointerMoveEvent e) {
    _lastX = e.position.dx;
    _lastY = e.position.dy;
    _lastTimeMillis = DateTime.now().millisecondsSinceEpoch;
  }

  void _onPointerCancel(PointerCancelEvent e) {
    _startX = null; _lastX = null; _startTimeMillis = null; _lastTimeMillis = null;
  }

  void _onPointerUp(PointerUpEvent e) {
    if (_startX == null || _lastX == null || _startTimeMillis == null || _lastTimeMillis == null) {
      _startX = null; _lastX = null; _startTimeMillis = null; _lastTimeMillis = null; return;
    }
    final dx = _lastX! - _startX!; // positive -> moved right
    final dy = (_lastY ?? _startY ?? 0) - (_startY ?? 0);
    // rozpoznaj gest poziomy tylko gdy przesunięcie X jest większe niż Y * 1.2
    if (dx.abs() <= (dy.abs() * 1.2)) {
      _startX = null; _lastX = null; _startTimeMillis = null; _lastTimeMillis = null; _startY = null; _lastY = null; return;
    }
    final dtMillis = (_lastTimeMillis! - _startTimeMillis!).clamp(1, 100000);
    final velocity = dx / dtMillis * 1000.0; // px/s

    bool triggered = false;
    if (velocity.abs() > _velocityThreshold) {
      if (velocity < 0) {
        if (widget.isWeekView) widget.onNextWeek?.call(); else widget.onNextMonth?.call();
      } else {
        if (widget.isWeekView) widget.onPrevWeek?.call(); else widget.onPrevMonth?.call();
      }
      triggered = true;
    }
    if (!triggered && dx.abs() > _dragThreshold) {
      if (dx < 0) {
        if (widget.isWeekView) widget.onNextWeek?.call(); else widget.onNextMonth?.call();
      } else {
        if (widget.isWeekView) widget.onPrevWeek?.call(); else widget.onPrevMonth?.call();
      }
    }

    _startX = null; _lastX = null; _startTimeMillis = null; _lastTimeMillis = null; _startY = null; _lastY = null;
    // Debug feedback removed: previously showed gesture info via SnackBar which was noisy during testing
  }

  DateTime _mondayOfWeekLocal(DateTime d) {
    final wd = d.weekday; // 1=Mon
    return DateTime(d.year, d.month, d.day).subtract(Duration(days: wd - 1));
  }

  @override
  void didUpdateWidget(covariant CalendarView oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Jeśli przesunięto fokus na inny tydzień, ustaw kierunek animacji
    final prev = _lastWeekMonday ?? _mondayOfWeekLocal(oldWidget.focusedDay);
    final now = _mondayOfWeekLocal(widget.focusedDay);
    final diff = now.difference(prev).inDays;
    if (diff > 0) _slideDir = -1; // move left to show next week
    else if (diff < 0) _slideDir = 1; // move right to show prev week
    else _slideDir = 0;
    _lastWeekMonday = now;
  }

  @override
  Widget build(BuildContext context) {
    final days = CalendarView.buildMonthDays(widget.focusedDay); // zawsze 42 dni
    final grid = _MonthGrid(
      days: days,
      focusedMonth: DateTime(widget.focusedDay.year, widget.focusedDay.month),
      selectedDay: widget.selectedDay,
      classesByDay: widget.classesByDay,
      onTap: widget.onDaySelected,
      isWeekView: widget.isWeekView,
    );
    // Zawsze opakuj grid w Listener; w trybie tygodniowym dodatkowo animujemy przejścia
    final child = widget.isWeekView
        ? (() {
            final newKeyStr = _mondayOfWeekLocal(widget.focusedDay).toIso8601String();
            return AnimatedSwitcher(
              duration: const Duration(milliseconds: 360),
              switchInCurve: Curves.easeOutCubic,
              switchOutCurve: Curves.easeInOutCubic,
              transitionBuilder: (childWidget, animation) {
                // Slide + Fade for a smoother week change feel
                final offsetAnimation = Tween<Offset>(begin: Offset(_slideDir.toDouble() * 0.25, 0), end: Offset.zero).animate(animation);
                return SlideTransition(position: offsetAnimation, child: FadeTransition(opacity: animation, child: childWidget));
              },
              child: SizedBox(key: ValueKey(newKeyStr), child: grid),
            );
          })()
         : grid;

    // Jeśli gesty są wyłączone, zwróć child bez Listener
    if (!widget.enableSwipe) {
      return child;
    }
    // W przeciwnym razie opakuj w Listener
    return Listener(
      behavior: HitTestBehavior.opaque,
      onPointerDown: _onPointerDown,
      onPointerMove: _onPointerMove,
      onPointerUp: _onPointerUp,
      onPointerCancel: _onPointerCancel,
      child: child,
    );
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
    return LayoutBuilder(
      builder: (context, constraints) {
        // contentWidth: szerokość dostępna wewnątrz paddingu (po obu stronach po 16px)
        final contentWidth = constraints.maxWidth - 32;
        // obszar od pionowej linii do końca frame'a to contentWidth - (kCalendarLeftReserve + 1)
        final available = (contentWidth - (kCalendarLeftReserve + 1)).clamp(0.0, double.infinity);
        final itemW = available / 7;
        const rowHeight = 38.0; // DayCell: 32 + 6 marg.
        final targetRows = isWeekView ? 1 : 6;

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 420),
            curve: Curves.easeInOutCubic,
            // animate the overall height between 1 and 6 rows; ClipRect ensures overflow is hidden
            height: rowHeight * targetRows,
            child: ClipRect(
              child: SizedBox(
                // keep inner Column layout stable (always 6 rows rendered), rely on clipping
                child: Column(
                  children: [
                    for (int w = 0; w < 6; w++)
                      _AnimatedMonthRow(
                        visible: !isWeekView || w == _weekIndexOf(selectedDay),
                        opacityVisible: !isWeekView || w == _weekIndexOf(selectedDay),
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
      },
    );
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

    const double diameter = 32; // stała średnica dla kółka

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
            : (isToday ? cs.primary.withValues(alpha: 0.16) : Colors.transparent),
        borderRadius: BorderRadius.circular(diameter / 2),
        border: isSelected
            ? null
            : (isToday ? Border.all(color: cs.primary.withValues(alpha: 0.32), width: 1) : null),
      ),
      alignment: Alignment.center,
      child: Text(text, style: dayTextStyle.copyWith(color: isSelected ? cs.onPrimary : (isToday ? cs.primary : dayTextStyle.color))),
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
    // Animate the row height between 0 and `height` and fade opacity.
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      curve: Curves.easeInOutCubic,
      height: visible ? height : 0,
      child: IgnorePointer(
        ignoring: !opacityVisible,
        child: AnimatedOpacity(
          duration: const Duration(milliseconds: 260),
          curve: Curves.easeInOut,
          opacity: opacityVisible ? 1 : 0,
          child: ClipRect(
            child: Align(
              alignment: Alignment.topCenter,
              heightFactor: visible ? 1 : 0,
              child: SizedBox(height: height, child: child),
            ),
          ),
        ),
      ),
    );
   }
 }
