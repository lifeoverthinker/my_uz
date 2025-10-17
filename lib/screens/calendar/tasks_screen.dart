import 'package:flutter/material.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/screens/home/details/task_edit_sheet.dart';
import 'package:my_uz/screens/calendar/task_details_screen.dart';
import 'package:my_uz/services/local_user_store.dart'; // keep for TaskWithDescription type
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:my_uz/providers/user_plan_provider.dart';

enum _TaskFilter { all, today, upcoming, completed }

class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key, this.showAppBar = true});

  final bool showAppBar;

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  final TasksProvider _provider = TasksProvider.instance;

  @override
  void initState() {
    super.initState();
    // initial load
    _provider.refresh();
  }

  Future<void> _addTask() async {
    TaskModel? pendingModel;
    String? pendingDesc;
    await TaskEditSheet.show(
      context,
      null,
      initialDate: DateTime.now(),
      onSave: (m) => pendingModel = m,
      onSaveDescription: (d) => pendingDesc = d,
    );
    if (pendingModel != null) {
      await _provider.addTask(TaskWithDescription(pendingModel!, pendingDesc));
    }
  }

  Future<void> _editTask(TaskModel model, String? desc) async {
    TaskModel? editedModel;
    String? editedDesc;
    await TaskEditSheet.show(
      context,
      model,
      initialDate: model.deadline,
      initialTitle: model.title,
      initialDescription: desc,
      onSave: (m) => editedModel = m.copyWith(id: model.id),
      onSaveDescription: (d) => editedDesc = d,
    );
    if (editedModel != null) {
      await _provider.editTask(TaskWithDescription(editedModel!, editedDesc));
    }
  }

  Future<void> _deleteTask(TaskModel model) async {
    await _provider.deleteTask(model.id);
  }

  Future<void> _toggleCompleted(TaskModel model, bool completed) async {
    await _provider.setTaskCompleted(model.id, completed);
  }

  Iterable<(DateTime, List<TaskWithDescription>)> _groupedFiltered(List<TaskWithDescription> items) {
    final now = DateTime.now();
    bool isSameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

    Iterable<TaskWithDescription> filtered;
    switch (_provider.filter) {
      case TaskFilter.today:
        filtered = items.where((e) => isSameDay(e.model.deadline, now));
        break;
      case TaskFilter.upcoming:
        final todayStart = DateTime(now.year, now.month, now.day);
        filtered = items.where((e) => !e.model.completed && e.model.deadline.isAfter(todayStart.subtract(const Duration(seconds: 1))));
        break;
      case TaskFilter.completed:
        filtered = items.where((e) => e.model.completed);
        break;
      case TaskFilter.all:
        filtered = items;
        break;
    }

    final Map<DateTime, List<TaskWithDescription>> map = {};
    for (final it in filtered) {
      final d = it.model.deadline;
      final key = DateTime(d.year, d.month, d.day);
      (map[key] ??= <TaskWithDescription>[]).add(it);
    }
    final entries = map.entries.toList()
      ..sort((a, b) => a.key.compareTo(b.key));
    return entries.map((e) => (e.key, e.value));
  }

  String _formatHeader(DateTime d) {
    // Zwięzły nagłówek po polsku
    final weekdayPl = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela'][d.weekday - 1];
    final monthPl = ['stycznia','lutego','marca','kwietnia','maja','czerwca','lipca','sierpnia','września','października','listopada','grudnia'][d.month - 1];
    return '$weekdayPl, ${d.day} $monthPl ${d.year}';
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _provider,
      builder: (context, _) {
        final loading = _provider.loading;
        final items = _provider.items;
        return Scaffold(
          appBar: widget.showAppBar
              ? AppBar(
                  title: const Text('Terminarz'),
                  actions: [
                    IconButton(
                      tooltip: 'Dodaj zadanie',
                      icon: const Icon(Icons.add),
                      onPressed: _addTask,
                    ),
                  ],
                )
              : null,
          body: loading
              ? const Center(child: CircularProgressIndicator())
              : items.isEmpty
                  ? _EmptyState()
                  : RefreshIndicator(
                      onRefresh: () => _provider.refresh(forceRefresh: true),
                      child: ListView(
                        padding: const EdgeInsets.only(bottom: 96),
                        children: [
                          const SizedBox(height: 8),
                          _Filters(
                            value: _provider.filter,
                            onChanged: (f) => setState(() => _provider.filter = f),
                          ),
                          const SizedBox(height: 8),
                          for (final (day, list) in _groupedFiltered(items)) ...[
                            // Render date column on the left and stacked cards on the right
                            Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 16),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  // Left date column (month short + circular day)
                                  SizedBox(
                                    width: 56,
                                    child: Column(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Text(
                                          _formatMonthShort(day.month),
                                          style: const TextStyle(
                                            color: Color(0xFF6750A4),
                                            fontSize: 12,
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                        const SizedBox(height: 6),
                                        Container(
                                          width: 28,
                                          height: 28,
                                          decoration: BoxDecoration(
                                            color: const Color(0xFF6750A4),
                                            borderRadius: BorderRadius.circular(100),
                                          ),
                                          alignment: Alignment.center,
                                          child: Text(day.day.toString(), style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w500, height: 1)),
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 12),

                                  // Right column: stacked task cards for this date
                                  Expanded(
                                    child: Column(
                                      children: [
                                        for (final it in list) ...[
                                          _GroupedTaskItem(
                                            model: it.model,
                                            description: it.description,
                                            onOpen: () async {
                                              await Navigator.of(context).push(
                                                MaterialPageRoute(
                                                  builder: (_) => TaskDetailsScreen(
                                                    task: it.model,
                                                    description: it.description,
                                                    onEdit: (m, d) => _provider.editTask(TaskWithDescription(m, d)),
                                                    onToggleCompleted: (v) => _provider.setTaskCompleted(it.model.id, v),
                                                    onDelete: () => _provider.deleteTask(it.model.id),
                                                  ),
                                                ),
                                              );
                                            },
                                            onEdit: () => _editTask(it.model, it.description),
                                            onToggleCompleted: (v) => _provider.setTaskCompleted(it.model.id, v),
                                            onDelete: () => _provider.deleteTask(it.model.id),
                                          ),
                                          const SizedBox(height: 8),
                                        ],
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(height: 8),
                          ],
                        ],
                      ),
                    ),
        );
      },
    );
  }

  String _formatMonthShort(int m) {
    const mies = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];
    return mies[m-1];
  }
}

class _DayHeader extends StatelessWidget {
  final String label;
  const _DayHeader({required this.label});
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
      child: Text(
        label,
        style: AppTextStyle.myUZTitleMedium.copyWith(
          fontSize: 18,
          height: 1.33,
          fontWeight: FontWeight.w600,
          color: Theme.of(context).colorScheme.onSurface,
        ),
      ),
    );
  }
}

