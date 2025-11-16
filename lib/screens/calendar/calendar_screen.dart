// Plik: lib/screens/calendar/calendar_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/providers/calendar_provider.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:provider/provider.dart';

import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/screens/calendar/components/calendar_view.dart';
import 'package:my_uz/screens/calendar/group_schedule_screen.dart';
import 'package:my_uz/screens/calendar/teacher_schedule_screen.dart';
import 'package:my_uz/screens/calendar/search_schedule_screen.dart';
import 'package:my_uz/screens/calendar/tasks_screen.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/utils/date_utils.dart';
import 'package:my_uz/widgets/top_menu_button.dart';
import 'dart:async';

const double kCalendarLeftReserve = 60;

class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> with WidgetsBindingObserver {
  bool _isMonthView = false;
  // --- POPRAWKA: Stan początkowy jest ustawiany w initState ---
  late String _activeSection; // 'calendar' | 'schedule'
  // --- KONIEC POPRAWKI ---
  bool _drawerOpen = false;

  DateTime _selectedDay = stripTime(DateTime.now());
  DateTime _focusedDay = stripTime(DateTime.now());

  List<ClassModel> _allClasses = [];
  String? _overrideGroupCode;
  String? _overrideGroupId;
  List<String>? _overrideSubgroups;

  bool _loading = false;
  bool _error = false;
  String? _errorMsg;

