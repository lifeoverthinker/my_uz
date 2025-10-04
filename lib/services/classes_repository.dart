import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/supabase.dart';

/// Repozytorium pobierania zajęć: dzień / tydzień / dowolny zakres.
class ClassesRepository {
  // Klucze prefów
  static const String _prefGroup = 'onb_group';
  static const String _prefSub = 'onb_group_sub';
  static const String _prefGroupId = 'onb_group_id';

  // Metryki diagnostyczne
  static int lastDayRows = -1;
  static int lastWeekRows = -1;
  static int lastRangeRows = -1;
  static DateTime? lastDayQueried;
  static DateTime? lastWeekStart;
  static DateTime? lastRangeFrom;
  static DateTime? lastRangeTo;
  static String lastDayVariant = '';
  static String lastWeekVariant = '';
  static String lastRangeVariant = '';
  static void resetMetrics() {
    lastDayRows = lastWeekRows = lastRangeRows = -1;
    lastDayVariant = lastWeekVariant = lastRangeVariant = '';
    lastDayQueried = lastWeekStart = lastRangeFrom = lastRangeTo = null;
  }

  // Prosty cache zakresów (key -> Entry) z TTL
  static final Map<String,_RangeCacheEntry> _rangeCache = {};
  static const Duration _cacheTtl = Duration(minutes: 5);
  static String _rangeKey(DateTime from, DateTime to, String group, List<String> subs){
    final subsKey = subs.map((e)=>e.trim().toLowerCase()).toList()..sort();
    return '${from.millisecondsSinceEpoch}:${to.millisecondsSinceEpoch}:$group:${subsKey.join('|')}';
  }

  static void _pruneCache(){
    final now = DateTime.now();
    _rangeCache.removeWhere((_,v)=> now.difference(v.inserted) > _cacheTtl);
  }

  static String _trim(String? s) => (s ?? '').trim();

