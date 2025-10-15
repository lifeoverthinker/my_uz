import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/supabase.dart';

/// Repozytorium pobierania zajęć: dzień / tygzień / dowolny zakres.
class ClassesRepository {
  // Klucze prefów
  static const String _prefGroup = 'onb_group';
  static const String _prefSub = 'onb_group_sub';
  static const String _prefGroupId = 'onb_group_id';
  static const String _prefGroupMeta = 'onb_group_meta';

  // Klucze ulubionych
  static const String _prefFavPlans = 'fav_plans';
  static const String _prefFavLabels = 'fav_labels';
  static const String _prefFavSubgroups = 'fav_subgroups';

  /// --- Helpers for favorites (DRY) ---
  static Future<Set<String>> loadFavorites() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavPlans);
    if (raw == null || raw.isEmpty) return <String>{};
    try {
      final List<dynamic> dec = jsonDecode(raw);
      return dec.map((e) => e.toString()).toSet();
    } catch (_) {
      return <String>{};
    }
  }

  static Future<void> saveFavorites(Set<String> favs) async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_prefFavPlans, jsonEncode(favs.toList()));
  }

  /// Toggle favorite entry (key like 'group:<id>' or 'teacher:<id>').
  /// If `label` is provided when adding, updates fav_labels accordingly.
  static Future<void> toggleFavorite(String key, {String? label}) async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavPlans);
    final List<String> list = raw == null || raw.isEmpty ? [] : List<String>.from(jsonDecode(raw).map((e) => e.toString()));
    final exists = list.contains(key);
    if (exists) {
      list.remove(key);
    } else {
      list.add(key);
    }
    await p.setString(_prefFavPlans, jsonEncode(list));

    // labels
    try {
      final rawLabels = p.getString(_prefFavLabels);
      Map<String, String> labels = {};
      if (rawLabels != null && rawLabels.isNotEmpty) {
        final dec = jsonDecode(rawLabels);
        if (dec is Map) dec.forEach((k, v) { if (k is String && v is String) labels[k] = v; });
      }
      if (!exists) {
        if (label != null && label.isNotEmpty) labels[key] = label;
      } else {
        labels.remove(key);
      }
      await p.setString(_prefFavLabels, jsonEncode(labels));
    } catch (_) {}
  }

  static Future<bool> isFavorite(String key) async {
    final favs = await loadFavorites();
    return favs.contains(key);
  }

  static Future<Map<String, String>> loadFavoriteLabels() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavLabels);
    if (raw == null || raw.isEmpty) return {};
    try {
      final dec = jsonDecode(raw);
      if (dec is Map) {
        final out = <String, String>{};
        dec.forEach((k, v) { if (k is String && v is String) out[k] = v; });
        return out;
      }
    } catch (_) {}
    return {};
  }

  static Future<void> setFavoriteLabel(String key, String? label) async {
    final p = await SharedPreferences.getInstance();
    final rawLabels = p.getString(_prefFavLabels);
    Map<String, String> labels = {};
    try {
      if (rawLabels != null && rawLabels.isNotEmpty) {
        final dec = jsonDecode(rawLabels);
        if (dec is Map) dec.forEach((k, v) { if (k is String && v is String) labels[k] = v; });
      }
      if (label == null || label.isEmpty) {
        labels.remove(key);
      } else {
        labels[key] = label;
      }
      await p.setString(_prefFavLabels, jsonEncode(labels));
    } catch (_) {}
  }

  static Future<Map<String, List<String>>> loadFavSubgroupsMap() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavSubgroups);
    if (raw == null || raw.isEmpty) return {};
    try {
      final dec = jsonDecode(raw);
      if (dec is Map) {
        final Map<String, List<String>> out = {};
        dec.forEach((k, v) {
          if (k is String && v is List) {
            out[k] = v.map((e) => e.toString()).toList();
          }
        });
        return out;
      }
    } catch (_) {}
    return {};
  }

  static Future<void> saveFavSubgroupsMap(Map<String, List<String>> map) async {
    final p = await SharedPreferences.getInstance();
    try {
      await p.setString(_prefFavSubgroups, jsonEncode(map));
    } catch (_) {}
  }

  // Stabilny regex UUID (wymóg)
  static final RegExp uuidRe = RegExp(r'^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$');

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
  // Cache dla list podgrup (key: groupCode -> entry)
  static final Map<String,_SubgroupsCacheEntry> _subgroupsCache = {};
  static const Duration _subgroupsCacheTtl = Duration(minutes: 30);
  static String _rangeKey(DateTime from, DateTime to, String group, List<String> subs){
    final subsKey = subs.map((e)=>e.trim().toLowerCase()).toList()..sort();
    return '${from.millisecondsSinceEpoch}:${to.millisecondsSinceEpoch}:$group:${subsKey.join('|')}';
  }

  static void _pruneCache(){
    final now = DateTime.now();
    _rangeCache.removeWhere((_,v)=> now.difference(v.inserted) > _cacheTtl);
    // prune subgroups cache as well
    _subgroupsCache.removeWhere((_,v)=> now.difference(v.inserted) > _subgroupsCacheTtl);
  }

  static void _clearCaches() {
    _rangeCache.clear();
    _subgroupsCache.clear();
    _groupIdCache.clear();
  }

  static String _trim(String? s) => (s ?? '').trim();

  /// Zwraca kanoniczny klucz ulubionego planu: 'group:<id>' jeśli id dostępne, inaczej 'group:<code>'
  static String canonicalFavKey({String? groupId, String? groupCode}) {
    if (groupId != null && groupId.trim().isNotEmpty) {
      return 'group:${groupId.trim()}';
    }
    return 'group:${(groupCode ?? '').trim()}';
  }

  /// Odczyt grupy i podgrup z prefów (subgrupy w JSON lub starszy CSV -> fallback).
  static Future<(String? groupCode, List<String> subgroups)> loadGroupPrefs() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
    // Preferuj JSON array, wsteczna kompatybilność z CSV
    final subsJson = p.getString(_prefSub);
    List<String> subs = const [];
    if (subsJson != null && subsJson.isNotEmpty) {
      try {
        final dec = jsonDecode(subsJson);
        if (dec is List) {
          subs = dec.map((e)=>e.toString()).where((e)=>e.trim().isNotEmpty).toList();
        } else if (dec is String) {
          subs = dec.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
        }
      } catch (_) {
        // może być CSV
        subs = subsJson.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
      }
    }
    return (groupCode, subs);
  }

  /// Odczyt kontekstu grupy: kod, podgrupy (nigdy null) i zapisany groupId (jeśli istnieje)
  static Future<(String? groupCode, List<String> subgroups, String? groupId)> loadGroupContext() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
    // subgroups JSON-first
    List<String> subs = const [];
    final subsRaw = p.getString(_prefSub);
    if (subsRaw != null && subsRaw.isNotEmpty) {
      try {
        final dec = jsonDecode(subsRaw);
        if (dec is List) {
          subs = dec.map((e)=>e.toString()).where((e)=>e.trim().isNotEmpty).toList();
        } else if (dec is String) {
          subs = dec.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
        }
      } catch (_) {
        subs = subsRaw.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
      }
    }
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

  /// Publiczne: uzyskaj ID grupy po kodzie (bez heurystyk id vs code).
  static Future<String?> resolveGroupIdByCode(String code) => _resolveGroupId(code);

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
    final code = groupCode?.trim() ?? '';
    if (code.isEmpty) {
      await p.remove(_prefGroup);
      await p.remove(_prefSub);
      await p.remove(_prefGroupId);
      await p.remove(_prefGroupMeta);
      _clearCaches();
      debugPrint('[ClassesRepo][setGroupPrefs] cleared (no group)');
      return;
    }
    await p.setString(_prefGroup, code);
    // Zapisz subgrupy jako JSON array (pusta lista jeśli brak)
    try {
      final jsonArr = jsonEncode((subgroups..removeWhere((e)=>e.trim().isEmpty)).toList());
      await p.setString(_prefSub, jsonArr);
    } catch (_) {
      await p.setString(_prefSub, jsonEncode(<String>[]));
    }
    // ID cache może być nieaktualne – usuń istniejące entry dla tej grupy
    _groupIdCache.remove(code);
    // Usuń zapisany group id – wymusi resolver asynchronicznie
    try { await p.remove(_prefGroupId); } catch (_) {}
    _clearCaches();
    debugPrint('[ClassesRepo][setGroupPrefs] saved code=$code, subs=${subgroups.join(',')}');

    // Best-effort: rozwiąż ID po kodzie i zaktualizuj zapisane ID + migracja fav keys w tle
    () async {
      try {
        final id = await _resolveGroupId(code);
        if (id != null && id.isNotEmpty) {
          await p.setString(_prefGroupId, id);
          _groupIdCache[code] = id;
          debugPrint('[ClassesRepo][setGroupPrefs] resolved id=$id for $code');
          await _migrateFavKeysIfNeeded();
        }
      } catch (e) { debugPrint('[ClassesRepo][setGroupPrefs][resolve/migrate] $e'); }
    }();
  }

  /// Zapis grupy po ID i kodzie – preferowany sposób, gwarantuje jednoznaczność
  static Future<void> setGroupPrefsById({required String groupId, required String groupCode, List<String> subgroups = const []}) async {
    final p = await SharedPreferences.getInstance();
    final id = groupId.trim();
    final code = groupCode.trim();
    await p.setString(_prefGroup, code);
    try { await p.setString(_prefSub, jsonEncode((subgroups..removeWhere((e)=>e.trim().isEmpty)).toList())); } catch (_) { await p.setString(_prefSub, jsonEncode(<String>[])); }
    await p.setString(_prefGroupId, id);
    _groupIdCache[code] = id; // wypełnij cache
    _clearCaches();
    // Spróbuj pobrać metadane grupy (jeśli istnieją) i zapisać je w prefs jako JSON
    try {
      final row = await Supa.client.from('grupy').select().eq('id', id).maybeSingle();
      if (row != null) {
        try {
          final json = jsonEncode(row);
          await p.setString(_prefGroupMeta, json);
        } catch (_) {}
      }
    } catch (_) {
      // ignore network errors — metadane są opcjonalne
    }
    // Best-effort: migracja kluczy ulubionych
    () async { try { await _migrateFavKeysIfNeeded(); } catch (e) { debugPrint('[ClassesRepo][setGroupPrefsById][migrate] $e'); } }();
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

  static Future<List<String>> getSubgroupsForGroup(String groupCode, {bool forceRefresh = false}) async {
    final g = _trim(groupCode);
    if (g.isEmpty) return [];
    // Check cache first (unless forceRefresh requested)
    final now = DateTime.now();
    final cached = _subgroupsCache[g];
    if (!forceRefresh && cached != null && now.difference(cached.inserted) <= _subgroupsCacheTtl) {
      return List<String>.from(cached.data);
    }

    // Fallback chain: a) tabela 'podgrupy' (jeśli istnieje), b) RPC (jeśli dostępne), c) distinct podgrupa z 'plan_zajec' po kod_grupy, d) fallback po zajecia_grupy via grupa_id, e) []
    List<String> subs = [];
    try {
      // a) podgrupy table
      try {
        final rows = await Supa.client
            .from('podgrupy')
            .select('podgrupa')
            .eq('kod_grupy', g) as List<dynamic>;
        if (rows.isNotEmpty) {
          subs = rows
              .map((e) => (e['podgrupa'] as String?)?.trim())
              .whereType<String>()
              .where((s) => s.isNotEmpty)
              .toSet()
              .toList()
            ..sort();
        }
      } catch (_) {
        // tabela może nie istnieć — pomijamy
      }

      // b) RPC (jeśli byłoby dostępne) — nazwa przykładowa 'get_subgroups_for_group'
      if (subs.isEmpty) {
        try {
          final rpc = await Supa.client.rpc('get_subgroups_for_group', params: {'p_kod_grupy': g});
          if (rpc is List) {
            subs = rpc.map((e)=> e.toString()).where((s)=>s.trim().isNotEmpty).toSet().toList()..sort();
          } else if (rpc is Map && rpc['subgroups'] is List) {
            final l = rpc['subgroups'] as List;
            subs = l.map((e)=> e.toString()).where((s)=>s.trim().isNotEmpty).toSet().toList()..sort();
          }
        } catch (_) {/* brak RPC lub błąd — ignoruj */}
      }

      // c) distinct from plan_zajec by kod_grupy
      if (subs.isEmpty) {
        try {
          final rows = await Supa.client
              .from('plan_zajec')
              .select('podgrupa')
              .eq('kod_grupy', g) as List<dynamic>;
          if (rows.isNotEmpty) {
            subs = rows
                .map((e) => (e['podgrupa'] as String?)?.trim())
                .whereType<String>()
                .where((s) => s.isNotEmpty)
                .toSet()
                .toList()
              ..sort();
          }
        } catch (_) {/* tabela może nie istnieć — ignoruj */}
      }

      // d) fallback: zajecia_grupy via grupa_id (obecny model)
      if (subs.isEmpty) {
        try {
          final groupId = await _resolveGroupIdRobust(g);
          if (groupId != null) {
            final subRes = await Supa.client
                .from('zajecia_grupy')
                .select('podgrupa')
                .eq('grupa_id', groupId)
                .not('podgrupa', 'is', null) as List<dynamic>;
            subs = subRes
                .map((e) => (e['podgrupa'] as String?)?.trim())
                .whereType<String>()
                .where((s) => s.isNotEmpty)
                .toSet()
                .toList()
              ..sort();
          }
        } catch (_) {/* ignoruj */}
      }
    } catch (e) {
      debugPrint('[ClassesRepo][getSubgroupsForGroup] err $e');
      subs = [];
    }

    // save to cache
    _subgroupsCache[g] = _SubgroupsCacheEntry(subs);
    return subs;
  }

  /// Pobierz zakres zajęć dla nauczyciela. Zakłada istnienie tabeli 'zajecia_nauczyciela' z kolumną 'nauczyciel_id' i polami kompatybilnymi z ClassModel.
  static Future<List<ClassModel>> fetchTeacherRange({required DateTime from, required DateTime to, required String teacherId}) async {
    final start = DateTime(from.year, from.month, from.day, from.hour, from.minute);
    final end = DateTime(to.year, to.month, to.day, to.hour, to.minute);
    try {
      final data = await Supa.client
          .from('zajecia_nauczyciela')
          .select('*')
          .gte('od', start.toIso8601String())
          .lt('od', end.toIso8601String())
          .eq('nauczyciel_id', teacherId)
          .order('od', ascending: true) as List<dynamic>;
      final list = <ClassModel>[];
      for (final r in data) {
        try { list.add(ClassModel.fromMap(Map<String,dynamic>.from(r as Map))); } catch (e) { debugPrint('[ClassesRepo][teacherRange][PARSE] $e'); }
      }
      return list;
    } catch (e) {
      debugPrint('[ClassesRepo][teacherRange] err $e');
      return const [];
    }
  }

  static Future<List<ClassModel>> fetchTeacherDay(DateTime day, {required String teacherId}) async {
    final start = DateTime(day.year, day.month, day.day);
    final end = start.add(const Duration(days:1));
    final list = await fetchTeacherRange(from: start, to: end, teacherId: teacherId);
    return list;
  }

  static Future<List<ClassModel>> fetchTeacherWeek(DateTime anyDay, {required String teacherId}) async {
    final monday = _mondayOfWeek(anyDay);
    final end = monday.add(const Duration(days:7));
    final list = await fetchTeacherRange(from: monday, to: end, teacherId: teacherId);
    return list;
  }

  static Future<List<ClassModel>> fetchTeacherDayWithWeekFallback(DateTime day, {required String teacherId}) async {
    final primary = await fetchTeacherDay(day, teacherId: teacherId);
    if (primary.isNotEmpty) return primary;
    final week = await fetchTeacherWeek(day, teacherId: teacherId);
    return week.where((c)=> c.startTime.year==day.year && c.startTime.month==day.month && c.startTime.day==day.day).toList()..sort((a,b)=>a.startTime.compareTo(b.startTime));
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

  /// Parsuje pole instytut/institute z bazy i zwraca listę czytelnych nazw.
  /// Normalizuje nowe linie/taby, scala krótkie fragmenty oraz dzieli po przecinku/średniku.
  static List<String> parseInstitutes(String? instRaw) {
    final institutes = <String>[];
    if (instRaw == null || instRaw.trim().isEmpty) return institutes;

    // Normalizacja: usuń CR, zamień taby na spacje i usuń niewidoczne/sterujące znaki
    var normalized = instRaw.replaceAll('\r', '').replaceAll('\t', ' ');
    // Usuń zero-width i soft-hyphen oraz innych kontrolnych, zamień NBSP na zwykłą spację
    normalized = normalized
        .replaceAll(RegExp(r'[\u200B\u200C\u200D\u2060\u00AD]'), '')
        .replaceAll('\u00A0', ' ');

    // Rozbijaj po nowych liniach, usuń puste i przytnij
    final lines = normalized.split('\n').map((s) => s.trim()).where((s) => s.isNotEmpty).toList();

    final merged = <String>[];
    for (var i = 0; i < lines.length; i++) {
      final cur = lines[i];
      if (cur.length <= 2 && i + 1 < lines.length) {
        lines[i + 1] = ('${cur} ${lines[i + 1]}').trim();
        continue;
      }
      merged.add(cur);
    }

    for (final m in merged) {
      final parts = m.split(RegExp(r'[;,]')).map((s) => s.trim()).where((s) => s.isNotEmpty);
      institutes.addAll(parts);
    }

    if (institutes.isEmpty) {
      var collapsed = normalized.replaceAll(RegExp(r'\s+'), ' ').trim();
      if (collapsed.isNotEmpty) institutes.add(collapsed);
    }

    return institutes;
  }

  /// Zwraca inicjały z pełnej nazwy (np. "Jan Kowalski" -> "JK", "Agnieszka" -> "A").
  static String initialsFromName(String? name) {
    final s = (name ?? '').trim();
    if (s.isEmpty) return 'A';
    final parts = s.split(RegExp(r'\s+')).where((p) => p.isNotEmpty).toList();
    if (parts.isEmpty) return 'A';
    String firstChar(String str) {
      final runes = str.runes;
      if (runes.isEmpty) return '';
      return String.fromCharCode(runes.first).toUpperCase();
    }
    if (parts.length == 1) return firstChar(parts.first);
    final first = firstChar(parts.first);
    final last = firstChar(parts.last);
    return '$first$last';
  }

  /// Publiczne: uzyskaj ID grupy na podstawie kodu (z wariantami). Zwraca null, jeśli nie znaleziono.
  static Future<String?> resolveGroupIdForCode(String groupCode) => _resolveGroupIdRobust(groupCode);

  /// Ustawia preferencje nauczyciela na podstawie jego ID
  static Future<void> setTeacherPrefsById(String teacherId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('teacher_id', teacherId);
  }

  /// Migracja kluczy ulubionych: group:<code> -> group:<id> (best-effort). Aktualizuje również fav_labels i fav_subgroups.
  static Future<void> _migrateFavKeysIfNeeded() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavPlans);
    if (raw == null || raw.isEmpty) return;
    List<dynamic> dec;
    try { dec = jsonDecode(raw) as List<dynamic>; } catch (_) { return; }
    bool changed = false;
    final List<String> list = dec.map((e)=>e.toString()).toList();
    final Map<String,String> replacements = {}; // old->new

    for (final k in list) {
      if (!k.startsWith('group:')) continue;
      final token = k.substring('group:'.length);
      if (uuidRe.hasMatch(token)) continue; // już id
      final code = token.trim();
      if (code.isEmpty) continue;
      try {
        final id = await _resolveGroupIdRobust(code);
        if (id != null && id.isNotEmpty) {
          final newKey = 'group:$id';
          replacements[k] = newKey;
        }
      } catch (_) {}
    }

    if (replacements.isNotEmpty) {
      // fav_plans
      final Set<String> newSet = list.toSet();
      for (final entry in replacements.entries) {
        if (newSet.remove(entry.key)) { newSet.add(entry.value); changed = true; }
      }
      if (changed) {
        await p.setString(_prefFavPlans, jsonEncode(newSet.toList()));
      }
      // fav_labels
      try {
        final rl = p.getString(_prefFavLabels);
        if (rl != null && rl.isNotEmpty) {
          final Map<String,dynamic> lm = jsonDecode(rl) as Map<String,dynamic>;
          bool labelsChanged = false;
          for (final entry in replacements.entries) {
            if (lm.containsKey(entry.key)) {
              final v = lm.remove(entry.key);
              lm[entry.value] = v;
              labelsChanged = true;
            }
          }
          if (labelsChanged) await p.setString(_prefFavLabels, jsonEncode(lm));
        }
      } catch (_) {}
      // fav_subgroups
      try {
        final rs = p.getString(_prefFavSubgroups);
        if (rs != null && rs.isNotEmpty) {
          final Map<String,dynamic> sm = jsonDecode(rs) as Map<String,dynamic>;
          bool subsChanged = false;
          for (final entry in replacements.entries) {
            if (sm.containsKey(entry.key)) {
              final v = sm.remove(entry.key);
              sm[entry.value] = v;
              subsChanged = true;
            }
          }
          if (subsChanged) await p.setString(_prefFavSubgroups, jsonEncode(sm));
        }
      } catch (_) {}
    }
  }
}

class _RangeCacheEntry {
  final List<ClassModel> data;
  final DateTime inserted = DateTime.now();
  _RangeCacheEntry(this.data);
}

class _SubgroupsCacheEntry {
  final List<String> data;
  final DateTime inserted = DateTime.now();
  _SubgroupsCacheEntry(this.data);
}
