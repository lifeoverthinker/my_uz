import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:my_uz/services/user_tasks_repository.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/widgets/tasks/task_details.dart';
import 'package:my_uz/widgets/tasks/task_edit_sheet.dart';

class TasksScreen extends StatefulWidget {
  final bool showAppBar;
  const TasksScreen({super.key, this.showAppBar = true});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  bool _loading = true;
  bool _openingSheet = false; // guard
  List<TaskModel> _tasks = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final tasks = await UserTasksRepository.instance.fetchUserTasks();
      if (!mounted) return;
      setState(() {
        _tasks = tasks;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _tasks = const [];
        _loading = false;
      });
    }
  }

  Future<void> _openDetails(TaskModel task) async {
    final desc = await UserTasksRepository.instance.getTaskDescription(task.id) ?? '';
    if (!mounted) return;
    // TaskDetailsSheet.show expects positional (context, task, ...)
    await TaskDetailsSheet.show(
      context,
      task,
      description: desc,
      onDelete: () async {
        await UserTasksRepository.instance.deleteTask(task.id);
        if (!mounted) return;
        setState(() => _tasks.removeWhere((t) => t.id == task.id));
      },
      onToggleCompleted: (completed) async {
        await UserTasksRepository.instance.setTaskCompleted(task.id, completed);
        if (!mounted) return;
        setState(() {
          final idx = _tasks.indexWhere((t) => t.id == task.id);
          if (idx != -1) _tasks[idx] = _tasks[idx].copyWith(completed: completed);
        });
      },
      onSaveEdit: (updated) async {
        final saved = await UserTasksRepository.instance.upsertTask(updated);
        if (!mounted) return;
        setState(() {
          final idx = _tasks.indexWhere((t) => t.id == saved.id);
          if (idx != -1) {
            _tasks[idx] = saved;
          } else {
            _tasks.add(saved);
          }
        });
      },
    );
  }

  Map<DateTime, List<TaskModel>> _groupByDay(List<TaskModel> list) {
    final map = <DateTime, List<TaskModel>>{};
    for (final t in list) {
      final d = DateTime(t.deadline.year, t.deadline.month, t.deadline.day);
      map.putIfAbsent(d, () => []).add(t);
    }
    final sortedKeys = map.keys.toList()..sort();
    final out = <DateTime, List<TaskModel>>{};
    for (final k in sortedKeys) {
      final items = map[k]!..sort((a, b) => a.deadline.compareTo(b.deadline));
      out[k] = items;
    }
    return out;
  }

  Widget _dateBadge(DateTime date) {
    final month = DateFormat('LLL', 'pl').format(date);
    final day = DateFormat('d', 'pl').format(date);
    return SizedBox(
      width: 37,
      child: Column(
        mainAxisSize: MainAxisSize.min,
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
    );
  }

  Widget _groupRow(DateTime date, List<TaskModel> tasks) {
    final cs = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _dateBadge(date),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              children: [
                for (final t in tasks) ...[
                  Container(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: TaskCard(
                      title: t.title,
                      deadline: t.deadline,
                      subject: t.subject,
                      type: t.type,
                      showAvatar: false,
                      backgroundColor: cs.secondaryContainer,
                      onTap: () => _openDetails(t),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  PreferredSizeWidget? _buildAppBar() {
    if (!widget.showAppBar) return null;
    return AppBar(
      backgroundColor: Colors.white,
      elevation: 0,
      leading: Padding(
        padding: const EdgeInsets.only(left: 12),
        child: InkWell(
          borderRadius: BorderRadius.circular(100),
          onTap: () => Navigator.of(context).pop({'openDrawer': true}),
          child: Container(
            width: 48,
            height: 48,
            decoration: const BoxDecoration(shape: BoxShape.circle, color: Color(0xFFF7F2F9)),
            alignment: Alignment.center,
            child: const Icon(MyUz.menu_01, color: Color(0xFF1D1B20)),
          ),
        ),
      ),
      title: const Text('Terminarz', style: TextStyle(color: Color(0xFF1D1B20), fontWeight: FontWeight.w600)),
      centerTitle: false,
      actions: [
        IconButton(
          icon: const Icon(Icons.add, color: Color(0xFF1D1B20)),
          onPressed: () async {
            if (_openingSheet) return;
            _openingSheet = true;
            try {
              final saved = await TaskEditSheet.showWithOptions(
                context,
                initial: null,
                initialDate: DateTime.now(),
              );
              if (saved != null) {
                try {
                  await UserTasksRepository.instance.upsertTask(saved);
                  TasksProvider.instance.refresh();
                  _load();
                } catch (_) {}
              }
            } finally {
              _openingSheet = false;
            }
          },
        ),
        IconButton(icon: const Icon(MyUz.dots_vertical, color: Color(0xFF1D1B20)), onPressed: () {}),
        const SizedBox(width: 6),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final grouped = _groupByDay(_tasks);

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: _buildAppBar(),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : (_tasks.isEmpty
          ? Center(child: Text('Brak zadań', style: AppTextStyle.myUZBodySmall))
          : ListView(
        padding: const EdgeInsets.only(top: 12, bottom: 24),
        children: [
          for (final entry in grouped.entries) _groupRow(entry.key, entry.value),
        ],
      )),
    );
  }
}