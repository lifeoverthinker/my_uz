import 'package:flutter/material.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';
import 'package:my_uz/widgets/teacher_info_modal.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/widgets/schedule_app_bar.dart';

/// Widok planu nauczyciela (bardziej spójny ze stylem GroupScheduleScreen).
/// Różnice względem planu grupy:
/// - Brak podgrup
/// - Ikonka "info" pokazuje dane nauczyciela (TeacherInfoModal)
class TeacherScheduleScreen extends StatefulWidget {
  final String teacherId;
  final String teacherName;
  final VoidCallback? onToggleFavorite;
  const TeacherScheduleScreen({super.key, required this.teacherId, required this.teacherName, this.onToggleFavorite});

  @override
  State<TeacherScheduleScreen> createState() => _TeacherScheduleScreenState();
}

class _TeacherScheduleScreenState extends State<TeacherScheduleScreen> {
  bool _isFavorite = false;
  bool _favAnimating = false;
  late DateTime _selectedDay;
  List<ClassModel>? _loadedClasses;
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _selectedDay = DateTime.now();
    _loadFavoriteStatus();
    _loadClassesForDay();
  }

  Future<void> _showTeacherInfo() async {
    // Wyświetl modal z dodatkowymi informacjami o nauczycielu.
    await TeacherInfoModal.show(context, widget.teacherId, widget.teacherName);
  }

  Future<void> _loadFavoriteStatus() async {
    try {
      final key = 'teacher:${widget.teacherId}';
      final fav = await ClassesRepository.isFavorite(key);
      if (mounted) setState(() => _isFavorite = fav);
    } catch (_) { if (mounted) setState(() => _isFavorite = false); }
  }

  Future<void> _toggleFavorite() async {
    try {
      final key = 'teacher:${widget.teacherId}';
      final label = widget.teacherName.isNotEmpty ? widget.teacherName : widget.teacherId;
      await ClassesRepository.toggleFavorite(key, label: label);
      final fav = await ClassesRepository.isFavorite(key);
      if (mounted) {
        setState(() {
          _isFavorite = fav;
          _favAnimating = true;
        });
      }
      // powiadom rodzica (np. Drawer), aby mógł przeładować listę ulubionych
      try { widget.onToggleFavorite?.call(); } catch (_) {}

      Future.delayed(const Duration(milliseconds: 260), () { if (mounted) setState(() { _favAnimating = false; }); });

      // Feedback
      if (mounted) {
        final snack = _isFavorite ? 'Dodano do ulubionych' : 'Usunięto z ulubionych';
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(snack)));
      }
    } catch (_) {}
  }

  Future<void> _loadClassesForDay() async {
    if (!mounted) return;
    setState(() { _loading = true; });
    try {
      final list = await ClassesRepository.fetchTeacherDayWithWeekFallback(_selectedDay, teacherId: widget.teacherId);
      if (!mounted) return;
      setState(() { _loadedClasses = list; });
    } catch (e) {
      if (mounted) setState(() { _loadedClasses = <ClassModel>[]; });
    } finally {
      if (mounted) setState(() { _loading = false; });
    }
  }

  void _onDaySelected(DateTime d) {
    if (!mounted) return;
    setState(() { _selectedDay = d; });
    _loadClassesForDay();
  }

  DateTime _mondayOfWeek(DateTime d) {
    final wd = d.weekday; // 1=Mon
    return DateTime(d.year, d.month, d.day - (wd - 1));
  }

  void _goToNextWeekMonday() {
    final candidate = _selectedDay.add(const Duration(days: 7));
    final monday = _mondayOfWeek(candidate);
    _onDaySelected(monday);
  }

  void _goToPrevWeekMonday() {
    final candidate = _selectedDay.subtract(const Duration(days: 7));
    final monday = _mondayOfWeek(candidate);
    _onDaySelected(monday);
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final heartColor = _isFavorite ? cs.primary : Theme.of(context).iconTheme.color;

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: ScheduleAppBar(
        title: 'Plan nauczyciela',
        subtitle: widget.teacherName.isNotEmpty ? widget.teacherName : 'Nauczyciel',
        onBack: () => Navigator.of(context).pop(),
        actions: [
          ScheduleAppBarAction(
            icon: const Icon(Icons.info_outline),
            tooltip: 'Informacje o nauczycielu',
            onTap: _showTeacherInfo,
          ),
          ScheduleAppBarAction(
            icon: AnimatedScale(
              scale: _favAnimating ? 1.15 : 1.0,
              duration: const Duration(milliseconds: 220),
              curve: Curves.easeOutBack,
              child: Icon(_isFavorite ? Icons.favorite : Icons.favorite_border, color: heartColor),
            ),
            tooltip: _isFavorite ? 'Usuń z ulubionych' : 'Dodaj do ulubionych',
            onTap: _toggleFavorite,
          ),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Spójny pasek dni tygodnia (tak jak w GroupScheduleScreen)
          _DaysOfWeekBar(
            selectedDay: _selectedDay,
            onDaySelected: _onDaySelected,
            onPrevWeek: _goToPrevWeekMonday,
            onNextWeek: _goToNextWeekMonday,
          ),
          const Divider(height: 1),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : CalendarDayView(
              day: _selectedDay,
              classes: _loadedClasses ?? [],
              onNextDay: () => _onDaySelected(_selectedDay.add(const Duration(days: 1))),
              onPrevDay: () => _onDaySelected(_selectedDay.subtract(const Duration(days: 1))),
            ),
          ),
        ],
      ),
    );
  }
}

/// DaysOfWeekBar – spójny z implementacją w GroupScheduleScreen
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
            const SizedBox(width: 60), // kCalendarLeftReserve (aligned with CalendarDayView timeline)
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
                          color: isSelected ? cs.primary : (isToday ? cs.primary.withOpacity(0.16) : Colors.transparent),
                          borderRadius: BorderRadius.circular(16),
                          border: isSelected ? null : (isToday ? Border.all(color: cs.primary.withOpacity(0.32)) : null),
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