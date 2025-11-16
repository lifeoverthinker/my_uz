// CalendarView — poprawiona wersja: stabilna wysokość aby uniknąć RenderFlex overflow
import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';

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
  final bool enableSwipe;

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
    this.enableSwipe = true,
  });

  /// Zwraca listę 42 dat rozpoczynając od poniedziałku, zawierającą komórki miesiąca widoku.
  static List<DateTime> buildMonthDays(DateTime focus) {
    // first day of month in local time
    final firstOfMonth = DateTime(focus.year, focus.month, 1);
    // weekday: Monday=1 ... Sunday=7
    final firstWeekday = firstOfMonth.weekday;
    // start = first day of the grid: back up to Monday
    final start = firstOfMonth.subtract(Duration(days: firstWeekday - 1));
    return List.generate(42, (i) => DateTime(start.year, start.month, start.day + i));
  }

  @override
  State<CalendarView> createState() => _CalendarViewState();
}

class _CalendarViewState extends State<CalendarView> with SingleTickerProviderStateMixin {
  double? _startX;
  double? _lastX;
  int? _startTimeMillis;
  int? _lastTimeMillis;
  double? _startY;
  double? _lastY;

  static const double _dragThreshold = 40;
  static const double _velocityThreshold = 250;
  DateTime? _lastWeekMonday;
  int _slideDir = 0;

  void _resetPointers() {
    _startX = null;
    _lastX = null;
    _startTimeMillis = null;
    _lastTimeMillis = null;
    _startY = null;
    _lastY = null;
  }

  void _onPointerDown(PointerDownEvent e) {
    _startX = e.position.dx;
    _startY = e.position.dy;
    _lastX = _startX;
    _lastY = _startY;
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
    _resetPointers();
  }

  void _onPointerUp(PointerUpEvent e) {
    if (_startX == null || _lastX == null || _startTimeMillis == null || _lastTimeMillis == null) {
      _resetPointers();
      return;
    }
    final dx = _lastX! - _startX!;
    final dy = (_lastY ?? _startY ?? 0) - (_startY ?? 0);

    // treat as horizontal swipe only when X movement dominates
    if (dx.abs() <= (dy.abs() * 1.2)) {
      _resetPointers();
      return;
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

    _resetPointers();
  }

  DateTime _mondayOfWeekLocal(DateTime d) {
    final wd = d.weekday; // 1 = Mon
    return DateTime(d.year, d.month, d.day).subtract(Duration(days: wd - 1));
  }

  @override
  void didUpdateWidget(covariant CalendarView oldWidget) {
    super.didUpdateWidget(oldWidget);
    final prev = _lastWeekMonday ?? _mondayOfWeekLocal(oldWidget.focusedDay);
    final now = _mondayOfWeekLocal(widget.focusedDay);
    final diff = now.difference(prev).inDays;
    if (diff > 0) {
      _slideDir = -1;
    } else if (diff < 0) {
      _slideDir = 1;
    } else {
      _slideDir = 0;
    }
    _lastWeekMonday = now;
  }

  @override
  Widget build(BuildContext context) {
    final days = CalendarView.buildMonthDays(widget.focusedDay);
    final grid = _MonthGrid(
      days: days,
      focusedMonth: DateTime(widget.focusedDay.year, widget.focusedDay.month),
      selectedDay: widget.selectedDay,
      classesByDay: widget.classesByDay,
      onTap: widget.onDaySelected,
      isWeekView: widget.isWeekView,
    );

    final child = widget.isWeekView
        ? (() {
      final newKeyStr = _mondayOfWeekLocal(widget.focusedDay).toIso8601String();
      return AnimatedSwitcher(
        duration: const Duration(milliseconds: 360),
        switchInCurve: Curves.easeOutCubic,
        switchOutCurve: Curves.easeInOutCubic,
        transitionBuilder: (childWidget, animation) {
          final offsetAnimation = Tween<Offset>(begin: Offset(_slideDir.toDouble() * 0.25, 0), end: Offset.zero).animate(animation);
          return SlideTransition(position: offsetAnimation, child: FadeTransition(opacity: animation, child: childWidget));
        },
        child: SizedBox(key: ValueKey(newKeyStr), child: grid),
      );
    })()
        : grid;

    final animated = AnimatedSize(
      duration: const Duration(milliseconds: 360),
      curve: Curves.easeInOutCubic,
      child: child,
    );

    // Calculate a stable height for the calendar area to avoid Column overflow.
    const double rowHeight = 42.0;
    final int visibleRows = widget.isWeekView ? 1 : 6;
    final double totalHeight = visibleRows * rowHeight;

    final Widget sized = SizedBox(
      height: totalHeight,
      child: animated,
    );

    if (!widget.enableSwipe) return sized;

    return Listener(
      behavior: HitTestBehavior.opaque,
      onPointerDown: _onPointerDown,
      onPointerMove: _onPointerMove,
      onPointerUp: _onPointerUp,
      onPointerCancel: _onPointerCancel,
      child: sized,
    );
  }
}

class _MonthGrid extends StatelessWidget {
  final List<DateTime> days; // 42 elementy
  final DateTime focusedMonth;
  final DateTime selectedDay;
  final Map<DateTime, List<ClassModel>> classesByDay;
  final ValueChanged<DateTime> onTap;
  final bool isWeekView;

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
      final contentWidth = constraints.maxWidth - 32;
      final available = (contentWidth - (kCalendarLeftReserve + 1)).clamp(0.0, double.infinity);
      final itemW = available / 7;
      const rowHeight = 42.0;

      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: ClipRect(
          child: Column(
            children: [
              for (int w = 0; w < 6; w++)
                _AnimatedMonthRow(
                  visible: !isWeekView || w == _weekIndexOf(selectedDay),
                  opacityVisible: !isWeekView || w == _weekIndexOf(selectedDay),
                  height: rowHeight,
                  child: SizedBox(
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
                ),
            ],
          ),
        ),
      );
    });
  }

  int _weekIndexOf(DateTime day) {
    final first = DateTime(focusedMonth.year, focusedMonth.month, 1);
    final firstWeekday = first.weekday; // 1=Mon
    final start = first.subtract(Duration(days: firstWeekday - 1));
    final diff = day.difference(start).inDays;
    return (diff / 7).floor();
  }
}

