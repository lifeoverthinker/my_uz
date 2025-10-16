import 'package:flutter/material.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/screens/home/details/task_edit_sheet.dart';
import 'package:my_uz/screens/calendar/task_details_screen.dart';
import 'package:my_uz/services/local_user_store.dart';

enum _TaskFilter { all, today, upcoming, completed }

class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  bool _loading = true;
  List<TaskWithDescription> _items = const [];
  _TaskFilter _filter = _TaskFilter.all;

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  Future<void> _refresh() async {
    setState(() => _loading = true);
    final list = await LocalUserStore.loadTaskEntries();
    list.sort((a, b) {
      final ad = a.model.deadline;
      final bd = b.model.deadline;
      final cmp = ad.compareTo(bd);
      if (cmp != 0) return cmp;
      // nieukończone wyżej
      if (a.model.completed != b.model.completed) return a.model.completed ? 1 : -1;
      return a.model.title.toLowerCase().compareTo(b.model.title.toLowerCase());
    });
    if (mounted) setState(() { _items = list; _loading = false; });
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
      await LocalUserStore.upsertTask(pendingModel!, description: pendingDesc);
      await _refresh();
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
      await LocalUserStore.upsertTask(editedModel!, description: editedDesc);
      await _refresh();
    }
  }

  Future<void> _deleteTask(TaskModel model) async {
    await LocalUserStore.deleteTask(model.id);
    await _refresh();
  }

  Future<void> _toggleCompleted(TaskModel model, bool completed) async {
    await LocalUserStore.setTaskCompleted(model.id, completed);
    await _refresh();
  }

  Iterable<(DateTime, List<TaskWithDescription>)> _groupedFiltered() {
    final now = DateTime.now();
    bool isSameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

    Iterable<TaskWithDescription> filtered;
    switch (_filter) {
      case _TaskFilter.today:
        filtered = _items.where((e) => isSameDay(e.model.deadline, now));
        break;
      case _TaskFilter.upcoming:
        final todayStart = DateTime(now.year, now.month, now.day);
        filtered = _items.where((e) => !e.model.completed && e.model.deadline.isAfter(todayStart.subtract(const Duration(seconds: 1))));
        break;
      case _TaskFilter.completed:
        filtered = _items.where((e) => e.model.completed);
        break;
      case _TaskFilter.all:
        filtered = _items;
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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Terminarz zadań'),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addTask,
        backgroundColor: AppColors.myUZSysLightPrimary,
        foregroundColor: Colors.white,
        child: const Icon(Icons.add),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _items.isEmpty
              ? _EmptyState(onAdd: _addTask)
              : RefreshIndicator(
                  onRefresh: _refresh,
                  child: ListView(
                    padding: const EdgeInsets.only(bottom: 96),
                    children: [
                      const SizedBox(height: 8),
                      _Filters(
                        value: _filter,
                        onChanged: (f) => setState(() => _filter = f),
                      ),
                      const SizedBox(height: 8),
                      for (final (day, list) in _groupedFiltered()) ...[
                        _DayHeader(label: _formatHeader(day)),
                        const SizedBox(height: 8),
                        ...list.map((it) => _TaskListItem(
                              model: it.model,
                              description: it.description,
                              onOpen: () async {
                                await Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (_) => TaskDetailsScreen(
                                      task: it.model,
                                      description: it.description,
                                      onEdit: (m, d) => _editTask(m, d),
                                      onToggleCompleted: (v) => _toggleCompleted(it.model, v),
                                      onDelete: () => _deleteTask(it.model),
                                    ),
                                  ),
                                );
                                await _refresh();
                              },
                              onEdit: () => _editTask(it.model, it.description),
                              onToggleCompleted: (v) => _toggleCompleted(it.model, v),
                              onDelete: () => _deleteTask(it.model),
                            )),
                        const SizedBox(height: 8),
                      ],
                    ],
                  ),
                ),
    );
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
  final VoidCallback onAdd;
  const _EmptyState({required this.onAdd});
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
            Text('Dodaj pierwsze zadanie, aby zacząć zarządzać terminami.',
                textAlign: TextAlign.center,
                style: AppTextStyle.myUZBodySmall.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant)),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: onAdd, child: const Text('Dodaj zadanie')),
          ],
        ),
      ),
    );
  }
}
