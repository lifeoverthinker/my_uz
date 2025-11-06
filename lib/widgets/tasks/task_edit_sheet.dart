import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/rz_dictionary.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

import 'package:my_uz/widgets/date_picker.dart';
import 'package:my_uz/widgets/full_width_divider.dart';

/// Widżet "opakowujący" formularz w modal.
/// Używany do TWORZENIA nowych zadań.
class TaskEditSheet {
  static Future<TaskModel?> show(
      BuildContext context, {
        TaskModel? initial,
        DateTime? initialDate,
        String? initialDescription, // DODANE
      }) {
    return SheetScaffold.showAsModal(
      context,
      // Używa teraz publicznego widżetu formularza
      child: TaskEditSheetContent(
        initial: initial,
        initialDate: initialDate,
        initialDescription: initialDescription, // DODANE
        // W trybie modala, onSave jest null, więc _handleSave wywoła Navigator.pop
        onSave: null,
        // W trybie modala, onClose powinno zamknąć modal
        onClose: () => Navigator.pop(context),
      ),
    ).then((result) => result as TaskModel?);
  }

  static Future<TaskModel?> showWithOptions(
      BuildContext context, {
        TaskModel? initial,
        DateTime? initialDate,
        String? initialDescription, // DODANE
      }) =>
      show(
        context,
        initial: initial,
        initialDate: initialDate,
        initialDescription: initialDescription, // DODANE
      );
}

/// GŁÓWNY WIDŻET FORMULARZA (teraz publiczny)
/// Może być używany zarówno w modalu (TaskEditSheet)
/// jak i wewnątrz innego widżetu (TaskDetailsSheet).
class TaskEditSheetContent extends StatefulWidget {
  final TaskModel? initial;
  final DateTime? initialDate;
  final String? initialDescription;
  final VoidCallback? onClose; // Callback do zamknięcia (np. ikona 'X')
  final ValueChanged<TaskModel>? onSave; // Callback do zapisu

  const TaskEditSheetContent({
    super.key,
    this.initial,
    this.initialDate,
    this.initialDescription,
    this.onClose,
    this.onSave,
  });

  @override
  State<TaskEditSheetContent> createState() => _TaskEditSheetContentState();
}

class _TaskEditSheetContentState extends State<TaskEditSheetContent> {
  late TextEditingController _titleCtrl;
  late TextEditingController _descCtrl;
  late String _selectedSubject;
  late String _selectedType;
  late DateTime _selectedDeadline;
  bool _isSaving = false;

  List<String> _availableTypes = [];

  @override
  void initState() {
    super.initState();
    _titleCtrl = TextEditingController(text: widget.initial?.title ?? '');
    // POPRAWKA: Używa `initialDescription` zamiast `widget.initial.description`
    _descCtrl = TextEditingController(text: widget.initialDescription ?? '');
    _selectedSubject = widget.initial?.subject ?? '';
    _selectedType = widget.initial?.type ?? '';
    _selectedDeadline = widget.initialDate ?? widget.initial?.deadline ?? DateTime.now();

    if (_selectedSubject.isNotEmpty) {
      _fetchTypesForSubject(_selectedSubject);
    }
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (_titleCtrl.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Tytuł nie może być pusty')),
      );
      return;
    }

