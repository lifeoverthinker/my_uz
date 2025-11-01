import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/rz_suggestions.dart';

abstract class TaskDetailsSheet {
  static Future<void> show(
      BuildContext context, {
        required TaskModel task,
        String? description,
        ClassModel? relatedClass,
        VoidCallback? onDelete,
        ValueChanged<bool>? onToggleCompleted,
        ValueChanged<TaskModel>? onSaveEdit,
        ValueChanged<TaskModel>? onDuplicate,
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
        onDuplicate: onDuplicate,
      ),
    );
  }
}

const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
const double _kTopRadius = 24;
const double _kHitArea = 48;
const double _kCircleSmall = 32;
const double _kCircleLarge = 40;
const double _kIconToTextGap = 12;

class _TaskDetailsDraggable extends StatefulWidget {
  final TaskModel task;
  final String? description;
  final ClassModel? relatedClass;
  final VoidCallback? onDelete;
  final ValueChanged<bool>? onToggleCompleted;
  final ValueChanged<TaskModel>? onSaveEdit;
  final ValueChanged<TaskModel>? onDuplicate;

  const _TaskDetailsDraggable({
    required this.task,
    this.description,
    this.relatedClass,
    this.onDelete,
    this.onToggleCompleted,
    this.onSaveEdit,
    this.onDuplicate,
  });

  @override
  State<_TaskDetailsDraggable> createState() => _TaskDetailsDraggableState();
}

class _TaskDetailsDraggableState extends State<_TaskDetailsDraggable> {
  late TaskModel _task;
  bool _editing = false;

  late TextEditingController _titleCtrl;
  late TextEditingController _descCtrl;
  DateTime _deadline = DateTime.now();
  String _subject = '';
  String _rz = '';
  String _type = '';
  int _reminderMin = 0;
  List<String> _subjectOptions = [];
  List<String> _rzOptions = [];

  static const _typePresets = <String>[
    'Zadanie domowe',
    'Projekt',
    'Kolokwium',
    'Wejściówka',
    'Prezentacja',
    'Referat',
    'Inne',
  ];

  @override
  void initState() {
    super.initState();
    _task = widget.task;
    _initEditState();
  }

  void _initEditState() {
    _titleCtrl = TextEditingController(text: _task.title);
    _descCtrl = TextEditingController(text: (widget.description ?? ''));
    _deadline = _task.deadline;
    _subject = _task.subject;
    _type = _task.type ?? '';
    _rz = widget.relatedClass?.type ?? '';
    _reminderMin = 0;
    _loadSubjects();
    _loadRzOptions();
  }

  Future<void> _loadSubjects() async {
    try {
      final list = await ClassesRepository.getSubjectsForDefaultGroup();
      if (!mounted) return;
      setState(() => _subjectOptions = list);
    } catch (_) {}
  }

