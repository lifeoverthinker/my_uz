import 'package:flutter/material.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/confirm_modal.dart';
import 'package:my_uz/widgets/date_picker.dart';
import 'package:my_uz/services/classes_repository.dart';

/// Arkusz edycji zadania w stylu Google Calendar / spójny z TaskDetailsSheet
class TaskEditSheet {
  static Future show(BuildContext context, TaskModel? task, {
    required DateTime initialDate,
    String? initialTitle,
    String? initialDescription,
    ValueChanged<TaskModel>? onSave,
    ValueChanged<String>? onSaveDescription,
  }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      useSafeArea: false,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (ctx) {
        final h = MediaQuery.of(ctx).size.height * 0.95;
        return SizedBox(
          height: h,
          child: _TaskEditSheetContent(
            task: task,
            initialDate: initialDate,
            initialTitle: initialTitle,
            initialDescription: initialDescription,
            onSave: onSave,
            onSaveDescription: onSaveDescription,
          ),
        );
      },
    );
  }
}

class _TaskEditSheetContent extends StatefulWidget {
  final TaskModel? task;
  final DateTime initialDate;
  final String? initialTitle;
  final String? initialDescription;
  final ValueChanged<TaskModel>? onSave;
  final ValueChanged<String>? onSaveDescription;
  const _TaskEditSheetContent({
    this.task,
    required this.initialDate,
    this.initialTitle,
    this.initialDescription,
    this.onSave,
    this.onSaveDescription,
  });

  @override
  State<_TaskEditSheetContent> createState() => _TaskEditSheetContentState();
}

class _TaskEditSheetContentState extends State<_TaskEditSheetContent> {
  late TextEditingController _titleController;
  late TextEditingController _descController;
  late DateTime _deadline;
  String _type = '';
  String _subject = '';
  bool _completed = false;

  // dynamiczne listy + fallbacki
  List<String> _types = const [];
  List<String> _subjects = const [];
  bool _loadingSubjects = true;
  bool _loadingTypes = true;

  // fallbacki w razie braku danych z bazy
  static const List<String> _fallbackTypes = [
    'Laboratorium','Egzamin','Projekt','Kolokwium','Wykład','Ćwiczenia','Seminarium','Zaliczenie',
  ];

