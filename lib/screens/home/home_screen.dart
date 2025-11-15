// Plik: lib/screens/home/home_screen.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:my_uz/services/classes_repository.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

// MODELE
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/models/event_model.dart';

// Szczegóły/edycja
import 'package:my_uz/screens/home/details/class_details.dart';
import 'package:my_uz/screens/home/details/event_details.dart';

// SEKCJE
import 'components/upcoming_classes.dart';
import 'components/tasks_section.dart';
import 'components/events_section.dart';

// Import dla filtrowania dat
import 'package:my_uz/utils/date_utils.dart' as date_utils;

/// HomeScreen – Dashboard (Figma – obraz 4)
class HomeScreen extends StatefulWidget {
  final VoidCallback onOpenDrawer;
  final Function(int) onNavigateToCalendar;

  const HomeScreen({
    super.key,
    required this.onOpenDrawer,
    required this.onNavigateToCalendar
  });
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

// Klucze personalizacji
const String _kPrefOnbMode = 'onb_mode';
const String _kPrefOnbSalutation = 'onb_salutation';
const String _kPrefOnbFirst = 'onb_first';

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  String _greetingName = 'Student';
  String? _greetingSubtitle;
  String? _groupCode;
  List<String> _subgroups = [];
  bool _loading = true;
  bool _classesLoading = false;
  DateTime? _classesForDate;
  Timer? _refreshTimer;

