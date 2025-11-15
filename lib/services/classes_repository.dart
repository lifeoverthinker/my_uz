// Plik: lib/services/classes_repository.dart
// (Wprowadziłem 3 poprawki: w _normalizeRowDates, _prepareRowForClassModel,
// oraz w zapytaniu 'group_id_cache' w fetchRange, aby używać 'do_' zamiast 'do')

import 'dart:async';
import 'dart:convert';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/supabase.dart';

class ClassesRepository {
  final SupabaseClient _client;

  ClassesRepository._internal({required SupabaseClient client}) : _client = client;

  static final ClassesRepository instance = ClassesRepository._internal(client: Supa.client);
  static SupabaseClient get _supabaseClient => instance._client;

  static const String _prefGroup = 'onb_group';
  static const String _prefSub = 'onb_group_sub';
  static const String _prefGroupId = 'onb_group_id';
  static const String _prefGroupMeta = 'onb_group_meta';
  static const String _prefFavPlans = 'fav_plans';
  static const String _prefFavLabels = 'fav_labels';
  static const String _prefFavSubgroups = 'fav_subgroups';

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
    _notifyFavoritesChanged();
  }

  static Future<void> toggleFavorite(String key, {String? label}) async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_prefFavPlans);
    final List<String> list = raw == null || raw.isEmpty ? [] : List<String>.from(jsonDecode(raw) as List<dynamic>).map((e) => e.toString()).toList();
    final exists = list.contains(key);
    if (exists) {
      list.remove(key);
    } else {
      list.add(key);
    }
    await p.setString(_prefFavPlans, jsonEncode(list));

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
    _notifyFavoritesChanged();
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

  static Future<List<String>> getValidatedSubgroupsForFavorite(String favKey) async {
    final favSubgroups = await loadFavSubgroupsMap();
    List<String> savedSubs = favSubgroups[favKey] ?? <String>[];
    if (savedSubs.isEmpty) return <String>[];

    final parts = favKey.split(':');
    if (parts.length != 2 || parts[0] != 'group') return <String>[];
    final groupId = parts[1];
    if (groupId.isEmpty) return <String>[];

    List<String> currentSubs = <String>[];
    try {
      final groupRow = await _supabaseClient.from('grupy').select('kod_grupy').eq('id', groupId).maybeSingle();
      if (groupRow != null && groupRow['kod_grupy'] != null) {
        final code = groupRow['kod_grupy'] as String;
        currentSubs = await getSubgroupsForGroup(code, forceRefresh: false);
      }
    } catch (_) {
      return savedSubs;
    }

    if (currentSubs.isEmpty) return savedSubs;

    final currentSet = currentSubs.toSet();
    final validated = savedSubs.where((s) => currentSet.contains(s)).toList();
    return validated;
  }

  static Future<void> setValidatedSubgroupsForFavorite(String favKey, List<String> subs) async {
    final favSubgroups = await loadFavSubgroupsMap();
    List<String> toSave = subs;
    if (toSave.isNotEmpty) {
      final parts = favKey.split(':');
      if (parts.length == 2 && parts[0] == 'group') {
        final groupId = parts[1];
        if (groupId.isNotEmpty) {
          try {
            final groupRow = await _supabaseClient.from('grupy').select('kod_grupy').eq('id', groupId).maybeSingle();
            if (groupRow != null && groupRow['kod_grupy'] != null) {
              final code = groupRow['kod_grupy'] as String;
              final currentSubs = await getSubgroupsForGroup(code, forceRefresh: false);
              if (currentSubs.isNotEmpty) {
                final currentSet = currentSubs.toSet();
                toSave = toSave.where((s) => currentSet.contains(s)).toList();
              }
            }
          } catch (_) {
            // błąd
          }
        }
      }
    }
    favSubgroups[favKey] = toSave;
    await saveFavSubgroupsMap(favSubgroups);
  }

  static final RegExp uuidRe = RegExp(r'^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$');

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

  static final Map<String,_RangeCacheEntry> _rangeCache = {};
  static const Duration _cacheTtl = Duration(minutes: 5);
  static final Map<String, _SubgroupsCacheEntry> _subgroupsCache = {};
  static const Duration _subgroupsCacheTtl = Duration(minutes: 30);
  static final Map<String, _SubjectsCacheEntry> _subjectsByGroupCache = {};
  static final Map<String, _TypesCacheEntry> _typesByGroupSubjectCache = {};
  static const Duration _subjectsTtl = Duration(minutes: 20);
  static const Duration _typesTtl = Duration(minutes: 20);
  static String _rangeKey(DateTime from, DateTime to, String group, List<String> subs){
    final subsKey = subs.map((e)=>e.trim().toLowerCase()).toList()..sort();
    return '${from.millisecondsSinceEpoch}:${to.millisecondsSinceEpoch}:$group:${subsKey.join('|')}';
  }

  static void _pruneCache(){
    final now = DateTime.now();
    _rangeCache.removeWhere((_,v)=> now.difference(v.inserted) > _cacheTtl);
    _subgroupsCache.removeWhere((_,v)=> now.difference(v.inserted) > _subgroupsCacheTtl);
    _subjectsByGroupCache.removeWhere((_, v) => now.difference(v.inserted) > _subjectsTtl);
    _typesByGroupSubjectCache.removeWhere((_, v) => now.difference(v.inserted) > _typesTtl);
  }

  static void _clearCaches() {
    _rangeCache.clear();
    _subgroupsCache.clear();
    _groupIdCache.clear();
  }

  static String _trim(String? s) => (s ?? '').trim();

  static Map<String, dynamic> _normalizeRowDates(Map<String, dynamic> row) {
    final out = Map<String, dynamic>.from(row);

    void _normKey(String key) {
      try {
        if (!out.containsKey(key)) return;
        final v = out[key];
        if (v == null) return;
        if (v is DateTime) {
          out[key] = v.toLocal().toIso8601String();
        } else if (v is String) {
          try {
            final parsed = DateTime.parse(v);
            out[key] = parsed.toLocal().toIso8601String();
          } catch (_) {
            try {
              final parts = v.split(RegExp(r'[ T]'));
              if (parts.isNotEmpty) {
                final datePart = parts[0];
                final timePart = parts.length > 1 ? parts[1] : '00:00:00';
                final dateSegments = datePart.split(RegExp(r'[-/.]')).map((s) => int.tryParse(s) ?? 0).toList();
                final timeSegments = timePart.split(RegExp(r'[:.]')).map((s) => int.tryParse(s) ?? 0).toList();
                if (dateSegments.length >= 3) {
                  final d = DateTime(
                    dateSegments[0],
                    dateSegments[1],
                    dateSegments[2],
                    timeSegments.isNotEmpty ? timeSegments[0] : 0,
                    timeSegments.length > 1 ? timeSegments[1] : 0,
                    timeSegments.length > 2 ? timeSegments[2] : 0,
                  );
                  out[key] = d.toLocal().toIso8601String();
                }
              }
            } catch (_) {
              // ignore
            }
          }
        }
      } catch (_) {
        // ignore
      }
    }

    // ✅ POPRAWKA 1: Dodano 'do_' do listy kluczy
    const possibleKeys = <String>['od', 'do', 'do_', 'start', 'end', 'czas_od', 'czas_do', 'date', 'dt'];
    for (final k in possibleKeys) _normKey(k);

    return out;
  }

  static Map<String, dynamic> _prepareRowForClassModel(Map<String, dynamic> raw) {
    final normalized = _normalizeRowDates(raw);
    final out = Map<String, dynamic>.from(normalized);

    if (out.containsKey('od') && out['od'] != null) {
      out['startTime'] = out['od'];
    } else if (out.containsKey('start') && out['start'] != null) {
      out['startTime'] = out['start'];
    }

    // ✅ POPRAWKA 2: Sprawdź 'do_' przed 'do' (zgodnie z Twoim plikiem class_model.dart)
    if (out.containsKey('do_') && out['do_'] != null) {
      out['endTime'] = out['do_'];
    } else if (out.containsKey('do') && out['do'] != null) {
      out['endTime'] = out['do'];
    } else if (out.containsKey('end') && out['end'] != null) {
      out['endTime'] = out['end'];
    }

    return out;
  }

  static ClassModel parseRowToClassModel(Map<String, dynamic> raw) {
    final prepared = _prepareRowForClassModel(raw);
    return ClassModel.fromMap(prepared);
  }

  static String canonicalFavKey({String? groupId, String? groupCode}) {
    if (groupId != null && groupId.trim().isNotEmpty) {
      return 'group:$groupId';
    }
    return 'group:${(groupCode ?? '').trim()}';
  }

  static Future<(String? groupCode, List<String> subgroups)> loadGroupPrefs() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
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
        subs = subsJson.split(',').map((e)=>e.trim()).where((e)=>e.isNotEmpty).toList();
      }
    }
    return (groupCode, subs);
  }

  static Future<(String? groupCode, List<String> subgroups, String? groupId)> loadGroupContext() async {
    final p = await SharedPreferences.getInstance();
    final rawGroup = _trim(p.getString(_prefGroup));
    final groupCode = rawGroup.isEmpty ? null : rawGroup;
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
      _groupIdCache[groupCode] = groupId;
    }
    return (groupCode, subs, groupId);
  }

  static final Map<String,String?> _groupIdCache = {};
  static Future<String?> _resolveGroupId(String groupCode) async {
    if (_groupIdCache.containsKey(groupCode)) return _groupIdCache[groupCode];
    try {
      final res = await _supabaseClient.from('grupy').select('id').eq('kod_grupy', groupCode).limit(1).maybeSingle();
      if (res == null || res['id'] == null) {
        _groupIdCache[groupCode] = null;
        return null;
      }
      final id = res['id'] as String;
      _groupIdCache[groupCode] = id;
      return id;
    } catch (_) {
      _groupIdCache[groupCode] = null;
      return null;
    }
  }
  static Future<String?> _resolveGroupIdRobust(String group) async {
    final tried = <String>{};
    Future<String?> attempt(String g) async { if (tried.contains(g)) return null; tried.add(g); return await _resolveGroupId(g); }
    final variants = [group, group.replaceAll('-', ''), group.replaceAll(' ', ''), group.replaceAll('-', ' '), group.toUpperCase(), group.toLowerCase()];
    for (final v in variants) { final id = await attempt(v); if (id != null) return id; }
    return null;
  }

  static Future<String?> resolveGroupIdByCode(String code) => _resolveGroupId(code);

  static Future<List<Map<String,dynamic>>> searchGroups(String q, {int limit = 50}) async {
    final trimmed = _trim(q);
    if (trimmed.isEmpty) return [];
    try {
      final rows = await _supabaseClient.from('grupy').select('id,kod_grupy').ilike('kod_grupy', '%$trimmed%').limit(limit) as List<dynamic>;
      return rows.map((r) => Map<String,dynamic>.from(r as Map)).toList();
    } catch (e) {
      print('[ClassesRepo][searchGroups] err $e');
      return [];
    }
  }

  static Future<List<Map<String,dynamic>>> searchTeachers(String q, {int limit = 50}) async {
    final trimmed = _trim(q);
    if (trimmed.isEmpty) return [];
    try {
      final rows = await _supabaseClient.from('nauczyciele').select('id,nazwa').ilike('nazwa', '%$trimmed%').limit(limit) as List<dynamic>;
      return rows.map((r) => Map<String,dynamic>.from(r as Map)).toList();
    } catch (e) {
      print('[ClassesRepo][searchTeachers] err $e');
      return [];
    }
  }

  static Future<void> setGroupPrefs(String? groupCode, List<String> subgroups) async {
    final p = await SharedPreferences.getInstance();
    final code = groupCode?.trim() ?? '';
    if (code.isEmpty) {
      await p.remove(_prefGroup);
      await p.remove(_prefSub);
      await p.remove(_prefGroupId);
      await p.remove(_prefGroupMeta);
      _clearCaches();
      print('[ClassesRepo][setGroupPrefs] cleared (no group)');
      return;
    }
    await p.setString(_prefGroup, code);
    try {
      final jsonArr = jsonEncode((subgroups..removeWhere((e)=>e.trim().isEmpty)).toList());
      await p.setString(_prefSub, jsonArr);
    } catch (_) {
      await p.setString(_prefSub, jsonEncode(<String>[]));
    }
    _groupIdCache.remove(code);
    _clearCaches();
    print('[ClassesRepo][setGroupPrefs] saved code=$code, subs=${subgroups.join(',')}');

    () async {
      try {
        final id = await _resolveGroupId(code);
        if (id != null && id.isNotEmpty) {
          await p.setString(_prefGroupId, id);
          _groupIdCache[code] = id;
          print('[ClassesRepo][setGroupPrefs] resolved id=$id for $code');
          await _migrateFavKeysIfNeeded();
        }
      } catch (e) { print('[ClassesRepo][setGroupPrefs][resolve/migrate] $e'); }
    }();
  }

  static Future<void> setGroupPrefsById({required String groupId, required String groupCode, List<String> subgroups = const []}) async {
    final p = await SharedPreferences.getInstance();
    final id = groupId.trim();
    final code = groupCode.trim();
    await p.setString(_prefGroup, code);
    try { await p.setString(_prefSub, jsonEncode((subgroups..removeWhere((e)=>e.trim().isEmpty)).toList())); } catch (_) { await p.setString(_prefSub, jsonEncode(<String>[])); }
    await p.setString(_prefGroupId, id);
    _groupIdCache[code] = id;
    _clearCaches();
    try {
      final row = await _supabaseClient.from('grupy').select().eq('id', id).maybeSingle();
      if (row != null) {
        try {
          final json = jsonEncode(row);
          await p.setString(_prefGroupMeta, json);
        } catch (_) {}
      }
    } catch (_) {
      // ignore
    }
    () async { try { await _migrateFavKeysIfNeeded(); } catch (e) { print('[ClassesRepo][setGroupPrefsById][migrate] $e'); } }();
  }

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

  static Future<List<ClassModel>> fetchRange({required DateTime from, required DateTime to, String? groupCode, List<String> subgroups = const [], String? groupId}) async {
    final rawGroup = _trim(groupCode);
    final group = rawGroup;
    final start = DateTime(from.year, from.month, from.day, from.hour, from.minute);
    final end = DateTime(to.year, to.month, to.day, to.hour, to.minute);
    lastRangeFrom = start; lastRangeTo = end; lastRangeRows = 0; lastRangeVariant = 'none';
    if (group.isEmpty && (groupId == null || groupId.trim().isEmpty)) { print('[ClassesRepo][range] brak groupCode/groupId – zwracam pustą listę'); return const []; }

    List data = <dynamic>[];

    _pruneCache();
    final ck = _rangeKey(start, end, group, subgroups);
    final cached = _rangeCache[ck];
    if (cached != null) {
      lastRangeRows = cached.data.length;
      lastRangeVariant = 'cache';
      return cached.data;
    }

    final String _resolvedRaw = _trim(groupId);
    String? resolvedGroupId = _resolvedRaw.isEmpty ? null : _resolvedRaw;

    if (resolvedGroupId != null) {
      try {
        data = await _supabaseClient
            .from('zajecia_grupy')
            .select('*')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', resolvedGroupId)
            .order('od', ascending: true) as List;
        lastRangeVariant = 'grupa_id:param';
      } catch(e) { print('[ClassesRepo][range] grupa_id:param err $e'); }
    }

    if (data.isEmpty && resolvedGroupId == null && group.isNotEmpty) {
      final resolved = await _resolveGroupIdRobust(group);
      if (resolved != null) {
        try {
          data = await _supabaseClient
              .from('zajecia_grupy')
              .select('*')
              .gte('od', start.toIso8601String())
              .lt('od', end.toIso8601String())
              .eq('grupa_id', resolved)
              .order('od', ascending: true) as List;
          lastRangeVariant = 'grupa_id:resolved';
          resolvedGroupId = resolved;
        } catch (e) { print('[ClassesRepo][range] grupa_id:resolved err $e'); }
      }
    }

    // ✅ POPRAWKA 3: Zmieniono 'do' na 'do_' i usunięto 'kod_grupy'
    if (data.isEmpty && resolvedGroupId != null) {
      final id = resolvedGroupId;
      try {
        final rows = await _supabaseClient
            .from('zajecia_grupy')
        // Używamy do_ zamiast do i usunęliśmy 'kod_grupy'
            .select('od,do_,grupa_id,przedmiot,nauczyciel,typ,pracownia,pole')
            .gte('od', start.toIso8601String())
            .lt('od', end.toIso8601String())
            .eq('grupa_id', id)
            .order('od', ascending: true) as List;
        data = rows;
        lastRangeVariant = 'group_id_cache_resolved';
      } catch (e) { print('[ClassesRepo][range] group_id_cache_resolved err $e'); }
    }

    if (data.isEmpty && resolvedGroupId != null && resolvedGroupId.isNotEmpty) {
      final id = resolvedGroupId;
      try {
        final rows = await _supabaseClient.from('zajecia_grupy').select().eq('id', id).maybeSingle();
        if (rows != null) data = [rows];
        lastRangeVariant = 'by_id_fallback';
      } catch (e) { print('[ClassesRepo][range] by id err $e'); }
    }

    lastRangeRows = data.length;

    for (int i=0;i<data.length;i++){ final d=data[i]; if(d is Map && d.containsKey('grupy')) d.remove('grupy'); }
    final list = <ClassModel>[];
    for (final r in data) {
      try {
        final prepared = Map<String,dynamic>.from(r as Map);
        final model = parseRowToClassModel(prepared);
        list.add(model);
      } catch(e){
        print('[ClassesRepo][range][PARSE] $e');
      }
    }

    final lowerSubs = subgroups.map((e)=>e.toLowerCase()).toSet();
    if (lowerSubs.isNotEmpty) {
      final before = list.length;
      list.retainWhere((c){
        final pg = (c.subgroup??'').trim();
        if (pg.isEmpty) return true;
        return lowerSubs.contains(pg.toLowerCase());
      });
      print('[ClassesRepo][range] subgroup filter $before -> ${list.length} (${lowerSubs.join(',')})');
    } else {
      // brak
    }
    list.sort((a,b)=>a.startTime.compareTo(b.startTime));
    print('[ClassesRepo][range] final=${list.length} variant=$lastRangeVariant from=$start to=$end');

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

  static Future<List<ClassModel>> fetchMultiDay(DateTime from, int days,{String? groupCode,List<String> subgroups=const [], String? groupId}) async {
    final start = DateTime(from.year, from.month, from.day);
    final end = start.add(Duration(days: days));
    return fetchRange(from:start,to:end,groupCode:groupCode,subgroups:subgroups, groupId: groupId);
  }

  static Future<List<String>> getSubgroupsForGroup(String groupCode, {bool forceRefresh = false}) async {
    final g = _trim(groupCode);
    if (g.isEmpty) return [];
    final now = DateTime.now();
    final cached = _subgroupsCache[g];
    if (!forceRefresh && cached != null && now.difference(cached.inserted) <= _subgroupsCacheTtl) {
      return List<String>.from(cached.data);
    }

    List<String> subs = [];
    try {
      try {
        final rows = await _supabaseClient
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
        // ignore
      }

      if (subs.isEmpty) {
        try {
          final rpc = await _supabaseClient.rpc('get_subgroups_for_group', params: {'p_kod_grupy': g});
          if (rpc != null && rpc is List) {
            final List<String> out = rpc.map((e)=> e.toString()).toList();
            _subgroupsCache[g] = _SubgroupsCacheEntry(out);
            return out;
          }
        } catch (e) { print('[ClassesRepo][subgroups] rpc err $e'); }
      }

      if (subs.isEmpty) {
        try {
          final rows = await _supabaseClient
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
        } catch (_) {/* ignore */}
      }

      if (subs.isEmpty) {
        try {
          final groupId = await _resolveGroupIdRobust(g);
          if (groupId != null) {
            final subRes = await _supabaseClient
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
        } catch (_) {/* ignore */}
      }
    } catch (e) {
      print('[ClassesRepo][getSubgroupsForGroup] err $e');
      subs = [];
    }

    _subgroupsCache[g] = _SubgroupsCacheEntry(subs);
    return subs;
  }

  static Future<List<ClassModel>> fetchTeacherRange({required DateTime from, required DateTime to, required String teacherId}) async {
    final start = DateTime(from.year, from.month, from.day, from.hour, from.minute);
    final end = DateTime(to.year, to.month, to.day, to.hour, to.minute);
    try {
      final data = await _supabaseClient
          .from('zajecia_nauczyciela')
          .select('*')
          .gte('od', start.toIso8601String())
          .lt('od', end.toIso8601String())
          .eq('nauczyciel_id', teacherId)
          .order('od', ascending: true) as List<dynamic>;
      final list = <ClassModel>[];
      for (final r in data) {
        try {
          final prepared = Map<String,dynamic>.from(r as Map);
          final model = parseRowToClassModel(prepared);
          list.add(model);
        } catch (e) {
          print('[ClassesRepo][teacherRange][PARSE] $e');
        }
      }
      return list;
    } catch (e) {
      print('[ClassesRepo][teacherRange] err $e');
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

  static Future<Map<String,dynamic>?> getTeacherDetails(String teacherId) async {
    try {
      final row = await _supabaseClient.from('nauczyciele').select('id,nazwa,email,instytut').eq('id', teacherId).maybeSingle();
      if (row == null) return null;
      return Map<String,dynamic>.from(row as Map);
    } catch (e) { print('[ClassesRepo][teacher] err $e'); }
    return null;
  }

  static Future<Map<String,dynamic>?> getGroupById(String groupId) async {
    try {
      final row = await _supabaseClient.from('grupy').select('id,kod_grupy,nazwa').eq('id', groupId).maybeSingle();
      if (row == null) return null;
      return Map<String,dynamic>.from(row as Map);
    } catch (e) { print('[ClassesRepo][getGroupById] err $e'); }
    return null;
  }

  static List<String> parseInstitutes(String? instRaw) {
    final institutes = <String>[];
    if (instRaw == null || instRaw.trim().isEmpty) return institutes;

    var normalized = instRaw.replaceAll('\r', '').replaceAll('\t', ' ');
    normalized = normalized
        .replaceAll(RegExp(r'[\u200B\u200C\u200D\u2060\u00AD]'), ' ')
        .replaceAll('\u00A0', ' ');

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

  static Future<String?> resolveGroupIdForCode(String groupCode) => _resolveGroupIdRobust(groupCode);

  static Future<void> setTeacherPrefsById(String teacherId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('teacher_id', teacherId);
  }

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
      if (uuidRe.hasMatch(token)) continue;
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
      final Set<String> newSet = list.toSet();
      for (final entry in replacements.entries) {
        if (newSet.remove(entry.key)) { newSet.add(entry.value); changed = true; }
      }
      if (changed) {
        await p.setString(_prefFavPlans, jsonEncode(newSet.toList()));
      }
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

  static Future<List<String>> getSubjectsForDefaultGroup({bool forceRefresh = false}) async {
    try {
      final ctx = await loadGroupContext();
      final groupCode = _trim(ctx.$1);
      final groupId = _trim(ctx.$3);
      final cacheKey = groupId.isNotEmpty ? 'id:$groupId' : 'code:${groupCode.toLowerCase()}';
      _pruneCache();
      if (!forceRefresh) {
        final cached = _subjectsByGroupCache[cacheKey];
        if (cached != null) {
          return List<String>.from(cached.data);
        }
      }

      Set<String> subjects = <String>{};

      if (groupId.isNotEmpty) {
        try {
          final rows = await _supabaseClient
              .from('zajecia_grupy')
              .select('przedmiot')
              .eq('grupa_id', groupId) as List<dynamic>;
          for (final r in rows) {
            final s = _trim(r['przedmiot'] as String?);
            if (s.isNotEmpty) subjects.add(s);
          }
        } catch (e) { print('[ClassesRepo][subjects:id] $e'); }
      }

      if (subjects.isEmpty && groupCode.isNotEmpty) {
        try {
          final rows = await _supabaseClient
              .from('plan_zajec')
              .select('przedmiot')
              .eq('kod_grupy', groupCode) as List<dynamic>;
          for (final r in rows) {
            final s = _trim(r['przedmiot'] as String?);
            if (s.isNotEmpty) subjects.add(s);
          }
        } catch (e) { print('[ClassesRepo][subjects:plan_zajec] $e'); }
      }

      final list = subjects.toList()..sort((a,b)=> a.toLowerCase().compareTo(b.toLowerCase()));
      _subjectsByGroupCache[cacheKey] = _SubjectsCacheEntry(list);
      return list;
    } catch (e) {
      print('[ClassesRepo][getSubjectsForDefaultGroup] err $e');
      return const [];
    }
  }

  static Future<List<String>> getTypesForSubjectInDefaultGroup(String subject, {bool forceRefresh = false}) async {
    final subj = _trim(subject);
    if (subj.isEmpty) return const [];
    try {
      final ctx = await loadGroupContext();
      final groupCode = _trim(ctx.$1);
      final groupId = _trim(ctx.$3);
      final cacheKey = (groupId.isNotEmpty ? 'id:$groupId' : 'code:${groupCode.toLowerCase()}') + '::' + subj.toLowerCase();
      _pruneCache();
      if (!forceRefresh) {
        final cached = _typesByGroupSubjectCache[cacheKey];
        if (cached != null) {
          return List<String>.from(cached.data);
        }
      }

      Set<String> types = <String>{};

      if (groupId.isNotEmpty) {
        try {
          final rows = await _supabaseClient
              .from('zajecia_grupy')
              .select('typ,rz,type,przedmiot')
              .eq('grupa_id', groupId)
              .eq('przedmiot', subj) as List<dynamic>;
          for (final r in rows) {
            final t = _trim((r['typ'] as String?) ?? (r['rz'] as String?) ?? (r['type'] as String?));
            if (t.isNotEmpty) types.add(t);
          }
        } catch (e) { print('[ClassesRepo][types:id] $e'); }
      }

      if (types.isEmpty && groupCode.isNotEmpty) {
        try {
          final rows = await _supabaseClient
              .from('plan_zajec')
              .select('typ,rz,type,przedmiot')
              .eq('kod_grupy', groupCode)
              .eq('przedmiot', subj) as List<dynamic>;
          for (final r in rows) {
            final t = _trim((r['typ'] as String?) ?? (r['rz'] as String?) ?? (r['type'] as String?));
            if (t.isNotEmpty) types.add(t);
          }
        } catch (e) { print('[ClassesRepo][types:plan_zajec] $e'); }
      }

      final list = types.toList()..sort((a,b)=> a.toLowerCase().compareTo(b.toLowerCase()));
      _typesByGroupSubjectCache[cacheKey] = _TypesCacheEntry(list);
      return list;
    } catch (e) {
      print('[ClassesRepo][getTypesForSubjectInDefaultGroup] err $e');
      return const [];
    }
  }

  static const String subjectTypesAggregationSQL = r'''
-- (SQL bez zmian)
''';

  void clearCache() {
    _clearCaches();
  }

  static void clearAllCaches() {
    _clearCaches();
  }

  static final StreamController<Set<String>> _favoritesController = StreamController<Set<String>>.broadcast();

  static Stream<Set<String>> get favoritesStream => _favoritesController.stream;

  static void _notifyFavoritesChanged() async {
    try {
      final favs = await loadFavorites();
      if (!_favoritesController.isClosed) _favoritesController.add(favs);
    } catch (_) {}
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

class _SubjectsCacheEntry {
  final List<String> data;
  final DateTime inserted = DateTime.now();
  _SubjectsCacheEntry(this.data);
}

class _TypesCacheEntry {
  final List<String> data;
  final DateTime inserted = DateTime.now();
  _TypesCacheEntry(this.data);
}