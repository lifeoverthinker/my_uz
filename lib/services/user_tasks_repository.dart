import 'dart:async';

import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/sqlite_user_store.dart';
import 'package:my_uz/supabase.dart';

/// UserTasksRepository – synchronizacja z tabelą `user_tasks` w Supabase.
/// Prostszą wersję: lokalny store = SqliteUserStore (po migracji).
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
  static final UserTasksRepository instance =
  UserTasksRepository(client: Supa.client);

  // ----- Local storage helpers (SQLite only) -----
  Future<List<TaskModel>> _loadLocalTasks() async {
    try {
      final tuples = await SqliteUserStore.loadTasks();
      return tuples.map((t) => t.$1).toList();
    } catch (_) {
      return const [];
    }
  }

  Future<void> _saveLocalTasks(List<TaskModel> tasks) async {
    try {
      final tuples = tasks.map((t) => (t, null as String?)).toList();
      await SqliteUserStore.saveTasks(tuples);
    } catch (_) {}
  }

  Future<TaskModel> _upsertLocalTask(TaskModel task, {String? description}) async {
    try {
      return await SqliteUserStore.upsertTask(task, description: description);
    } catch (_) {
      return task;
    }
  }

  Future<void> _deleteLocalTask(String id) async {
    try {
      await SqliteUserStore.deleteTask(id);
    } catch (_) {}
  }

  Future<void> _setLocalTaskCompleted(String id, bool completed) async {
    try {
      await SqliteUserStore.setTaskCompleted(id, completed);
    } catch (_) {}
  }

  Future<String?> _getLocalTaskDescription(String id) async {
    try {
      return await SqliteUserStore.getTaskDescription(id);
    } catch (_) {
      return null;
    }
  }

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

    // Najpierw spróbuj pobrać z Supabase, w razie błędu fallback do lokalnego SQLite.
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
        await _saveLocalTasks(out);
      } catch (_) {}

      // Ustaw cache
      _tasksCache = out;
      _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);

      return out;
    } on TimeoutException {
      // Timeout -> fallback do lokalnego store
      final result = await _loadLocalTasks();
      _tasksCache = result;
      _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
      return result;
    } catch (_) {
      // inny błąd -> fallback do lokalnego store
      final result = await _loadLocalTasks();
      _tasksCache = result;
      _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
      return result;
    }
  }

  /// Upsert zadania: najpierw zapisz lokalnie (SqliteUserStore.upsertTask generuje id jeśli brak),
  /// następnie spróbuj zapisać na Supabase (best-effort). Zwraca zapisany lokalny model.
  Future<TaskModel> upsertTask(TaskModel task, {String? description}) async {
    final savedLocal = await _upsertLocalTask(task, description: description);
    // Aktualizuj cache lokalnie
    _tasksCache = (_tasksCache ?? []).where((t) => t.id != savedLocal.id).toList()..add(savedLocal);
    _tasksCacheExpiry = DateTime.now().add(defaultCacheTtl);
    if (description != null) _descCache[savedLocal.id] = description;

    try {
      final map = savedLocal.toMap();
      if (description != null && description.trim().isNotEmpty) map['description'] = description.trim();
      await _client.from('user_tasks').upsert(map).timeout(_timeout);
    } catch (_) {
      // best-effort: ignoruj błąd z serwera
    }
    return savedLocal;
  }

  /// Usuń zadanie lokalnie i na serwerze (best-effort).
  Future<void> deleteTask(String id) async {
    try {
      await _deleteLocalTask(id);
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
      await _setLocalTaskCompleted(id, completed);
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
      final descLocal = await _getLocalTaskDescription(id);
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
    } on TimeoutException {
      // ignore
    } catch (_) {}
    return null;
  }
}