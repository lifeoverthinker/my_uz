import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/confirm_modal.dart';
import 'package:my_uz/widgets/modal_date_picker.dart';

/// Arkusz szczegółów zadania - Google Calendar style
/// Struktura oparta na class_details.dart z możliwością edycji i usuwania
abstract class TaskDetailsSheet {
  static Future show(BuildContext context, TaskModel task, {
    String? description,
    ClassModel? relatedClass,
    VoidCallback? onEdit,
    VoidCallback? onDelete,
    ValueChanged<bool>? onToggleCompleted,
    ValueChanged<TaskModel>? onSaveEdit,
  }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (_) => _TaskDetailsDraggable(
        task: task,
        initialDescription: description,
        relatedClass: relatedClass,
        externalOnDelete: onDelete,
        externalOnToggleCompleted: onToggleCompleted,
        externalOnSaveEdit: onSaveEdit,
      ),
    );
  }

  /// Formatowanie daty po polsku
  static String plDate(DateTime d) {
    const dni = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela'];
    const mies = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];
    return '${dni[d.weekday - 1]}, ${d.day} ${mies[d.month - 1]} ${d.year}';
  }
}

// KONFIG - zgodnie z Material 3 / Google Calendar style
const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
const double _kTopRadius = 24;
const double _kIconToTextGap = 12; // odstęp ikona -> tekst
const double _kHitArea = 48; // hit area dla ikon/slotów (jak w class_details)
const double _kCircleSmall = 32; // dla ikon 16/20
const double _kCircleLarge = 40; // dla ikon 24

/// Główny kontener z DraggableScrollableSheet
class _TaskDetailsDraggable extends StatefulWidget {
  final TaskModel task;
  final String? initialDescription;
  final ClassModel? relatedClass;
  final VoidCallback? externalOnDelete;
  final ValueChanged<bool>? externalOnToggleCompleted;
  final ValueChanged<TaskModel>? externalOnSaveEdit;
  const _TaskDetailsDraggable({
    required this.task,
    this.initialDescription,
    this.relatedClass,
    this.externalOnDelete,
    this.externalOnToggleCompleted,
    this.externalOnSaveEdit,
  });
  @override
  State<_TaskDetailsDraggable> createState() => _TaskDetailsDraggableState();
}

class _TaskDetailsDraggableState extends State<_TaskDetailsDraggable> {
  late TaskModel currentTask;
  String? currentDescription;
  bool _editing = false;

  // Pola edycji
  late TextEditingController _titleController;
  late TextEditingController _descController;
  late DateTime _editDeadline;
  late String _editSubject;
  late String _editType;
  late bool _editCompleted;

  final List<String> _types = [ 'Laboratorium','Egzamin','Projekt','Kolokwium','Wykład','Ćwiczenia','Seminarium','Zaliczenie' ];
  final List<String> _subjects = [ 'Bazy danych','Analiza II','Programowanie obiektowe','Fizyka','Algebra','PPO','Matematyka','Algorytmy','Inny' ];

  @override
  void initState() {
    super.initState();
    currentTask = widget.task;
    currentDescription = widget.initialDescription;
  }

  @override
  void dispose() {
    if (_editing) { _titleController.dispose(); _descController.dispose(); }
    super.dispose();
  }

  void _startEdit() {
    setState(() {
      _editing = true;
      _titleController = TextEditingController(text: currentTask.title);
      _descController = TextEditingController(text: currentDescription ?? '');
      _editDeadline = currentTask.deadline;
      _editSubject = currentTask.subject;
      _editType = currentTask.type ?? _types.first;
      _editCompleted = currentTask.completed;
    });
  }

  bool get _hasEditChanges =>
      _titleController.text.trim() != currentTask.title ||
      _descController.text.trim() != (currentDescription ?? '') ||
      _editDeadline != currentTask.deadline ||
      _editSubject != currentTask.subject ||
      _editType != (currentTask.type ?? _types.first) ||
      _editCompleted != currentTask.completed;

