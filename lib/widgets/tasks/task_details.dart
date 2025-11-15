// Plik: lib/widgets/tasks/task_details.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/rz_dictionary.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/tasks/task_edit_sheet.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';
import 'package:my_uz/utils/constants.dart';

abstract class TaskDetailsSheet {
  static Future<void> show(
      BuildContext context,
      TaskModel task, {
        String? description,
        ClassModel? relatedClass,
        VoidCallback? onDelete,
        ValueChanged<bool>? onToggleCompleted,
        Function(TaskModel, String?)? onSaveEdit,
        bool startInEditMode = false,
      }) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (_) => _TaskDetailsDraggable(
        task: task,
        description: description,
        relatedClass: relatedClass,
        onDelete: onDelete,
        onToggleCompleted: onToggleCompleted,
        onSaveEdit: onSaveEdit,
        startInEditMode: startInEditMode,
      ),
    );
  }
}

class _TaskDetailsDraggable extends StatefulWidget {
  final TaskModel task;
  final String? description;
  final ClassModel? relatedClass;
  final VoidCallback? onDelete;
  final ValueChanged<bool>? onToggleCompleted;
  final Function(TaskModel, String?)? onSaveEdit;
  final bool startInEditMode;

  const _TaskDetailsDraggable({
    required this.task,
    this.description,
    this.relatedClass,
    this.onDelete,
    this.onToggleCompleted,
    this.onSaveEdit,
    this.startInEditMode = false,
  });

  @override
  State<_TaskDetailsDraggable> createState() => _TaskDetailsDraggableState();
}

class _TaskDetailsDraggableState extends State<_TaskDetailsDraggable> {
  late bool _isCompleted;
  bool _isEditing = false;

  final GlobalKey<TaskEditSheetContentState> _formKey =
  GlobalKey<TaskEditSheetContentState>();

  @override
  void initState() {
    super.initState();
    _isCompleted = widget.task.completed;
    _isEditing = widget.startInEditMode;
  }

  void _toggleCompleted() {
    final next = !_isCompleted;
    widget.onToggleCompleted?.call(next);
    if (mounted) setState(() => _isCompleted = next);
  }

  void _handleSave(TaskModel updatedTask, String? description) {
    widget.onSaveEdit?.call(updatedTask, description);
    if (mounted) {
      if (widget.startInEditMode) {
        Navigator.of(context).pop();
      } else {
        setState(() => _isEditing = false);
      }
    }
  }

  // 1. POPRAWKA: Przywrócenie metody _handleMore
  void _handleMore() async {
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
  }


  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: 0.40,
      initialChildSize: 1.0,
      maxChildSize: 1.0,
      builder: (context, scrollController) {
        return _SheetScaffoldBody(
          scrollController: scrollController,
          isEditing: _isEditing,
          onClose: () {
            if (widget.startInEditMode) {
              Navigator.of(context).pop();
            } else if (_isEditing) {
              setState(() => _isEditing = false);
            } else {
              Navigator.of(context).pop();
            }
          },
          // 2. POPRAWKA: Przekazanie onEdit i onMore
          onEdit: () => setState(() => _isEditing = true),
          onMore: _handleMore,
          onSave: () {
            _formKey.currentState?.publicHandleSave();
          },
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 300),
            transitionBuilder: (child, animation) {
              return FadeTransition(opacity: animation, child: child);
            },
            child: _isEditing
                ? TaskEditSheetContent(
              key: _formKey,
              initial: widget.task,
              initialDescription: widget.description,
              initialDate: widget.task.deadline,
              onSave: _handleSave,
              onClose: () => setState(() => _isEditing = false),
            )
                : _buildDetailsView(context),
          ),
        );
      },
    );
  }

  /// Widok samych detali
  Widget _buildDetailsView(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final deadlineStr =
    DateFormat('E, d MMM y', 'pl_PL').format(widget.task.deadline);
    final title = widget.task.title;
    final subjectRaw = widget.task.subject.trim();
    final typeRaw = (widget.task.type ?? '').trim();
    final subject = subjectRaw.isEmpty ? '—' : subjectRaw;
    final classKind =
    typeRaw.isEmpty ? '—' : RzDictionary.getDescription(typeRaw);

    return Container(
      key: const ValueKey('details'),
      // 3. POPRAWKA: Dodanie paddingu, ponieważ SingleChildScrollView jest full-width
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                AdaptiveIconSlot(
                  iconSize: 24,
                  child: InkWell(
                    onTap: _toggleCompleted,
                    borderRadius: BorderRadius.circular(6),
                    child: Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(6),
                        color: _isCompleted
                            ? AppColors.myUZSysLightPrimary
                            : AppColors.myUZSysLightPrimaryContainer,
                      ),
                      child: _isCompleted
                          ? const Icon(MyUz.check, size: 14, color: Colors.white)
                          : null,
                    ),
                  ),
                ),
                const SizedBox(width: kIconToTextGap),
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
            _DetailRow(
              label: 'Przedmiot',
              value: subject,
              icon: MyUz.book_open_01,
            ),
            const SizedBox(height: 12),
            _DetailRow(
              label: 'Rodaj zajęć',
              value: classKind,
              icon: MyUz.check_square_broken,
            ),
            const SizedBox(height: 16),
            if ((widget.description ?? '').trim().isNotEmpty)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: cs.outlineVariant.withOpacity(0.5),
                  ),
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

