import 'dart:async';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/screens/calendar/components/calendar_view.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Ekran kalendarza – widok tygodniowy + dzienny timeline (siatka godzinowa).
class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});
  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> with WidgetsBindingObserver {
  bool _isMonthView = false; // false=tydzień (1 rząd), true=miesiąc (6 rzędów)
  String _activeSection = 'calendar';
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  bool _drawerOpen = false;
  DateTime _selectedDay = _stripTime(DateTime.now());
  DateTime _focusedDay = _stripTime(DateTime.now());
  List<ClassModel> _allClasses = [];
  bool _loading = false;
  bool _error = false;
  String? _errorMsg;
  final Map<DateTime, List<ClassModel>> _weekCache = {};
  final Set<DateTime> _loadingWeeks = {};

  DateTime? _lastSwipeTime;
  static const int _swipeCooldownMs = 420;

  void _trySwipe(VoidCallback cb) {
    final now = DateTime.now();
    if (_lastSwipeTime != null && now.difference(_lastSwipeTime!).inMilliseconds < _swipeCooldownMs) return;
    _lastSwipeTime = now;
    cb();
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _ensureWeekLoaded(_focusedDay);
  }
  @override
  void dispose() { WidgetsBinding.instance.removeObserver(this); super.dispose(); }
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      final today = _stripTime(DateTime.now());
      setState(() { _focusedDay = today; _selectedDay = today; });
      _ensureWeekLoaded(today);
    }
  }
  void _toggleMonthView(){ setState(()=> _isMonthView = !_isMonthView); }
  Future<void> _ensureWeekLoaded(DateTime anyDay) async {
    final monday = _mondayOfWeek(anyDay);
    if (_weekCache.containsKey(monday)) {
      if (mounted) {
        setState(() { _allClasses = _weekCache[monday]!; });
      }
      return;
    }
    await _fetchWeekData(monday);
    // Prefetch sąsiednich tygodni (fire & forget)
    _fetchWeekData(monday.subtract(const Duration(days: 7)));
    _fetchWeekData(monday.add(const Duration(days: 7)));
  }

  Future<void> _fetchWeekData(DateTime monday) async {
    final mondayKey = _stripTime(monday);
    if (_loadingWeeks.contains(mondayKey)) return;
    _loadingWeeks.add(mondayKey);
    if (mondayKey == _mondayOfWeek(_focusedDay)) {
      setState(() { _loading = true; _error = false; _errorMsg = null; });
    }
    try {
      final (groupCode, subgroups) = await ClassesRepository.loadGroupPrefs();
      final list = await ClassesRepository.fetchWeek(mondayKey, groupCode: groupCode, subgroups: subgroups);
      if (!mounted) return;
      _weekCache[mondayKey] = list;
      if (_mondayOfWeek(_focusedDay)==mondayKey) {
        setState(()=> _allClasses = list);
      }
    } catch (e) {
      if (!mounted) return;
      if (_mondayOfWeek(_focusedDay)==mondayKey) {
        setState(() { _error = true; _errorMsg = e.toString(); _allClasses = []; });
      }
    } finally {
      _loadingWeeks.remove(mondayKey);
      if (mounted && _mondayOfWeek(_focusedDay)==mondayKey) setState(()=> _loading = false);
    }
  }

  void _selectDay(DateTime day) {
    final d0 = _stripTime(day);
    setState(() {
      _selectedDay = d0;
      _focusedDay = d0; // aktualizujemy fokus aby _ensureWeekLoaded ustawił loading dla właściwego tygodnia
    });
    _ensureWeekLoaded(d0); // jeśli tydzień nie był w cache – zostanie pobrany
  }

  void _prevWeek() {
    setState(() { _focusedDay = _focusedDay.subtract(const Duration(days: 7)); });
    _ensureWeekLoaded(_focusedDay);
  }

  void _nextWeek() {
    setState(() { _focusedDay = _focusedDay.add(const Duration(days: 7)); });
    _ensureWeekLoaded(_focusedDay);
  }

  // Dodane: przesunięcie o 1 dzień do przodu
  void _nextDay() {
    _selectDay(_selectedDay.add(const Duration(days: 1)));
  }

  // Dodane: przesunięcie o 1 dzień do tyłu
  void _prevDay() {
    _selectDay(_selectedDay.subtract(const Duration(days: 1)));
  }

  void _prevMonth(){
    setState(() {
      final d = DateTime(_focusedDay.year, _focusedDay.month - 1, 1);
      _focusedDay = d;
      if (_isMonthView) {
        // utrzymaj selectedDay w obrębie nowego miesiąca (jeśli stary dzień nie istnieje)
        _selectedDay = DateTime(d.year, d.month, 1);
      }
    });
    _ensureWeekLoaded(_focusedDay);
  }
  void _nextMonth(){
    setState(() {
      final d = DateTime(_focusedDay.year, _focusedDay.month + 1, 1);
      _focusedDay = d;
      if (_isMonthView) {
        _selectedDay = DateTime(d.year, d.month, 1);
      }
    });
    _ensureWeekLoaded(_focusedDay);
  }

  @override
  Widget build(BuildContext context) {
    final classesByDay = _groupByDay(_allClasses);
    final selectedClasses = classesByDay[_selectedDay] ?? const [];
    final monthTitle = DateFormat('LLLL', 'pl').format(_focusedDay);
    Widget topCalendarRegion = Column(children: [
      const _CalendarDaysHeader(),
      CalendarView(
        focusedDay: _focusedDay,
        selectedDay: _selectedDay,
        isWeekView: !_isMonthView,
        classesByDay: classesByDay,
        onDaySelected: (d){ _selectDay(d); },
        onPrevWeek: _prevWeek,
        onNextWeek: _nextWeek,
        onPrevMonth: _prevMonth,
        onNextMonth: _nextMonth,
      ),
    ]);
    // Wrap header+grid with GestureDetector so swiping on header also changes week/month
    topCalendarRegion = GestureDetector(
      behavior: HitTestBehavior.opaque,
      onHorizontalDragEnd: (details) {
        final v = details.primaryVelocity ?? 0;
        if (v.abs() < 40) return; // zgodnie z progiem w CalendarView
        if (!_isMonthView) {
          if (v < 0) _trySwipe(_nextWeek); else _trySwipe(_prevWeek);
        } else {
          if (v < 0) _trySwipe(_nextMonth); else _trySwipe(_prevMonth);
        }
      },
      child: topCalendarRegion,
    );
    Widget bodyContent = Expanded(
      child: _loading
        ? const Center(child: CircularProgressIndicator())
        : _error
          ? _ErrorReload(onReload: ()=>_ensureWeekLoaded(_focusedDay), msg: _errorMsg)
          : CalendarDayView(
              day: _selectedDay,
              classes: selectedClasses,
              onNextDay: _nextDay,
              onPrevDay: _prevDay,
            ),
    );
    return Scaffold(
      key: _scaffoldKey,
      backgroundColor: Colors.white,
      body: Stack(children:[
        SafeArea(child: Column(children:[
          _CalendarTopBar(
            monthTitle: monthTitle,
            isMonthOpen: _isMonthView && _activeSection=='calendar',
            onTapMonth: _activeSection=='calendar'? _toggleMonthView : (){},
            onMenu: ()=> setState(()=> _drawerOpen=true),
            onSearch: _openSearchPlaceholder,
            onAdd: _openAddTaskPlaceholder,
          ),
          if (_activeSection=='calendar') topCalendarRegion,
          if (_activeSection=='calendar') bodyContent else Expanded(child: Center(child: Text(_activeSection=='schedule'?'Terminarz – miejsce na implementację.':'Porównaj plany – miejsce na implementację.'))),
        ])),
        if (_drawerOpen) Positioned.fill(child: GestureDetector(onTap: ()=> setState(()=> _drawerOpen=false), child: AnimatedOpacity(duration: const Duration(milliseconds:200), opacity: _drawerOpen?1:0, child: Container(color: Colors.black.withValues(alpha:0.32))))),
        AnimatedPositioned(duration: const Duration(milliseconds:340), curve: Curves.easeOutCubic, left: _drawerOpen?0:-300, top:0, bottom:0, width:288, child: Material(elevation:16, color:Colors.white, borderRadius: const BorderRadius.only(topRight: Radius.circular(16), bottomRight: Radius.circular(16)), clipBehavior: Clip.antiAlias, child: _CalendarDrawer(activeSection:_activeSection, onSelectSection:(s){ setState((){ _activeSection=s; _drawerOpen=false; });})) ),
      ]),
    );
  }

  // ===== Helpers przywrócone po refaktorze =====
  static DateTime _stripTime(DateTime d) => DateTime(d.year, d.month, d.day);
  static DateTime _mondayOfWeek(DateTime day) {
    final wd = day.weekday; // 1=pon
    return _stripTime(day.subtract(Duration(days: wd - 1)));
  }
  Map<DateTime, List<ClassModel>> _groupByDay(List<ClassModel> list) {
    final map = <DateTime, List<ClassModel>>{};
    for (final c in list) {
      final key = _stripTime(c.startTime);
      map.putIfAbsent(key, () => []).add(c);
    }
    for (final v in map.values) { v.sort((a,b)=>a.startTime.compareTo(b.startTime)); }
    return map;
  }

  void _openSearchPlaceholder(){
    showModalBottomSheet(context: context, builder: (_) => const _PlaceholderSheet(title: 'Wyszukiwarka', description: 'Tutaj w przyszłości pojawi się wyszukiwanie zajęć / zadań.'));
  }
  void _openAddTaskPlaceholder(){
    showModalBottomSheet(context: context, builder: (_) => const _PlaceholderSheet(title: 'Dodaj zadanie', description: 'Formularz dodawania zadania pojawi się tutaj.'));
  }
}

