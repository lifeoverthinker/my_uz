import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/widgets/cards/task_card.dart';

/// Lista zadań w Terminarzu z kółkiem daty po lewej:
/// - dla każdej daty (dzień) renderuje "wrz/paź" + kółko z numerem dnia
/// - obok grupy TaskCardów dla tej daty
class TasksSectionByDay extends StatelessWidget {
  final List<TaskModel> tasks;
  final EdgeInsetsGeometry padding;

  const TasksSectionByDay({super.key, required this.tasks, this.padding = const EdgeInsets.symmetric(horizontal: 16)});

  @override
  Widget build(BuildContext context) {
    final groups = _groupByDay(tasks);
    final cs = Theme.of(context).colorScheme;

    return Padding(
      padding: padding,
      child: Column(
        children: [
          for (final entry in groups.entries) ...[
            _DateWithTasks(date: entry.key, tasks: entry.value, cardColor: cs.secondaryContainer),
            const SizedBox(height: 16),
          ],
        ],
      ),
    );
  }

  static Map<DateTime, List<TaskModel>> _groupByDay(List<TaskModel> tasks) {
    final map = <DateTime, List<TaskModel>>{};
    for (final t in tasks) {
      final d = DateTime(t.deadline.year, t.deadline.month, t.deadline.day);
      map.putIfAbsent(d, () => []).add(t);
    }
    final sortedKeys = map.keys.toList()..sort();
    return {for (final k in sortedKeys) k: map[k]!..sort((a, b) => a.deadline.compareTo(b.deadline))};
  }
}

class _DateWithTasks extends StatelessWidget {
  final DateTime date;
  final List<TaskModel> tasks;
  final Color cardColor;

  const _DateWithTasks({required this.date, required this.tasks, required this.cardColor});

  @override
  Widget build(BuildContext context) {
    final month = DateFormat('LLL', 'pl').format(date); // wrz, paź
    final day = DateFormat('d', 'pl').format(date);

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 37,
          child: Column(
            children: [
              Text(
                month,
                style: const TextStyle(
                  color: Color(0xFF6750A4),
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  height: 1.33,
                  letterSpacing: 0.50,
                ),
              ),
              const SizedBox(height: 4),
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(color: const Color(0xFF6750A4), borderRadius: BorderRadius.circular(100)),
                alignment: Alignment.center,
                child: Text(
                  day,
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w500, height: 1),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            children: [
              for (final task in tasks) ...[
                Container(
                  width: double.infinity,
                  margin: const EdgeInsets.only(bottom: 8),
                  child: TaskCard(
                    title: task.title,
                    deadline: task.deadline,
                    subject: task.subject,
                    type: task.type,
                    showAvatar: false, // bez inicjału
                    backgroundColor: cardColor, // fioletowy (secondaryContainer)
                  ),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}