  Future<bool> _maybeDiscard() async {
    if (!_hasEditChanges) return true;
    final res = await ConfirmModal.show(
      context,
      title: 'Odrzucić niezapisane zmiany?',
      confirmText: 'Odrzuć',
      cancelText: 'Kontynuuj',
    );
    return res == true;
  }

  void _cancelEdit() async {
    if (await _maybeDiscard()) {
      setState(() { _editing = false; });
      _titleController.dispose();
      _descController.dispose();
    }
  }

  void _saveEdit() {
    if (_titleController.text.trim().isEmpty) return;
    final updated = currentTask.copyWith(
      title: _titleController.text.trim(),
      deadline: _editDeadline,
      subject: _editSubject,
      type: _editType,
      completed: _editCompleted,
    );
    setState(() {
      currentTask = updated;
      currentDescription = _descController.text.trim().isEmpty ? null : _descController.text.trim();
      _editing = false;
    });
    widget.externalOnSaveEdit?.call(updated);
  }

  void _toggleCompletion() {
    if (_editing) {
      setState(() => _editCompleted = !_editCompleted);
    } else {
      setState(() { currentTask = currentTask.copyWith(completed: !currentTask.completed); });
      widget.externalOnToggleCompleted?.call(currentTask.completed);
    }
  }

  Future<void> _pickDate() async {
    final picked = await ModalDatePicker.showCenterDialog(
      context,
      initialDate: _editDeadline,
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
    );
    if (picked != null) setState(() => _editDeadline = picked);
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _SheetScaffold(
          scrollController: scrollController,
          onEdit: _editing ? null : _startEdit,
          onDelete: widget.externalOnDelete,
          editing: _editing,
          onCancelEdit: _cancelEdit,
          onSaveEdit: _saveEdit,
          canSave: _editing && _titleController.text.trim().isNotEmpty,
          editChanges: _editing ? _hasEditChanges : false,
          child: _editing ? _EditForm(
            scrollController: scrollController,
            titleController: _titleController,
            descController: _descController,
            deadline: _editDeadline,
            subject: _editSubject,
            type: _editType,
            subjects: _subjects,
            types: _types,
            completed: _editCompleted,
            onPickDate: _pickDate,
            onChangeSubject: (v)=> setState(()=> _editSubject = v),
            onChangeType: (v)=> setState(()=> _editType = v),
            onToggleCompleted: _toggleCompletion,
            onChangeTitle: ()=> setState(()=>{}),
          ) : _DetailsContent(
            task: currentTask,
            description: currentDescription,
            relatedClass: widget.relatedClass,
            onToggleCompleted: _toggleCompletion,
          ),
        );
      },
    );
  }
}