class _PlaceholderSheet extends StatelessWidget {
  final String title; final String description;
  const _PlaceholderSheet({required this.title, required this.description});
  @override
  Widget build(BuildContext context){
    return Padding(
      padding: const EdgeInsets.fromLTRB(24,16,24,32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
            Text(description, style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 20),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(onPressed: ()=>Navigator.pop(context), child: const Text('Zamknij')),
          )
        ],
      ),
    );
  }
}

/// Pasek górny: menu | wybór miesiąca (tap) + strzałki tygodnia | search | plus
class _CalendarTopBar extends StatelessWidget {
  final String monthTitle;
  final bool isMonthOpen;
  final VoidCallback onTapMonth;
  final VoidCallback onMenu;
  final VoidCallback onSearch;
  final VoidCallback onAdd;
  const _CalendarTopBar({required this.monthTitle, required this.isMonthOpen, required this.onTapMonth, required this.onMenu, required this.onSearch, required this.onAdd});
  @override
  Widget build(BuildContext context) {
    final tt = Theme.of(context).textTheme;
    final cs = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 4),
      child: Row(
        children: [
          _IconCircle(icon: MyUz.menu_01, onTap: onMenu),
          const SizedBox(width: 12),
          GestureDetector(
            onTap: onTapMonth,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Flexible(
                  child: Text(
                    _cap(monthTitle),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: tt.headlineSmall?.copyWith(
                      fontSize: 24,
                      fontWeight: FontWeight.w600,
                      height: 1,
                      color: const Color(0xFF1D192B),
                    ),
                  ),
                ),
                const SizedBox(width: 4),
                Icon(isMonthOpen ? MyUz.chevron_up : MyUz.chevron_down, size: 20, color: cs.onSurface),
              ],
            ),
          ),
          const Spacer(),
          _IconCircle(icon: MyUz.search_sm, onTap: onSearch),
          const SizedBox(width: 8),
          _IconCircle(icon: MyUz.plus, onTap: onAdd),
        ],
      ),
    );
  }
  String _cap(String t) => t.isEmpty ? t : t[0].toUpperCase() + t.substring(1);
}

