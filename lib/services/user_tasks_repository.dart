import 'dart:async';

import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/local_user_store.dart';
import 'package:my_uz/supabase.dart';

/// UserTasksRepository – synchronizacja z tabelą `user_tasks` w Supabase.
/// Refaktoryzacja:
/// - instancyjne repozytorium (wstrzykiwanie SupabaseClient) dla testowalności
/// - krótkoterminowy cache pamięciowy + clearCache()
/// - timeout + try/catch + rethrow dla zewnętrznych wywołań
/// - zachowano kompatybilny `instance` do stopniowej migracji
class UserTasksRepository {
  final SupabaseClient _client;
  final Duration _timeout;

  // Cache
  List<TaskModel>? _tasksCache;
  DateTime? _tasksCacheExpiry;
  final Map<String, String?> _descCache = {};

  /// Domyślny TTL dla cache (krótkoterminowy)
  static const Duration defaultCacheTtl = Duration(seconds: 30);

  UserTasksRepository({required SupabaseClient client, Duration? timeout})
      : _client = client,
        _timeout = timeout ?? const Duration(seconds: 8);

  /// Domyślna instancja dla kompatybilności (używa singletonu Supa.client).
  /// W nowym kodzie preferuj wstrzykiwanie repozytorium z konstruktorów/providerów.
  static final UserTasksRepository instance =
      UserTasksRepository(client: Supa.client);

  // ---------- Backward-compatible static wrappers ----------
  // These allow existing call sites (UserTasksRepository.fetchUserTasks()) to continue
  // working while new code should inject an instance.
  // static Future<List<TaskModel>> fetchUserTasks({bool forceRefresh = false}) =>
  //     instance.fetchUserTasks(forceRefresh: forceRefresh);

  // static Future<TaskModel> upsertTask(TaskModel task, {String? description}) =>
  //     instance.upsertTask(task, description: description);

  // static Future<void> deleteTask(String id) => instance.deleteTask(id);

  // Keep original name for compatibility -- many callers use setTaskCompleted
  // static Future<void> setTaskCompleted(String id, bool completed) =>
  //     instance.setTaskCompleted(id, completed);

  // static Future<String?> getTaskDescription(String id) => instance.getTaskDescription(id);

  // static void clearCacheStatic() => instance.clearCache();

  // NOTE: removed static wrappers to avoid duplicate declarations with instance methods.
  // Use UserTasksRepository.instance.method(...) or inject IUserTasksRepository instead.

  /// Wyczyść cache w pamięci.
  void clearCache() {
    _tasksCache = null;
    _tasksCacheExpiry = null;
    _descCache.clear();
  }

  bool _isCacheValid() {
    return _tasksCache != null && _tasksCacheExpiry != null && DateTime.now().isBefore(_tasksCacheExpiry!);
  }

