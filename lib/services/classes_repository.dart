import 'dart:convert';
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
  static const String _prefGroupMeta = 'onb_group_meta';

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

  /// Odczyt kontekstu grupy: kod, podgrupy i zapisany groupId (jeśli istnieje)
  static Future<(String? groupCode, List<String> subgroups, String? groupId)> loadGroupContext() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
    final subsCsv = p.getString(_prefSub) ?? '';
    final subs = subsCsv.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
    final savedId = _trim(p.getString(_prefGroupId));
    final groupId = savedId.isEmpty ? null : savedId;
    if (groupCode != null && groupId != null) {
      // warm cache dla danego kodu
      _groupIdCache[groupCode] = groupId;
    }
    return (groupCode, subs, groupId);
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

  /// Szuka grup po fragmencie kodu (ilike) – zwraca listę map {id, kod_grupy}
  static Future<List<Map<String,dynamic>>> searchGroups(String q, {int limit = 50}) async {
    final trimmed = _trim(q);
    if (trimmed.isEmpty) return [];
    try {
      final rows = await Supa.client.from('grupy').select('id,kod_grupy').ilike('kod_grupy', '%$trimmed%').limit(limit) as List<dynamic>;
      return rows.map((r) => Map<String,dynamic>.from(r as Map)).toList();
    } catch (e) {
      debugPrint('[ClassesRepo][searchGroups] err $e');
      return [];
    }
  }

  /// Szuka nauczycieli po fragmencie nazwy – zwraca listę map {id, nazwa}
  static Future<List<Map<String,dynamic>>> searchTeachers(String q, {int limit = 50}) async {
    final trimmed = _trim(q);
    if (trimmed.isEmpty) return [];
    try {
      final rows = await Supa.client.from('nauczyciele').select('id,nazwa').ilike('nazwa', '%$trimmed%').limit(limit) as List<dynamic>;
      return rows.map((r) => Map<String,dynamic>.from(r as Map)).toList();
    } catch (e) {
      debugPrint('[ClassesRepo][searchTeachers] err $e');
      return [];
    }
  }

  /// Ustawia wybraną grupę i podgrupy w SharedPreferences oraz czyści wewnętrzne cache
  static Future<void> setGroupPrefs(String? groupCode, List<String> subgroups) async {
    final p = await SharedPreferences.getInstance();
    if (groupCode == null || groupCode.trim().isEmpty) {
      await p.remove(_prefGroup);
      await p.remove(_prefSub);
      await p.remove(_prefGroupId);
      _groupIdCache.clear();
      return;
    }
    await p.setString(_prefGroup, groupCode.trim());
    await p.setString(_prefSub, subgroups.join(','));
    // ID cache może być nieaktualne – usuń istniejące entry dla tej grupy
    _groupIdCache.remove(groupCode);
    // Usuń zapisany group id jeśli był inny
    try { await p.remove(_prefGroupId); } catch (_) {}
  }

  /// Zapis grupy po ID i kodzie – preferowany sposób, gwarantuje jednoznaczność
  static Future<void> setGroupPrefsById({required String groupId, required String groupCode, List<String> subgroups = const []}) async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_prefGroup, groupCode.trim());
    await p.setString(_prefSub, subgroups.join(','));
    await p.setString(_prefGroupId, groupId);
    _groupIdCache[groupCode] = groupId; // wypełnij cache
    // Spróbuj pobrać metadane grupy (jeśli istnieją) i zapisać je w prefs jako JSON
    try {
      final row = await Supa.client.from('grupy').select().eq('id', groupId).maybeSingle();
      if (row != null) {
        try {
          final json = jsonEncode(row);
          await p.setString(_prefGroupMeta, json);
        } catch (_) {}
      }
    } catch (_) {
      // ignore network errors — metadane są opcjonalne
    }
  }

  /// Zwróć mapę meta (rozpakowany JSON) jeśli jest dostępna.
  static Future<Map<String,dynamic>?> loadGroupMeta() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefGroupMeta);
    if (raw == null || raw.isEmpty) return null;
    try {
      final m = jsonDecode(raw) as Map<String,dynamic>;
      return m;
    } catch (_) {
      return null;
    }
  }

  /// Uniwersalne pobranie zakresu [from, to). Zwraca posortowaną listę.
  static Future<List<ClassModel>> fetchRange({required DateTime from, required DateTime to, String? groupCode, List<String> subgroups = const [], String? groupId}) async {
    final rawGroup = _trim(groupCode);
    final group = rawGroup; // brak sztucznego fallbacku – jeśli brak grupy zwracamy []
    final start = DateTime(from.year, from.month, from.day, from.hour, from.minute);
    final end = DateTime(to.year, to.month, to.day, to.hour, to.minute);
    lastRangeFrom = start; lastRangeTo = end; lastRangeRows = 0; lastRangeVariant = 'none';
    if (group.isEmpty && (groupId==null || groupId.trim().isEmpty)) { debugPrint('[ClassesRepo][range] brak groupCode/groupId – zwracam pustą listę'); return const []; }

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

    // 1) jeśli przekazano groupId – użyj go bez żadnych fallbacków
    final String _resolvedRaw = _trim(groupId);
    String? resolvedGroupId = _resolvedRaw.isEmpty ? null : _resolvedRaw;

    if (resolvedGroupId != null) {
      try {
        data = await Supa.client
            .from('zajecia_grupy')
            .select('*')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', resolvedGroupId)
            .order('od', ascending: true) as List;
        lastRangeVariant = 'grupa_id:param';
      } catch(e) { debugPrint('[ClassesRepo][range] grupa_id:param err $e'); }
    }

    // 2) jeśli nie ma param groupId albo zapytanie zwróciło pusto – spróbuj ustalić ID po kodzie
    if (data.isEmpty && resolvedGroupId == null && group.isNotEmpty) {
      final resolved = await _resolveGroupIdRobust(group);
      if (resolved != null) {
        try {
          data = await Supa.client
              .from('zajecia_grupy')
              .select('*')
              .gte('od', start.toIso8601String())
              .lt('od', end.toIso8601String())
              .eq('grupa_id', resolved)
              .order('od', ascending: true) as List;
          lastRangeVariant = 'grupa_id:$resolved';
        } catch(e) { debugPrint('[ClassesRepo][range] grupa_id resolved err $e'); }
      }

      // 3) fallback po kod_grupy tylko jeśli nie udało się ustalić groupId
      if (data.isEmpty && resolved == null) {
        final variants = <String>{
          group,
          group.replaceAll('-', ''),
          group.replaceAll(' ', ''),
          group.replaceAll('-', ' '),
          group.toUpperCase(),
          group.toLowerCase(),
        };
        for (final v in variants) {
          if (v.isEmpty) continue;
          try {
            final rows = await Supa.client
                .from('zajecia_grupy')
                .select('*,grupy(kod_grupy)')
                .gte('od', start.toIso8601String())
                .lt('od', end.toIso8601String())
                .eq('grupy.kod_grupy', v)
                .order('od', ascending: true) as List;
            if (rows.isNotEmpty) { data = rows; lastRangeVariant = 'kod_grupy:$v'; break; }
          } catch (e) { debugPrint('[ClassesRepo][range] kod_grupy $v err $e'); }
        }
      }
    }

    lastRangeRows = data.length;

    for (int i=0;i<data.length;i++){ final d=data[i]; if(d is Map && d.containsKey('grupy')) d.remove('grupy'); }
    final list = <ClassModel>[];
    for (final r in data) { try { list.add(ClassModel.fromMap(Map<String,dynamic>.from(r as Map))); } catch(e){ debugPrint('[ClassesRepo][range][PARSE] $e'); } }

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

  static Future<List<ClassModel>> fetchDay(DateTime day,{String? groupCode,List<String> subgroups=const [], String? groupId}) async {
    final start = DateTime(day.year, day.month, day.day);
    final end = start.add(const Duration(days:1));
    lastDayQueried = start; lastDayVariant=''; lastDayRows=0;
    final list = await fetchRange(from:start,to:end,groupCode:groupCode,subgroups:subgroups, groupId: groupId);
    lastDayRows = list.length; lastDayVariant = lastRangeVariant; return list;
  }

  static Future<List<ClassModel>> fetchWeek(DateTime anyDay,{String? groupCode,List<String> subgroups=const [], String? groupId}) async {
    final monday = _mondayOfWeek(anyDay);
    final end = monday.add(const Duration(days:7));
    lastWeekStart = monday; lastWeekVariant=''; lastWeekRows = 0;
    final list = await fetchRange(from:monday,to:end,groupCode:groupCode,subgroups:subgroups, groupId: groupId);
    lastWeekRows = list.length; lastWeekVariant = lastRangeVariant; return list;
  }

  static Future<List<ClassModel>> fetchDayWithWeekFallback(DateTime day,{String? groupCode,List<String> subgroups=const [], String? groupId}) async {
    final primary = await fetchDay(day, groupCode: groupCode, subgroups: subgroups, groupId: groupId);
    if (primary.isNotEmpty) return primary;
    final week = await fetchWeek(day, groupCode: groupCode, subgroups: subgroups, groupId: groupId);
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
  static Future<List<ClassModel>> fetchMultiDay(DateTime from, int days,{String? groupCode,List<String> subgroups=const [], String? groupId}) async {
    final start = DateTime(from.year, from.month, from.day);
    final end = start.add(Duration(days: days));
    return fetchRange(from:start,to:end,groupCode:groupCode,subgroups:subgroups, groupId: groupId);
  }

  static Future<List<String>> getSubgroupsForGroup(String groupCode) async {
    final g = _trim(groupCode);
    if (g.isEmpty) return [];
    try {
      final groupId = await _resolveGroupIdRobust(g);
      if (groupId == null) return [];
      final subRes = await Supa.client
          .from('zajecia_grupy')
          .select('podgrupa')
          .eq('grupa_id', groupId)
          .not('podgrupa', 'is', null) as List<dynamic>;
      final subs = subRes
          .map((e) => (e['podgrupa'] as String?)?.trim())
          .whereType<String>()
          .where((s) => s.isNotEmpty)
          .toSet()
          .toList()
        ..sort();
      return subs;
    } catch (e) {
      debugPrint('[ClassesRepo][getSubgroupsForGroup] err $e');
      return [];
    }
  }

  /// Pobierz szczegóły nauczyciela (email, instytut, nazwa) jeśli są dostępne
  static Future<Map<String,dynamic>?> getTeacherDetails(String teacherId) async {
    try {
      final row = await Supa.client.from('nauczyciele').select('id,nazwa,email,instytut').eq('id', teacherId).maybeSingle();
      if (row == null) return null;
      return Map<String,dynamic>.from(row as Map);
    } catch (e) {
      debugPrint('[ClassesRepo][getTeacherDetails] err $e');
      return null;
    }
  }

  /// Pobierz meta grupy po ID (id, kod_grupy, nazwa) - pomocnicze dla drawer/ulubionych
  static Future<Map<String,dynamic>?> getGroupById(String groupId) async {
    try {
      final row = await Supa.client.from('grupy').select('id,kod_grupy,nazwa').eq('id', groupId).maybeSingle();
      if (row == null) return null;
      return Map<String,dynamic>.from(row as Map);
    } catch (e) {
      debugPrint('[ClassesRepo][getGroupById] err $e');
      return null;
    }
  }

  /// Publiczne: uzyskaj ID grupy na podstawie kodu (z wariantami). Zwraca null, jeśli nie znaleziono.
  static Future<String?> resolveGroupIdForCode(String groupCode) => _resolveGroupIdRobust(groupCode);

  /// Ustawia preferencje nauczyciela na podstawie jego ID
  static Future<void> setTeacherPrefsById(String teacherId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('teacher_id', teacherId);
  }
}

class _RangeCacheEntry {
  final List<ClassModel> data;
  final DateTime inserted = DateTime.now();
  _RangeCacheEntry(this.data);
}
