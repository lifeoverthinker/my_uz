// Plik: lib/screens/home/components/tasks_section.dart
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart'; // POPRAWKA: Przywrócono import ikon
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
// POPRAWKA: Nie używamy już SectionHeader, więc import jest zbędny
// import 'package:my_uz/widgets/section_header.dart';
import 'package:my_uz/utils/date_utils.dart';

/// Sekcja "Zadania" – wariant „hug height”
class TasksSection extends StatelessWidget {
  final List<TaskModel> tasks;
  final EdgeInsetsGeometry padding;
  final void Function(TaskModel task) onTap;
  final VoidCallback onGoToTasks;

  const TasksSection({
    super.key,
    required this.tasks,
    this.padding = const EdgeInsets.symmetric(horizontal: 16),
    required this.onTap,
    required this.onGoToTasks,
  });

  static const double _kCardWidth = 264;
  // POPRAWKA: Używamy stałej wysokości, tak jak w UpcomingClassesSection
  static const double _kListHeight = 68;
  static const double _kHeaderGap = 12;

  //
  // Logika filtrowania (bez zmian)
  //
  static DateTime _monday(DateTime d) {
    final wd = d.weekday;
    return stripTime(d).subtract(Duration(days: wd - 1));
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
          // POPRAWKA: Użyto niestandardowego Row (jak w UpcomingClasses)
          // aby przywrócić ikonę i usunąć przycisk "Więcej".
          Row(children: [
            Icon(MyUz.book_open_01, size: 20, color: cs.onSurface),
            const SizedBox(width: 8),
            Text(
                'Zadania',
                style: AppTextStyle.myUZTitleMedium.copyWith(fontSize: 18, height: 1.33, fontWeight: FontWeight.w500, color: cs.onSurface)
            ),
            const Spacer(),
            // Przycisk "Więcej" usunięty zgodnie z prośbą
          ]),
          const SizedBox(height: _kHeaderGap),
          // POPRAWKA: Użyto SizedBox o stałej wysokości
          SizedBox(
            height: _kListHeight,
            child: visible.isEmpty
                ? Align( // Wyrównanie do lewej, aby pasowało do list
              alignment: Alignment.centerLeft,
              child: Text(
                'Brak zadań w tym i przyszłym tygodniu',
                style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
              ),
            )
                : SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              // POPRAWKA: Usunięto IntrinsicHeight
              // Lista kart będzie się rozciągać do wysokości SizedBox (68)
              physics: const ClampingScrollPhysics(),
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
                          height: double.infinity,
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
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}