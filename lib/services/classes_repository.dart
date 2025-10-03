import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/supabase.dart';

/// Wspólne repozytorium do pobierania zajęć (dzień / tydzień) – unifikuje logikę z Home i Calendar.
class ClassesRepository {
  static const String _prefGroup = 'onb_group';
  static const String _prefSub = 'onb_group_sub';
  static const String _prefGroupId = 'onb_group_id';

  // --- Metrics (diagnostyka) ---
  static int lastDayRows = -1;       // liczba wierszy zwróconych PRZED filtracją subgrup (day)
  static int lastWeekRows = -1;      // liczba wierszy zwróconych PRZED filtracją subgrup (week)
  static DateTime? lastDayQueried;   // ostatnia data day
  static DateTime? lastWeekStart;    // poniedziałek ostatniego tygodnia
  static String lastDayVariant = ''; // jaki wariant zadziałał (kod_grupy / grupa_id / no-group)
  static String lastWeekVariant = '';
  static void resetMetrics(){
    lastDayRows = -1; lastWeekRows = -1; lastDayVariant=''; lastWeekVariant=''; lastDayQueried=null; lastWeekStart=null;
  }

  static String _trim(String? raw) => (raw ?? '').trim();

  /// Ładuje grupę i podgrupy z SharedPreferences (z fallbackiem).
  static Future<(String? groupCode, List<String> subgroups)> loadGroupPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final rawGroup = _trim(prefs.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
    final subsCsv = prefs.getString(_prefSub) ?? '';
    final subs = subsCsv.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
    return (groupCode, subs);
  }

  static final Map<String, String?> _groupIdCache = {};

  static Future<String?> _resolveGroupId(String groupCode) async {
    if (_groupIdCache.containsKey(groupCode)) return _groupIdCache[groupCode];
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString(_prefGroupId);
    if (stored != null && stored.isNotEmpty) {
      _groupIdCache[groupCode] = stored; return stored;
    }
    try {
      final res = await Supa.client.from('grupy').select('id').eq('kod_grupy', groupCode).limit(1).maybeSingle();
      if (res == null || res['id'] == null) { _groupIdCache[groupCode] = null; return null; }
      final id = res['id'] as String;
      await prefs.setString(_prefGroupId, id);
      _groupIdCache[groupCode] = id; return id;
    } catch (_) {
      _groupIdCache[groupCode] = null; return null;
    }
  }

  /// Buduje OR filtr dla kolumny podgrupa uwzględniający NULL i pusty string.
  static String _buildSubgroupOr(List<String> subgroups) {
    if (subgroups.isNotEmpty) {
      final inList = subgroups.map((e) => "'${e.replaceAll("'", "\\'")}'").join(',');
      return "podgrupa.is.null,podgrupa.eq.'',podgrupa.in.($inList)";
    }
    return "podgrupa.is.null,podgrupa.eq.''";
  }

  /// Pobiera zajęcia dla pojedynczego zakresu [day ... day+1). Zwraca listę posortowaną po starcie.
  static Future<List<ClassModel>> fetchDay(DateTime day, {String? groupCode, List<String> subgroups = const []}) async {
    final group = _trim(groupCode);
    final start = DateTime(day.year, day.month, day.day);
    final end = start.add(const Duration(days: 1));
    List data = <dynamic>[];
    lastDayQueried = start; lastDayRows = 0; lastDayVariant = 'none';

    if (group.isEmpty) {
      debugPrint('[ClassesRepo][day] brak groupCode -> []');
      return const [];
    }
    // resolve groupId (robust variants)
    final groupId = await _resolveGroupIdRobust(group);
    if (groupId != null) {
      try {
        data = await Supa.client.from('zajecia_grupy')
            .select('*')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', groupId)
            .order('od', ascending: true) as List;
        lastDayVariant = 'grupa_id:$groupId';
        debugPrint('[ClassesRepo][day] grupa_id=$groupId rows=${data.length}');
      } catch (e) { debugPrint('[ClassesRepo][day] grupa_id error: $e'); }
    }
    if (data.isEmpty) {
      // final fallback: join po kod_grupy
      try {
        data = await Supa.client.from('zajecia_grupy')
            .select('*,grupy(kod_grupy)')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupy.kod_grupy', group)
            .order('od', ascending: true) as List;
        lastDayVariant = 'kod_grupy:$group';
        debugPrint('[ClassesRepo][day] join kod_grupy=$group rows=${data.length}');
      } catch (e) { debugPrint('[ClassesRepo][day] join kod_grupy error: $e'); }
    }
    lastDayRows = data.length;

    for (var i = 0; i < data.length; i++) {
      final d = data[i];
      if (d is Map && d.containsKey('grupy')) d.remove('grupy');
    }
    final list = <ClassModel>[];
    for (final row in data) {
      try { list.add(ClassModel.fromMap(Map<String,dynamic>.from(row as Map))); } catch(e){ debugPrint('[ClassesRepo][day][PARSE] $e'); }
    }
    list.sort((a,b)=>a.startTime.compareTo(b.startTime));
    return list;
  }

