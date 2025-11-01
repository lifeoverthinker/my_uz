import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/rz_suggestions.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/date_picker.dart';

abstract class TaskEditSheet {
  static bool _isOpen = false;

  static Future<TaskModel?> show(
      BuildContext context,
      TaskModel? initial, {
        DateTime? initialDate,
      }) async {
    if (_isOpen) return null;
    _isOpen = true;
    try {
      return await showModalBottomSheet<TaskModel?>(
        context: context,
        isScrollControlled: true,
        useSafeArea: true,
        useRootNavigator: true,
        backgroundColor: Colors.transparent,
        barrierColor: Colors.black54,
        routeSettings: RouteSettings(name: 'task_edit_sheet_${DateTime.now().microsecondsSinceEpoch}'),
        builder: (_) => _TaskEditDraggable(initial: initial, initialDate: initialDate),
      );
    } finally {
      _isOpen = false;
    }
  }
}

const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
const double _kTopRadius = 24;
const double _kDialogRadius = 20;
const double _kHitArea = 48;
const double _hPad = 16;
const double _iconToTextGap = 12;
const double _leadingTextOffset = _kHitArea + _iconToTextGap;

class _TaskEditDraggable extends StatefulWidget {
  final TaskModel? initial;
  final DateTime? initialDate;
  const _TaskEditDraggable({required this.initial, this.initialDate});

  @override
  State<_TaskEditDraggable> createState() => _TaskEditDraggableState();
}

class _TaskEditDraggableState extends State<_TaskEditDraggable> {
  late final TextEditingController _titleCtrl;
  late final TextEditingController _descCtrl;

  DateTime _deadline = DateTime.now();
  String _subject = '';
  String _rz = '';
  String _type = '';
  int _reminderMin = 0;
  bool _allDay = false;

  List<String> _subjectOptions = [];
  List<String> _rzOptions = [];

  bool get _isEditing => widget.initial != null;

  static const _typePresets = <String>[
    'Zadanie domowe',
    'Projekt',
    'Kolokwium',
    'Wejściówka',
    'Prezentacja',
    'Referat',
    'Inne',
  ];

  late final Map<String, Color> _typeColors;

  @override
  void initState() {
    super.initState();
    final i = widget.initial;
    _titleCtrl = TextEditingController(text: i?.title ?? '');
    _descCtrl = TextEditingController(text: '');
    _deadline = i?.deadline ?? (widget.initialDate ?? DateTime.now());
    _subject = i?.subject ?? '';
    _type = i?.type ?? '';
    _rz = '';
    _reminderMin = 0;
    _allDay = false;

    _typeColors = {
      'Zadanie domowe': const Color(0xFF2962FF),
      'Projekt': const Color(0xFF00B8D4),
      'Kolokwium': const Color(0xFFD81B60),
      'Wejściówka': const Color(0xFFF9A825),
      'Prezentacja': const Color(0xFF2E7D32),
      'Referat': const Color(0xFF6A1B9A),
      'Inne': const Color(0xFF5F6368),
    };

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
      if (_subject.isEmpty) {
        if (!mounted) return;
        setState(() => _rzOptions = []);
        return;
      }

      final repoTypes = await ClassesRepository.getTypesForSubjectInDefaultGroup(_subject);
      List<String> list;
      if (repoTypes.isNotEmpty) {
        list = repoTypes;
      } else {
        list = await RzSuggestions.fetchForSubject(_subject);
      }
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

  Widget _hitIcon({required Widget icon, String? semanticsLabel, VoidCallback? onTap}) {
    Widget child = SizedBox(
      width: _kHitArea,
      height: _kHitArea,
      child: Center(child: icon),
    );
    if (onTap != null) {
      child = Material(
        type: MaterialType.transparency,
        child: InkWell(
          customBorder: const CircleBorder(),
          onTap: onTap,
          child: child,
        ),
      );
    }
    if (semanticsLabel != null) {
      child = Semantics(button: onTap != null, label: semanticsLabel, child: child);
    }
    return child;
  }

  Widget _fullBleedDivider() {
    return Padding(
      padding: const EdgeInsetsDirectional.only(start: -_hPad, end: -_hPad),
      child: const Divider(height: 16, thickness: 1),
    );
  }

  String _formatDate(DateTime d) {
    final wd = DateFormat('EEE', 'pl').format(d);
    final mon = DateFormat('LLL', 'pl').format(d);
    return '${wd[0].toUpperCase()}${wd.substring(1)}, ${d.day} $mon ${d.year}';
  }

  String _formatTime(DateTime d) {
    final h = d.hour.toString().padLeft(2, '0');
    final m = d.minute.toString().padLeft(2, '0');
    return '$h:$m';
  }

  Future<void> _pickDateTime() async {
    final pickedDate = await ModalDatePicker.showCenterDialog(context, initialDate: _deadline);
    if (pickedDate == null) return;

    if (_allDay) {
      setState(() => _deadline = DateTime(pickedDate.year, pickedDate.month, pickedDate.day, 0, 0));
      return;
    }

    final pickedTime = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(_deadline));
    if (pickedTime == null) return;

    setState(() {
      _deadline = DateTime(pickedDate.year, pickedDate.month, pickedDate.day, pickedTime.hour, pickedTime.minute);
    });
  }