/// Szkielet arkusza: białe tło + uchwyt + przyciski akcji + treść
class _SheetScaffold extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;
  final bool editing;
  final VoidCallback? onCancelEdit;
  final VoidCallback? onSaveEdit;
  final bool canSave;
  final bool editChanges;
  const _SheetScaffold({
    required this.child,
    required this.scrollController,
    this.onEdit,
    this.onDelete,
    this.editing = false,
    this.onCancelEdit,
    this.onSaveEdit,
    this.canSave = false,
    this.editChanges = false,
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
      padding: EdgeInsets.only(top: topPadding + handleTopGap),
      decoration: const BoxDecoration(
        color: Colors.white, // Biały styl
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
            // Grip - uchwyt do przeciągania
            const _Grip(),
            const SizedBox(height: handleToXGap),

            // Pasek akcji
            Row(
              children: [
                _AdaptiveIconSlot(
                  iconSize: 24,
                  semanticsLabel: editing ? 'Anuluj edycję' : 'Zamknij szczegóły zadania',
                  isButton: true,
                  onTap: () async {
                    if (editing) {
                      onCancelEdit?.call();
                    } else {
                      Navigator.of(context).maybePop();
                    }
                  },
                  child: Icon(MyUz.x_close, size: 24, color: const Color(0xFF1D192B)),
                ),
                const Spacer(),
                if (!editing && onEdit != null)
                  _AdaptiveIconSlot(
                    iconSize: 24,
                    semanticsLabel: 'Edytuj zadanie',
                    isButton: true,
                    onTap: onEdit,
                    child: Icon(MyUz.edit_02, size: 24, color: const Color(0xFF1D192B)),
                  ),
                if (editing)
                  TextButton(
                    onPressed: canSave ? onSaveEdit : null,
                    style: TextButton.styleFrom(
                      backgroundColor: canSave ? AppColors.myUZSysLightPrimary : const Color(0xFFCAC4D0),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                    ),
                    child: const Text('Zapisz'),
                  ),
                if (onDelete != null)
                  _AdaptiveIconSlot(
                    iconSize: 24,
                    semanticsLabel: 'Więcej opcji',
                    isButton: true,
                    onTap: () async {
                      final theme = Theme.of(context);
                      final selected = await showMenu<String>(
                        context: context,
                        position: const RelativeRect.fromLTRB(1000, 56, 16, 0),
                        items: const [
                          PopupMenuItem(value: 'delete', child: Text('Usuń zadanie')),
                        ],
                        // wymuszenie białego tła przez Theme override
                      );
                      if (selected == 'delete') onDelete?.call();
                    },
                    child: Icon(MyUz.dots_vertical, size: 24, color: const Color(0xFF1D192B)),
                  ),
              ],
            ),
            const SizedBox(height: xToHeaderGap),

            // Treść przewijalna
            Expanded(
              child: SingleChildScrollView(
                controller: scrollController,
                child: child,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Uchwyt (grip) – 40x4, radius 2
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

/// Slot ikony bez kółka w edycji
class _AdaptiveIconSlot extends StatelessWidget {
  final double iconSize; // 16/20/24
  final Widget child; // glif lub marker
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
      child: Center(
        child: Container(
          width: circle,
          height: circle,
          // brak dodatkowego tła – zachowujemy transparent
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(circle / 2),
          ),
          alignment: Alignment.center,
          child: SizedBox(
            width: iconSize,
            height: iconSize,
            child: FittedBox(fit: BoxFit.contain, child: child),
          ),
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
      inner = Semantics(
        button: isButton,
        label: semanticsLabel,
        child: inner,
      );
    }
    return inner;
  }
}

/// Kolorowy marker typu zajęć 16x16, radius 4 (kopiowany z class_details for parity)
class _TypeColorMarker extends StatelessWidget {
  final Color color;
  const _TypeColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 16,
      height: 16,
      decoration: ShapeDecoration(
        color: color,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
    );
  }
}

/// Właściwa treść zadania (nagłówek + szczegóły + switch)
class _DetailsContent extends StatelessWidget {
  final TaskModel task;
  final String? description;
  final ClassModel? relatedClass;
  final VoidCallback? onToggleCompleted;

  const _DetailsContent({
    required this.task,
    this.description,
    this.relatedClass,
    this.onToggleCompleted,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    const headerBottomGap = 28.0;
    const rowVerticalGap = 12.0;
    final formattedDate = TaskDetailsSheet.plDate(task.deadline);

    final subjectText = relatedClass?.subject ?? task.subject;
    final typeText = (relatedClass?.type ?? task.type)?.trim();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Nagłówek: marker + tytuł + data
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _AdaptiveIconSlot(
              iconSize: 16,
              child: _TypeColorMarker(color: AppColors.myUZSysLightPrimaryContainer),
            ),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    task.title,
                    style: AppTextStyle.myUZTitleLarge.copyWith(
                      fontWeight: FontWeight.w500,
                      color: const Color(0xFF1D192B),
                      decoration: task.completed ? TextDecoration.lineThrough : null,
                      decorationThickness: task.completed ? 2 : null,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    formattedDate,
                    style: AppTextStyle.myUZBodyMedium.copyWith(
                      color: cs.onSurfaceVariant,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: headerBottomGap),
        // 1) Status zaliczenia
        _CompletionRow(
          completed: task.completed,
          onToggle: onToggleCompleted,
        ),
        const SizedBox(height: rowVerticalGap),
        // 2) Przedmiot + Rodzaj zajęć (zawsze razem w jednym wierszu)
        _DetailRow(
          icon: MyUz.book_open_01,
          primaryText: subjectText,
          secondaryText: (typeText == null || typeText.isEmpty) ? '-' : typeText,
          forceTwoLines: true,
        ),
        // 3) Opis (opcjonalnie)
        if (description != null && description!.trim().isNotEmpty) ...[
          const SizedBox(height: rowVerticalGap),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _AdaptiveIconSlot(
                iconSize: 20,
                child: Icon(MyUz.menu_03, size: 20, color: cs.onSurface),
              ),
              const SizedBox(width: _kIconToTextGap),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(top: 14),
                  child: Text(
                    description!.trim(),
                    style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface),
                  ),
                ),
              ),
            ],
          ),
        ],
      ],
    );
  }
}