  /// Pobiera zajęcia dla całego tygodnia [monday .. monday+7).
  static Future<List<ClassModel>> fetchWeek(DateTime anyDay, {String? groupCode, List<String> subgroups = const []}) async {
    final monday = _mondayOfWeek(anyDay);
    final start = monday;
    final end = start.add(const Duration(days: 7));
    final group = _trim(groupCode);
    List data = <dynamic>[];
    lastWeekStart = monday; lastWeekRows = 0; lastWeekVariant='none';

    if (group.isEmpty) {
      debugPrint('[ClassesRepo][week] brak groupCode -> []');
      return const [];
    }
    final groupId = await _resolveGroupIdRobust(group);
    if (groupId != null) {
      try {
        data = await Supa.client.from('zajecia_grupy')
            .select('*')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', groupId)
            .order('od', ascending: true) as List;
        lastWeekVariant = 'grupa_id:$groupId';
        debugPrint('[ClassesRepo][week] grupa_id=$groupId rows=${data.length}');
      } catch (e) { debugPrint('[ClassesRepo][week] grupa_id error: $e'); }
    }
    if (data.isEmpty) {
      try {
        data = await Supa.client.from('zajecia_grupy')
            .select('*,grupy(kod_grupy)')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupy.kod_grupy', group)
            .order('od', ascending: true) as List;
        lastWeekVariant = 'kod_grupy:$group';
        debugPrint('[ClassesRepo][week] join kod_grupy=$group rows=${data.length}');
      } catch (e) { debugPrint('[ClassesRepo][week] join kod_grupy error: $e'); }
    }
    lastWeekRows = data.length;

    for (var i=0;i<data.length;i++) {
      final d = data[i]; if (d is Map && d.containsKey('grupy')) d.remove('grupy');
    }

    final list = <ClassModel>[];
    for (final r in data) { try { list.add(ClassModel.fromMap(Map<String,dynamic>.from(r as Map))); } catch(e){ debugPrint('[ClassesRepo][week][PARSE] $e'); }}

    if (subgroups.isNotEmpty) {
      list.retainWhere((c){
        final pg = (c.subgroup ?? '').trim();
        if (pg.isEmpty) return true; return subgroups.contains(pg);
      });
    }

    list.sort((a,b)=>a.startTime.compareTo(b.startTime));
    return list;
  }

  static DateTime _mondayOfWeek(DateTime day) {
    final wd = day.weekday; // 1=pon
    return DateTime(day.year, day.month, day.day - (wd - 1));
  }

  /// Zwraca listę bieżących i przyszłych zajęć z jednego dnia + jeśli puste, ewentualnie całe dzień.
  static List<ClassModel> filterRemainingOrAll(List<ClassModel> classes, DateTime day, DateTime now, {bool allowEndedIfAllEnded = true}) {
    final todayList = classes.where((c)=> c.startTime.year==day.year && c.startTime.month==day.month && c.startTime.day==day.day).toList();
    final remaining = todayList.where((c){
      final st = c.startTime; final et = c.endTime;
      final ongoing = (st.isBefore(now) || st.isAtSameMomentAs(now)) && et.isAfter(now);
      final upcoming = st.isAfter(now);
      return ongoing || upcoming;
    }).toList()..sort((a,b)=>a.startTime.compareTo(b.startTime));
    if (remaining.isNotEmpty) return remaining;
    if (allowEndedIfAllEnded && todayList.isNotEmpty) {
      todayList.sort((a,b)=>a.startTime.compareTo(b.startTime));
      return todayList;
    }
    return const [];
  }

  /// Pobierz dzień z fallbackiem na tydzień jeśli day-query zwróci pustą listę.
  static Future<List<ClassModel>> fetchDayWithWeekFallback(DateTime day, {String? groupCode, List<String> subgroups = const []}) async {
    final primary = await fetchDay(day, groupCode: groupCode, subgroups: subgroups);
    if (primary.isNotEmpty) return primary;
    final week = await fetchWeek(day, groupCode: groupCode, subgroups: subgroups);
    final filtered = week.where((c)=> c.startTime.year==day.year && c.startTime.month==day.month && c.startTime.day==day.day).toList()
      ..sort((a,b)=>a.startTime.compareTo(b.startTime));
    return filtered;
  }

  /// Próba rozwiązania groupId z wariantami formatującymi (dokładny, bez '-', bez spacji, upper, lower)
  static Future<String?> _resolveGroupIdRobust(String group) async {
    final tried = <String>{};
    Future<String?> attempt(String g) async {
      if (tried.contains(g)) return null; tried.add(g);
      final id = await _resolveGroupId(g);
      if (id != null) return id; return null;
    }
    final variants = [group, group.replaceAll('-', ''), group.replaceAll(' ', ''), group.replaceAll('-', ' '), group.toUpperCase(), group.toLowerCase()];
    for (final v in variants) {
      final id = await attempt(v);
      if (id != null) return id;
    }
    return null;
  }
}