  List<ClassModel> _classes = const [];
  List<TaskModel> _tasks = const []; // Ta lista będzie teraz filtrowana
  List<EventModel> _events = const [];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TasksProvider>().refresh();
    });
    WidgetsBinding.instance.addObserver(this);
    _loadPrefs();
    _loadTodayClasses();

    _refreshTimer = Timer.periodic(const Duration(minutes: 1), (_) {
      final now = DateTime.now();
      if (_classesForDate == null) {
        _loadTodayClasses();
        return;
      }
      final shown = DateTime(_classesForDate!.year, _classesForDate!.month, _classesForDate!.day);
      final today = DateTime(now.year, now.month, now.day);
      if (shown != today) {
        _loadTodayClasses();
      }
    });
  }

  Future<void> _loadPrefs() async {
    // ... (bez zmian)
    try {
      final p = await SharedPreferences.getInstance();
      final mode = p.getString(_kPrefOnbMode);
      final first = p.getString(_kPrefOnbFirst)?.trim();
      final sal = p.getString(_kPrefOnbSalutation)?.trim();

      final (String? groupCode, List<String> subs) = await ClassesRepository.loadGroupPrefs();

      String? subtitle;
      try {
        final meta = await ClassesRepository.loadGroupMeta();
        if (meta != null) {
          final parts = <String>[];
          if ((meta['wydzial'] ?? meta['wydzial_nazwa'] ?? meta['wydzial_name']) != null) parts.add((meta['wydzial'] ?? meta['wydzial_nazwa'] ?? meta['wydzial_name']).toString());
          if ((meta['kierunek'] ?? meta['kierunek_nazwa'] ?? meta['kierunek_name']) != null) parts.add((meta['kierunek'] ?? meta['kierunek_nazwa'] ?? meta['kierunek_name']).toString());
          if ((meta['tryb'] ?? meta['forma'] ?? meta['forma_studiow']) != null) parts.add((meta['tryb'] ?? meta['forma'] ?? meta['forma_studiow']).toString());
          if (parts.isNotEmpty) subtitle = parts.join(' • ');
        }
      } catch (_) {
        subtitle = null;
      }

      String name = 'Student';
      if (mode == 'data') {
        if (first != null && first.isNotEmpty) {
          name = first;
        } else if (sal != null && sal.isNotEmpty) {
          name = sal;
        }
      } else {
        if (sal != null && sal.isNotEmpty) {
          name = sal;
        }
      }

      if (!mounted) return;
      setState(() {
        _greetingName = name;
        _groupCode = (groupCode != null && groupCode.isNotEmpty) ? groupCode : null;
        _subgroups = subs;
        _greetingSubtitle = subtitle;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _loading = false);
    }
  }

  Future<void> _loadTodayClasses() async {
    // ... (bez zmian)
    if (_classesLoading) return;
    setState(() => _classesLoading = true);
    try {
      final (groupCode, subgroups, groupId) = await ClassesRepository.loadGroupContext();
      final now = DateTime.now();
      final today = DateTime(now.year, now.month, now.day);
      final todayList = await ClassesRepository.fetchDayWithWeekFallback(today, groupCode: groupCode, subgroups: subgroups, groupId: groupId);
      final remaining = ClassesRepository.filterRemainingOrAll(todayList, today, now, allowEndedIfAllEnded: false);
      if (!mounted) return;
      setState(() {
        _classes = remaining;
        _classesForDate = today;
        _classesLoading = false;
      });
    } catch (e) {
      debugPrint('[Home][_loadTodayClasses][ERR] $e');
      if (!mounted) return;
      setState(() => _classesLoading = false);
    }
  }

  void _onTapClass(ClassModel c) async {
    // ... (bez zmian)
    await ClassDetailsSheet.open(context, c);
  }

  void _onTapEvent(EventModel e) {
    // ... (bez zmian)
    EventDetailsSheet.open(context, e);
  }

  Future<void> _openTaskDetails(TaskModel task) async {
    // ... (bez zmian)
    if (!mounted) return;
    await context.read<TasksProvider>().openTaskDetails(context, task);
  }

  @override
  void dispose() {
    // ... (bez zmian)
    WidgetsBinding.instance.removeObserver(this);
    _refreshTimer?.cancel();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // ... (bez zmian)
    if (state == AppLifecycleState.resumed) {
      _loadPrefs();
      _loadTodayClasses();
      if (mounted) context.read<TasksProvider>().refresh();
    }
  }

  @override
  Widget build(BuildContext context) {
    // 1. POPRAWKA: Pobranie i filtrowanie zadań
    final tasksProvider = context.watch<TasksProvider>();
    final allTasks = tasksProvider.items.map((e) => e.model).toList();

    // Definiuj bieżący tydzień
    final now = DateTime.now();
    final monday = date_utils.mondayOfWeek(now);
    final sunday = monday.add(const Duration(days: 7));

    // Pokaż aktywne zadania z bieżącego tygodnia
    _tasks = allTasks.where((task) {
      final deadline = task.deadline;
      return !task.completed &&
          (deadline.isAfter(monday) || date_utils.isSameDay(deadline, monday)) &&
          deadline.isBefore(sunday);
    }).toList();

    // Sortuj wg daty
    _tasks.sort((a, b) => a.deadline.compareTo(b.deadline));


    final cs = Theme.of(context).colorScheme;
    const double footerTopSpacing = 10;

    return Container(
      color: Colors.white,
      child: _loading
          ? const Center(child: CircularProgressIndicator())
          : CustomScrollView(
        physics: const ClampingScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Container(
              color: cs.surface,
              child: _Header(
                dateText: _plDate(DateTime.now()),
                greetingName: _greetingName,
                subtitle: _greetingSubtitle,
                onOpenDrawer: widget.onOpenDrawer,
              ),
            ),
          ),
          SliverFillRemaining(
            hasScrollBody: false,
            child: Transform.translate(
              offset: const Offset(0, -8),
              child: Material(
                color: Colors.white,
                clipBehavior: Clip.antiAlias,
                shape: const RoundedRectangleBorder(
                  borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
                ),
                child: _ContentContainer(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Builder(
                        builder: (context) {
                          const header = 'Najbliższe zajęcia';
                          final emptyMsg = _classesLoading
                              ? ''
                              : (_classes.isEmpty
                              ? (_groupCode == null ? 'Wybierz grupę w ustawieniach.' : 'Dziś brak nadchodzących zajęć')
                              : null);
                          return UpcomingClassesSection(
                            classes: _classes,
                            onTap: _onTapClass,
                            groupCode: _groupCode,
                            subgroups: _subgroups,
                            headerTitle: header,
                            isLoading: _classesLoading,
                            emptyMessage: emptyMsg,
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      TasksSection(
                        tasks: _tasks, // Przekaż przefiltrowaną listę
                        onTap: _openTaskDetails,
                        onGoToTasks: () => widget.onNavigateToCalendar(1),
                      ),
                      const SizedBox(height: 12),
                      EventsSection(
                        events: _events,
                        onTap: _onTapEvent,
                      ),
                      const SizedBox(height: footerTopSpacing),
                      const HomeFooter(color: AppColors.myUZSysLightOutline),
                      const SizedBox(height: kBottomNavigationBarHeight + 16),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// Reszta pliku (_Header, _ContentContainer, HomeFooter, _ActionCircle, _plDate, _noop)
// pozostaje bez zmian.

/// HEADER
class _Header extends StatelessWidget {
  // ... (bez zmian)
  final String dateText;
  final String greetingName;
  final String? subtitle;
  final VoidCallback onOpenDrawer;

  const _Header({
    required this.dateText,
    required this.greetingName,
    this.subtitle,
    required this.onOpenDrawer,
  });

  static const double _hPad = 16;
  static const double _topAfterSafe = 8;
  static const double _dateToGreeting = 20;
  static const double _greetingToSubtitle = 4;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(_hPad, _topAfterSafe, _hPad, 8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisSize: MainAxisSize.max,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Expanded(
                  child: Text(
                    dateText,
                    style: AppTextStyle.myUZLabelLarge.copyWith(color: const Color(0xFF1D192B), height: 1.43),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: const [
                    _ActionCircle(icon: MyUz.map_02, tooltip: 'Mapa kampusu', onTap: _noop),
                    SizedBox(width: 8),
                    _ActionCircle(icon: MyUz.mail_01, tooltip: 'Skrzynka pocztowa', onTap: _noop, showBadge: true, badgeColor: Color(0xFFB3261E)),
                  ],
                ),
              ],
            ),
            const SizedBox(height: _dateToGreeting),
            Text('Cześć, $greetingName 👋', style: AppTextStyle.myUZHeadlineMedium.copyWith(fontWeight: FontWeight.w600, color: cs.onSurface)),
            const SizedBox(height: _greetingToSubtitle),
            if (subtitle != null && subtitle!.isNotEmpty)
              Text(subtitle!, style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant, height: 2)),
          ],
        ),
      ),
    );
  }
}

class _ContentContainer extends StatelessWidget {
  // ... (bez zmian)
  final Widget child;
  const _ContentContainer({required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.only(top: 32, bottom: 0),
      child: child,
    );
  }
}

class HomeFooter extends StatelessWidget {
  // ... (bez zmian)
  final Color color;
  const HomeFooter({super.key, required this.color});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Text(
        'MyUZ 2025',
        style: AppTextStyle.myUZLabelLarge.copyWith(
          fontSize: 14,
          height: 1.14,
          fontWeight: FontWeight.w500,
          color: color,
        ),
      ),
    );
  }
}

class _ActionCircle extends StatelessWidget {
  // ... (bez zmian)
  final IconData icon;
  final String? tooltip;
  final VoidCallback onTap;
  final bool showBadge;
  final Color? badgeColor;
  const _ActionCircle({
    required this.icon,
    this.tooltip,
    required this.onTap,
    this.showBadge = false,
    this.badgeColor,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final circle = Material(
      color: cs.secondaryContainer,
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        splashColor: Colors.transparent,
        highlightColor: Colors.transparent,
        onTap: onTap,
        child: SizedBox(
          width: 48,
          height: 48,
          child: Center(
            child: Icon(icon, size: 24, color: cs.onSecondaryContainer),
          ),
        ),
      ),
    );
    Widget btn = SizedBox(
      width: 48,
      height: 48,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          circle,
          if (showBadge)
            Positioned(
              right: 6,
              top: 6,
              child: Container(
                width: 8,
                height: 8,
                decoration: ShapeDecoration(color: badgeColor ?? const Color(0xFFB3261E), shape: const OvalBorder()),
              ),
            ),
        ],
      ),
    );
    if (tooltip != null && tooltip!.isNotEmpty) {
      btn = Tooltip(message: tooltip!, child: btn);
    }
    return btn;
  }
}

String _plDate(DateTime d) {
  // ... (bez zmian)
  const dni = ['Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota', 'Niedziela'];
  const mies = ['stycznia', 'lutego', 'marca', 'kwietnia', 'maja', 'czerwca', 'lipca', 'sierpnia', 'września', 'października', 'listopada', 'grudnia'];
  return '${dni[d.weekday - 1]}, ${d.day} ${mies[d.month - 1]}';
}

void _noop() {}