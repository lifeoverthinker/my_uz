// Plik: lib/screens/home/components/tasks_section.dart
import 'package:flutter/material.dart';
// import 'package:my_uz/icons/my_uz_icons.dart'; // <-- 1. USUNIĘTY NIEUŻYWANY IMPORT
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/widgets/section_header.dart';

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

  @override
  Widget build(BuildContext context) {
    if (tasks.isEmpty) {
      return const SizedBox.shrink();
    }

    // 2. USUNIĘTO .take(3) - teraz pokazuje wszystkie przefiltrowane zadania
    // final displayTasks = tasks.take(3).toList();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Column(
        children: [
          SectionHeader(
            title: 'Zadania',
            actions: [
              TextButton(
                onPressed: onGoToTasks,
                child: const Text('Zobacz wszystko'),
              )
            ],
          ),
          const SizedBox(height: 12),
          // 3. Użyto 'tasks' bezpośrednio
          for (final task in tasks)
            Container(
              margin: const EdgeInsets.only(bottom: 8),
              child: TaskCard(
                title: task.title,
                deadline: task.deadline,
                subject: task.subject,
                type: task.type,
                backgroundColor: AppColors.myUZSysLightPrimaryContainer,
                onTap: () => onTap(task),
              ),
            ),
        ],
      ),
    );
  }
}