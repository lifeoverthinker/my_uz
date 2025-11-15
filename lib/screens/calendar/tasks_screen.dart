// Plik: lib/screens/calendar/tasks_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:provider/provider.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';

class TasksScreen extends StatefulWidget {
  final bool showAppBar;
  const TasksScreen({super.key, this.showAppBar = true});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

// 1. Dodano 'with TickerProviderStateMixin' dla TabBar
class _TasksScreenState extends State<TasksScreen> with TickerProviderStateMixin {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TasksProvider>().refresh();
    });
  }

  Future<void> _openDetails(TaskModel task) async {
    if (!mounted) return;
    await context.read<TasksProvider>().openTaskDetails(context, task);
  }

  PreferredSizeWidget? _buildAppBar(BuildContext tabContext) {
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
            if (mounted) {
              await context.read<TasksProvider>().showAddTaskSheet(context);
            }
          },
        ),
        IconButton(icon: const Icon(MyUz.dots_vertical, color: Color(0xFF1D1B20)), onPressed: () {}),
        const SizedBox(width: 6),
      ],
      // 2. Dodano TabBar
      bottom: TabBar(
        controller: DefaultTabController.of(tabContext),
        tabs: const [
          Tab(text: 'Aktywne'),
          Tab(text: 'Zakończone'),
        ],
        labelColor: AppColors.myUZSysLightPrimary,
        unselectedLabelColor: AppColors.myUZSysLightOnSurfaceVariant,
        indicatorColor: AppColors.myUZSysLightPrimary,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TasksProvider>();

    // 3. Podział zadań na dwie listy
    final allTasksWithDesc = provider.items;
    final activeTasks = allTasksWithDesc.where((t) => !t.model.completed).map((t) => t.model).toList();
    final completedTasks = allTasksWithDesc.where((t) => t.model.completed).map((t) => t.model).toList();

    // 4. Zastosowanie DefaultTabController
    return DefaultTabController(
      length: 2, // Dwie zakładki
      child: Builder(
          builder: (tabContext) {
            return Scaffold(
              backgroundColor: Colors.white,
              appBar: _buildAppBar(tabContext),
              body: provider.loading
                  ? const Center(child: CircularProgressIndicator())
                  : TabBarView(
                controller: DefaultTabController.of(tabContext),
                children: [
                  // Zakładka "Aktywne"
                  _TaskList(
                    tasks: activeTasks,
                    onTap: _openDetails,
                    emptyMessage: 'Brak aktywnych zadań',
                  ),
                  // Zakładka "Zakończone"
                  _TaskList(
                    tasks: completedTasks,
                    onTap: _openDetails,
                    emptyMessage: 'Brak zakończonych zadań',
                  ),
                ],
              ),
            );
          }
      ),
    );
  }
}

/// 5. Reużywalna lista zadań, aby uniknąć duplikacji kodu
class _TaskList extends StatelessWidget {
  final List<TaskModel> tasks;
  final ValueChanged<TaskModel> onTap;
  final String emptyMessage;

  const _TaskList({
    required this.tasks,
    required this.onTap,
    required this.emptyMessage,
  });

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

  Widget _groupRow(BuildContext context, DateTime date, List<TaskModel> tasks) {
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
                      backgroundColor: AppColors.myUZSysLightPrimaryContainer,
                      onTap: () => onTap(t),
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

  @override
  Widget build(BuildContext context) {
    if (tasks.isEmpty) {
      return Center(child: Text(emptyMessage, style: AppTextStyle.myUZBodySmall));
    }

    final grouped = _groupByDay(tasks);

    return ListView(
      padding: const EdgeInsets.only(top: 12, bottom: 24),
      children: [
        for (final entry in grouped.entries)
          _groupRow(context, entry.key, entry.value),
      ],
    );
  }
}