  Future<int?> _showReminderDialog({required int currentMinutes}) {
    final options = <(int, String)>[
      (10, '10 minut wcześniej'),
      (300, '5 godzin wcześniej'),
      (600, '10 godzin wcześniej'),
      (1440, '1 dzień wcześniej'),
      (4320, '3 dni wcześniej'),
      (10080, '1 tydzień wcześniej'),
      (-1, 'Niestandardowe...'),
    ];
    return showDialog<int>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(_kDialogRadius)),
        contentPadding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
        content: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 360),
          child: ListView(
            shrinkWrap: true,
            children: [
              for (final opt in options)
                RadioListTile<int>(
                  value: opt.$1,
                  groupValue: currentMinutes == 0 ? null : currentMinutes,
                  title: Text(opt.$2),
                  onChanged: (v) => Navigator.of(ctx).pop(v),
                  visualDensity: VisualDensity.compact,
                ),
            ],
          ),
        ),
      ),
    );
  }

  Future<String?> _showStringPickerDialog({
    required String title,
    required List<String> options,
    required String current,
  }) {
    return showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(_kDialogRadius)),
        title: Text(title),
        contentPadding: const EdgeInsets.fromLTRB(0, 0, 0, 8),
        content: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 360),
          child: ListView.builder(
            shrinkWrap: true,
            itemCount: options.length,
            itemBuilder: (_, i) {
              final v = options[i];
              final sel = v == current;
              return ListTile(
                dense: true,
                title: Text(v),
                trailing: sel ? const Icon(Icons.check_rounded) : null,
                onTap: () => Navigator.of(ctx).pop(v),
              );
            },
          ),
        ),
        actions: [TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Anuluj'))],
      ),
    );
  }

  void _save() {
    final t = _titleCtrl.text.trim();
    if (t.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Podaj tytuł zadania')));
      return;
    }

    final base = widget.initial ??
        TaskModel(
          id: '',
          title: t,
          deadline: _deadline,
          subject: _subject.trim().isEmpty ? 'Inny' : _subject.trim(),
          classId: null,
          completed: false,
          type: _type.trim().isEmpty ? null : _type.trim(),
        );

    final out = base.copyWith(
      title: t,
      deadline: _deadline,
      subject: _subject.trim().isEmpty ? 'Inny' : _subject.trim(),
      type: _type.trim().isEmpty ? null : _type.trim(),
    );

    Navigator.of(context).pop(out);
  }

  Widget _typePills(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    Color bg(bool selected) => selected ? cs.secondaryContainer : cs.surfaceVariant.withOpacity(0.6);
    Color fg(bool selected) => selected ? cs.onSecondaryContainer : cs.onSurface;

    Widget dot(Color color) => Container(width: 6, height: 6, decoration: BoxDecoration(color: color, shape: BoxShape.circle));

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: EdgeInsets.zero,
      child: Row(
        children: _typePresets.map((name) {
          final selected = _type == name;
          final color = _typeColors[name] ?? cs.primary;
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: InkWell(
              borderRadius: BorderRadius.circular(999),
              onTap: () => setState(() => _type = name),
              child: Container(
                height: 28,
                padding: const EdgeInsets.symmetric(horizontal: 10),
                decoration: const ShapeDecoration(color: Colors.transparent, shape: StadiumBorder(), shadows: []),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: ShapeDecoration(color: bg(selected), shape: const StadiumBorder()),
                  child: Row(mainAxisSize: MainAxisSize.min, children: [
                    dot(color),
                    const SizedBox(width: 6),
                    Text(name, style: AppTextStyle.myUZBodySmall.copyWith(color: fg(selected)), overflow: TextOverflow.ellipsis),
                  ]),
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;

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
            padding: const EdgeInsets.fromLTRB(_hPad, 0, _hPad, 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.black26, borderRadius: BorderRadius.circular(2))),
                const SizedBox(height: 8),
                Row(
                  children: [
                    _hitIcon(icon: const Icon(MyUz.x_close, size: 24, color: Color(0xFF1D192B)), semanticsLabel: 'Zamknij edycję', onTap: () => Navigator.of(context).maybePop()),
                    const Spacer(),
                    FilledButton(onPressed: _save, child: const Text('Zapisz')),
                  ],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: SingleChildScrollView(
                    controller: scrollController,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.only(left: _leadingTextOffset),
                          child: TextField(
                            controller: _titleCtrl,
                            autofocus: !_isEditing,
                            maxLines: null,
                            minLines: 1,
                            decoration: const InputDecoration(hintText: 'Dodaj tytuł', border: InputBorder.none),
                            style: AppTextStyle.myUZTitleLarge,
                          ),
                        ),
                        _fullBleedDivider(),
                        Padding(padding: const EdgeInsets.only(left: _leadingTextOffset), child: _typePills(context)),
                        _fullBleedDivider(),
                        Row(
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(MyUz.book_open_01, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(
                              child: InkWell(
                                onTap: () async {
                                  if (_subjectOptions.isEmpty) await _loadSubjects();
                                  final chosen = await _showStringPickerDialog(title: 'Wybierz przedmiot', options: _subjectOptions, current: _subject);
                                  if (chosen != null) {
                                    setState(() {
                                      _subject = chosen;
                                      _rz = '';
                                    });
                                    _loadRzOptions();
                                  }
                                },
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  child: Text(_subject.isEmpty ? 'Wybierz przedmiot' : _subject, style: AppTextStyle.myUZBodyLarge),
                                ),
                              ),
                            ),
                          ],
                        ),
                        Row(
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(MyUz.stand, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(
                              child: InkWell(
                                onTap: () async {
                                  if (_subject.isEmpty) {
                                    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Najpierw wybierz przedmiot')));
                                    return;
                                  }
                                  if (_rzOptions.isEmpty) await _loadRzOptions();
                                  if (_rzOptions.isEmpty) {
                                    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Brak podpowiedzi dla wybranego przedmiotu')));
                                    return;
                                  }
                                  final chosen = await _showStringPickerDialog(title: 'Wybierz rodzaj zajęć', options: _rzOptions, current: _rz);
                                  if (chosen != null) setState(() => _rz = chosen);
                                },
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  child: Text(_rz.isEmpty ? 'Wybierz rodzaj zajęć' : _rz, style: AppTextStyle.myUZBodyLarge),
                                ),
                              ),
                            ),
                          ],
                        ),
                        _fullBleedDivider(),
                        Row(
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(Icons.wb_sunny_rounded, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(child: Text('Cały dzień', style: AppTextStyle.myUZBodyLarge)),
                            Switch(
                              value: _allDay,
                              onChanged: (v) {
                                setState(() {
                                  _allDay = v;
                                  if (_allDay) _deadline = DateTime(_deadline.year, _deadline.month, _deadline.day, 0, 0);
                                });
                              },
                            ),
                          ],
                        ),
                        _fullBleedDivider(),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(MyUz.calendar, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(
                              child: InkWell(
                                onTap: _pickDateTime,
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  child: Row(
                                    children: [
                                      Expanded(child: Text(_formatDate(_deadline), style: AppTextStyle.myUZBodyLarge, overflow: TextOverflow.ellipsis)),
                                      if (!_allDay) ...[
                                        const SizedBox(width: 12),
                                        Text(_formatTime(_deadline), style: AppTextStyle.myUZBodyLarge),
                                      ],
                                    ],
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                        _fullBleedDivider(),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(MyUz.bell_03, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(
                              child: InkWell(
                                onTap: () async {
                                  final selected = await _showReminderDialog(currentMinutes: _reminderMin);
                                  if (selected == null) return;
                                  if (selected == -1) {
                                    return;
                                  }
                                  setState(() => _reminderMin = selected);
                                },
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  child: Text(_reminderMin == 0 ? 'Dodaj powiadomienie' : 'Powiadomienie: $_reminderMin min wcześniej', style: AppTextStyle.myUZBodyLarge),
                                ),
                              ),
                            ),
                          ],
                        ),
                        _fullBleedDivider(),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            SizedBox(width: _kHitArea, height: _kHitArea, child: const Center(child: Icon(MyUz.menu_03, size: 20))),
                            const SizedBox(width: _iconToTextGap),
                            Expanded(
                              child: TextField(
                                controller: _descCtrl,
                                maxLines: null,
                                minLines: 3,
                                decoration: const InputDecoration(hintText: 'Dodaj opis', border: InputBorder.none),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}