  Future<void> _loadRzOptions() async {
    try {
      final list = await RzSuggestions.fetchForSubject(_subject);
      if (!mounted) return;
      setState(() => _rzOptions = list);
    } catch (_) {}
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  void _toggleEdit() {
    setState(() => _editing = true);
  }

  void _applySave() {
    final t = _titleCtrl.text.trim();
    if (t.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Podaj tytuł zadania')));
      return;
    }
    final updated = _task.copyWith(
      title: t,
      deadline: _deadline,
      subject: _subject.trim().isEmpty ? _task.subject : _subject.trim(),
      type: _type.trim().isEmpty ? null : _type.trim(),
    );
    setState(() {
      _task = updated;
      _editing = false;
    });
    try {
      widget.onSaveEdit?.call(updated);
    } catch (_) {}
  }

  Future<void> _confirmDelete() async {
    final res = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Usunąć zadanie?'),
        content: const Text('Tej operacji nie można cofnąć.'),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(false), child: const Text('Anuluj')),
          FilledButton.icon(icon: const Icon(MyUz.trash_01), onPressed: () => Navigator.of(ctx).pop(true), label: const Text('Usuń')),
        ],
      ),
    );
    if (res == true) {
      try {
        widget.onDelete?.call();
      } catch (_) {}
      if (mounted) Navigator.of(context).maybePop();
    }
  }

  void _duplicate() {
    final copy = _task.copyWith(id: '');
    if (widget.onDuplicate != null) {
      widget.onDuplicate!(copy);
    } else {
      widget.onSaveEdit?.call(copy);
    }
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Utworzono duplikat zadania')));
  }

  Future<void> _pickDateTime() async {
    final pickedDate = await showDatePicker(
      context: context,
      initialDate: _deadline,
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
      locale: const Locale('pl', 'PL'),
    );
    if (pickedDate == null) return;
    final pickedTime = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(_deadline));
    if (pickedTime == null) return;
    final dt = DateTime(pickedDate.year, pickedDate.month, pickedDate.day, pickedTime.hour, pickedTime.minute);
    setState(() => _deadline = dt);
  }

  Future<void> _pickReminder() async {
    final options = <(int, String)>[
      (10, '10 minut wcześniej'),
      (300, '5 godzin wcześniej'),
      (600, '10 godzin wcześniej'),
      (1440, '1 dzień wcześniej'),
      (4320, '3 dni wcześniej'),
      (10080, '1 tydzień wcześniej'),
      (-1, 'Niestandardowe...'),
    ];
    final selected = await showModalBottomSheet<int>(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            for (final opt in options)
              RadioListTile<int>(
                value: opt.$1,
                groupValue: _reminderMin == 0 ? null : _reminderMin,
                title: Text(opt.$2),
                onChanged: (v) => Navigator.of(ctx).pop(v),
              ),
          ],
        ),
      ),
    );
    if (selected == null) return;
    if (selected == -1) {
      return;
    }
    setState(() => _reminderMin = selected);
  }

  String _fullDeadlineLine(DateTime d) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final d0 = DateTime(d.year, d.month, d.day);
    String dayLabel;
    if (d0 == today) {
      dayLabel = 'Dzisiaj';
    } else if (d0 == today.add(const Duration(days: 1))) {
      dayLabel = 'Jutro';
    } else {
      dayLabel = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela'][d.weekday - 1];
    }
    final monthShort = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'][d.month - 1];
    final showYear = d.year != now.year;
    final datePart = showYear ? '$dayLabel, ${d.day} $monthShort ${d.year}' : '$dayLabel, ${d.day} $monthShort';
    final timePart = DateFormat('HH:mm').format(d);
    return '$datePart • $timePart';
  }

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    final cs = Theme.of(context).colorScheme;

    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return Container(
          padding: EdgeInsets.only(top: topPadding + 8),
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(_kTopRadius)),
            boxShadow: [
              BoxShadow(color: Color(0x4C000000), blurRadius: 3, offset: Offset(0, 1)),
              BoxShadow(color: Color(0x26000000), blurRadius: 8, offset: Offset(0, 4), spreadRadius: 3),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(horizontal, 0, horizontal, 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.black26, borderRadius: BorderRadius.circular(2))),
                const SizedBox(height: 8),
                Row(
                  children: [
                    _AdaptiveIconSlot(
                      iconSize: 24,
                      semanticsLabel: _editing ? 'Anuluj edycję' : 'Zamknij szczegóły',
                      isButton: true,
                      onTap: () => Navigator.of(context).maybePop(),
                      child: const Icon(MyUz.x_close, size: 24, color: Color(0xFF1D192B)),
                    ),
                    const Spacer(),
                    if (_editing)
                      FilledButton(onPressed: _applySave, child: const Text('Zapisz'))
                    else ...[
                      _AdaptiveIconSlot(
                        iconSize: 24,
                        isButton: true,
                        onTap: _toggleEdit,
                        semanticsLabel: 'Edytuj zadanie',
                        child: Icon(MyUz.edit_05, size: 24, color: cs.onSurface),
                      ),
                      SizedBox(
                        width: _kHitArea,
                        height: _kHitArea,
                        child: Align(
                          alignment: Alignment.center,
                          child: PopupMenuButton<String>(
                            tooltip: 'Opcje',
                            position: PopupMenuPosition.under,
                            itemBuilder: (ctx) => [
                              PopupMenuItem<String>(
                                value: 'duplicate',
                                child: Row(children: const [Icon(Icons.copy, size: 18), SizedBox(width: 8), Text('Duplikuj')]),
                              ),
                              PopupMenuItem<String>(
                                value: 'delete',
                                child: Row(children: const [Icon(MyUz.trash_01, size: 18), SizedBox(width: 8), Text('Usuń')]),
                              ),
                            ],
                            icon: Icon(MyUz.dots_vertical, size: 24, color: cs.onSurface),
                            onSelected: (v) {
                              if (v == 'delete') _confirmDelete();
                              if (v == 'duplicate') _duplicate();
                            },
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 12),

                Expanded(
                  child: SingleChildScrollView(
                    controller: scrollController,
                    child: _editing ? _buildEdit(cs) : _buildDetails(cs),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildDetails(ColorScheme cs) {
    const headerBottomGap = 28.0;
    const rowVerticalGap = 12.0;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const _AdaptiveIconSlot(iconSize: 16, child: _TypeColorMarker(color: AppColors.myUZSysLightPrimaryContainer)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(_task.title, style: AppTextStyle.myUZTitleLarge.copyWith(fontWeight: FontWeight.w500, color: const Color(0xFF1D192B)), maxLines: 3, overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 6),
                  Text(_fullDeadlineLine(_task.deadline), style: AppTextStyle.myUZBodyMedium.copyWith(color: cs.onSurfaceVariant, fontWeight: FontWeight.w400)),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: headerBottomGap),
        _SwitchRow(icon: MyUz.check_square_broken, label: 'Ukończone', value: _task.completed, onChanged: (v) {
          setState(() => _task = _task.copyWith(completed: v));
          try {
            widget.onToggleCompleted?.call(v);
          } catch (_) {}
        }),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(icon: MyUz.book_open_01, label: 'Przedmiot', value: _task.subject),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(icon: MyUz.stand, label: 'Rodzaj zajęć', value: (widget.relatedClass?.type ?? '—')),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(icon: MyUz.check_square_broken, label: 'Typ zaliczenia', value: (_task.type ?? '—').toString().trim().isEmpty ? '—' : _task.type!.trim()),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(icon: MyUz.menu_03, label: 'Opis', value: (widget.description ?? '').trim().isEmpty ? 'Brak opisu' : widget.description!.trim()),
      ],
    );
  }

  Widget _buildEdit(ColorScheme cs) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextField(controller: _titleCtrl, autofocus: true, maxLines: null, minLines: 1, decoration: const InputDecoration(hintText: 'Dodaj tytuł', border: InputBorder.none), style: AppTextStyle.myUZTitleLarge),
        const Divider(height: 16),
        Text('Typ zaliczenia', style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: _typePresets.map((name) {
            final sel = _type == name;
            return ChoiceChip(
              label: Text(name),
              selected: sel,
              onSelected: (_) => setState(() => _type = name),
              selectedColor: cs.secondaryContainer,
              labelStyle: TextStyle(color: sel ? cs.onSecondaryContainer : cs.onSurface),
            );
          }).toList(),
        ),
        const Divider(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const _AdaptiveIconSlot(iconSize: 20, child: Icon(MyUz.book_open_01, size: 20)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: InkWell(
                onTap: () async {
                  final chosen = await _pickFromList(context, 'Wybierz przedmiot', _subjectOptions, _subject);
                  if (chosen != null) {
                    setState(() {
                      _subject = chosen;
                    });
                    _loadRzOptions();
                  }
                },
                child: Padding(padding: const EdgeInsets.symmetric(vertical: 10), child: Text(_subject.isEmpty ? 'Wybierz przedmiot' : _subject, style: AppTextStyle.myUZBodyLarge)),
              ),
            ),
          ],
        ),
        const Divider(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const _AdaptiveIconSlot(iconSize: 20, child: Icon(MyUz.stand, size: 20)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: _rzOptions.isEmpty
                  ? Text(_rz.isEmpty ? 'Brak podpowiedzi' : _rz, style: AppTextStyle.myUZBodyLarge)
                  : Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _rzOptions.map((o) {
                  final sel = _rz == o;
                  return ChoiceChip(
                    label: Text(o),
                    selected: sel,
                    onSelected: (_) => setState(() => _rz = o),
                    selectedColor: cs.secondaryContainer,
                    labelStyle: TextStyle(color: sel ? cs.onSecondaryContainer : cs.onSurface),
                  );
                }).toList(),
              ),
            ),
          ],
        ),
        const Divider(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const _AdaptiveIconSlot(iconSize: 20, child: Icon(MyUz.calendar, size: 20)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: InkWell(
                onTap: _pickDateTime,
                child: Padding(padding: const EdgeInsets.symmetric(vertical: 10), child: Text(DateFormat('EEE, d LLL yyyy • HH:mm', 'pl').format(_deadline), style: AppTextStyle.myUZBodyLarge)),
              ),
            ),
          ],
        ),
        const Divider(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const _AdaptiveIconSlot(iconSize: 20, child: Icon(MyUz.bell_03, size: 20)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: InkWell(
                onTap: _pickReminder,
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 10),
                  child: Text(_reminderMin == 0 ? 'Dodaj powiadomienie' : 'Powiadomienie: $_reminderMin min wcześniej', style: AppTextStyle.myUZBodyLarge),
                ),
              ),
            ),
          ],
        ),
        const Divider(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const _AdaptiveIconSlot(iconSize: 20, child: Icon(MyUz.menu_03, size: 20)),
            const SizedBox(width: _kIconToTextGap),
            Expanded(child: TextField(controller: _descCtrl, maxLines: null, minLines: 3, decoration: const InputDecoration(hintText: 'Dodaj opis', border: InputBorder.none))),
          ],
        ),
      ],
    );
  }

  Future<String?> _pickFromList(BuildContext context, String title, List<String> options, String current) async {
    return showModalBottomSheet<String>(
      context: context,
      useSafeArea: true,
      builder: (_) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(title: Text(title, style: Theme.of(context).textTheme.titleMedium)),
            const Divider(height: 1),
            Flexible(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: options.length,
                itemBuilder: (ctx, i) {
                  final v = options[i];
                  final sel = v == current;
                  return ListTile(
                    title: Text(v),
                    trailing: sel ? const Icon(MyUz.check) : null,
                    onTap: () => Navigator.of(ctx).pop(v),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _AdaptiveIconSlot extends StatelessWidget {
  final double iconSize;
  final Widget child;
  final VoidCallback? onTap;
  final String? semanticsLabel;
  final bool isButton;

  const _AdaptiveIconSlot({
    required this.iconSize,
    required this.child,
    this.onTap,
    this.semanticsLabel,
    this.isButton = false,
  });

  @override
  Widget build(BuildContext context) {
    final double circle = iconSize >= 24 ? _kCircleLarge : _kCircleSmall;
    Widget inner = SizedBox(
      width: _kHitArea,
      height: _kHitArea,
      child: Align(
        alignment: Alignment.center,
        child: Container(
          width: circle,
          height: circle,
          alignment: Alignment.center,
          child: SizedBox(width: iconSize, height: iconSize, child: FittedBox(fit: BoxFit.contain, child: child)),
        ),
      ),
    );

    if (onTap != null) {
      inner = Material(
        type: MaterialType.transparency,
        child: InkWell(
          borderRadius: BorderRadius.circular(circle / 2),
          splashColor: Colors.black12,
          onTap: onTap,
          child: inner,
        ),
      );
    }
    if (semanticsLabel != null) {
      inner = Semantics(button: isButton, label: semanticsLabel, child: inner);
    }
    return inner;
  }
}

class _TypeColorMarker extends StatelessWidget {
  final Color color;
  const _TypeColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(width: 16, height: 16, decoration: ShapeDecoration(color: color, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4))));
  }
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _DetailRow({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        _AdaptiveIconSlot(iconSize: 20, child: Icon(icon, size: 20, color: cs.onSurface)),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Align(
            alignment: Alignment.centerLeft,
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(label, style: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onSurfaceVariant)),
              const SizedBox(height: 6),
              Text(value, style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface)),
            ]),
          ),
        ),
      ],
    );
  }
}

class _SwitchRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _SwitchRow({required this.icon, required this.label, required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        _AdaptiveIconSlot(iconSize: 20, child: Icon(icon, size: 20, color: cs.onSurface)),
        const SizedBox(width: _kIconToTextGap),
        Expanded(child: Align(alignment: Alignment.centerLeft, child: Text(label, style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface)))),
        Switch(value: value, onChanged: onChanged),
      ],
    );
  }
}