// Ujednolicone kolory Material 3 Primary dla spójności z ModalDatePicker
const Color _kM3Primary = Color(0xFF6750A4);
const Color _kM3OnPrimary = Colors.white;

class _DayCell extends StatelessWidget {
  final DateTime day;
  final bool isSelected;
  final bool isOutside;
  final bool hasClasses;
  final VoidCallback onTap;
  final double size;

  const _DayCell({required this.day, required this.isSelected, required this.isOutside, required this.hasClasses, required this.onTap, required this.size});

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
      textColor = _kM3OnPrimary; // ZMIANA: Biały kolor tekstu dla zaznaczonego dnia (M3 OnPrimary)
    } else if (isOutside) {
      textColor = cs.onSurface.withAlpha((0.35 * 255).round());
    } else if (isPast) {
      textColor = const Color(0xFFB0B0B0);
    } else {
      textColor = const Color(0xFF494949);
    }

    const double diameter = 32;
    // Nie zmieniamy dayTextStyle na stałe, bo dayTextStyle.color jest używany dla nieaktywnych kolorów.
    final TextStyle dayTextStyle = (baseStyle ?? const TextStyle()).copyWith(fontWeight: FontWeight.w600, fontSize: 16, color: textColor, height: 1);

    Widget circleContent(String text) => AnimatedContainer(
      duration: const Duration(milliseconds: 160),
      width: diameter,
      height: diameter,
      decoration: BoxDecoration(
        // ZMIANA: Tło dla wybranego dnia (M3 Primary) i usunięcie light-primary background dla isToday
        color: isSelected ? _kM3Primary : Colors.transparent,
        borderRadius: BorderRadius.circular(diameter / 2),
        // ZMIANA: Obwódka dla dnia dzisiejszego (M3 Primary, brak przezroczystości)
        border: isSelected ? null : (isToday ? Border.all(color: _kM3Primary, width: 1) : null),
      ),
      alignment: Alignment.center,
      child: Text(
          text,
          style: dayTextStyle.copyWith(
            // ZMIANA: Kolor tekstu dla zaznaczonego (M3 OnPrimary) i dzisiejszego (M3 Primary)
              color: isSelected ? _kM3OnPrimary : (isToday ? _kM3Primary : dayTextStyle.color)
          )
      ),
    );

    final bool showCircle = isSelected || isToday;
    final Widget child = showCircle
        ? circleContent(day.day.toString())
        : SizedBox(width: diameter, height: diameter, child: Center(child: Text(day.day.toString(), style: dayTextStyle)));

    // Use a stable height equal to the row height (diameter + 8) and center the content.
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: SizedBox(
        height: diameter + 8,
        child: Center(child: child),
      ),
    );
  }
}

class _AnimatedMonthRow extends StatelessWidget {
  final bool visible;
  final bool opacityVisible;
  final double height;
  final Widget child;

  const _AnimatedMonthRow({required this.visible, required this.opacityVisible, required this.height, required this.child});

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 340),
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