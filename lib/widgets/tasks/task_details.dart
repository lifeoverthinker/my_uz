import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/rz_dictionary.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

class TaskDetailsSheet extends StatefulWidget {
  final TaskModel task;
  final String? description;
  final VoidCallback? onDelete;
  final ValueChanged<bool>? onToggleCompleted;
  final ValueChanged<TaskModel>? onSaveEdit;

  const TaskDetailsSheet({
    super.key,
    required this.task,
    this.description,
    this.onDelete,
    this.onToggleCompleted,
    this.onSaveEdit,
  });

  // relatedClass dodane tylko dla kompatybilności z wywołaniami (np. w HomeScreen)
  static Future show(
      BuildContext context,
      TaskModel task, {
        String? description,
        VoidCallback? onDelete,
        ValueChanged<bool>? onToggleCompleted,
        ValueChanged<TaskModel>? onSaveEdit,
        Object? relatedClass, // nieużywane
      }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: TaskDetailsSheet(
          task: task,
          description: description,
          onDelete: onDelete,
          onToggleCompleted: onToggleCompleted,
          onSaveEdit: onSaveEdit,
        ),
      ),
    );
  }

  @override
  State<TaskDetailsSheet> createState() => _TaskDetailsSheetState();
}

class _TaskDetailsSheetState extends State<TaskDetailsSheet> {
  late bool _isCompleted;

  @override
  void initState() {
    super.initState();
    _isCompleted = widget.task.completed;
  }

  void _toggleCompleted() {
    final next = !_isCompleted;
    widget.onToggleCompleted?.call(next);
    if (mounted) setState(() => _isCompleted = next);
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    // Data – format() zwraca nie-null String, więc bez zbędnego ??
    final deadlineStr =
    DateFormat('EEE, d MMM yyyy', 'pl_PL').format(widget.task.deadline);

    // Tytuł – non-null w modelu
    final title = widget.task.title;

    // Pola nullable normalizujemy do lokalnych Stringów
    final subjectRaw = (widget.task.subject ?? '').trim();
    final typeRaw = (widget.task.type ?? '').trim();

    final subject = subjectRaw.isEmpty ? '—' : subjectRaw;
    final classKind =
    typeRaw.isEmpty ? '—' : RzDictionary.getDescription(typeRaw);

    return SafeArea(
      top: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Góra: X po lewej, edycja i kropki po prawej
            Row(
              children: [
                IconButton(
                  icon: const Icon(MyUz.x_close, size: 20),
                  tooltip: 'Zamknij',
                  onPressed: () => Navigator.of(context).pop(),
                ),
                const Spacer(),
                // KLUCZOWE: callback, nie wynik wywołania (usuwa use_of_void_result)
                IconButton(
                  icon: const Icon(MyUz.edit_05, size: 20),
                  tooltip: 'Edytuj',
                  onPressed: () => widget.onSaveEdit?.call(widget.task),
                ),
                IconButton(
                  icon: const Icon(MyUz.dots_vertical, size: 20),
                  tooltip: 'Więcej',
                  onPressed: () async {
                    final selected = await showMenu<String>(
                      context: context,
                      position: const RelativeRect.fromLTRB(1000, 72, 16, 0),
                      items: const [
                        PopupMenuItem(
                          value: 'delete',
                          child: Text('Usuń'),
                        ),
                      ],
                    );
                    if (selected == 'delete') {
                      widget.onDelete?.call();
                    }
                  },
                ),
              ],
            ),

            const SizedBox(height: 8),

            // Kwadracik + tytuł + data (jak w class_details)
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                InkWell(
                  onTap: _toggleCompleted,
                  borderRadius: BorderRadius.circular(6),
                  child: Container(
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(
                        color: _isCompleted
                            ? AppColors.myUZSysLightPrimary
                            : cs.outlineVariant,
                        width: 2,
                      ),
                      color: _isCompleted
                          ? AppColors.myUZSysLightPrimary
                          : Colors.transparent,
                    ),
                    child: _isCompleted
                        ? const Icon(MyUz.check, size: 14, color: Colors.white)
                        : null,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        maxLines: 3,
                        overflow: TextOverflow.ellipsis,
                        style: AppTextStyle.myUZHeadlineMedium.copyWith(
                          fontWeight: FontWeight.w600,
                          color: cs.onSurface,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        deadlineStr,
                        style: AppTextStyle.myUZBodySmall.copyWith(
                          color: cs.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Przedmiot
            _DetailRow(
              label: 'Przedmiot',
              value: subject,
              icon: MyUz.book_open_01,
            ),
            const SizedBox(height: 12),

            // Rodzaj zajęć
            _DetailRow(
              label: 'Rodzaj zajęć',
              value: classKind,
              icon: MyUz.check_square_broken,
            ),

            const SizedBox(height: 16),

            // Opis (opcjonalny)
            if ((widget.description ?? '').trim().isNotEmpty)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: cs.surfaceVariant.withOpacity(0.3),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  widget.description!.trim(),
                  style: AppTextStyle.myUZBodySmall.copyWith(
                    color: cs.onSurface,
                    height: 1.5,
                  ),
                ),
              ),
            if ((widget.description ?? '').trim().isNotEmpty)
              const SizedBox(height: 8),

            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _DetailRow({
    required this.icon,
    required this.label,
    required this.value,
  });

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
              Text(
                label,
                style: AppTextStyle.myUZLabelSmall.copyWith(
                  color: cs.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                value,
                style: AppTextStyle.myUZBodySmall.copyWith(
                  color: cs.onSurface,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
