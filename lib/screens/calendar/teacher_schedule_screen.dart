import 'package:flutter/material.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/widgets/schedule_app_bar.dart';
import 'package:my_uz/widgets/days_of_week_bar.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/widgets/teacher_info_modal.dart';

class TeacherScheduleScreen extends StatefulWidget {
  final String teacherId;
  final String teacherName;

  const TeacherScheduleScreen({
    super.key,
    required this.teacherId,
    required this.teacherName,
  });

  @override
  State<TeacherScheduleScreen> createState() => _TeacherScheduleScreenState();
}

class _TeacherScheduleScreenState extends State<TeacherScheduleScreen> {
  late DateTime _selectedDay;
  List<ClassModel> _loadedClasses = [];
  bool _loading = false;
  bool _isFav = false;

  // Multiselect podgrup (wyznaczane z danych)
  List<String> _allSubgroups = [];
  List<String> _selectedSubgroups = [];

  @override
  void initState() {
    super.initState();
    _selectedDay = DateTime.now();
    _loadFavoriteStatus();
    _loadClassesForDay();
  }

  Future<void> _loadFavoriteStatus() async {
    try {
      final key = 'teacher:${widget.teacherId}';
      final fav = await ClassesRepository.isFavorite(key);
      if (mounted) setState(() => _isFav = fav);
    } catch (_) {
      if (mounted) setState(() => _isFav = false);
    }
  }

  Future<void> _toggleFavorite() async {
    try {
      final key = 'teacher:${widget.teacherId}';
      await ClassesRepository.toggleFavorite(key, label: widget.teacherName);
      final fav = await ClassesRepository.isFavorite(key);
      if (mounted) setState(() => _isFav = fav);
    } catch (_) {}
  }

  Future<void> _loadClassesForDay() async {
    setState(() {
      _loading = true;
    });
    try {
      final list = await ClassesRepository.fetchTeacherDayWithWeekFallback(
        _selectedDay,
        teacherId: widget.teacherId,
      );
      // Wyznacz dostępne podgrupy z danych dnia (lub tygodnia, jeśli wolisz)
      final allSubs = <String>{};
      for (final c in list) {
        final s = (c.subgroup ?? '').trim();
        if (s.isNotEmpty) allSubs.add(s);
      }
      if (mounted) {
        setState(() {
          _loadedClasses = list;
          _allSubgroups = allSubs.toList()..sort();
          if (_selectedSubgroups.isEmpty && _allSubgroups.isNotEmpty) {
            _selectedSubgroups = List.from(_allSubgroups);
          }
        });
      }
    } finally {
      if (mounted) setState(() => _loading = false);
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
            onSelected: (v) {
              setState(() {
                if (v) {
                  _selectedSubgroups.add(s);
                } else {
                  _selectedSubgroups.remove(s);
                }
              });
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

    // Filtrowanie wyników wg wybranych podgrup
    final dayClasses = _loadedClasses.where((c) {
      final d = c.startTime.toLocal();
      final isSameDay = d.year == _selectedDay.year && d.month == _selectedDay.month && d.day == _selectedDay.day;
      if (!isSameDay) return false;
      final s = (c.subgroup ?? '').trim();
      if (_allSubgroups.isEmpty) return true; // brak rozróżnienia -> wszystko
      if (_selectedSubgroups.isEmpty) return false; // nic wybrane -> nic
      if (s.isEmpty) return true; // zajęcia bez podgrupy -> pokaż zawsze
      return _selectedSubgroups.contains(s);
    }).toList();

    final actions = [
      ScheduleAppBarAction(
        icon: const Icon(MyUz.info_circle),
        tooltip: 'Informacje o nauczycielu',
        onTap: () => TeacherInfoModal.show(context, widget.teacherId, widget.teacherName),
      ),
      ScheduleAppBarAction(
        icon: Icon(_isFav ? Icons.favorite : Icons.favorite_border, color: heartColor),
        tooltip: _isFav ? 'Usuń z ulubionych' : 'Dodaj do ulubionych',
        onTap: _toggleFavorite,
      ),
    ];

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: ScheduleAppBar(
        title: 'Plan nauczyciela',
        subtitle: widget.teacherName,
        onBack: () => Navigator.of(context).pop(),
        actions: actions,
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
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
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : CalendarDayView(
              day: _selectedDay,
              classes: dayClasses,
              onNextDay: () => _onDaySelected(_selectedDay.add(const Duration(days: 1))),
              onPrevDay: () => _onDaySelected(_selectedDay.subtract(const Duration(days: 1))),
            ),
          ),
        ],
      ),
    );
  }
}
