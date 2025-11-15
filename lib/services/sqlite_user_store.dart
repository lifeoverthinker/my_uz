import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:path/path.dart' as p;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite/sqflite.dart';
import 'package:my_uz/models/task_model.dart';

/// TaskWithDescription helper (używany wcześniej w LocalUserStore)
class TaskWithDescription {
  final TaskModel model;
  final String? description;
  const TaskWithDescription(this.model, this.description);
}

/// SqliteUserStore - migracja i nowa implementacja lokalnego magazynu zadań.
/// (Pełna, samodzielna wersja: nie wymaga LocalUserStore)
class SqliteUserStore {
  static const String _kMigratedFlag = 'user_db_migrated_v1';
  static const String _dbName = 'my_uz_user.db';

  static Database? _db;
  static bool _initializing = false;

  static Future<void> init() async {
    if (_db != null) return;
    if (_initializing) {
      // wait while another init finishes
      while (_initializing && _db == null) {
        await Future.delayed(const Duration(milliseconds: 50));
      }
      return;
    }
    _initializing = true;
    try {
      final databasesPath = await getDatabasesPath();
      final path = p.join(databasesPath, _dbName);
      _db = await openDatabase(
        path,
        version: 1,
        onCreate: (db, version) async {
          await db.execute('''
            CREATE TABLE tasks(
              id TEXT PRIMARY KEY,
              title TEXT NOT NULL,
              deadline TEXT NOT NULL,
              subject TEXT NOT NULL,
              class_id TEXT,
              completed INTEGER NOT NULL,
              type TEXT,
              description TEXT
            );
          ''');
          await db.execute('''
            CREATE TABLE grades(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              data TEXT NOT NULL
            );
          ''');
          await db.execute('''
            CREATE TABLE absences(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              data TEXT NOT NULL
            );
          ''');
        },
      );

      // Run migration from SharedPreferences (legacy LocalUserStore) if not done yet
      final prefs = await SharedPreferences.getInstance();
      final migrated = prefs.getBool(_kMigratedFlag) ?? false;
      if (!migrated) {
        try {
          // Try to read legacy prefs (if present) to migrate tasks. We expect key 'user_tasks_v1'
          final raw = prefs.getString('user_tasks_v1');
          if (raw != null && raw.isNotEmpty) {
            final List<dynamic> arr = jsonDecode(raw);
            if (arr.isNotEmpty) {
              final batch = _db!.batch();
              for (final e in arr) {
                if (e is Map) {
                  final id = (e['id'] as String?)?.trim() ?? _uuidV4();
                  final map = {
                    'id': id,
                    'title': e['title'] ?? '',
                    'deadline': e['deadline'] ?? DateTime.now().toIso8601String(),
                    'subject': e['subject'] ?? 'Inny',
                    'class_id': e['class_id'],
                    'completed': (e['completed'] == true) ? 1 : 0,
                    'type': e['type'],
                    'description': e['description'],
                  };
                  batch.insert('tasks', map, conflictAlgorithm: ConflictAlgorithm.replace);
                }
              }
              await batch.commit(noResult: true);
            }
          }
          // mark migration done
          await prefs.setBool(_kMigratedFlag, true);
        } catch (e, st) {
          debugPrint('[SqliteUserStore] migration failed: $e\n$st');
          // migration failed -> don't block app
        }
      }
    } finally {
      _initializing = false;
    }
  }

  // ===== Tasks API =====
  static Future<List<(TaskModel, String?)>> loadTasks() async {
    await init();
    if (_db == null) return const [];
    try {
      final rows = await _db!.query('tasks', orderBy: 'deadline ASC');
      final out = <(TaskModel, String?)>[];
      for (final r in rows) {
        try {
          final map = <String, dynamic>{
            'id': r['id'],
            'title': r['title'],
            'deadline': r['deadline'],
            'subject': r['subject'],
            'class_id': r['class_id'],
            'completed': (r['completed'] as int) == 1,
            'type': r['type'],
          };
          final model = TaskModel.fromMap(map);
          final desc = r['description'] as String?;
          out.add((model, desc));
        } catch (_) { /* skip */ }
      }
      return out;
    } catch (_) {
      return const [];
    }
  }

  static Future<List<TaskWithDescription>> loadTaskEntries() async {
    final tuples = await loadTasks();
    return tuples.map((t) => TaskWithDescription(t.$1, t.$2)).toList();
  }

