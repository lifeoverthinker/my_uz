import 'dart:async';
import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/providers/user_plan_provider.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/widgets/days_of_week_bar.dart';
import 'package:my_uz/widgets/schedule_app_bar.dart';

class GroupScheduleScreen extends StatefulWidget {
  final String? groupId;
  final String? groupCode;
  final String? groupName;

  const GroupScheduleScreen({
    super.key,
    this.groupId,
    this.groupCode,
    this.groupName,
  });

  @override
  State<GroupScheduleScreen> createState() => _GroupScheduleScreenState();
}

class _GroupScheduleScreenState extends State<GroupScheduleScreen> {
  late DateTime _selectedDay;
  List<ClassModel> _loadedClasses = [];
  bool _loadingClasses = false;
  String? _groupCode;
  String? _groupId;
  bool _isFav = false;
  bool _previewMode = false;

  // Podgrupy
  List<String> _allSubgroups = [];
  List<String> _selectedSubgroups = [];

  @override
  void initState() {
    super.initState();
    _selectedDay = DateTime.now();
    _previewMode = (widget.groupId != null && widget.groupId!.isNotEmpty) ||
        (widget.groupCode != null && widget.groupCode!.isNotEmpty);
    _init();
    UserPlanProvider.instance.addListener(_onPlanChanged);
  }

  @override
  void dispose() {
    UserPlanProvider.instance.removeListener(_onPlanChanged);
    super.dispose();
  }

  void _onPlanChanged() {
    if (!_previewMode) {
      _init();
    }
  }

  Future<void> _init() async {
    await _loadContext();
    await _loadAllSubgroups(); // załaduj listę dostępnych subgrup dla multiselectu
    await _loadClassesForDay();
  }

  Future<void> _loadContext() async {
    String? code;
    String? id;
    List<String> subs = const [];

    if (_previewMode) {
      code = widget.groupCode;
      id = widget.groupId;
    } else {
      final (ctxCode, ctxSubs, ctxId) = await ClassesRepository.loadGroupContext();
      code = ctxCode;
      subs = ctxSubs;
      id = ctxId;
    }

    bool isFav = false;
    final favKey = ClassesRepository.canonicalFavKey(groupId: id, groupCode: code);
    if (favKey.contains(':')) {
      isFav = await ClassesRepository.isFavorite(favKey);
    }

    if (!mounted) return;
    setState(() {
      _groupCode = code;
      _groupId = id;
      _selectedSubgroups = List.from(subs);
      _isFav = isFav;
    });
  }

  Future<void> _loadAllSubgroups() async {
    final code = _previewMode ? widget.groupCode : _groupCode;
    final list = (code == null || code.isEmpty)
        ? <String>[]
        : await ClassesRepository.getSubgroupsForGroup(code);
    if (mounted) {
      setState(() {
        _allSubgroups = list;
        // Jeżeli brak wybranych, domyślnie nic (lub wszystkie – w zależności od polityki)
        if (_selectedSubgroups.isEmpty && list.isNotEmpty) {
          _selectedSubgroups = List.from(list);
        }
      });
    }
  }

  Future<void> _toggleFavorite() async {
    final key = ClassesRepository.canonicalFavKey(
      groupId: _groupId ?? widget.groupId,
      groupCode: _groupCode ?? widget.groupCode,
    );
    if (key.contains(':')) {
      final label = await _getGroupLabel();
      await ClassesRepository.toggleFavorite(key, label: label);
      final fav = await ClassesRepository.isFavorite(key);
      if (mounted) setState(() => _isFav = fav);
    }
  }

  Future<String?> _getGroupLabel() async {
    if ((_groupCode ?? widget.groupCode) != null) {
      return (_groupCode ?? widget.groupCode);
    }
    if ((_groupId ?? widget.groupId) != null) {
      try {
        final meta = await ClassesRepository.getGroupById((_groupId ?? widget.groupId)!);
        return meta?['kod_grupy'] as String?;
      } catch (_) {}
    }
    return null;
  }