  /// Odczyt grupy i podgrup z prefów.
  static Future<(String? groupCode, List<String> subgroups)> loadGroupPrefs() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
    final subsCsv = p.getString(_prefSub) ?? '';
    final subs = subsCsv.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
    return (groupCode, subs);
  }

  // Cache groupId
  static final Map<String,String?> _groupIdCache = {};
  static Future<String?> _resolveGroupId(String groupCode) async {
    if (_groupIdCache.containsKey(groupCode)) return _groupIdCache[groupCode];
    try {
      final res = await Supa.client.from('grupy').select('id').eq('kod_grupy', groupCode).limit(1).maybeSingle();
      if (res == null || res['id'] == null) { _groupIdCache[groupCode] = null; return null; }
      final id = res['id'] as String; (await SharedPreferences.getInstance()).setString(_prefGroupId, id); _groupIdCache[groupCode]=id; return id;
    } catch (_) { _groupIdCache[groupCode]=null; return null; }
  }
  static Future<String?> _resolveGroupIdRobust(String group) async {
    final tried = <String>{};
    Future<String?> attempt(String g) async { if (tried.contains(g)) return null; tried.add(g); return await _resolveGroupId(g); }
    final variants = [group, group.replaceAll('-', ''), group.replaceAll(' ', ''), group.replaceAll('-', ' '), group.toUpperCase(), group.toLowerCase()];
    for (final v in variants) { final id = await attempt(v); if (id!=null) return id; }
    return null;
  }

  /// Uniwersalne pobranie zakresu [from, to). Zwraca posortowaną listę.
  static Future<List<ClassModel>> fetchRange({required DateTime from, required DateTime to, String? groupCode, List<String> subgroups = const []}) async {
    final rawGroup = _trim(groupCode);
    final group = rawGroup; // brak sztucznego fallbacku – jeśli brak grupy zwracamy []
    final start = DateTime(from.year, from.month, from.day, from.hour, from.minute);
    final end = DateTime(to.year, to.month, to.day, to.hour, to.minute);
    lastRangeFrom = start; lastRangeTo = end; lastRangeRows = 0; lastRangeVariant = 'none';
    if (group.isEmpty) { debugPrint('[ClassesRepo][range] brak groupCode – zwracam pustą listę'); return const []; }

    // Serwerowo pobieramy CAŁY zakres bez filtrów podgrup (poza grupą) – filtr lokalnie daje pełną kontrolę.
    List data = <dynamic>[];

    // Cache
    _pruneCache();
    final ck = _rangeKey(start, end, group, subgroups);
    final cached = _rangeCache[ck];
    if (cached != null) {
      lastRangeRows = cached.data.length;
      lastRangeVariant = 'cache';
      return cached.data;
    }

    // 1) grupa_id
    final groupId = await _resolveGroupIdRobust(group);
    if (groupId != null) {
      try {
        data = await Supa.client
            .from('zajecia_grupy')
            .select('*')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', groupId)
            .order('od', ascending: true) as List;
        lastRangeVariant = 'grupa_id:$groupId';
      } catch(e) { debugPrint('[ClassesRepo][range] grupa_id err $e'); }
    }

    // Usunięto fallback po kod_grupy!
    // Jeśli nie znaleziono po grupa_id, zwracamy pustą listę
    if (data.isEmpty) {
      lastRangeRows = 0;
      lastRangeVariant = 'no_data';
      return [];
    }

    for (int i=0;i<data.length;i++){ final d=data[i]; if(d is Map && d.containsKey('grupy')) {
        final grp = d['grupy'];
        if (grp is Map && grp.containsKey('kod_grupy')) {
          try { d['kod_grupy'] = grp['kod_grupy']; } catch(_){}
        }
        d.remove('grupy');
      } }
    final list = <ClassModel>[];
    for (final r in data) { try { list.add(ClassModel.fromMap(Map<String,dynamic>.from(r as Map))); } catch(e){ debugPrint('[ClassesRepo][range][PARSE] $e'); } }

    // Capture variant used to fetch data - if server already filtered by group, skip strict local filter
    final fetchedVariant = lastRangeVariant;

    // DEBUG: dump unikalnych groupCode przed filtrowaniem
    if (kDebugMode) {
      final Map<String,int> gcount = {};
      for (final c in list) {
        final k = (c.groupCode ?? '<empty>').trim();
        gcount[k] = (gcount[k] ?? 0) + 1;
      }
      debugPrint('[ClassesRepo][range] unique groupCodes: ${gcount.entries.map((e)=>'${e.key}=${e.value}').join(', ')}');
      debugPrint('[ClassesRepo][range] fetchedVariant=$fetchedVariant');
    }

    // Dodatkowy filtr bezpieczeństwa po kodzie grupy (jeśli jest w modelu)
    if (group.isNotEmpty) {
      // If server already filtered by grupa_id or kod_grupy or data came from cache -> skip local strict filter
      if (fetchedVariant.startsWith('grupa_id') || fetchedVariant.startsWith('kod_grupy') || fetchedVariant == 'cache') {
        if (kDebugMode) debugPrint('[ClassesRepo][range] skipping local groupCode filter because fetchedVariant=$fetchedVariant');
      } else {
        String _normalize(String s) => s.replaceAll(RegExp(r'[^A-Za-z0-9]'), '').toLowerCase();
        final before = list.length;
        final gNorm = _normalize(group);
        list.retainWhere((c) {
          final cgRaw = (c.groupCode ?? '').trim();
          if (cgRaw.isEmpty) return false; // brak danych w rekordzie -> odrzucamy, wymagana zgodność
          final cg = _normalize(cgRaw);
          return cg == gNorm;
        });
        if (kDebugMode) {
          debugPrint('[ClassesRepo][range] group filter $before -> ${list.length} for group=$group (norm=$gNorm)');
        }
      }
    }

    final lowerSubs = subgroups.map((e)=>e.toLowerCase()).toSet();
    if (lowerSubs.isNotEmpty) {
      final before = list.length;
      list.retainWhere((c){
        final pg = (c.subgroup??'').trim();
        if (pg.isEmpty) return true; // zawsze akceptujemy puste
        return lowerSubs.contains(pg.toLowerCase());
      });
      debugPrint('[ClassesRepo][range] subgroup filter $before -> ${list.length} (${lowerSubs.join(',')})');
    } else {
      // brak wybranych podgrup – pokaż wszystko (w tym A/B) – zachowanie bardziej przyjazne użytkownikowi
    }
    list.sort((a,b)=>a.startTime.compareTo(b.startTime));
    debugPrint('[ClassesRepo][range] final=${list.length} variant=$lastRangeVariant from=$start to=$end');

    // Cache save
    _rangeCache[ck] = _RangeCacheEntry(list);
    return list;
  }

  static Future<List<ClassModel>> fetchDay(DateTime day,{String? groupCode,List<String> subgroups=const []}) async {
    final start = DateTime(day.year, day.month, day.day);
    final end = start.add(const Duration(days:1));
    lastDayQueried = start; lastDayVariant=''; lastDayRows=0;
    final list = await fetchRange(from:start,to:end,groupCode:groupCode,subgroups:subgroups);
    lastDayRows = list.length; lastDayVariant = lastRangeVariant; return list;
  }

  static Future<List<ClassModel>> fetchWeek(DateTime anyDay,{String? groupCode,List<String> subgroups=const []}) async {
    final monday = _mondayOfWeek(anyDay);
    final end = monday.add(const Duration(days:7));
    lastWeekStart = monday; lastWeekVariant=''; lastWeekRows = 0;
    final list = await fetchRange(from:monday,to:end,groupCode:groupCode,subgroups:subgroups);
    lastWeekRows = list.length; lastWeekVariant = lastRangeVariant; return list;
  }

  static Future<List<ClassModel>> fetchDayWithWeekFallback(DateTime day,{String? groupCode,List<String> subgroups=const []}) async {
    final primary = await fetchDay(day, groupCode: groupCode, subgroups: subgroups);
    if (primary.isNotEmpty) return primary;
    final week = await fetchWeek(day, groupCode: groupCode, subgroups: subgroups);
    return week.where((c)=> c.startTime.year==day.year && c.startTime.month==day.month && c.startTime.day==day.day).toList()..sort((a,b)=>a.startTime.compareTo(b.startTime));
  }

  static DateTime _mondayOfWeek(DateTime d){ final wd=d.weekday; return DateTime(d.year,d.month,d.day-(wd-1)); }

  static List<ClassModel> filterRemainingOrAll(List<ClassModel> classes, DateTime day, DateTime now,{bool allowEndedIfAllEnded=true}) {
    final sameDay = classes.where((c)=> c.startTime.year==day.year && c.startTime.month==day.month && c.startTime.day==day.day).toList();
    final remaining = sameDay.where((c){ final st=c.startTime; final et=c.endTime; return (st.isBefore(now)||st.isAtSameMomentAs(now)) && et.isAfter(now) || st.isAfter(now); }).toList()..sort((a,b)=>a.startTime.compareTo(b.startTime));
    if (remaining.isNotEmpty) return remaining;
    if (allowEndedIfAllEnded && sameDay.isNotEmpty) { sameDay.sort((a,b)=>a.startTime.compareTo(b.startTime)); return sameDay; }
    return const [];
  }

  /// Multi-day (n dni) – wrapper.
  static Future<List<ClassModel>> fetchMultiDay(DateTime from, int days,{String? groupCode,List<String> subgroups=const []}) async {
    final start = DateTime(from.year, from.month, from.day);
    final end = start.add(Duration(days: days));
    return fetchRange(from:start,to:end,groupCode:groupCode,subgroups:subgroups);
  }
}

class _RangeCacheEntry {
  final List<ClassModel> data;
  final DateTime inserted = DateTime.now();
  _RangeCacheEntry(this.data);
}