  static Future<void> saveTasks(List<(TaskModel, String?)> tasks) async {
    await init();
    if (_db == null) return;
    final batch = _db!.batch();
    await _db!.delete('tasks');
    for (final t in tasks) {
      final (model, desc) = t;
      final id = model.id.trim().isEmpty ? _uuidV4() : model.id;
      final map = {
        'id': id,
        'title': model.title,
        'deadline': model.deadline.toIso8601String(),
        'subject': model.subject,
        'class_id': model.classId,
        'completed': model.completed ? 1 : 0,
        'type': model.type,
        'description': desc,
      };
      batch.insert('tasks', map, conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await batch.commit(noResult: true);
  }

  static Future<TaskModel> upsertTask(TaskModel task, {String? description}) async {
    await init();
    if (_db == null) return task;
    TaskModel model = task;
    if (model.id.trim().isEmpty) model = model.copyWith(id: _uuidV4());
    final map = {
      'id': model.id,
      'title': model.title,
      'deadline': model.deadline.toIso8601String(),
      'subject': model.subject,
      'class_id': model.classId,
      'completed': model.completed ? 1 : 0,
      'type': model.type,
      'description': description,
    };
    await _db!.insert('tasks', map, conflictAlgorithm: ConflictAlgorithm.replace);
    return model;
  }

  static Future<void> deleteTask(String id) async {
    await init();
    if (_db == null) return;
    await _db!.delete('tasks', where: 'id = ?', whereArgs: [id]);
  }

  static Future<void> setTaskCompleted(String id, bool completed) async {
    await init();
    if (_db == null) return;
    await _db!.update('tasks', {'completed': completed ? 1 : 0}, where: 'id = ?', whereArgs: [id]);
  }

  static Future<String?> getTaskDescription(String id) async {
    await init();
    if (_db == null) return null;
    final rows = await _db!.query('tasks', columns: ['description'], where: 'id = ?', whereArgs: [id], limit: 1);
    if (rows.isEmpty) return null;
    return rows.first['description'] as String?;
  }

  // ===== Grades / Absences delegate to DB =====
  static Future<List<Map<String, dynamic>>> loadGrades() async {
    await init();
    if (_db == null) return const [];
    try {
      final rows = await _db!.query('grades', orderBy: 'id ASC');
      final out = <Map<String, dynamic>>[];
      for (final r in rows) {
        try {
          final raw = r['data'] as String?;
          if (raw == null || raw.isEmpty) continue;
          final decoded = jsonDecode(raw) as Map<String, dynamic>;
          out.add(decoded);
        } catch (_) {}
      }
      return out;
    } catch (_) {
      return const [];
    }
  }

  static Future<void> saveGrades(List<Map<String, dynamic>> grades) async {
    await init();
    if (_db == null) return;
    final batch = _db!.batch();
    await _db!.delete('grades');
    for (final g in grades) {
      batch.insert('grades', {'data': jsonEncode(g)}, conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await batch.commit(noResult: true);
  }

  static Future<List<Map<String, dynamic>>> loadAbsences() async {
    await init();
    if (_db == null) return const [];
    try {
      final rows = await _db!.query('absences', orderBy: 'id ASC');
      final out = <Map<String, dynamic>>[];
      for (final r in rows) {
        try {
          final raw = r['data'] as String?;
          if (raw == null || raw.isEmpty) continue;
          final decoded = jsonDecode(raw) as Map<String, dynamic>;
          out.add(decoded);
        } catch (_) {}
      }
      return out;
    } catch (_) {
      return const [];
    }
  }

  static Future<void> saveAbsences(List<Map<String, dynamic>> absences) async {
    await init();
    if (_db == null) return;
    final batch = _db!.batch();
    await _db!.delete('absences');
    for (final a in absences) {
      batch.insert('absences', {'data': jsonEncode(a)}, conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await batch.commit(noResult: true);
  }

  // ===== Helpers =====
  static String _uuidV4() {
    final rnd = Random.secure();
    final b = List<int>.generate(16, (_) => rnd.nextInt(256));
    b[6] = (b[6] & 0x0f) | 0x40; // version 4
    b[8] = (b[8] & 0x3f) | 0x80; // variant
    String hex(int i) => b[i].toRadixString(16).padLeft(2, '0');
    return '${hex(0)}${hex(1)}${hex(2)}${hex(3)}-${hex(4)}${hex(5)}-${hex(6)}${hex(7)}-${hex(8)}${hex(9)}-${hex(10)}${hex(11)}${hex(12)}${hex(13)}${hex(14)}${hex(15)}';
  }
}