  Future<void> _loadClassesForDay() async {
    if (_previewMode && widget.groupId == null && widget.groupCode == null) {
      if (mounted) setState(() => _loadedClasses = []);
      return;
    }
    if (!_previewMode && _groupId == null && (_groupCode == null || _groupCode!.isEmpty)) {
      if (mounted) setState(() => _loadedClasses = []);
      return;
    }

    setState(() => _loadingClasses = true);
    try {
      final list = await ClassesRepository.fetchDayWithWeekFallback(
        _selectedDay,
        groupCode: _previewMode ? widget.groupCode : _groupCode,
        subgroups: _previewMode ? (_selectedSubgroups) : _selectedSubgroups,
        groupId: _previewMode ? widget.groupId : _groupId,
      );
      if (mounted) setState(() => _loadedClasses = list);
    } finally {
      if (mounted) setState(() => _loadingClasses = false);
    }
  }

  void _onDaySelected(DateTime day) {
    setState(() {
      _selectedDay = day;
    });
    _loadClassesForDay();
  }

  DateTime _mondayOfWeek(DateTime d) => DateTime(d.year, d.month, d.day - (d.weekday - 1));
  void _nextWeek() => _onDaySelected(_mondayOfWeek(_selectedDay.add(const Duration(days: 7))));
  void _prevWeek() => _onDaySelected(_mondayOfWeek(_selectedDay.subtract(const Duration(days: 7))));

  Widget _buildSubgroupChips() {
    if (_allSubgroups.isEmpty) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      child: Wrap(
        spacing: 8,
        runSpacing: 8,
        children: _allSubgroups.map((s) {
          final sel = _selectedSubgroups.contains(s);
          return FilterChip(
            label: Text(s),
            selected: sel,
            onSelected: (v) async {
              setState(() {
                if (v) {
                  _selectedSubgroups.add(s);
                } else {
                  _selectedSubgroups.remove(s);
                }
              });
              await _loadClassesForDay();
              // zapisz do ulubionych (jeśli to ulubiona grupa)
              final key = ClassesRepository.canonicalFavKey(
                groupId: _groupId ?? widget.groupId,
                groupCode: _groupCode ?? widget.groupCode,
              );
              if (key.startsWith('group:')) {
                await ClassesRepository.setValidatedSubgroupsForFavorite(key, _selectedSubgroups);
              }
            },
          );
        }).toList(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final heartColor = _isFav ? cs.primary : null;
    final bool hasGroup = (_previewMode && ((widget.groupId ?? '').isNotEmpty || (widget.groupCode ?? '').isNotEmpty)) ||
        (!_previewMode && (((_groupId ?? '').isNotEmpty) || ((_groupCode ?? '').isNotEmpty)));
    final subtitle = widget.groupName ?? widget.groupCode ?? _groupCode ?? '—';

    final actions = [
      if (hasGroup)
        ScheduleAppBarAction(
          icon: Icon(_isFav ? Icons.favorite : Icons.favorite_border, color: heartColor),
          tooltip: _isFav ? 'Usuń z ulubionych' : 'Dodaj do ulubionych',
          onTap: _toggleFavorite,
        ),
    ];

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: ScheduleAppBar(
        title: 'Plan grupy',
        subtitle: subtitle,
        onBack: () => Navigator.of(context).pop(),
        actions: actions,
      ),
      body: Column(
        children: [
          // Multiselect podgrup nad paskiem dni
          _buildSubgroupChips(),

          // Pasek dni
          DaysOfWeekBar(
            selectedDay: _selectedDay,
            onDaySelected: _onDaySelected,
            onPrevWeek: _prevWeek,
            onNextWeek: _nextWeek,
            leftReserve: 60.0,
          ),

          Expanded(
            child: _loadingClasses
                ? const Center(child: CircularProgressIndicator())
                : CalendarDayView(
              day: _selectedDay,
              classes: _loadedClasses,
              onNextDay: () => _onDaySelected(_selectedDay.add(const Duration(days: 1))),
              onPrevDay: () => _onDaySelected(_selectedDay.subtract(const Duration(days: 1))),
            ),
          ),
        ],
      ),
    );
  }
}
