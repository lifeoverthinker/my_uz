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
    setState(() { _loading = true; });
    try {
      final list = await ClassesRepository.fetchTeacherDayWithWeekFallback(
        _selectedDay,
        teacherId: widget.teacherId,
      );
      if (mounted) setState(() { _loadedClasses = list; });
    } finally {
      if (mounted) setState(() { _loading = false; });
    }
  }

  void _onDaySelected(DateTime day) {
    setState(() { _selectedDay = day; });
    _loadClassesForDay();
  }

  void _onInfoTap() {
    TeacherInfoModal.show(context, widget.teacherId, widget.teacherName);
  }

  DateTime _mondayOfWeek(DateTime d) => DateTime(d.year, d.month, d.day - (d.weekday - 1));
  void _nextWeek() => _onDaySelected(_mondayOfWeek(_selectedDay.add(const Duration(days: 7))));
  void _prevWeek() => _onDaySelected(_mondayOfWeek(_selectedDay.subtract(const Duration(days: 7))));

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final heartColor = _isFav ? cs.primary : null;
    final dayClasses = _loadedClasses.where((c) {
      final d = c.startTime.toLocal();
      return d.year == _selectedDay.year && d.month == _selectedDay.month && d.day == _selectedDay.day;
    }).toList();

    final actions = <ScheduleAppBarAction>[
      ScheduleAppBarAction(
        icon: const Icon(MyUz.info_circle),
        tooltip: 'Informacje o nauczycielu',
        onTap: _onInfoTap,
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
          // Bez dodatkowego odstępu – jak w kalendarzu
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