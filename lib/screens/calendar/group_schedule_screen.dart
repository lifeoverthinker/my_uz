import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/screens/calendar/components/calendar_view.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

class GroupScheduleScreen extends StatefulWidget {
  // Możliwe sposoby użycia:
  // - przekazanie listy `classes` (preview / test)
  // - przekazanie `groupCode` / `groupId` (wywołanie z wyszukiwania)
  final String? groupCode;
  final String? groupId;
  final String? groupName;
  final List<ClassModel>? classes;
  final bool isFavorite;
  final VoidCallback? onToggleFavorite;
  final List<String>? subgroups;
  final List<String>? selectedSubgroups;
  final ValueChanged<List<String>?>? onSubgroupSelected;

  const GroupScheduleScreen({
    super.key,
    this.groupCode,
    this.groupId,
    this.groupName,
    this.classes,
    this.isFavorite = false,
    this.onToggleFavorite,
    this.subgroups,
    this.selectedSubgroups,
    this.onSubgroupSelected,
  });

  @override
  State<GroupScheduleScreen> createState() => _GroupScheduleScreenState();
}

class _GroupScheduleScreenState extends State<GroupScheduleScreen> {
  late DateTime _selectedDay;
  List<ClassModel>? _loadedClasses;
  bool _loading = false;
  List<String>? _selectedSubgroups;
  List<String>? _availableSubgroups;
  bool _isFav = false;
  bool _favAnimating = false;
  String? _resolvedGroupCode; // preferowany kod do wyświetlenia
  bool _showAllSubgroups = false; // <--- nowy stan rozwijania podgrup

  // Prosta heurystyka: czy warto traktować przekazany string jako surowe id (UUID-like)?
  bool _isLikelyId(String? s) {
    if (s == null) return false;
    final t = s.trim();
    if (t.isEmpty) return false;
    // UUID ma myślniki i hex-digits; zmienna długość -> wykryj długi ciąg z myślnikami lub same hexy
    final uuidLike = RegExp(r'^[0-9a-fA-F\-]{12,}$');
    return uuidLike.hasMatch(t);
  }

  // Preferowany tekst do wyświetlenia jako "kod grupy" (nie pokazuj surowego id)
  String? get _displayGroupCode {
    if (_resolvedGroupCode != null && _resolvedGroupCode!.trim().isNotEmpty) return _resolvedGroupCode;
    if (widget.groupCode != null && widget.groupCode!.trim().isNotEmpty && !_isLikelyId(widget.groupCode)) return widget.groupCode!.trim();
    if (widget.groupName != null && widget.groupName!.trim().isNotEmpty) return widget.groupName!.trim();
    return null;
  }

  @override
  void initState() {
    super.initState();
    _selectedDay = DateTime.now();
    _selectedSubgroups = widget.selectedSubgroups == null ? <String>[] : List<String>.from(widget.selectedSubgroups!);
    _availableSubgroups = widget.subgroups;
    // First resolve a readable group code (if we only have groupId), then fetch subgroups and classes
    _loadFavoriteStatus();
    _ensureGroupCodeResolved().then((_) {
      // after resolving group code, attempt to load subgroups and classes
      if (widget.classes == null) {
        _maybeLoadSubgroups();
        _loadClassesForDay();
      }
    });
  }

  Future<void> _ensureGroupCodeResolved() async {
    // Jeśli nie mamy czytelnego kodu grupy, spróbuj rozwiązać z groupId
    final hasGroupId = widget.groupId != null && widget.groupId!.trim().isNotEmpty;
    final providedCode = widget.groupCode;
    final providedLooksLikeId = _isLikelyId(providedCode);
    if ((providedCode == null || providedCode.trim().isEmpty || providedLooksLikeId) && hasGroupId) {
      try {
        final meta = await ClassesRepository.getGroupById(widget.groupId!);
        if (!mounted) return;
        setState(() { _resolvedGroupCode = meta?['kod_grupy'] as String?; });
        return;
      } catch (_) {}
    }
    // fallback: jeśli providedCode wygląda sensownie (nie jest id), użyj go
    _resolvedGroupCode = (providedCode != null && providedCode.trim().isNotEmpty && !providedLooksLikeId) ? providedCode.trim() : null;
  }