  // wartości początkowe do porównań "czy zmieniono"
  late final String _initialTitle;
  late final String _initialDescription;
  late final DateTime _initialDate;
  late final String _initialSubject;
  late final String _initialType;
  late final bool _initialCompleted;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.initialTitle ?? '');
    _descController = TextEditingController(text: widget.initialDescription ?? '');
    _deadline = widget.initialDate;
    _completed = widget.task?.completed ?? false;
    _subject = widget.task?.subject ?? '';
    _type = widget.task?.type ?? '';

    _initialTitle = widget.initialTitle ?? '';
    _initialDescription = widget.initialDescription ?? '';
    _initialDate = widget.initialDate;
    _initialSubject = _subject;
    _initialType = _type;
    _initialCompleted = _completed;

    _titleController.addListener(() => setState(() {}));

    // Załaduj przedmioty i typy asynchronicznie z repozytorium
    _loadSubjectsAndTypes();
  }

  Future<void> _loadSubjectsAndTypes() async {
    setState(() { _loadingSubjects = true; _loadingTypes = true; });
    try {
      final subs = await ClassesRepository.getSubjectsForDefaultGroup();
      // Upewnij się, że podczas edycji obecny przedmiot jest na liście
      final List<String> subjects = [...subs];
      if (_subject.isNotEmpty && !subjects.contains(_subject)) subjects.add(_subject);
      if (subjects.isEmpty) {
        // brak danych – dodaj "Inny" jako sensowny placeholder
        subjects.add('Inny');
      }
      subjects.sort((a,b)=> a.toLowerCase().compareTo(b.toLowerCase()));
      String nextSubject = _subject.isNotEmpty ? _subject : subjects.first;
      if (!mounted) return;
      setState(() {
        _subjects = subjects;
        _subject = nextSubject;
        _loadingSubjects = false;
      });
      await _loadTypesFor(_subject);
    } catch (_) {
      if (!mounted) return;
      setState(() {
        // awaria – fallback tylko do "Inny"
        _subjects = ['Inny'];
        if (_subject.isEmpty) _subject = 'Inny';
        _loadingSubjects = false;
      });
      await _loadTypesFor(_subject);
    }
  }

  Future<void> _loadTypesFor(String subject) async {
    setState(() { _loadingTypes = true; });
    try {
      final t = await ClassesRepository.getTypesForSubjectInDefaultGroup(subject);
      final List<String> types = [...t];
      // zachowaj typ edytowanego zadania, jeśli nie ma go w zbiorze
      if (_type.isNotEmpty && !types.contains(_type)) types.add(_type);
      if (types.isEmpty) types.addAll(_fallbackTypes);
      types.sort((a,b)=> a.toLowerCase().compareTo(b.toLowerCase()));
      String nextType = _type.isNotEmpty ? _type : types.first;
      if (!mounted) return;
      setState(() {
        _types = types;
        _type = nextType;
        _loadingTypes = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _types = [..._fallbackTypes];
        if (_type.isEmpty) _type = _fallbackTypes.first;
        _loadingTypes = false;
      });
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    super.dispose();
  }

  Future<void> _pickDate() async {
    final picked = await ModalDatePicker.showCenterDialog(
      context,
      initialDate: _deadline,
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
    );
    if (picked != null) setState(() => _deadline = picked);
  }

  bool get _hasChanges {
    return _titleController.text.trim() != _initialTitle ||
        _descController.text.trim() != _initialDescription ||
        _deadline != _initialDate ||
        _type != _initialType ||
        _completed != _initialCompleted ||
        _subject != _initialSubject;
  }

  Future<bool> _confirmDiscard() async {
    if (_hasChanges) {
      final result = await ConfirmModal.show(
        context,
        title: 'Odrzucić zmiany wprowadzone w tym zadaniu?',
        confirmText: 'Odrzuć',
        cancelText: 'Edytuj dalej',
      );
      return result == true;
    }
    return true;
  }

  void _save() {
    if (_titleController.text.trim().isEmpty) return;
    final newTask = TaskModel(
      id: widget.task?.id ?? '',
      title: _titleController.text.trim(),
      subject: _subject.isEmpty ? 'Inny' : _subject,
      deadline: _deadline,
      type: _type.isEmpty ? null : _type,
      completed: _completed,
    );
    widget.onSave?.call(newTask);
    widget.onSaveDescription?.call(_descController.text.trim());
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => FocusScope.of(context).unfocus(),
      child: Material(
        color: Colors.white,
        elevation: 4,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(_kTopRadius)),
        ),
        clipBehavior: Clip.antiAlias,
        child: Column(
          mainAxisSize: MainAxisSize.max,
          children: [
            const SizedBox(height: 8),
            const _Grip(),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: _buildActionBar(),
            ),
            const SizedBox(height: 8),
            const _FullBleedDivider(),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Tytuł
                    _buildTitleSection(),
                    const SizedBox(height: 20),
                    const _FullBleedDivider(),
                    const SizedBox(height: 20),
                    // Status
                    Row(
                      children: [
                        Icon(MyUz.check_square_broken, size: 20, color: _completed ? AppColors.myUZSysLightPrimary : const Color(0xFF1D192B)),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            'Zadanie zaliczone',
                            style: AppTextStyle.myUZBodyLarge.copyWith(
                              fontWeight: FontWeight.w500,
                              color: const Color(0xFF1D192B),
                            ),
                          ),
                        ),
                        Switch(
                          value: _completed,
                          onChanged: (v) => setState(() => _completed = v),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    const _FullBleedDivider(),
                    const SizedBox(height: 20),
                    // Przedmiot
                    Text('Przedmiot', style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF5F6368))),
                    const SizedBox(height: 8),
                    if (_loadingSubjects)
                      _ShimmerDropdownPlaceholder()
                    else
                      _SimpleDropdown(
                        value: _subject.isNotEmpty ? _subject : (_subjects.isNotEmpty ? _subjects.first : 'Inny'),
                        values: _subjects.contains(_subject) ? _subjects : [..._subjects, if (_subject.isNotEmpty) _subject],
                        onChanged: (v) async {
                          setState(() { _subject = v; });
                          await _loadTypesFor(v);
                        },
                      ),
                    const SizedBox(height: 24),
                    // Typ
                    Text('Typ', style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF5F6368))),
                    const SizedBox(height: 8),
                    if (_loadingTypes)
                      _ShimmerDropdownPlaceholder()
                    else
                      _SimpleDropdown(
                        value: _type.isNotEmpty ? _type : (_types.isNotEmpty ? _types.first : _fallbackTypes.first),
                        values: _types.contains(_type) ? _types : [..._types, if (_type.isNotEmpty) _type],
                        onChanged: (v) => setState(() => _type = v),
                      ),
                    const SizedBox(height: 24),
                    const _FullBleedDivider(),
                    const SizedBox(height: 20),
                    // Data
                    Text('Termin', style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF5F6368))),
                    const SizedBox(height: 8),
                    InkWell(
                      onTap: _pickDate,
                      borderRadius: BorderRadius.circular(12),
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                        decoration: BoxDecoration(
                          border: Border.all(color: const Color(0xFFE7E0EC)),
                          borderRadius: BorderRadius.circular(12),
                          color: Colors.white,
                        ),
                        child: Row(
                          children: [
                            const Icon(MyUz.calendar, size: 20, color: AppColors.myUZSysLightPrimary),
                            const SizedBox(width: 12),
                            Text(
                              _dateInline(_deadline),
                              style: AppTextStyle.myUZBodyMedium.copyWith(color: const Color(0xFF1D192B)),
                            ),
                            const Spacer(),
                            const Icon(MyUz.chevron_right, size: 18, color: Color(0xFF5F6368)),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),
                    const _FullBleedDivider(),
                    const SizedBox(height: 20),
                    // Opis
                    Text('Opis', style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF5F6368))),
                    const SizedBox(height: 8),
                    TextField(
                      controller: _descController,
                      maxLines: 6,
                      minLines: 3,
                      decoration: InputDecoration(
                        hintText: 'Dodaj opis',
                        hintStyle: AppTextStyle.myUZBodyLarge.copyWith(color: const Color(0xFF938F99)),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: Color(0xFFE7E0EC)),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: AppColors.myUZSysLightPrimary, width: 1.5),
                        ),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                      ),
                      style: AppTextStyle.myUZBodyLarge.copyWith(color: const Color(0xFF1D192B)),
                      textCapitalization: TextCapitalization.sentences,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionBar() {
    final canSave = _titleController.text.trim().isNotEmpty;
    return Row(
      children: [
        _IconButtonSlot(
          icon: MyUz.x_close,
          tooltip: 'Zamknij',
          onTap: () async {
            if (await _confirmDiscard() && mounted) {
              Navigator.of(context).maybePop();
            }
          },
        ),
        const Spacer(),
        AnimatedOpacity(
          opacity: canSave ? 1.0 : 0.5,
          duration: const Duration(milliseconds: 150),
          child: TextButton(
            onPressed: canSave ? _save : null,
            style: TextButton.styleFrom(
              backgroundColor: canSave ? AppColors.myUZSysLightPrimary : const Color(0xFFCAC4D0),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            ),
            child: const Text('Zapisz', style: TextStyle(fontWeight: FontWeight.w500)),
          ),
        ),
      ],
    );
  }

  Widget _buildTitleSection() {
    final cs = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.only(left: 48 + 12),
      child: TextField(
        controller: _titleController,
        style: AppTextStyle.myUZTitleLarge.copyWith(
          fontWeight: FontWeight.w500,
          color: const Color(0xFF1D192B),
          decoration: _completed ? TextDecoration.lineThrough : null,
          decorationThickness: _completed ? 2 : null,
          decorationColor: cs.onSurfaceVariant,
        ),
        decoration: InputDecoration(
          border: InputBorder.none,
          hintText: 'Dodaj tytuł',
          hintStyle: AppTextStyle.myUZTitleLarge.copyWith(
            color: const Color(0xFF938F99),
            fontWeight: FontWeight.w500,
          ),
          contentPadding: EdgeInsets.zero,
        ),
        textCapitalization: TextCapitalization.sentences,
      ),
    );
  }
}