class _IconCircle extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  const _IconCircle({required this.icon, required this.onTap});
  @override
  Widget build(BuildContext context) {
    return InkWell(
      customBorder: const CircleBorder(),
      onTap: onTap,
      child: Container(
        width: 48,
        height: 48,
        decoration: const BoxDecoration(
          color: Color(0xFFF7F2F9),
          shape: BoxShape.circle,
        ),
        alignment: Alignment.center,
        child: Icon(icon, size: 24, color: const Color(0xFF1D192B)),
      ),
    );
  }
}

class _ErrorReload extends StatelessWidget {
  final VoidCallback onReload; final String? msg;
  const _ErrorReload({required this.onReload, this.msg});
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.error_outline, color: Colors.redAccent, size: 40),
          const SizedBox(height: 12),
          Text(msg ?? 'Błąd ładowania danych', textAlign: TextAlign.center),
          const SizedBox(height: 8),
          OutlinedButton(onPressed: onReload, child: const Text('Odśwież')),
        ],
      ),
    );
  }
}

class _CalendarDaysHeader extends StatelessWidget {
  const _CalendarDaysHeader();
  @override
  Widget build(BuildContext context) {
    const names = ['P','W','Ś','C','P','S','N'];
    final style = Theme.of(context).textTheme.labelSmall?.copyWith(fontWeight: FontWeight.w500, color: const Color(0xFF494949));
    return LayoutBuilder(builder: (context, constraints) {
      // mirror calculation from _MonthGrid: available = maxWidth - leftReserve - padding(16*2)
      final available = constraints.maxWidth - kCalendarLeftReserve - 32; // 32 = padding horizontal (16*2)
      final itemW = available / 7;
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal:16),
        child: Row(children:[
          const SizedBox(width:kCalendarLeftReserve),
          for (int i = 0; i < 7; i++)
            SizedBox(width: itemW, child: SizedBox(height:24, child: Center(child: Text(names[i], style: style)))),
        ]),
      );
    });
  }
}