  Future<void> _loadFavoriteStatus() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString('fav_plans');
      if (raw == null || raw.isEmpty) { setState(()=> _isFav = false); return; }
      final List<dynamic> l = jsonDecode(raw);
      final key = widget.groupId != null && widget.groupId!.isNotEmpty ? 'group:${widget.groupId}' : 'group:${widget.groupCode ?? ''}';
      setState(() => _isFav = l.map((e)=>e.toString()).contains(key));
    } catch (_) { setState(()=> _isFav = false); }
  }

  Future<void> _toggleFavorite() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString('fav_plans');
      final List<String> list = raw == null || raw.isEmpty ? [] : List<String>.from(jsonDecode(raw).map((e)=>e.toString()));
      final key = widget.groupId != null && widget.groupId!.isNotEmpty ? 'group:${widget.groupId}' : 'group:${widget.groupCode ?? ''}';
      if (list.contains(key)) { list.remove(key); setState(()=> _isFav = false); }
      else { list.add(key); setState(()=> _isFav = true); }
      await prefs.setString('fav_plans', jsonEncode(list));
      // Aktualizuj również mapę etykiet (fav_labels) tak, by Drawer mógł wyświetlić kod grupy
      try {
        final rawLabels = prefs.getString('fav_labels');
        Map<String,String> labels = {};
        if (rawLabels != null && rawLabels.isNotEmpty) {
          final dec = jsonDecode(rawLabels);
          if (dec is Map) dec.forEach((k,v){ if (k is String && v is String) labels[k]=v; });
        }
        if (list.contains(key)) {
          var label = _displayGroupCode ?? widget.groupCode ?? widget.groupName ?? '';
          if (label.isEmpty && (widget.groupId != null && widget.groupId!.isNotEmpty)) {
            try {
              final metaTry = await ClassesRepository.getGroupById(widget.groupId!);
              if (metaTry != null) label = (metaTry['kod_grupy'] as String?) ?? (metaTry['nazwa'] as String?) ?? '';
            } catch (_) {}
          }
          if (label.isNotEmpty) labels[key] = label;
        } else {
           labels.remove(key);
         }
        await prefs.setString('fav_labels', jsonEncode(labels));
      } catch (_) {}
      // notify parent (e.g. Drawer) to refresh UI if provided
      try { widget.onToggleFavorite?.call(); } catch(_) {}
      setState(() { _favAnimating = true; });
      Future.delayed(const Duration(milliseconds: 260), () { if (mounted) setState(() { _favAnimating = false; }); });
    } catch (e) {
      // ignore
    }
  }

  Future<void> _maybeLoadSubgroups() async {
    final effectiveCode = (widget.groupCode != null && widget.groupCode!.trim().isNotEmpty && !_isLikelyId(widget.groupCode))
        ? widget.groupCode!.trim()
        : (_resolvedGroupCode != null && _resolvedGroupCode!.trim().isNotEmpty ? _resolvedGroupCode!.trim() : null);
    if ((_availableSubgroups == null || _availableSubgroups!.isEmpty) && (effectiveCode != null && effectiveCode.isNotEmpty)) {
      try {
        final subs = await ClassesRepository.getSubgroupsForGroup(effectiveCode);
        if (mounted) setState(() => _availableSubgroups = subs);
      } catch (_) {}
      return;
    }

    // FALLBACK: jeśli nie mamy effectiveCode (np. otwarto ekran dla "mojej" grupy), spróbuj pobrać kontekst preferencji grupy
    if ((_availableSubgroups == null || _availableSubgroups!.isEmpty)) {
      try {
        final ctx = await ClassesRepository.loadGroupContext(); // zwraca (groupCode, subgroups, groupId)
        // ctx is a Dart record: access via .$1 / .$2 / .$3
        // Try to read as a Dart record returned by loadGroupContext()
        try {
          final groupCodeFromCtx = ctx.$1;
          final subsFromCtx = ctx.$2 as List<String>?;
          final groupIdFromCtx = ctx.$3;

          // Jeśli kontekst dotyczy tej samej grupy (albo my nie mamy groupCode), użyj podgrup
          final matches = (groupCodeFromCtx != null && groupCodeFromCtx.isNotEmpty && (widget.groupCode == null || widget.groupCode!.isEmpty || widget.groupCode == groupCodeFromCtx)) ||
              (groupIdFromCtx != null && widget.groupId != null && widget.groupId == groupIdFromCtx) ||
              (widget.groupCode == null && widget.groupId == null && groupCodeFromCtx != null);
          if (matches && subsFromCtx != null && subsFromCtx.isNotEmpty) {
            if (mounted) setState(() { _availableSubgroups = subsFromCtx; });
          }
        } catch (_) {
          // If ctx isn't a record or has unexpected shape, silently ignore
        }
      } catch (_) {}
    }
  }

  Future<void> _loadClassesForDay() async {
    setState(() { _loading = true; });
    try {
      final effectiveCode = (widget.groupCode != null && widget.groupCode!.trim().isNotEmpty && !_isLikelyId(widget.groupCode))
          ? widget.groupCode!.trim()
          : (_resolvedGroupCode != null && _resolvedGroupCode!.trim().isNotEmpty ? _resolvedGroupCode!.trim() : null);
      final subList = (_selectedSubgroups == null || _selectedSubgroups!.isEmpty) ? <String>[] : _selectedSubgroups!;
      final list = await ClassesRepository.fetchDayWithWeekFallback(
        _selectedDay,
        groupCode: effectiveCode,
        subgroups: subList,
        groupId: widget.groupId,
      );
      if (mounted) setState(() { _loadedClasses = list; });
    } catch (e) {
      if (mounted) setState(() { _loadedClasses = <ClassModel>[]; });
    } finally {
      if (mounted) setState(() { _loading = false; });
    }
  }

  void _onDaySelected(DateTime day) {
    setState(() {
      _selectedDay = day;
    });
    if (widget.classes == null) {
      _loadClassesForDay();
    }
  }

  void _nextWeek(){
    _onDaySelected(_selectedDay.add(const Duration(days:7)));
  }
  void _prevWeek(){
    _onDaySelected(_selectedDay.subtract(const Duration(days:7)));
  }

  void _onSubgroupChangedToggle(String value) {
    setState(() {
      _selectedSubgroups ??= <String>[];
      if (_selectedSubgroups!.contains(value)) _selectedSubgroups!.remove(value); else _selectedSubgroups!.add(value);
    });
    widget.onSubgroupSelected?.call(_selectedSubgroups);
    if (widget.classes == null) _loadClassesForDay();
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final heartColor = _isFav ? cs.primary : null;

    // Use either provided classes or loaded classes
    // final allClasses = widget.classes ?? _loadedClasses ?? <ClassModel>[];
    // Filter by selected day using startTime
    // final classesForDay = allClasses.where((c) {
    //   final st = c.startTime.toLocal();
    //   final sameDay = st.year == _selectedDay.year && st.month == _selectedDay.month && st.day == _selectedDay.day;
    //   final subgroupMatch = _selectedSubgroup == null || _selectedSubgroup!.isEmpty || c.subgroup == null || c.subgroup == _selectedSubgroup;
    //   return sameDay && subgroupMatch;
    // }).toList();
    // CalendarDayView will receive the available classes (either provided or loaded).
    // Filtering by day/subgroup is handled inside CalendarDayView or by repository fetch.

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        centerTitle: false,
        leading: IconButton(
          icon: const Icon(MyUz.chevron_left, size: 24),
          onPressed: () => Navigator.of(context).pop(),
        ),
        titleSpacing: 0,
        backgroundColor: Colors.white,
        foregroundColor: Theme.of(context).colorScheme.onSurface,
        elevation: 0,
        title: Align(
          alignment: Alignment.centerLeft,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Plan grupy', style: Theme.of(context).textTheme.titleLarge),
              if (_displayGroupCode != null)
                Text(_displayGroupCode!, style: Theme.of(context).textTheme.bodySmall),
            ],
          ),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 8.0),
            child: AnimatedScale(
              scale: _favAnimating ? 1.15 : 1.0,
              duration: const Duration(milliseconds: 220),
              curve: Curves.easeOutBack,
              child: IconButton(
                icon: Icon(_isFav ? Icons.favorite : Icons.favorite_border, color: heartColor),
                onPressed: _toggleFavorite,
                tooltip: _isFav ? 'Usuń z ulubionych' : 'Dodaj do ulubionych',
              ),
            ),
          ),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Jeśli istnieją podgrupy – pokaż je bezpośrednio pod AppBar (zamiast powtarzać kod grupy)
          if (_availableSubgroups != null && _availableSubgroups!.isNotEmpty)
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 12, 12, 6),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: [
                        const SizedBox(width: 8),
                        FilterChip(
                          label: const Text('Wszystkie'),
                          selected: _selectedSubgroups == null || _selectedSubgroups!.isEmpty,
                          onSelected: (v) {
                            setState(() { _selectedSubgroups = <String>[]; });
                            widget.onSubgroupSelected?.call(_selectedSubgroups);
                            if (widget.classes == null) _loadClassesForDay();
                          },
                        ),
                        const SizedBox(width: 8),
                        ...(_showAllSubgroups
                          ? _availableSubgroups!
                          : _availableSubgroups!.take(4)).map((s) => Padding(
                          padding: const EdgeInsets.only(right: 8.0),
                          child: FilterChip(
                            label: Text(s),
                            selected: _selectedSubgroups != null && _selectedSubgroups!.contains(s),
                            onSelected: (_) => _onSubgroupChangedToggle(s),
                          ),
                        )),
                        if (_availableSubgroups!.length > 4)
                          Padding(
                            padding: const EdgeInsets.only(right: 8.0),
                            child: ActionChip(
                              label: Text(_showAllSubgroups ? 'Pokaż mniej' : 'Pokaż więcej'),
                              avatar: Icon(_showAllSubgroups ? Icons.expand_less : Icons.expand_more, size: 18),
                              onPressed: () {
                                setState(() { _showAllSubgroups = !_showAllSubgroups; });
                              },
                            ),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            )
          else
            const SizedBox(height: 12),

          _DaysOfWeekBar(
            selectedDay: _selectedDay,
            onDaySelected: _onDaySelected,
            onPrevWeek: _prevWeek,
            onNextWeek: _nextWeek,
          ),
          const Divider(height: 1),
          Expanded(
            child: _loading && widget.classes == null
                ? const Center(child: CircularProgressIndicator())
                : CalendarDayView(
                    day: _selectedDay,
                    classes: widget.classes != null ? (widget.classes ?? []) : ( _loadedClasses ?? [] ),
                    onNextDay: _nextDay,
                    onPrevDay: _prevDay,
                  ),
          ),
        ],
      ),
    );
  }

  // Navigate to next / previous day
  void _nextDay() {
    _onDaySelected(_selectedDay.add(const Duration(days: 1)));
  }

  void _prevDay() {
    _onDaySelected(_selectedDay.subtract(const Duration(days: 1)));
  }
}

