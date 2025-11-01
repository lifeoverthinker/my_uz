import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/task_card.dart';

/// Sekcja "Zadania" – wariant „hug height”
/// - brak sztywnej wysokości listy; sekcja dopasowuje się do najwyższej karty
/// - wszystkie karty mają tę samą wysokość (wysokość najwyższej karty), bez ucinania tekstu
/// - padding poziomy 16, szerokość karty 264
class TasksSection extends StatelessWidget {
  final List<TaskModel> tasks;
  final EdgeInsetsGeometry padding;
  final void Function(TaskModel task) onTap;

  const TasksSection({
    super.key,
    required this.tasks,
    this.padding = const EdgeInsets.symmetric(horizontal: 16),
    required this.onTap,
  });

  static const double _kCardWidth = 264;
  static const double _kHeaderGap = 12;

  static DateTime _monday(DateTime d) {
    final wd = d.weekday;
    return DateTime(d.year, d.month, d.day).subtract(Duration(days: wd - 1));
  }

  static DateTime _sunday(DateTime d) {
    final mon = _monday(d);
    return mon.add(const Duration(days: 6, hours: 23, minutes: 59, seconds: 59, milliseconds: 999));
  }

  static List<TaskModel> _inRange(List<TaskModel> src, DateTime from, DateTime to) {
    final list = src.where((t) {
      final d = t.deadline;
      return !d.isBefore(from) && !d.isAfter(to);
    }).toList();
    list.sort((a, b) => a.deadline.compareTo(b.deadline));
    return list;
  }

  static List<TaskModel> _currentOrNextWeek(List<TaskModel> src, DateTime now) {
    final start = _monday(now);
    final end = _sunday(now);
    final cur = _inRange(src, start, end);
    if (cur.isNotEmpty) return cur;

    final nextStart = start.add(const Duration(days: 7));
    final nextEnd = end.add(const Duration(days: 7));
    return _inRange(src, nextStart, nextEnd);
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final visible = _currentOrNextWeek(tasks, DateTime.now());

    return Padding(
      padding: padding,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(MyUz.book_open_01, size: 20, color: cs.onSurface),
              const SizedBox(width: 8),
              Text('Zadania', style: AppTextStyle.myUZTitleMedium.copyWith(fontSize: 18, height: 1.33, fontWeight: FontWeight.w500, color: cs.onSurface)),
            ],
          ),
          const SizedBox(height: _kHeaderGap),
          if (visible.isEmpty)
            Text('Brak zadań w tym i przyszłym tygodniu', style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant))
          else
          // Hug height: IntrinsicHeight + Row + SizedBox(height: double.infinity) w kartach
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              physics: const ClampingScrollPhysics(),
              child: IntrinsicHeight(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    ...List.generate(visible.length, (i) {
                      final task = visible[i];
                      return Padding(
                        padding: EdgeInsets.only(right: i == visible.length - 1 ? 0 : 12),
                        child: ConstrainedBox(
                          constraints: const BoxConstraints.tightFor(width: _kCardWidth),
                          child: SizedBox(
                            height: double.infinity, // wyrównanie wysokości do najwyższej karty
                            child: TaskCard(
                              title: task.title,
                              deadline: task.deadline,
                              subject: task.subject,
                              type: task.type,
                              showAvatar: true,
                              onTap: () => onTap(task),
                            ),
                          ),
                        ),
                      );
                    }),
                    const SizedBox(width: 16),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}