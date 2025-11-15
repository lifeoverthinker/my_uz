import 'package:flutter/material.dart';

/// Wspólny widget paska dni tygodnia (zastępuje zduplikowane _DaysOfWeekBar)
/// leftReserve: offset od lewej aby wyrównać z timeline (domyślnie 60)
class DaysOfWeekBar extends StatelessWidget {
  final DateTime selectedDay;
  final ValueChanged<DateTime> onDaySelected;
  final VoidCallback? onPrevWeek;
  final VoidCallback? onNextWeek;
  final double leftReserve;
  final Map<DateTime, bool> eventsByDay;

  const DaysOfWeekBar({
    super.key,
    required this.selectedDay,
    required this.onDaySelected,
    this.onPrevWeek,
    this.onNextWeek,
    this.leftReserve = 60.0,
    this.eventsByDay = const {},
  });

  // Podniesione do 72, aby uniknąć sporadycznego overflow przy większych fontach
  static const double _kHeight = 72.0;

  @override
  Widget build(BuildContext context) {
    final startOfWeek =
        selectedDay.subtract(Duration(days: selectedDay.weekday - 1));
    final cs = Theme.of(context).colorScheme;
    final baseStyle = Theme.of(context).textTheme.bodyMedium;
    final dayTextStyle = (baseStyle ?? const TextStyle())
        .copyWith(fontWeight: FontWeight.w600, fontSize: 16, height: 1);

    return SizedBox(
      height: _kHeight,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onHorizontalDragEnd: (details) {
          final v = details.primaryVelocity ?? 0;
          if (v.abs() < 200) return;
          if (v < 0) {
            onNextWeek?.call();
          } else {
            onPrevWeek?.call();
          }
        },
        child: Padding(
          // vertical zmniejszone do 6 dla dodatkowej rezerwy
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          child: Row(
            children: [
              SizedBox(width: leftReserve),
              Expanded(
                child: Row(
                  // dzieci teraz są wrapped w Expanded - wyrównanie nie musi być spaceBetween
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: List.generate(7, (i) {
                    final day = startOfWeek.add(Duration(days: i));
                    final isSelected = day.year == selectedDay.year &&
                        day.month == selectedDay.month &&
                        day.day == selectedDay.day;
                    final isToday = DateTime.now().year == day.year &&
                        DateTime.now().month == day.month &&
                        DateTime.now().day == day.day;

                    Widget dayCircle() {
                      final showCircle = isSelected || isToday;
                      if (showCircle) {
                        return AnimatedContainer(
                          duration: const Duration(milliseconds: 180),
                          width: 32,
                          height: 32,
                          decoration: BoxDecoration(
                            color: isSelected
                                ? cs.primary
                                : (isToday
                                    ? cs.primary.withAlpha((0.16 * 255).round())
                                    : Colors.transparent),
                            borderRadius: BorderRadius.circular(16),
                            border: isSelected
                                ? null
                                : (isToday
                                    ? Border.all(
                                        color: cs.primary
                                            .withAlpha((0.32 * 255).round()))
                                    : null),
                          ),
                          alignment: Alignment.center,
                          child: Text(day.day.toString(),
                              style: dayTextStyle.copyWith(
                                  color: isSelected
                                      ? cs.onPrimary
                                      : (isToday
                                          ? cs.primary
                                          : const Color(0xFF494949)))),
                        );
                      } else {
                        return SizedBox(
                          width: 32,
                          height: 32,
                          child: Center(
                              child: Text(day.day.toString(),
                                  style: dayTextStyle.copyWith(
                                      color: const Color(0xFF494949)))),
                        );
                      }
                    }

                    return Expanded(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_weekdayShort(day.weekday),
                              style: Theme.of(context)
                                  .textTheme
                                  .labelSmall
                                  ?.copyWith(
                                      fontWeight: FontWeight.w500,
                                      color: const Color(0xFF494949))),
                          const SizedBox(height: 2),
                          Material(
                            color: Colors.transparent,
                            child: InkWell(
                              borderRadius: BorderRadius.circular(16),
                              onTap: () => onDaySelected(day),
                              child: Center(child: dayCircle()),
                            ),
                          ),
                        ],
                      ),
                    );
                  }),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _weekdayShort(int weekday) {
    const days = ['P', 'W', 'Ś', 'C', 'P', 'S', 'N'];
    return days[weekday - 1];
  }
}