class _Grip extends StatelessWidget {
  const _Grip();
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 40,
      height: 4,
      decoration: BoxDecoration(
        color: Colors.black26,
        borderRadius: BorderRadius.circular(2),
      ),
    );
  }
}

class _IconButtonSlot extends StatelessWidget {
  final IconData icon;
  final VoidCallback? onTap;
  final String? tooltip;
  const _IconButtonSlot({required this.icon, this.onTap, this.tooltip});
  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: _kHitArea,
      height: _kHitArea,
      child: Material(
        type: MaterialType.transparency,
        child: InkWell(
          borderRadius: BorderRadius.circular(_kHitArea / 2),
          onTap: onTap,
          child: Tooltip(
            message: tooltip ?? '',
            child: Center(
              child: Icon(icon, size: 24, color: const Color(0xFF1D192B)),
            ),
          ),
        ),
      ),
    );
  }
}

class _FullBleedDivider extends StatelessWidget {
  const _FullBleedDivider();
  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 1,
      color: const Color(0xFFE7E0EC),
    );
  }
}

// Prosty dropdown zastępujący chipy
class _SimpleDropdown extends StatelessWidget {
  final String value;
  final List<String> values;
  final ValueChanged<String> onChanged;
  const _SimpleDropdown({required this.value, required this.values, required this.onChanged});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      decoration: BoxDecoration(
        border: Border.all(color: const Color(0xFFE7E0EC)),
        borderRadius: BorderRadius.circular(12),
        color: Colors.white,
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: value,
            icon: const Icon(MyUz.chevron_down, size: 18, color: Color(0xFF5F6368)),
          style: AppTextStyle.myUZBodyMedium.copyWith(color: const Color(0xFF1D192B)),
          isExpanded: true,
          items: values.map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
          onChanged: (v) { if (v!=null) onChanged(v); },
        ),
      ),
    );
  }
}

class _ShimmerDropdownPlaceholder extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    // prosty placeholder bez zależności – wyszarzony box na czas ładowania
    return Container(
      height: 44,
      decoration: BoxDecoration(
        color: const Color(0xFFF3EDF7),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFFE7E0EC)),
      ),
    );
  }
}

// Konfiguracja stałych (przywrócone po refaktorze)
const double _kTopRadius = 24;
const double _kHitArea = 48;

String _dateInline(DateTime d) {
  const dni = ['Pon.', 'Wt.', 'Śr.', 'Czw.', 'Pt.', 'Sob.', 'Nd.'];
  const mies = ['sty', 'lut', 'mar', 'kwi', 'maj', 'cze', 'lip', 'sie', 'wrz', 'paź', 'lis', 'gru'];
  return '${dni[d.weekday - 1]}, ${d.day} ${mies[d.month - 1]} ${d.year}';
}