class _DaysOfWeekBar extends StatelessWidget {
  final DateTime selectedDay;
  final ValueChanged<DateTime> onDaySelected;
  final VoidCallback? onPrevWeek;
  final VoidCallback? onNextWeek;
  const _DaysOfWeekBar({required this.selectedDay, required this.onDaySelected, this.onPrevWeek, this.onNextWeek});

  @override
  Widget build(BuildContext context) {
    final startOfWeek = selectedDay.subtract(Duration(days: selectedDay.weekday - 1));
    final cs = Theme.of(context).colorScheme;
    final baseStyle = Theme.of(context).textTheme.bodyMedium;
    final dayTextStyle = (baseStyle ?? const TextStyle()).copyWith(fontWeight: FontWeight.w600, fontSize: 16, height: 1);

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onHorizontalDragEnd: (details){
        final v = details.primaryVelocity ?? 0;
        if (v.abs() < 200) return;
        if (v < 0) { onNextWeek?.call(); } else { onPrevWeek?.call(); }
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        child: Row(
          children: [
            const SizedBox(width: kCalendarLeftReserve),
            Expanded(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: List.generate(7, (i) {
                  final day = startOfWeek.add(Duration(days: i));
                  final isSelected = day.year == selectedDay.year && day.month == selectedDay.month && day.day == selectedDay.day;
                  final isToday = DateTime.now().year==day.year && DateTime.now().month==day.month && DateTime.now().day==day.day;

                  Widget dayCircle() {
                    final showCircle = isSelected || isToday;
                    if (showCircle) {
                      return AnimatedContainer(
                        duration: const Duration(milliseconds:180),
                        width: 32, height: 32,
                        decoration: BoxDecoration(
                          color: isSelected ? cs.primary : (isToday ? cs.primary.withValues(alpha: 0.16) : Colors.transparent),
                          borderRadius: BorderRadius.circular(16),
                          border: isSelected ? null : (isToday ? Border.all(color: cs.primary.withValues(alpha: 0.32)) : null),
                        ),
                        alignment: Alignment.center,
                        child: Text(day.day.toString(), style: dayTextStyle.copyWith(color: isSelected ? cs.onPrimary : (isToday ? cs.primary : const Color(0xFF494949)))),
                      );
                    } else {
                      return SizedBox(
                        width: 32, height: 32,
                        child: Center(child: Text(day.day.toString(), style: dayTextStyle.copyWith(color: const Color(0xFF494949)))),
                      );
                    }
                  }

                  return Column(
                    children: [
                      Text(_weekdayShort(day.weekday), style: Theme.of(context).textTheme.labelSmall?.copyWith(fontWeight: FontWeight.w500, color: const Color(0xFF494949))),
                      const SizedBox(height:4),
                      // Ripple only on the circular day widget
                      Material(
                        color: Colors.transparent,
                        child: InkWell(
                          borderRadius: BorderRadius.circular(16),
                          onTap: () => onDaySelected(day),
                          child: dayCircle(),
                        ),
                      ),
                    ],
                  );
                }),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _weekdayShort(int weekday) {
    const days = ['P','W','Ś','C','P','S','N'];
    return days[weekday-1];
  }
}

// ignore: unused_element
class _ClassCard extends StatelessWidget {
  final ClassModel classModel;
  const _ClassCard({required this.classModel});

  String _formatTime(DateTime dt) {
    final t = dt.toLocal();
    final h = t.hour.toString().padLeft(2, '0');
    final m = t.minute.toString().padLeft(2, '0');
    return '$h:$m';
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: Colors.purple[50],
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              classModel.subject,
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                const Icon(Icons.access_time, size: 18),
                const SizedBox(width: 4),
                Text('${_formatTime(classModel.startTime)} - ${_formatTime(classModel.endTime)}'),
                const SizedBox(width: 16),
                if (classModel.room.isNotEmpty) ...[
                  const Icon(Icons.meeting_room, size: 18),
                  const SizedBox(width: 4),
                  Text(classModel.room),
                ],
                const Spacer(),
                // small dot on the right like in mock
                Container(width: 8, height: 8, decoration: BoxDecoration(color: Colors.brown, shape: BoxShape.circle)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
