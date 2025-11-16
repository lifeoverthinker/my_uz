// Plik: lib/screens/home/components/tasks_section.dart
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/task_card.dart';

class TasksSection extends StatelessWidget {
  final List<TaskModel> tasks;
  final ValueChanged<TaskModel> onTap;
  final VoidCallback onGoToTasks;

  const TasksSection({
    super.key,
    required this.tasks,
    required this.onTap,
    required this.onGoToTasks,
  });

  static const double _kCardWidth = 264;
  static const double _kListHeight = 68;

  @override
  Widget build(BuildContext context) {
    if (tasks.isEmpty) {
      return const SizedBox.shrink();
    }

    final cs = Theme.of(context).colorScheme;

    // Ujednolicona struktura z UpcomingClassesSection
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start, // POPRAWKA: Wyrównanie do lewej (jak w UpcomingClasses)
        children: [
          // Ujednolicony nagłówek (Icon, Title, Spacer, Action)
          Row(
            children: [
              Icon(MyUz.book_open_01, size: 20, color: cs.onSurface),
              const SizedBox(width: 8),
              Text(
                'Zadania',
                style: AppTextStyle.myUZTitleMedium.copyWith(
                    fontSize: 18,
                    height: 1.33,
                    fontWeight: FontWeight.w500,
                    color: cs.onSurface),
              ),
              const Spacer(),
              TextButton(
                onPressed: onGoToTasks,
                child: const Text('Więcej'),
              )
            ],
          ),
          const SizedBox(height: 12),
          // Pozioma lista kart o stałej wysokości
          SizedBox(
            height: _kListHeight,
            child: ListView.builder(
              padding: EdgeInsets.zero,
              scrollDirection: Axis.horizontal,
              physics: const ClampingScrollPhysics(),
              itemCount: tasks.length + 1,
              itemBuilder: (context, i) {
                if (i == tasks.length) return const SizedBox(width: 16);
                final task = tasks[i];

                return Padding(
                  padding: EdgeInsets.only(right: i == tasks.length - 1 ? 0 : 8),
                  child: SizedBox(
                    width: _kCardWidth,
                    child: TaskCard(
                      title: task.title,
                      deadline: task.deadline,
                      subject: task.subject,
                      type: task.type,
                      backgroundColor: AppColors.myUZSysLightPrimaryContainer,
                      onTap: () => onTap(task),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}