class _CalendarDrawer extends StatefulWidget {
  final String activeSection;
  final ValueChanged<String> onSelectSection;
  const _CalendarDrawer({this.activeSection = 'calendar', required this.onSelectSection});
  @override
  State<_CalendarDrawer> createState() => _CalendarDrawerState();
}

class _CalendarDrawerState extends State<_CalendarDrawer> {
  String? _groupCode; List<String> _subs = []; bool _loading = true;
  @override
  void initState(){ super.initState(); _load(); }
  Future<void> _load() async {
    final p = await SharedPreferences.getInstance();
    final g = p.getString('onb_group');
    final s = p.getString('onb_group_sub') ?? '';
    if(!mounted) return;
    setState(() { _groupCode = g; _subs = s.split(',').where((e)=>e.trim().isNotEmpty).toList(); _loading=false; });
  }

  Widget _sectionLabel(String text) => Padding(
    padding: const EdgeInsets.fromLTRB(16, 18, 16, 8),
    child: Text(
      text,
      style: const TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w500,
        height: 1.43,
        letterSpacing: 0.10,
        color: Color(0xFF49454F),
      ),
    ),
  );

  Widget _divider() => const Padding(
    padding: EdgeInsets.symmetric(horizontal:16),
    child: Divider(height: 1, thickness: 1, color: Color(0xFFCAC4D0)),
  );

  Widget _item({required IconData icon, required String label, bool active=false, VoidCallback? onTap}) {
    final bg = active ? const Color(0xFFE8DEF8) : Colors.transparent;
    final txtColor = active ? const Color(0xFF4A4459) : const Color(0xFF49454F);
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(100),
        onTap: onTap,
        child: Container(
          height: 56,
            padding: const EdgeInsets.only(left:16,right:24),
            decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(100)),
            child: Row(
              children: [
                SizedBox(
                  width: 24, height: 24,
                  child: Icon(icon, size: 24, color: const Color(0xFF49454F)),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    label,
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: active? FontWeight.w600 : FontWeight.w500,
                      height: 1.43,
                      letterSpacing: 0.10,
                      color: txtColor,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            )
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final groupLabel = _groupCode == null
        ? 'Brak zapisanej grupy'
        : (_subs.isEmpty ? _groupCode! : '${_groupCode!} ${_subs.join('/')}' );

    return SafeArea(
      left: false,
      bottom: false,
      child: _loading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(12),
              child: SingleChildScrollView(
                padding: EdgeInsets.zero,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _sectionLabel('Menu'),
                    _item(icon: MyUz.calendar, label: 'Kalendarz', active: widget.activeSection=='calendar', onTap: ()=>widget.onSelectSection('calendar')),
                    _item(icon: MyUz.book_open_01, label: 'Terminarz', active: widget.activeSection=='schedule', onTap: ()=>widget.onSelectSection('schedule')),
                    _item(icon: MyUz.switch_horizontal_02, label: 'Porównaj plany', active: widget.activeSection=='compare', onTap: ()=>widget.onSelectSection('compare')),
                    _divider(),
                    _sectionLabel('Ulubione'),
                    _item(icon: MyUz.user_01, label: groupLabel),
                    _item(icon: MyUz.users_01, label: 'Nauczyciele'),
                    _divider(),
                    _sectionLabel('Udostępnione terminarze'),
                    _item(icon: MyUz.bookmark, label: 'Terminarz Leny'),
                    const SizedBox(height: 24),
                  ],
                ),
              ),
            ),
    );
  }
}
