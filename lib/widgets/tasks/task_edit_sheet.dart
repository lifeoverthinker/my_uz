// Plik: lib/widgets/tasks/task_edit_sheet.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/rz_dictionary.dart';
import 'package:my_uz/theme/text_style.dart';

import 'package:my_uz/widgets/date_picker.dart';
import 'package:my_uz/utils/constants.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';

class TaskEditSheetContent extends StatefulWidget {
  final TaskModel? initial;
  final DateTime? initialDate;
  final String? initialDescription;
  final VoidCallback? onClose;
  final Function(TaskModel, String?)? onSave;

  const TaskEditSheetContent({
    super.key,
    this.initial,
    this.initialDate,
    this.initialDescription,
    this.onClose,
    this.onSave,
  });

  @override
  TaskEditSheetContentState createState() => TaskEditSheetContentState();
}

class TaskEditSheetContentState extends State<TaskEditSheetContent> {
  late TextEditingController _titleCtrl;
  late TextEditingController _descCtrl;
  late String _selectedSubject;
  late String _selectedType;
  late DateTime _selectedDeadline;

  List<String> _availableTypes = [];
  Future<List<String>>? _subjectsFuture;

  @override
  void initState() {
    super.initState();
    _titleCtrl = TextEditingController(text: widget.initial?.title ?? '');
    _descCtrl = TextEditingController(text: widget.initialDescription ?? '');
    _selectedSubject = widget.initial?.subject ?? '';
    _selectedType = widget.initial?.type ?? '';
    _selectedDeadline = widget.initialDate ?? widget.initial?.deadline ?? DateTime.now();

    _subjectsFuture = ClassesRepository.getSubjectsForDefaultGroup();
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

  Future<void> publicHandleSave() async {
    if (_titleCtrl.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Tytuł nie może być pusty')),
      );
      return;
    }

    try {
      final task = TaskModel(
        id: widget.initial?.id ?? DateTime.now().millisecondsSinceEpoch.toString(),
        title: _titleCtrl.text.trim(),
        deadline: _selectedDeadline,
        subject: _selectedSubject,
        type: _selectedType,
        completed: widget.initial?.completed ?? false,
      );

      final description = _descCtrl.text.trim();

      if (widget.onSave != null) {
        widget.onSave!(task, description);
      } else if (mounted) {
        Navigator.of(context).pop({
          'task': task,
          'description': description,
        });
      }
    } finally {
      // (nic do zrobienia)
    }
  }

  Future<void> _fetchTypesForSubject(String subject) async {
    if (subject.isEmpty) {
      if (mounted) setState(() => _availableTypes = []);
      return;
    }
    final types = await ClassesRepository.getTypesForSubjectInDefaultGroup(subject);
    if (mounted) {
      setState(() {
        _availableTypes = types;
        if (!_availableTypes.contains(_selectedType)) {
          _selectedType = '';
        }
      });
    }
  }

  Future<void> _showDateTimePicker(BuildContext context) async {
    final selected = await ModalDatePicker.show(
      context,
      initialDate: _selectedDeadline,
      firstDate: DateTime(2024),
      lastDate: DateTime(2026),
    );
    if (selected != null) {
      if (!mounted) return;
      setState(() => _selectedDeadline =
          DateTime(selected.year, selected.month, selected.day));
    }
  }