  bool _drawerLoading = true;
  List<Map<String, String>> _favorites = [];
  StreamSubscription? _favoritesSubscription;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);

    // --- POPRAWKA: Ustaw stan początkowy na podstawie providera ---
    final calendarProvider = context.read<CalendarProvider>();
    // Odczytaj żądaną sekcję (np. ustawioną przez HomeScreen)
    _activeSection = calendarProvider.initialSection;
    // Zresetuj wartość w providerze, aby nie wpływała na przyszłe nawigacje
    calendarProvider.initialSection = 'calendar';
    // --- KONIEC POPRAWKI ---

    final tasksProvider = context.read<TasksProvider>();

    _ensureWeekLoaded(calendarProvider, _focusedDay);
    _loadDrawerData();
    _favoritesSubscription = ClassesRepository.favoritesStream.listen((_) => _loadDrawerData());

    tasksProvider.refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _favoritesSubscription?.cancel();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      final today = stripTime(DateTime.now());
      if (!mounted) return;
      setState(() {
        _focusedDay = today;
        _selectedDay = today;
      });
      _ensureWeekLoaded(context.read<CalendarProvider>(), today);
    }
  }

  Future<void> _ensureWeekLoaded(CalendarProvider provider, DateTime anyDay) async {
    final monday = mondayOfWeek(anyDay);
    if (provider.weekCache.containsKey(monday)) {
      if (!mounted) return;
      setState(() {
        _allClasses = provider.weekCache[monday]!;
        _loading = provider.loading;
        _error = provider.error;
        _errorMsg = provider.lastError;
      });
      return;
    }

    if (!mounted) return;
    setState(() {
      _loading = true;
      _error = false;
      _errorMsg = null;
    });

    await provider.ensureWeekLoaded(anyDay,
        overrideGroupCode: _overrideGroupCode,
        overrideGroupId: _overrideGroupId,
        overrideSubgroups: _overrideSubgroups);

    if (!mounted) return;
    setState(() {
      _allClasses = provider.weekCache[monday] ?? [];
      _loading = provider.loading;
      _error = provider.error;
      _errorMsg = provider.lastError;
    });
  }

  void _selectDay(DateTime day) {
    final d0 = stripTime(day);
    if (!mounted) return;
    setState(() {
      _selectedDay = d0;
      _focusedDay = d0;
    });
    _ensureWeekLoaded(context.read<CalendarProvider>(), d0);
  }

  void _prevWeek() {
    final candidate = _focusedDay.subtract(const Duration(days: 7));
    final monday = mondayOfWeek(candidate);
    if (!mounted) return;
    setState(() {
      _focusedDay = monday;
      _selectedDay = monday;
    });
    _ensureWeekLoaded(context.read<CalendarProvider>(), monday);
  }

  void _nextWeek() {
    final candidate = _focusedDay.add(const Duration(days: 7));
    final monday = mondayOfWeek(candidate);
    if (!mounted) return;
    setState(() {
      _focusedDay = monday;
      _selectedDay = monday;
    });
    _ensureWeekLoaded(context.read<CalendarProvider>(), monday);
  }

  void _prevDay() => _selectDay(_selectedDay.subtract(const Duration(days: 1)));
  void _nextDay() => _selectDay(_selectedDay.add(const Duration(days: 1)));

  void _prevMonth() {
    if (!mounted) return;
    setState(() {
      final d = DateTime(_focusedDay.year, _focusedDay.month - 1, 1);
      _focusedDay = d;
      if (_isMonthView) _selectedDay = DateTime(d.year, d.month, 1);
    });
    _ensureWeekLoaded(context.read<CalendarProvider>(), _focusedDay);
  }

  void _nextMonth() {
    if (!mounted) return;
    setState(() {
      final d = DateTime(_focusedDay.year, _focusedDay.month + 1, 1);
      _focusedDay = d;
      if (_isMonthView) _selectedDay = DateTime(d.year, d.month, 1);
    });
    _ensureWeekLoaded(context.read<CalendarProvider>(), _focusedDay);
  }

  Future<void> _loadDrawerData() async {
    if (!mounted) return;
    setState(() => _drawerLoading = true);
    try {
      final favs = await ClassesRepository.loadFavorites();
      final labels = await ClassesRepository.loadFavoriteLabels();

      final out = <Map<String, String>>[];
      for (final key in favs) {
        final parts = key.split(':');
        if (parts.length != 2) continue;
        final type = parts[0];
        final token = parts[1];
        String label = labels[key] ?? '';

        if (type == 'group') {
          if (label.isEmpty) {
            if (ClassesRepository.uuidRe.hasMatch(token)) {
              try {
                final meta = await ClassesRepository.getGroupById(token);
                if (meta != null) label = (meta['kod_grupy'] as String?) ?? (meta['nazwa'] as String?) ?? token;
              } catch (_) {}
            } else {
              label = token;
            }
          }
        } else if (type == 'teacher') {
          if (label.isEmpty) {
            try {
              final meta = await ClassesRepository.getTeacherDetails(token);
              if (meta != null) label = (meta['nazwa'] as String?) ?? token;
            } catch (_) {
              label = token;
            }
          }
        }

        if (label.isEmpty) label = token;
        out.add({'type': type, 'id': token, 'label': label});
      }

      if (mounted) setState(() => _favorites = out);
    } catch (_) {
      if (mounted) setState(() => _favorites = []);
    } finally {
      if (mounted) setState(() => _drawerLoading = false);
    }
  }

  void _toggleMonthView() {
    if (!mounted) return;
    setState(() => _isMonthView = !_isMonthView);
  }

  @override
  Widget build(BuildContext context) {
    final classesByDay = _groupByDay(_allClasses);
    final selectedClasses = classesByDay[_selectedDay] ?? const [];
    final monthTitle = DateFormat('LLLL', 'pl').format(_focusedDay);

    Widget topBar;
    if (_activeSection == 'calendar') {
      topBar = _CalendarTopBar(
        monthTitle: monthTitle,
        isMonthOpen: _isMonthView,
        onTapMonth: _toggleMonthView,
        onMenu: () => setState(() => _drawerOpen = true),
        onSearch: () {
          Navigator.of(context).push(MaterialPageRoute(builder: (_) => const SearchScheduleScreen()));
        },
        onAdd: () async {
          if (mounted) {
            await context.read<TasksProvider>().showAddTaskSheet(context);
          }
        },
      );
    } else {
      topBar = _GenericTopBar(
        title: _activeSection == 'schedule' ? 'Terminarz' : 'Kalendarz',
        onMenu: () => setState(() => _drawerOpen = true),
        actions: _activeSection == 'schedule'
            ? [
          _CircleIconButton(
              icon: const Icon(Icons.add),
              onPressed: () async {
                if (mounted) {
                  await context.read<TasksProvider>().showAddTaskSheet(context);
                }
              }),
          const SizedBox(width: 8),
          _CircleIconButton(icon: const Icon(Icons.more_vert), onPressed: () {}),
        ]
            : [],
      );
    }

    return Scaffold(
      backgroundColor: Colors.white,
      body: Stack(children: [
        if (!_drawerOpen)
          Positioned(
            left: 0,
            top: 0,
            bottom: 0,
            width: 24,
            child: GestureDetector(
              behavior: HitTestBehavior.translucent,
              onHorizontalDragUpdate: (details) {
                if (details.delta.dx > 16) setState(() => _drawerOpen = true);
              },
              onTap: () {},
            ),
          ),
        SafeArea(
          left: false,
          bottom: false,
          child: Column(children: [
            topBar,
            if (_activeSection == 'calendar') ...[
              const _DaysOfWeekHeader(),
              CalendarView(
                focusedDay: _focusedDay,
                selectedDay: _selectedDay,
                isWeekView: !_isMonthView,
                classesByDay: classesByDay,
                onDaySelected: _selectDay,
                onPrevWeek: _prevWeek,
                onNextWeek: _nextWeek,
                onPrevMonth: _prevMonth,
                onNextMonth: _nextMonth,
                enableSwipe: true,
              ),
            ],
            if (_activeSection == 'calendar')
              Expanded(
                child: _loading
                    ? const Center(child: CircularProgressIndicator())
                    : _error
                    ? Center(child: Text(_errorMsg ?? 'Błąd ładowania danych'))
                    : AnimatedSwitcher(
                  duration: const Duration(milliseconds: 220),
                  child: SizedBox(
                    // --- POPRAWKA TUTAJ ---
                    key: ValueKey<String>(_selectedDay.toIso8601String()),
                    // --- KONIEC POPRAWKI ---
                    child: CalendarDayView(day: _selectedDay, classes: selectedClasses, onNextDay: _nextDay, onPrevDay: _prevDay),
                  ),
                ),
              )
            else
              Expanded(
                child: _activeSection == 'schedule' ? const TasksScreen(showAppBar: false) : Center(child: Text('Sekcja: $_activeSection')),
              ),
          ]),
        ),
        if (_drawerOpen)
          Positioned.fill(
            child: GestureDetector(onTap: () => setState(() => _drawerOpen = false), child: Container(color: Colors.black.withOpacity(0.32))),
          ),
        AnimatedPositioned(
          duration: const Duration(milliseconds: 340),
          curve: Curves.easeOutCubic,
          left: _drawerOpen ? 0 : -300,
          top: 0,
          bottom: 0,
          width: 288,
          child: _CalendarDrawer(
            activeSection: _activeSection,
            onSelectSection: (s) => setState(() {
              _activeSection = s;
              _drawerOpen = false;
              if (s == 'calendar') {
                _loadDrawerData();
              }
            }),
            favorites: _favorites,
            loading: _drawerLoading,
          ),
        ),
      ]),
    );
  }

  Map<DateTime, List<ClassModel>> _groupByDay(List<ClassModel> list) {
    final map = <DateTime, List<ClassModel>>{};
    for (final c in list) {
      final key = stripTime(c.startTime.toLocal());
      map.putIfAbsent(key, () => []).add(c);
    }
    for (final v in map.values) {
      v.sort((a, b) => a.startTime.compareTo(b.startTime));
    }
    return map;
  }
}