class _CompletionRow extends StatelessWidget {
  final bool completed;
  final VoidCallback? onToggle;
  const _CompletionRow({required this.completed, this.onToggle});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        _AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(
            completed ? MyUz.check_square_broken : MyUz.check_square_broken,
            size: 20,
            color: completed ? cs.primary : cs.onSurface,
          ),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Text(
            'Zadanie zaliczone',
            style: AppTextStyle.myUZBodyLarge.copyWith(
              color: cs.onSurface,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
        Switch(
          value: completed,
          thumbColor: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.selected)) return cs.primary;
            return cs.onSurfaceVariant;
          }),
          trackColor: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.selected)) return cs.primary.withValues(alpha: 0.35);
            return cs.onSurfaceVariant.withValues(alpha: 0.35);
          }),
          onChanged: (_) => onToggle?.call(),
        ),
      ],
    );
  }
}

/// Wiersz szczegółu - dwuliniowy jak w Google Calendar
/// Górny napis grubszy, dolny cieńszy, wyrównane do jednej ikonki
class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String primaryText;
  final String? secondaryText; // zachowane
  final bool forceTwoLines; // nowa flaga aby zawsze pokazać drugą linię

  const _DetailRow({
    required this.icon,
    required this.primaryText,
    this.secondaryText,
    this.forceTwoLines = false,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    final hasSecond = forceTwoLines || (secondaryText != null && secondaryText!.isNotEmpty);
    final topPadding = hasSecond ? 6.0 : 14.0;
    final sec = secondaryText ?? '';

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: cs.onSurface),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Padding(
            padding: EdgeInsets.only(top: topPadding),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  primaryText,
                  style: AppTextStyle.myUZBodyLarge.copyWith(
                    color: cs.onSurface,
                    fontWeight: FontWeight.w500,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 2),
                Text(
                  sec,
                  style: AppTextStyle.myUZBodyMedium.copyWith(
                    color: cs.onSurfaceVariant,
                    fontWeight: FontWeight.w400,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _EditForm extends StatelessWidget {
  final ScrollController scrollController;
  final TextEditingController titleController;
  final TextEditingController descController;
  final DateTime deadline;
  final String subject;
  final String type;
  final List<String> subjects;
  final List<String> types;
  final bool completed;
  final VoidCallback onPickDate;
  final ValueChanged<String> onChangeSubject;
  final ValueChanged<String> onChangeType;
  final VoidCallback onToggleCompleted;
  final VoidCallback onChangeTitle;
  const _EditForm({
    required this.scrollController,
    required this.titleController,
    required this.descController,
    required this.deadline,
    required this.subject,
    required this.type,
    required this.subjects,
    required this.types,
    required this.completed,
    required this.onPickDate,
    required this.onChangeSubject,
    required this.onChangeType,
    required this.onToggleCompleted,
    required this.onChangeTitle,
  });
  @override
  Widget build(BuildContext context) {
    const double sectionGap = 16;
    const double smallGap = 12;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Tytuł (specjalne wcięcie jak w podglądzie)
          Padding(
            padding: const EdgeInsets.only(left: 16 + _kHitArea + _kIconToTextGap - 16, right: 16),
            child: TextField(
              controller: titleController,
              onChanged: (_) => onChangeTitle(),
              style: AppTextStyle.myUZTitleLarge.copyWith(
                fontWeight: FontWeight.w500,
                color: const Color(0xFF1D192B),
                decoration: completed ? TextDecoration.lineThrough : null,
                decorationThickness: completed ? 2 : null,
              ),
              decoration: InputDecoration(
                border: InputBorder.none,
                hintText: 'Dodaj tytuł',
                hintStyle: AppTextStyle.myUZTitleLarge.copyWith(color: const Color(0xFF938F99), fontWeight: FontWeight.w500),
                contentPadding: EdgeInsets.zero,
              ),
              textCapitalization: TextCapitalization.sentences,
            ),
          ),
          const _SectionDivider(),
          SizedBox(height: smallGap),

          // Status zaliczenia
          _EditRow(
            icon: MyUz.check_square_broken,
            child: Row(
              children: [
                Expanded(
                  child: Text('Zadanie zaliczone', style: AppTextStyle.myUZBodyLarge.copyWith(fontWeight: FontWeight.w500, color: const Color(0xFF1D192B))),
                ),
                Switch(value: completed, onChanged: (_) => onToggleCompleted()),
              ],
            ),
          ),
          SizedBox(height: smallGap),
          const _SectionDivider(),
          SizedBox(height: smallGap),

          // Przedmiot + Typ (jedna sekcja pod jedną ikoną)
          _EditRow(
            icon: MyUz.book_open_01,
            topAlign: true,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SizedBox(width: double.infinity, child: _SmallDropdown(current: subject, values: subjects, onChanged: onChangeSubject)),
                const SizedBox(height: 8),
                SizedBox(width: double.infinity, child: _SmallDropdown(current: type, values: types, onChanged: onChangeType)),
              ],
            ),
          ),
          SizedBox(height: smallGap),
          const _SectionDivider(),
          SizedBox(height: smallGap),

          // Termin
          _EditRow(
            icon: MyUz.calendar,
            child: InkWell(
              onTap: onPickDate,
              borderRadius: BorderRadius.circular(8),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 10),
                child: Text(
                  TaskDetailsSheet.plDate(deadline),
                  style: AppTextStyle.myUZBodyMedium.copyWith(color: const Color(0xFF1D192B)),
                ),
              ),
            ),
          ),
          SizedBox(height: smallGap),
          const _SectionDivider(),
          SizedBox(height: smallGap),

          // Opis
          _EditRow(
            icon: MyUz.menu_03,
            topAlign: true,
            child: TextField(
              controller: descController,
              maxLines: 5,
              minLines: 1,
              decoration: InputDecoration(
                border: InputBorder.none,
                hintText: 'Dodaj opis',
                hintStyle: AppTextStyle.myUZBodyLarge.copyWith(color: const Color(0xFF938F99)),
              ),
              style: AppTextStyle.myUZBodyLarge.copyWith(color: const Color(0xFF1D192B)),
            ),
          ),
          SizedBox(height: sectionGap),
        ],
      ),
    );
  }
}