class _TaskListItem extends StatelessWidget {
  final TaskModel model;
  final String? description;
  final VoidCallback onOpen;
  final VoidCallback onEdit;
  final ValueChanged<bool> onToggleCompleted;
  final VoidCallback onDelete;
  const _TaskListItem({
    required this.model,
    required this.description,
    required this.onOpen,
    required this.onEdit,
    required this.onToggleCompleted,
    required this.onDelete,
  });

  String _ddmm(DateTime d) => '${d.day.toString().padLeft(2, '0')}.${d.month.toString().padLeft(2, '0')}';

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final initials = ClassesRepository.initialsFromName(model.subject);
    final desc = 'Do: ${_ddmm(model.deadline)} • ${model.subject}${model.type != null ? ' • ${model.type}' : ''}';

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
      child: Dismissible(
        key: ValueKey(model.id),
        background: _swipeBackground(Colors.green, Icons.check, 'Zaliczone'),
        secondaryBackground: _swipeBackground(Colors.red, Icons.delete, 'Usuń'),
        confirmDismiss: (dir) async {
          if (dir == DismissDirection.startToEnd) {
            onToggleCompleted(!model.completed);
            return false; // nie usuwamy elementu
          } else {
            // usuwanie
            onDelete();
            return true;
          }
        },
        child: InkWell(
          onTap: onOpen,
          onLongPress: onEdit,
          borderRadius: BorderRadius.circular(8),
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
          child: Stack(
            children: [
              TaskCard(
                title: model.title,
                description: desc,
                initial: initials,
                backgroundColor: AppColors.myUZSysLightSecondaryContainer,
                avatarColor: cs.primary,
              ),
              Positioned(
                right: 8,
                top: 8,
                child: Checkbox(
                  value: model.completed,
                  onChanged: (v) => onToggleCompleted(v ?? false),
                  materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _swipeBackground(Color color, IconData icon, String label) {
    return Container(
      alignment: Alignment.centerLeft,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      color: color.withValues(alpha: 0.1),
      child: Row(
        children: [
          Icon(icon, color: color),
          const SizedBox(width: 8),
          Text(label, style: TextStyle(color: color, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}

class _GroupedTaskItem extends StatelessWidget {
  final TaskModel model;
  final String? description;
  final VoidCallback onOpen;
  final VoidCallback onEdit;
  final ValueChanged<bool> onToggleCompleted;
  final VoidCallback onDelete;
  const _GroupedTaskItem({required this.model, required this.description, required this.onOpen, required this.onEdit, required this.onToggleCompleted, required this.onDelete});

  String _ddmm(DateTime d) => '${d.day.toString().padLeft(2,'0')}.${d.month.toString().padLeft(2,'0')}';

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final initials = ClassesRepository.initialsFromName(model.subject);
    final desc = 'Do: ${_ddmm(model.deadline)} • ${model.subject}${model.type != null ? ' • ${model.type}' : ''}';

    return Dismissible(
      key: ValueKey(model.id),
      background: Container(alignment: Alignment.centerLeft, padding: const EdgeInsets.symmetric(horizontal:16), color: Colors.green.withValues(alpha:0.1), child: Row(children:[Icon(Icons.check, color: Colors.green), const SizedBox(width:8), Text('Zaliczone', style: TextStyle(color: Colors.green, fontWeight: FontWeight.w600))])),
      secondaryBackground: Container(alignment: Alignment.centerRight, padding: const EdgeInsets.symmetric(horizontal:16), color: Colors.red.withValues(alpha:0.1), child: Row(mainAxisAlignment: MainAxisAlignment.end, children:[Icon(Icons.delete, color: Colors.red), const SizedBox(width:8), Text('Usuń', style: TextStyle(color: Colors.red, fontWeight: FontWeight.w600))])),
      confirmDismiss: (dir) async {
        if (dir == DismissDirection.startToEnd) { onToggleCompleted(!model.completed); return false; } else { onDelete(); return true; }
      },
      child: InkWell(
        onTap: onOpen,
        onLongPress: onEdit,
        child: TaskCard(
          title: model.title,
          description: 'Do: ${_ddmm(model.deadline)} • ${model.subject}',
          initial: ClassesRepository.initialsFromName(model.subject),
          backgroundColor: AppColors.myUZSysLightSecondaryContainer,
          avatarColor: cs.primary,
          showAvatar: true,
        ),
      ),
    );
  }
}

class _Filters extends StatelessWidget {
  final _TaskFilter value;
  final ValueChanged<_TaskFilter> onChanged;
  const _Filters({required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final items = const [
      (_TaskFilter.all, 'Wszystkie'),
      (_TaskFilter.today, 'Dziś'),
      (_TaskFilter.upcoming, 'Nadchodzące'),
      (_TaskFilter.completed, 'Zakończone'),
    ];
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      child: Row(
        children: [
          for (final (f, label) in items) ...[
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: ChoiceChip(
                label: Text(label),
                selected: value == f,
                onSelected: (_) => onChanged(f),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState();
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.event_note, size: 64, color: Color(0xFF938F99)),
            const SizedBox(height: 16),
            Text('Brak zadań', style: AppTextStyle.myUZTitleMedium.copyWith(color: Theme.of(context).colorScheme.onSurface)),
            const SizedBox(height: 8),
            Text(
              'Dodaj pierwsze zadanie z przycisku + w prawym górnym rogu.',
              textAlign: TextAlign.center,
              style: AppTextStyle.myUZBodySmall.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}
