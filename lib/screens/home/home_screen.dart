import 'dart:async';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
// import 'package:my_uz/supabase.dart'; // nieużywane po refaktoryzacji – zostawione zakomentowane dla ewentualnych przyszłych zadań
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/user_tasks_repository.dart';

import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

// MODELE
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/models/event_model.dart';
// DODANE: ekran szczegółów zajęć
import 'package:my_uz/screens/home/details/class_details.dart';
import 'package:my_uz/screens/home/details/task_details.dart';
import 'package:my_uz/screens/home/details/task_edit_sheet.dart';

// SEKCJE
import 'components/upcoming_classes.dart';
import 'components/tasks_section.dart';
import 'components/events_section.dart';

/// HomeScreen – Dashboard (Figma – obraz 4)
/// (uproszczona wersja – tylko mocki lokalne, bez repo / dodatkowych plików)
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

// Klucze personalizacji
const String _kPrefOnbMode = 'onb_mode';
const String _kPrefOnbSalutation = 'onb_salutation';
const String _kPrefOnbFirst = 'onb_first';

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
   String _greetingName = 'Student';
   String? _greetingSubtitle; // dodatkowa linia pod powitaniem (wydział/kierunek/tryb)
   String? _groupCode; // kod grupy
   List<String> _subgroups = []; // lista podgrup
   bool _loading = true; // ładowanie prefów
   bool _classesLoading = false; // ładowanie zajęć
   DateTime? _classesForDate; // data dla której aktualnie pokazujemy _classes
   Timer? _refreshTimer; // periodyczne sprawdzanie zmiany dnia

   List<ClassModel> _classes = const [];
   List<TaskModel> _tasks = const [];
   // Zamiast `late` inicjalizujemy od razu pustą listą, żeby uniknąć
   // LateInitializationError, gdy komponent renderuje się przed załadowaniem mocków/danych.
   List<EventModel> _events = const [];

   @override
   void initState() {
     super.initState();
     // Zamiast lokalnych mocków ładujemy zadania użytkownika z Supabase (jeśli dostępne)
     _loadTasks();
     WidgetsBinding.instance.addObserver(this);
     _loadPrefs();
     _loadTodayClasses();
     // timer, który odświeża listę jeśli zmieni się dzień (np. przejście przez północ)
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

   void _buildMocks() {
     // Usunięto mocki zajęć – realne dane ładowane po wyborze grupy.
     final now = DateTime.now();
     _tasks = [
       TaskModel(
         id: 't1',
         title: 'Projekt zaliczeniowy',
         subject: 'PPO',
         deadline: now.add(const Duration(days: 3)),
       ),
       TaskModel(
         id: 't2',
         title: 'Kolokwium – Algebra',
         subject: 'Algebra',
         deadline: now.add(const Duration(days: 5)),
       ),
       TaskModel(
         id: 't3',
         title: 'Sprawozdanie z laboratorium',
         subject: 'Fizyka',
         deadline: now.add(const Duration(days: 6)),
       ),
     ];
     _events = [
       EventModel(
         id: 'e1',
         title: 'Juwenalia 2025',
         description: 'Koncerty i atrakcje na kampusie.',
         date: 'Piątek, 4 paź 2025',
         time: '18:00 - 23:00',
         location: 'Kampus UZ',
         freeEntry: true,
       ),
       EventModel(
         id: 'e2',
         title: 'Dzień sportu',
         description: 'Turniej siatkówki + biegi.',
         date: 'Sobota, 10 paź 2025',
         time: '10:00 - 16:00',
         location: 'Stadion UZ',
         freeEntry: false,
       ),
       EventModel(
         id: 'e3',
         title: 'Hackathon UZ',
         description: '24h kodowania – zgłoś zespół.',
         date: 'Wtorek, 21 paź 2025',
         time: '09:00 - 09:00',
         location: 'Aula UZ',
         freeEntry: true,
       ),
     ];
   }

   Future<void> _loadPrefs() async {
     try {
       final p = await SharedPreferences.getInstance();
       final mode = p.getString(_kPrefOnbMode);
       final first = p.getString(_kPrefOnbFirst)?.trim();
       final sal = p.getString(_kPrefOnbSalutation)?.trim();
       // use centralized loader for group prefs to avoid mismatch with calendar
       final (String? groupCode, List<String> subs) = await ClassesRepository.loadGroupPrefs();
       // Dodatkowo spróbuj pobrać metadane grupy (wydział/kierunek/tryb) i złożyć krótką linie pod powitaniem
       String? subtitle;
       try {
         final meta = await ClassesRepository.loadGroupMeta();
         if (meta != null) {
           final parts = <String>[];
           // sprawdź kilka możliwych kluczy
           if ((meta['wydzial'] ?? meta['wydzial_nazwa'] ?? meta['wydzial_name']) != null) parts.add((meta['wydzial'] ?? meta['wydzial_nazwa'] ?? meta['wydzial_name']).toString());
           if ((meta['kierunek'] ?? meta['kierunek_nazwa'] ?? meta['kierunek_name']) != null) parts.add((meta['kierunek'] ?? meta['kierunek_nazwa'] ?? meta['kierunek_name']).toString());
           if ((meta['tryb'] ?? meta['forma'] ?? meta['forma_studiow']) != null) parts.add((meta['tryb'] ?? meta['forma'] ?? meta['forma_studiow']).toString());
           if (parts.isNotEmpty) subtitle = parts.join(' • ');
         }
       } catch (_) { subtitle = null; }
       String name = 'Student';
       if (mode == 'data') {
         if (first != null && first.isNotEmpty) {
           name = first;
         } else if (sal != null && sal.isNotEmpty) {
           name = sal;
         }
       } else {
         // anon or unspecified -> prefer salutation if present
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
     if (_classesLoading) return;
     setState(()=> _classesLoading = true);
     try {
       final (groupCode, subgroups, groupId) = await ClassesRepository.loadGroupContext();
       final now = DateTime.now();
       final today = DateTime(now.year, now.month, now.day);
       final todayList = await ClassesRepository.fetchDayWithWeekFallback(today, groupCode: groupCode, subgroups: subgroups, groupId: groupId);
       final remaining = ClassesRepository.filterRemainingOrAll(todayList, today, now, allowEndedIfAllEnded: false);
       if (!mounted) return;
       setState(() {
         _classes = remaining; // jeśli pusto -> komponent pokaże lewostronny komunikat
         _classesForDate = today;
         _classesLoading = false;
       });
     } catch (e) {
       debugPrint('[Home][_loadTodayClasses][ERR] $e');
       if (!mounted) return; setState(()=> _classesLoading=false);
     }
   }

   Future<void> _loadTasks() async {
     try {
       final tasks = await UserTasksRepository.instance.fetchUserTasks();
       if (!mounted) return;
       setState(() {
         _tasks = tasks;
       });
     } catch (e) {
       debugPrint('[Home][_loadTasks] err $e');
       if (!mounted) return;
       setState(() { _tasks = const []; });
     }
   }

   @override
   Widget build(BuildContext context) {
     final cs = Theme.of(context).colorScheme;
     const double _footerTopSpacing = 10; // Figma: odstęp treści od stopki
     return Container(
       // Root na biało, żeby przy overscrollu na dole NIE było widać powierzchni tła (#FEF7FF)
       color: Colors.white,
       child: _loading
           ? const Center(child: CircularProgressIndicator())
           : CustomScrollView(
               physics: const ClampingScrollPhysics(), // brak bounce = brak "prześwitu" na dole
               slivers: [
                 SliverToBoxAdapter(
                   child: Container(
                     color: cs.surface, // tylko header ma kolor surface
                     child: _Header(
                       dateText: _plDate(DateTime.now()),
                       greetingName: _greetingName,
                       subtitle: _greetingSubtitle,
                     ),
                   ),
                 ),
                 // Biała sekcja aż do dołu
                 SliverFillRemaining(
                   hasScrollBody: false,
                   child: Transform.translate(
                     offset: const Offset(0, -8), // overlap header so top radius is visible
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
                             // compute today/tomorrow as date-only values to decide whether to show 'jutro'
                             Builder(builder: (context) {
                               // Always show the same header label regardless whether we're showing tomorrow's classes
                               const header = 'Najbliższe zajęcia';
                               final emptyMsg = (_classesLoading ? '' : (_classes.isEmpty ? (_groupCode==null? 'Wybierz grupę w ustawieniach.' : 'Dziś brak nadchodzących zajęć') : null));
                                return UpcomingClassesSection(
                                  classes: _classes,
                                  onTap: _onTapClass,
                                  groupCode: _groupCode,
                                  subgroups: _subgroups,
                                  headerTitle: header,
                                  isLoading: _classesLoading,
                                  emptyMessage: emptyMsg,
                                );
                           }),
                           const SizedBox(height: 12),
                           TasksSection(
                             tasks: _tasks,
                             onTap: (task) async {
                               // Pobierz opis (lokalnie/serwer) i otwórz arkusz szczegółów
                               final desc = await UserTasksRepository.instance.getTaskDescription(task.id) ?? '';
                               if (!mounted) return;
                               TaskDetailsSheet.show(
                                 context,
                                 task,
                                 description: desc,
                                 relatedClass: _classes.where((c) => c.subject == task.subject).isNotEmpty
                                     ? _classes.where((c) => c.subject == task.subject).first
                                     : null,
                                 onEdit: () async {
                                   // Otwórz arkusz edycji i zapisz wynik przez repozytorium
                                   TaskModel? _lastSaved;
                                   await TaskEditSheet.show(
                                     context,
                                     task,
                                     initialDate: task.deadline,
                                     initialTitle: task.title,
                                     initialDescription: desc,
                                     onSave: (newTask) async {
                                       _lastSaved = await UserTasksRepository.instance.upsertTask(newTask);
                                       if (!mounted) return;
                                       setState(() {
                                         final idx = _tasks.indexWhere((t) => t.id == _lastSaved!.id);
                                         if (idx != -1) _tasks[idx] = _lastSaved!;
                                         else _tasks.insert(0, _lastSaved!);
                                       });
                                     },
                                     onSaveDescription: (d) async {
                                       try {
                                         if (_lastSaved != null) {
                                           await UserTasksRepository.instance.upsertTask(_lastSaved!.copyWith(), description: d);
                                         } else {
                                           // as fallback, spróbuj znaleźć zadanie po id w liście i zaktualizować
                                           final maybe = _tasks.firstWhere((t) => t.id == task.id, orElse: () => task);
                                           await UserTasksRepository.instance.upsertTask(maybe.copyWith(), description: d);
                                         }
                                       } catch (_) {}
                                     },
                                   );
                                 },
                                 onDelete: () async {
                                   await UserTasksRepository.instance.deleteTask(task.id);
                                   if (!mounted) return;
                                   setState(() { _tasks.removeWhere((t) => t.id == task.id); });
                                 },
                                 onToggleCompleted: (completed) async {
                                   await UserTasksRepository.instance.setTaskCompleted(task.id, completed);
                                   if (!mounted) return;
                                   setState(() {
                                     final idx = _tasks.indexWhere((t) => t.id == task.id);
                                     if (idx != -1) _tasks[idx] = _tasks[idx].copyWith(completed: completed);
                                   });
                                 },
                                 onSaveEdit: (updated) async {
                                   final saved = await UserTasksRepository.instance.upsertTask(updated);
                                   if (!mounted) return;
                                   setState(() {
                                     final idx = _tasks.indexWhere((t) => t.id == saved.id);
                                     if (idx != -1) _tasks[idx] = saved; else _tasks.insert(0, saved);
                                   });
                                 },
                               );
                             },
                           ),
                           const SizedBox(height: 12),
                           EventsSection(
                             events: _events,
                             onTap: _onTapEvent,
                           ),
                           const SizedBox(height: _footerTopSpacing),
                           const _Footer(color: AppColors.myUZSysLightOutline),
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

   void _onTapClass(ClassModel c) {
     // --- TAP: karta zajęć -> arkusz szczegółów (modal bottom sheet) ---
     ClassDetailsSheet.open(context, c);
   }
   void _onTapEvent(EventModel e) => debugPrint('[Home] event tap ${e.id}');

   @override
   void dispose() {
     WidgetsBinding.instance.removeObserver(this);
     _refreshTimer?.cancel();
     super.dispose();
   }

   @override
   void didChangeAppLifecycleState(AppLifecycleState state) {
     if (state == AppLifecycleState.resumed) {
       // reload prefs and classes when app resumes to keep Home in sync with other screens
       _loadPrefs();
       _loadTodayClasses();
     }
   }
}

/// HEADER – SafeArea + spacing z Figmy
class _Header extends StatelessWidget {
  final String dateText;
  final String greetingName;
  final String? subtitle;
  const _Header({required this.dateText, required this.greetingName, this.subtitle});

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
            // Date + actions
            Row(
              mainAxisSize: MainAxisSize.max,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                // Lewa strona: data (zajmuje pozostałą szerokość)
                Expanded(
                  child: Text(
                    dateText,
                    style: AppTextStyle.myUZLabelLarge.copyWith(
                      color: const Color(0xFF1D192B),
                      height: 1.43,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                // Prawa strona: ikony, opakowane w Row z minimalną szerokością
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Przycisk mapa
                    _ActionCircle(
                      icon: MyUz.map_02,
                      tooltip: 'Mapa kampusu',
                      onTap: () {
                        // TODO: akcja mapy
                      },
                    ),
                    const SizedBox(width: 8),
                    // Przycisk mail z kropką
                    _ActionCircle(
                      icon: MyUz.mail_01,
                      tooltip: 'Skrzynka pocztowa',
                      showBadge: true,
                      badgeColor: const Color(0xFFB3261E),
                      onTap: () {
                        // TODO: akcja mail
                      },
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: _dateToGreeting),
            Text(
              'Cześć, $greetingName 👋',
              style: AppTextStyle.myUZHeadlineMedium.copyWith(
                fontWeight: FontWeight.w600,
                color: cs.onSurface,
              ),
            ),
            const SizedBox(height: _greetingToSubtitle),
            Text(
              subtitle ?? 'UZ, Wydział Informatyki',
              style: AppTextStyle.myUZBodySmall.copyWith(
                color: cs.onSurfaceVariant,
                height: 2, // 12px * 2 = 24px jak w Figmie
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Biały panel z zaokrąglonym topem
class _ContentContainer extends StatelessWidget {
  final Widget child;
  const _ContentContainer({required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.only(top: 32, bottom: 0), // increased top padding to account for overlap so content isn't clipped
      child: child,
    );
  }
}

class _Footer extends StatelessWidget {
  final Color color;
  const _Footer({required this.color});

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
              bottom: 6,
              child: Container(
                width: 8,
                height: 8,
                decoration: ShapeDecoration(
                  color: badgeColor ?? const Color(0xFFB3261E),
                  shape: const OvalBorder(),
                ),
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

/// Data PL: Wtorek, 16 lipca
String _plDate(DateTime d) {
  const dni = ['Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota', 'Niedziela'];
  const mies = [
    'stycznia',
    'lutego',
    'marca',
    'kwietnia',
    'maja',
    'czerwca',
    'lipca',
    'sierpnia',
    'września',
    'października',
    'listopada',
    'grudnia'
  ];
  return '${dni[d.weekday - 1]}, ${d.day} ${mies[d.month - 1]}';
}