class _EditRow extends StatelessWidget {
  final IconData icon; final Widget child; final bool topAlign; const _EditRow({required this.icon, required this.child, this.topAlign=false});
  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: topAlign? CrossAxisAlignment.start: CrossAxisAlignment.center,
      children: [
        SizedBox(
          width: _kHitArea,
          height: _kHitArea,
          child: Center(child: Icon(icon, size: 20, color: const Color(0xFF1D192B))),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(child: child),
      ],
    );
  }
}

class _SmallDropdown extends StatelessWidget {
  final String current; final List<String> values; final ValueChanged<String> onChanged; const _SmallDropdown({required this.current, required this.values, required this.onChanged});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE7E0EC)),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: current,
          isExpanded: true,
          icon: const Icon(MyUz.chevron_down, size: 14, color: Color(0xFF49454F)),
          style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF1D192B), fontWeight: FontWeight.w500),
          onChanged: (v){ if(v!=null) onChanged(v); },
          items: values.map((e)=> DropdownMenuItem(value: e, child: Text(e, overflow: TextOverflow.ellipsis))).toList(),
        ),
      ),
    );
  }
}

/// Prosty divider pełnej szerokości bez hacków
class _SectionDivider extends StatelessWidget {
  const _SectionDivider();
  @override
  Widget build(BuildContext context) {
    return const Divider(height: 1, thickness: 1, color: Color(0xFFE7E0EC));
  }
}
