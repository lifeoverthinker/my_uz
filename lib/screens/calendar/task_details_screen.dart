import 'package:flutter/material.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/screens/home/details/task_edit_sheet.dart';
import 'package:my_uz/widgets/confirm_modal.dart';

class TaskDetailsScreen extends StatefulWidget {
  final TaskModel task;
  final String? description;
  final void Function(TaskModel updated, String? description)? onEdit;
  final ValueChanged<bool>? onToggleCompleted;
  final VoidCallback? onDelete;

  const TaskDetailsScreen({
    super.key,
    required this.task,
    this.description,
    this.onEdit,
    this.onToggleCompleted,
    this.onDelete,
  });

  @override
  State<TaskDetailsScreen> createState() => _TaskDetailsScreenState();
}

class _TaskDetailsScreenState extends State<TaskDetailsScreen> {
  late TaskModel _model;
  String? _desc;

  @override
  void initState() {
    super.initState();
    _model = widget.task;
    _desc = widget.description;
  }

  String _plLongDate(DateTime d) {
    const dni = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela'];
    const mies = ['stycznia','lutego','marca','kwietnia','maja','czerwca','lipca','sierpnia','września','października','listopada','grudnia'];
    return '${dni[d.weekday - 1]}, ${d.day} ${mies[d.month - 1]} ${d.year}';
  }

  Future<void> _toggleCompleted(bool v) async {
    setState(() => _model = _model.copyWith(completed: v));
    await TasksProvider.instance.setTaskCompleted(_model.id, v);
    widget.onToggleCompleted?.call(v);
  }

  Future<void> _edit() async {
    TaskModel? edited;
    String? editedDesc;
    await TaskEditSheet.show(
      context,
      _model,
      initialDate: _model.deadline,
      initialTitle: _model.title,
      initialDescription: _desc,
      onSave: (m) => edited = m.copyWith(id: _model.id),
      onSaveDescription: (d) => editedDesc = d,
    );
    if (edited != null) {
      // Use provider to persist and refresh centralized state
      await TasksProvider.instance.editTask(TaskWithDescription(edited!.copyWith(id: _model.id), editedDesc));
      setState(() { _model = edited!.copyWith(id: _model.id); _desc = editedDesc; });
      widget.onEdit?.call(_model, _desc);
    }
  }

  Future<void> _delete() async {
    final ok = await ConfirmModal.show(
      context,
      title: 'Usunąć to zadanie?',
      confirmText: 'Usuń',
      cancelText: 'Anuluj',
    );
    if (ok == true) {
      await TasksProvider.instance.deleteTask(_model.id);
      widget.onDelete?.call();
      if (mounted) Navigator.of(context).maybePop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Szczegóły zadania'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_outlined),
            tooltip: 'Edytuj',
            onPressed: _edit,
          ),
          PopupMenuButton<String>(
            onSelected: (v) { if (v == 'delete') _delete(); },
            itemBuilder: (_) => const [
              PopupMenuItem(value: 'delete', child: Text('Usuń zadanie')),
            ],
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          // Tytuł + status
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  _model.title,
                  style: AppTextStyle.myUZTitleLarge.copyWith(
                    fontWeight: FontWeight.w600,
                    color: cs.onSurface,
                    decoration: _model.completed ? TextDecoration.lineThrough : null,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('Zaliczone'),
                  Switch(value: _model.completed, onChanged: _toggleCompleted),
                ],
              ),
            ],
          ),
          const SizedBox(height: 16),
          _InfoTile(icon: Icons.event, label: 'Termin', value: _plLongDate(_model.deadline)),
          const SizedBox(height: 8),
          _InfoTile(icon: Icons.school, label: 'Przedmiot', value: _model.subject),
          if ((_model.type ?? '').isNotEmpty) ...[
            const SizedBox(height: 8),
            _InfoTile(icon: Icons.category_outlined, label: 'Typ', value: _model.type ?? ''),
          ],
          const SizedBox(height: 16),
          const Divider(),
          const SizedBox(height: 16),
          Text('Opis', style: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onSurfaceVariant)),
          const SizedBox(height: 8),
          if ((_desc ?? '').isNotEmpty)
            Text(
              _desc!,
              style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface),
            )
          else
            Text('Brak opisu', style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurfaceVariant)),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
          child: Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: _delete,
                  style: OutlinedButton.styleFrom(foregroundColor: cs.error),
                  child: const Text('Usuń'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton(
                  onPressed: _edit,
                  style: ElevatedButton.styleFrom(backgroundColor: cs.primary, foregroundColor: cs.onPrimary),
                  child: const Text('Edytuj'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _InfoTile({required this.icon, required this.label, required this.value});
  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 20, color: AppColors.myUZSysLightPrimary),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onSurfaceVariant)),
              const SizedBox(height: 4),
              Text(value, style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface)),
            ],
          ),
        ),
      ],
    );
  }
}