  /// Pobierz wszystkie zadania użytkownika.
  /// Jeśli cache jest świeży i forceRefresh==false, zwróci dane z pamięci.
  Future<List<TaskModel>> fetchUserTasks({bool forceRefresh = false}) async {
    if (!forceRefresh && _isCacheValid()) {
      return _tasksCache!;
    }

    // Najpierw spróbuj pobrać z Supabase, w razie błędu fallback do LocalUserStore.
    try {
      final future = _client.from('user_tasks').select('*');
      final rows = await future.timeout(_timeout) as List<dynamic>;
      final out = <TaskModel>[];
      for (final r in rows) {
        try {
          final map = Map<String, dynamic>.from(r as Map);
          // Normalizuj nazwy pól do konwencji TaskModel
          final normalized = <String, dynamic>{};
          normalized['id'] = map['id']?.toString() ?? '';
          normalized['title'] = map['title']?.toString() ?? map['name']?.toString() ?? '';
          // deadline może być String ISO lub DateTime
          final dl = map['deadline'] ?? map['due_date'] ?? map['due'];
          if (dl is String) normalized['deadline'] = dl;
          else if (dl is DateTime) normalized['deadline'] = dl.toIso8601String();
          else normalized['deadline'] = DateTime.now().toIso8601String();
          normalized['subject'] = map['subject']?.toString() ?? '';
          normalized['class_id'] = map['class_id']?.toString();
          normalized['completed'] = map['completed'] is bool ? map['completed'] : (map['completed'] == 1);
          normalized['type'] = map['type']?.toString();
          out.add(TaskModel.fromMap(normalized));
        } catch (_) {
          // pomiń uszkodzony rekord
        }
      }

      // Zapisz lokalną kopię (opcja) – mapowanie bez opisu
      try {
        final tuples = out.map((t) => (t, null as String?)).toList();
        await LocalUserStore.saveTasks(tuples);
      } catch (_) {}

      // Ustaw cache
      _tasksCache = out;
      _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);

      return out;
    } on TimeoutException catch (te) {
      // Timeout -> fallback do lokalnego store
      try {
        final local = await LocalUserStore.loadTasks();
        final result = local.map((t) => t.$1).toList();
        // ustaw cache z lokalnych
        _tasksCache = result;
        _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
        return result;
      } catch (_) {
        rethrow;
      }
    } catch (e) {
      // inny błąd -> fallback do lokalnego store
      try {
        final local = await LocalUserStore.loadTasks();
        final result = local.map((t) => t.$1).toList();
        _tasksCache = result;
        _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
        return result;
      } catch (_) {
        rethrow;
      }
    }
  }

  /// Upsert zadania: najpierw zapisz lokalnie (LocalUserStore.upsertTask generuje id jeśli brak),
  /// następnie spróbuj zapisać na Supabase (best-effort). Zwraca zapisany lokalny model.
  Future<TaskModel> upsertTask(TaskModel task, {String? description}) async {
    final savedLocal = await LocalUserStore.upsertTask(task, description: description);
    // Aktualizuj cache lokalnie
    _tasksCache = (_tasksCache ?? []).where((t) => t.id != savedLocal.id).toList()..add(savedLocal);
    _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
    if (description != null) _descCache[savedLocal.id] = description;

    try {
      final map = savedLocal.toMap();
      if (description != null && description.trim().isNotEmpty) map['description'] = description.trim();
      await _client.from('user_tasks').upsert(map).timeout(_timeout);
    } on TimeoutException catch (_) {
      // best-effort: pozostaw lokalnie i rzuć dalej jeśli potrzebne
    } catch (_) {
      // best-effort: ignoruj błąd z serwera
    }
    return savedLocal;
  }

  /// Usuń zadanie lokalnie i na serwerze (best-effort).
  Future<void> deleteTask(String id) async {
    try {
      await LocalUserStore.deleteTask(id);
    } catch (_) {}
    // Aktualizuj cache
    _tasksCache = _tasksCache?.where((t) => t.id != id).toList();
    _descCache.remove(id);
    try {
      await _client.from('user_tasks').delete().eq('id', id).timeout(_timeout);
    } catch (_) {}
  }

  /// Ustaw status ukończenia: lokalnie + serwer (best-effort)
  Future<void> setTaskCompleted(String id, bool completed) async {
    try {
      await LocalUserStore.setTaskCompleted(id, completed);
    } catch (_) {}
    // Aktualizuj cache
    if (_tasksCache != null) {
      _tasksCache = _tasksCache!.map((t) => t.id == id ? t.copyWith(completed: completed) : t).toList();
    }
    try {
      await _client.from('user_tasks').update({'completed': completed}).eq('id', id).timeout(_timeout);
    } catch (_) {}
  }

  /// Pobierz opis zadania (jeśli zapisany lokalnie lub w serwerze)
  Future<String?> getTaskDescription(String id) async {
    // Sprawdź cache
    if (_descCache.containsKey(id)) return _descCache[id];

    try {
      final descLocal = await LocalUserStore.getTaskDescription(id);
      if (descLocal != null) {
        _descCache[id] = descLocal;
        return descLocal;
      }
    } catch (_) {}

    try {
      final future = _client.from('user_tasks').select('description').eq('id', id).maybeSingle();
      final row = await future.timeout(_timeout);
      if (row != null && row['description'] != null) {
        final d = row['description'].toString();
        _descCache[id] = d;
        return d;
      }
    } on TimeoutException catch (_) {
      // ignore
    } catch (_) {}
    return null;
  }
}