class _DaysOfWeekHeader extends StatelessWidget {
  const _DaysOfWeekHeader();

  @override
  Widget build(BuildContext context) {
    const days = ['P', 'W', 'Ś', 'C', 'P', 'S', 'N'];
    final textStyle = Theme.of(context).textTheme.labelSmall?.copyWith(fontWeight: FontWeight.w500, color: const Color(0xFF494949));
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
      child: Row(
        children: [
          const SizedBox(width: kCalendarLeftReserve),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: days.map((day) => Text(day, style: textStyle)).toList(),
            ),
          ),
        ],
      ),
    );
  }
}

class _CircleIconButton extends StatelessWidget {
  final Widget icon;
  final VoidCallback onPressed;
  const _CircleIconButton({required this.icon, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      customBorder: const CircleBorder(),
      onTap: onPressed,
      child: Container(
        width: 48,
        height: 48,
        decoration: const BoxDecoration(
          color: Color(0xFFF7F2F9),
          shape: BoxShape.circle,
        ),
        alignment: Alignment.center,
        child: icon,
      ),
    );
  }
}

class _CalendarTopBar extends StatelessWidget {
  final String monthTitle;
  final bool isMonthOpen;
  final VoidCallback onTapMonth;
  final VoidCallback onMenu;
  final VoidCallback onSearch;
  final VoidCallback onAdd;