  /// Klikalny wiersz (zastępuje _EditRow)
  Widget _ClickableRow({
    required IconData icon,
    required Widget child,
    VoidCallback? onTap,
  }) {
    final cs = Theme.of(context).colorScheme;
    return InkWell(
      onTap: onTap,
      child: Padding(
        // 1. POPRAWKA: Dodano padding horyzontalny do wiersza
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 16.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center, // Centrowanie
          children: [
            AdaptiveIconSlot(
              iconSize: 20,
              child: Icon(icon, size: 20, color: cs.onSurfaceVariant),
            ),
            const SizedBox(width: kIconToTextGap),
            Expanded(child: child),
          ],
        ),
      ),
    );
  }

  /// 2. POPRAWKA: Nowy, połączony wiersz dla Przedmiotu i Typu
  Widget _buildCombinedSubjectTypeField(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final textStyle = AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurface);

    final canSelectType = _selectedSubject.isNotEmpty;
    final String typeLabel;
    if (_selectedSubject.isEmpty) {
      typeLabel = 'Najpierw wybierz przedmiot';
    } else if (_selectedType.isNotEmpty) {
      typeLabel = RzDictionary.getDescription(_selectedType);
    } else {
      typeLabel = 'Wybierz typ (opcjonalnie)';
    }

    return Padding(
      // 3. POPRAWKA: Dodano padding horyzontalny
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          AdaptiveIconSlot(
            iconSize: 20,
            child: Padding(
              padding: const EdgeInsets.only(top: 12.0), // Wyrównaj ikonę z pierwszym polem
              child: Icon(MyUz.book_open_01, size: 20, color: cs.onSurfaceVariant),
            ),
          ),
          const SizedBox(width: kIconToTextGap),
          Expanded(
            child: Column(
              children: [
                // --- Wybór Przedmiotu ---
                FutureBuilder<List<String>>(
                  future: _subjectsFuture,
                  builder: (context, snapshot) {
                    final subjects = snapshot.data ?? [];
                    final hasSubjects = subjects.isNotEmpty;
                    final currentLabel = _selectedSubject.isNotEmpty ? _selectedSubject : 'Wybierz przedmiot';

                    return DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        value: _selectedSubject.isNotEmpty ? _selectedSubject : null,
                        isExpanded: true,
                        hint: Text(
                          currentLabel,
                          style: textStyle.copyWith(color: cs.onSurfaceVariant),
                        ),
                        icon: const SizedBox.shrink(),
                        items: subjects.map((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(value, style: textStyle),
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
                    );
                  },
                ),

                // --- Wybór Typu (dynamicznie) ---
                // Pojawia się tylko po wybraniu przedmiotu
                if (canSelectType)
                  DropdownButtonHideUnderline(
                    child: DropdownButton<String>(
                      value: _selectedType.isNotEmpty ? _selectedType : null,
                      isExpanded: true,
                      hint: Text(
                        typeLabel,
                        style: textStyle.copyWith(color: cs.onSurfaceVariant),
                      ),
                      icon: const SizedBox.shrink(),
                      items: _availableTypes.map((String abbr) {
                        return DropdownMenuItem<String>(
                          value: abbr,
                          child: Text(RzDictionary.getDescription(abbr), style: textStyle),
                        );
                      }).toList(),
                      onChanged: canSelectType ? (String? newValue) {
                        if (newValue != null) {
                          setState(() => _selectedType = newValue);
                        }
                      } : null,
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }


  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // --- POLE TYTUŁU ---
          Padding(
            // 4. POPRAWKA: Dodano padding horyzontalny
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const AdaptiveIconSlot(iconSize: 24, child: SizedBox()),
                const SizedBox(width: kIconToTextGap),
                Expanded(
                  child: TextField(
                    controller: _titleCtrl,
                    maxLines: null,
                    autofocus: true,
                    style: AppTextStyle.myUZHeadlineMedium.copyWith(
                      fontWeight: FontWeight.w600,
                      color: cs.onSurface,
                    ),
                    decoration: InputDecoration(
                      hintText: 'Dodaj tytuł',
                      border: InputBorder.none,
                      hintStyle: AppTextStyle.myUZHeadlineMedium.copyWith(
                        fontWeight: FontWeight.w600,
                        color: cs.onSurfaceVariant.withOpacity(0.7),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),

          const Divider(height: 1, thickness: 1), // --- SEPARATOR ---

          // --- GRUPA: DATA ---
          _ClickableRow(
            icon: MyUz.calendar,
            onTap: () => _showDateTimePicker(context),
            child: Text(
              // 5. POPRAWKA: Format daty
              DateFormat('E, d MMM y', 'pl_PL').format(_selectedDeadline),
              style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurface),
            ),
          ),

          const Divider(height: 1, thickness: 1), // --- SEPARATOR ---

          // --- GRUPA: PRZEDMIOT I TYP ---
          _buildCombinedSubjectTypeField(context),

          const Divider(height: 1, thickness: 1), // --- SEPARATOR ---

          // --- GRUPA: OPIS ---
          Padding(
            // 6. POPRAWKA: Dodano padding horyzontalny
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Row(
              // 7. POPRAWKA: Wyrównanie ikonki opisu
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                AdaptiveIconSlot(
                  iconSize: 20,
                  child: Padding(
                    padding: const EdgeInsets.all(0),
                    child: Icon(Icons.notes, size: 20, color: cs.onSurfaceVariant),
                  ),
                ),
                const SizedBox(width: kIconToTextGap),
                Expanded(
                  child: TextField(
                    controller: _descCtrl,
                    maxLines: null,
                    minLines: 3,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurface),
                    decoration: InputDecoration(
                      hintText: 'Dodaj opis',
                      border: InputBorder.none,
                      hintStyle: AppTextStyle.myUZBodySmall.copyWith(
                        color: cs.onSurfaceVariant,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}