/// 4. POPRAWKA: Przebudowa _SheetScaffoldBody
class _SheetScaffoldBody extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  final bool isEditing;
  final VoidCallback onClose;
  final VoidCallback onSave;
  final VoidCallback onEdit;
  final VoidCallback onMore;

  const _SheetScaffoldBody({
    required this.child,
    required this.scrollController,
    required this.isEditing,
    required this.onClose,
    required this.onSave,
    required this.onEdit,
    required this.onMore,
  });

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    const handleTopGap = 8.0;
    const handleToXGap = 8.0;
    const xToHeaderGap = 12.0;

    return Container(
      margin: EdgeInsets.zero,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
        boxShadow: [
          BoxShadow(color: Color(0x4C000000), blurRadius: 3, offset: Offset(0, 1)),
          BoxShadow(
              color: Color(0x26000000),
              blurRadius: 8,
              offset: Offset(0, 4),
              spreadRadius: 3),
        ],
      ),
      // 5. POPRAWKA: Usunięto Padding, aby scroll view był full-width
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Padding tylko dla GripHandle
          Padding(
            padding: EdgeInsets.only(top: topPadding + handleTopGap),
            child: const GripHandle(),
          ),
          const SizedBox(height: handleToXGap),
          // Padding tylko dla AppBar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: horizontal),
            child: Row(
              children: [
                AdaptiveIconSlot(
                  iconSize: 24,
                  semanticsLabel: 'Zamknij',
                  isButton: true,
                  onTap: onClose,
                  child: const Icon(MyUz.x_close,
                      size: 24, color: Color(0xFF1D192B)),
                ),
                const Spacer(),

                if (isEditing)
                  TextButton(
                    style: TextButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      shape: const StadiumBorder(), // Bardziej okrągły
                      backgroundColor: AppColors.myUZSysLightPrimary,
                      foregroundColor: Colors.white,
                    ),
                    onPressed: onSave,
                    child: const Text('Zapisz'),
                  )
                else ...[
                  AdaptiveIconSlot(
                    iconSize: 24,
                    semanticsLabel: 'Edytuj',
                    isButton: true,
                    onTap: onEdit,
                    child: const Icon(MyUz.edit_05,
                        size: 24, color: Color(0xFF1D192B)),
                  ),
                  const SizedBox(width: 8),
                  AdaptiveIconSlot(
                    iconSize: 24,
                    semanticsLabel: 'Więcej',
                    isButton: true,
                    onTap: onMore,
                    child: const Icon(MyUz.dots_vertical,
                        size: 24, color: Color(0xFF1D192B)),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: xToHeaderGap),
          // 6. POPRAWKA: Expanded i SingleChildScrollView są teraz na zewnątrz
          // i NIE MAJĄ paddingu, co pozwoli Dividerom być full-width
          Expanded(
            child: SingleChildScrollView(
              controller: scrollController,
              child: child, // Tu trafia AnimatedSwitcher
            ),
          ),
        ],
      ),
    );
  }
}

/// Lokalny widget wiersza detali
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
        AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: AppColors.myUZSysLightPrimary),
        ),
        const SizedBox(width: kIconToTextGap),
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