  const _CalendarTopBar({
    required this.monthTitle,
    required this.isMonthOpen,
    required this.onTapMonth,
    required this.onMenu,
    required this.onSearch,
    required this.onAdd,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 4),
      child: Row(
        children: [
          TopMenuButton(onTap: onMenu),
          const SizedBox(width: 12),
          GestureDetector(
            onTap: onTapMonth,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  monthTitle.isEmpty ? '' : monthTitle[0].toUpperCase() + monthTitle.substring(1),
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w600, color: Color(0xFF1D1B20)),
                ),
                const SizedBox(width: 4),
                Icon(isMonthOpen ? MyUz.chevron_up : MyUz.chevron_down, size: 20),
              ],
            ),
          ),
          const Spacer(),
          _CircleIconButton(icon: const Icon(Icons.search), onPressed: onSearch),
          const SizedBox(width: 8),
          _CircleIconButton(icon: const Icon(Icons.add), onPressed: onAdd),
        ],
      ),
    );
  }
}

class _GenericTopBar extends StatelessWidget {
  final String title;
  final VoidCallback onMenu;
  final List<Widget> actions;

  const _GenericTopBar({required this.title, required this.onMenu, this.actions = const []});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 4),
      child: Row(children: [
        TopMenuButton(onTap: onMenu),
        const SizedBox(width: 12),
        Text(title, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w600, color: Color(0xFF1D1B20))),
        const Spacer(),
        ...actions,
      ]),
    );
  }
}

class _CalendarDrawer extends StatelessWidget {
  final String activeSection;
  final ValueChanged<String> onSelectSection;
  final List<Map<String, String>> favorites;
  final bool loading;

  const _CalendarDrawer({
    required this.activeSection,
    required this.onSelectSection,
    required this.favorites,
    required this.loading,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 16,
      color: Colors.white,
      borderRadius: const BorderRadius.only(topRight: Radius.circular(16), bottomRight: Radius.circular(16)),
      clipBehavior: Clip.antiAlias,
      child: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Text('Menu', style: TextStyle(fontWeight: FontWeight.w500)),
              ),
              _DrawerItem(
                label: 'Kalendarz',
                icon: MyUz.calendar,
                isActive: activeSection == 'calendar',
                onTap: () => onSelectSection('calendar'),
              ),
              _DrawerItem(
                label: 'Terminarz',
                icon: MyUz.book_open_01,
                isActive: activeSection == 'schedule',
                onTap: () => onSelectSection('schedule'),
              ),
              const Divider(height: 24),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Text('Ulubione', style: TextStyle(fontWeight: FontWeight.w500)),
              ),
              if (loading)
                const Center(child: Padding(padding: EdgeInsets.all(16.0), child: CircularProgressIndicator()))
              else if (favorites.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text('Brak ulubionych planów.', style: TextStyle(color: Colors.grey)),
                )
              else
                ...favorites.map((fav) {
                  final isGroup = fav['type'] == 'group';
                  return _DrawerItem(
                    label: fav['label'] ?? 'Brak etykiety',
                    icon: isGroup ? MyUz.users_01 : MyUz.user_01,
                    onTap: () {
                      Navigator.of(context).pop();
                      if (isGroup) {
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (_) => GroupScheduleScreen(groupId: fav['id']!, groupCode: fav['label']!),
                        ));
                      } else {
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (_) => TeacherScheduleScreen(teacherId: fav['id']!, teacherName: fav['label']!),
                        ));
                      }
                    },
                  );
                }),
            ],
          ),
        ),
      ),
    );
  }
}

class _DrawerItem extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isActive;
  final VoidCallback onTap;

  const _DrawerItem({required this.label, required this.icon, this.isActive = false, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: isActive ? Theme.of(context).primaryColor.withOpacity(0.1) : Colors.transparent,
      borderRadius: BorderRadius.circular(100),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(100),
        child: Container(
          height: 56,
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              Icon(icon, color: isActive ? Theme.of(context).primaryColor : const Color(0xFF49454F)),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontWeight: isActive ? FontWeight.w600 : FontWeight.w500,
                    color: isActive ? Theme.of(context).primaryColor : const Color(0xFF49454F),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}