    setState(() => _isSaving = true);
    try {
      final task = TaskModel(
        id: widget.initial?.id ?? DateTime.now().millisecondsSinceEpoch.toString(),
        title: _titleCtrl.text.trim(),
        deadline: _selectedDeadline,
        subject: _selectedSubject,
        type: _selectedType,
        completed: widget.initial?.completed ?? false,
        // POPRAWKA: Usunięto pole 'description' z modelu
      );

      // ZMIENIONA LOGIKA:
      // Jeśli `onSave` jest dostarczony (tryb edycji w TaskDetails), wywołaj go.
      // W przeciwnym razie (tryb modala "Dodaj"), zamknij modal z wynikiem.
      if (widget.onSave != null) {
        widget.onSave!(task);
        // UWAGA: W tym scenariuszu opis _descCtrl.text.trim() NIE jest
        // przekazywany, ponieważ callback `onSave` (z `tasks_screen.dart`)
        // akceptuje tylko `TaskModel`.
      } else if (mounted) {
        Navigator.of(context).pop(task);
      }

    } finally {
      if (mounted) setState(() => _isSaving = false);
    }
  }

  Future<void> _fetchTypesForSubject(String subject) async {
    if (subject.isEmpty) {
      if (mounted) setState(() => _availableTypes = []);
      return;
    }
    // Używam RzDictionary jako mock, tak jak w oryginalnym kodzie
    final types = RzDictionary.allAbbreviations;
    if (mounted) {
      setState(() => _availableTypes = types);
    }
  }

  Future<void> _showDateTimePicker(BuildContext context) async {
    final selected = await ModalDatePicker.show(
      context,
      initialDate: _selectedDeadline,
      firstDate: DateTime(2024),
      lastDate: DateTime(2026),
    );
    if (selected != null && mounted) {
      final time = await showTimePicker(
        context: context,
        initialTime: TimeOfDay.fromDateTime(_selectedDeadline),
      );
      if (time != null) {
        setState(() => _selectedDeadline = DateTime(
          selected.year,
          selected.month,
          selected.day,
          time.hour,
          time.minute,
        ));
      } else {
        setState(() => _selectedDeadline = DateTime(
          selected.year,
          selected.month,
          selected.day,
          _selectedDeadline.hour,
          _selectedDeadline.minute,
        ));
      }
    }
  }

  Future<void> _showNotificationPicker() async {
    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Dodaj powiadomienie'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: const [
            Text('TODO: Opcje powiadomień'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Anuluj'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Gotowe'),
          ),
        ],
      ),
    );
  }

  Future<void> _showDescriptionEdit(BuildContext context) async {
    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Opis zadania'),
        contentPadding: const EdgeInsets.fromLTRB(24, 12, 24, 0),
        content: TextField(
          controller: _descCtrl,
          maxLines: 6,
          minLines: 4,
          autofocus: true,
          decoration: const InputDecoration(
            border: OutlineInputBorder(),
            hintText: 'Wpisz opis...',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(ctx);
              setState(() {});
            },
            child: const Text('Gotowe'),
          ),
        ],
      ),
    );
  }

  Widget _buildSubjectPickerField(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return FutureBuilder<List<String>>(
      future: ClassesRepository.getSubjectsForDefaultGroup(),
      builder: (context, snapshot) {
        final subjects = snapshot.data ?? [];
        final bool hasSubjects = subjects.isNotEmpty;
        final currentLabel = _selectedSubject.isNotEmpty ? _selectedSubject : 'Wybierz przedmiot';

        return Row(
          children: [
            Icon(MyUz.book_open_01, size: 20, color: cs.onSurfaceVariant),
            const SizedBox(width: 12),
            Expanded(
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  value: _selectedSubject.isNotEmpty ? _selectedSubject : null,
                  isExpanded: true,
                  hint: Text(
                    currentLabel,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurface),
                  ),
                  icon: Icon(Icons.chevron_right, size: 20, color: cs.onSurfaceVariant),
                  items: subjects.map((String value) {
                    return DropdownMenuItem<String>(
                      value: value,
                      child: Text(value, style: AppTextStyle.myUZBodySmall),
                    );
                  }).toList(),
                  onChanged: hasSubjects ? (String? newValue) {
                    if (newValue != null) {
                      setState(() {
                        _selectedSubject = newValue;
                        _selectedType = '';
                        _availableTypes = [];
                      });
                      _fetchTypesForSubject(newValue);
                    }
                  } : null,
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildTypePickerField(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final bool hasTypes = _availableTypes.isNotEmpty;
    final bool canSelectType = _selectedSubject.isNotEmpty && hasTypes;

    final String currentLabel;
    if (_selectedSubject.isEmpty) {
      currentLabel = 'Najpierw wybierz przedmiot';
    } else if (_selectedType.isNotEmpty) {
      currentLabel = RzDictionary.getDescription(_selectedType);
    } else {
      currentLabel = 'Wybierz typ';
    }

    return Row(
      children: [
        Icon(MyUz.check_square_broken, size: 20, color: cs.onSurfaceVariant),
        const SizedBox(width: 12),
        Expanded(
          child: DropdownButtonHideUnderline(
            child: DropdownButton<String>(
              value: _selectedType.isNotEmpty ? _selectedType : null,
              isExpanded: true,
              hint: Text(
                currentLabel,
                style: AppTextStyle.myUZBodySmall.copyWith(
                  color: canSelectType ? cs.onSurface : cs.onSurfaceVariant,
                ),
              ),
              icon: Icon(Icons.chevron_right, size: 20, color: cs.onSurfaceVariant),
              items: _availableTypes.map((String abbr) {
                return DropdownMenuItem<String>(
                  value: abbr,
                  child: Text(RzDictionary.getDescription(abbr), style: AppTextStyle.myUZBodySmall),
                );
              }).toList(),
              onChanged: canSelectType ? (String? newValue) {
                if (newValue != null) {
                  setState(() => _selectedType = newValue);
                }
              } : null,
            ),
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isEditing = widget.initial != null;

    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. Górny pasek (Zamknij, Tytuł, Zapisz)
          Padding(
            padding: const EdgeInsets.fromLTRB(4, 12, 16, 12),
            child: Row(
              children: [
                IconButton(
                  icon: Icon(MyUz.x_close, color: cs.onSurface),
                  tooltip: 'Anuluj',
                  // Używa callbacka onClose (jeśli istnieje), aby poprawnie
                  // zamknąć modal LUB przełączyć widok w TaskDetails
                  onPressed: widget.onClose,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    isEditing ? 'Edytuj zadanie' : 'Dodaj zadanie',
                    style: AppTextStyle.myUZTitleLarge.copyWith(
                      fontWeight: FontWeight.w500,
                      fontSize: 20,
                    ),
                  ),
                ),
                ElevatedButton(
                  onPressed: _isSaving ? null : _handleSave,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.myUZSysLightPrimary,
                  ),
                  child: Text(
                    'Zapisz',
                    style: AppTextStyle.myUZLabelLarge.copyWith(color: Colors.white),
                  ),
                ),
              ],
            ),
          ),

          // 2. Pole Tytułu (zawijane)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
            child: TextField(
              controller: _titleCtrl,
              maxLines: null,
              style: AppTextStyle.myUZHeadlineSmall,
              decoration: InputDecoration(
                hintText: 'Dodaj tytuł',
                border: InputBorder.none,
                hintStyle: AppTextStyle.myUZHeadlineSmall.copyWith(
                  color: cs.onSurfaceVariant,
                ),
              ),
            ),
          ),

          // 3. Divider
          const FullWidthDivider(),

          // 4. Pola edycji
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildSubjectPickerField(context),
                const SizedBox(height: 12),
                const FullWidthDivider(),
                const SizedBox(height: 16),

                _buildTypePickerField(context),
                const SizedBox(height: 12),
                const FullWidthDivider(),
                const SizedBox(height: 16),

                _EditableField(
                  icon: MyUz.calendar,
                  label: DateFormat('d MMM y, HH:mm', 'pl_PL').format(_selectedDeadline),
                  onTap: () => _showDateTimePicker(context),
                ),
                const SizedBox(height: 12),
                const FullWidthDivider(),
                const SizedBox(height: 16),

                _EditableField(
                  icon: Icons.notifications_outlined,
                  label: 'Dodaj powiadomienie',
                  onTap: _showNotificationPicker,
                ),
                const SizedBox(height: 12),
                const FullWidthDivider(),
                const SizedBox(height: 16),

                _EditableField(
                  icon: Icons.notes,
                  label: 'Dodaj opis',
                  subtitle: _descCtrl.text.trim().isNotEmpty
                      ? _descCtrl.text.trim()
                      : null,
                  onTap: () => _showDescriptionEdit(context),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Widżet pomocniczy (przeniesiony z poprzedniej wersji)
class _EditableField extends StatelessWidget {
  final IconData icon;
  final String label;
  final String? subtitle;
  final VoidCallback onTap;

  const _EditableField({
    required this.icon,
    required this.label,
    this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.only(top: 2.0),
            child: Icon(icon, size: 20, color: cs.onSurfaceVariant),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: AppTextStyle.myUZBodySmall.copyWith(
                    color: cs.onSurface,
                  ),
                ),
                if (subtitle != null && subtitle!.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text(
                    subtitle!,
                    style: AppTextStyle.myUZBodySmall.copyWith(
                      color: cs.onSurfaceVariant,
                    ),
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(top: 2.0),
            child: Icon(Icons.chevron_right, size: 20, color: cs.onSurfaceVariant),
          ),
        ],
      ),
    );
  }
}