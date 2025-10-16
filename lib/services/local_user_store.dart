import 'dart:convert';
import 'dart:math';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/models/task_model.dart';

class TaskWithDescription {
  final TaskModel model;
  final String? description;
  const TaskWithDescription(this.model, this.description);
}

/// LocalUserStore – prosty magazyn danych użytkownika na urządzeniu
///
/// Używa SharedPreferences do przechowywania:
/// - zadań (lista JSON) wraz z opcjonalnym opisem
/// - (miejsce na) oceny i nieobecności – trzymane lokalnie jako JSON (na później)
class LocalUserStore {
  // Klucze
  static const String _kTasks = 'user_tasks_v1';
  static const String _kGrades = 'user_grades_v1';
  static const String _kAbsences = 'user_absences_v1';

  // ===== Zadania =====
  /// Zwraca listę zadań wraz z opisem: (TaskModel, String? description)
  static Future<List<(TaskModel, String?)>> loadTasks() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_kTasks);
    if (raw == null || raw.isEmpty) return const [];
    try {
      final List<dynamic> arr = jsonDecode(raw);
      final out = <(TaskModel, String?)>[];
      for (final e in arr) {
        if (e is Map) {
          final map = Map<String, dynamic>.from(e as Map);
          // Obsługa możliwych braków danych
          if (map['id'] == null || map['title'] == null || map['deadline'] == null || map['subject'] == null) {
            continue;
          }
          try {
            final tm = TaskModel.fromMap(map);
            final desc = (map['description'] as String?)?.toString();
            out.add((tm, desc));
          } catch (_) {
            // pomiń uszkodzone wpisy
          }
        }
      }
      return out;
    } catch (_) {
      return const [];
    }
  }

  /// Wersja pomocnicza bez rekordów – zwraca listę TaskWithDescription
  static Future<List<TaskWithDescription>> loadTaskEntries() async {
    final tuples = await loadTasks();
    return tuples.map((t) => TaskWithDescription(t.$1, t.$2)).toList();
  }

  /// Nadpisuje całą listę zadań (bezpiecznie)
  static Future<void> saveTasks(List<(TaskModel, String?)> tasks) async {
    final p = await SharedPreferences.getInstance();
    final list = tasks.map((t) {
      final (model, desc) = t;
      final m = model.toMap();
      if (desc != null && desc.trim().isNotEmpty) m['description'] = desc.trim();
      return m;
    }).toList();
    await p.setString(_kTasks, jsonEncode(list));
  }

  /// Dodaj lub zaktualizuj zadanie (po id). Jeśli id jest puste – generuje nowe UUID.
  static Future<TaskModel> upsertTask(TaskModel task, {String? description}) async {
    final list = await loadTasks();
    TaskModel model = task;
    if (model.id.trim().isEmpty) {
      model = model.copyWith(id: _uuidV4());
    }
    bool updated = false;
    for (int i = 0; i < list.length; i++) {
      if (list[i].$1.id == model.id) {
        list[i] = (model, description);
        updated = true;
        break;
      }
    }
    if (!updated) list.add((model, description));
    await saveTasks(list);
    return model;
  }

  /// Usuń zadanie po id
  static Future<void> deleteTask(String id) async {
    final list = await loadTasks();
    list.removeWhere((e) => e.$1.id == id);
    await saveTasks(list);
  }

  /// Ustaw status ukończenia zadania
  static Future<void> setTaskCompleted(String id, bool completed) async {
    final list = await loadTasks();
    for (int i = 0; i < list.length; i++) {
      if (list[i].$1.id == id) {
        final updated = list[i].$1.copyWith(completed: completed);
        list[i] = (updated, list[i].$2);
        break;
      }
    }
    await saveTasks(list);
  }

  /// Pobierz opis zadania po id
  static Future<String?> getTaskDescription(String id) async {
    final list = await loadTasks();
    final found = list.where((e) => e.$1.id == id).toList();
    if (found.isEmpty) return null;
    return found.first.$2;
  }

  // ===== Oceny (placeholder – lokalny JSON) =====
  static Future<List<Map<String, dynamic>>> loadGrades() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_kGrades);
    if (raw == null || raw.isEmpty) return const [];
    try {
      final List<dynamic> arr = jsonDecode(raw);
      return arr.map((e) => Map<String, dynamic>.from(e as Map)).toList();
    } catch (_) {
      return const [];
    }
  }

  static Future<void> saveGrades(List<Map<String, dynamic>> grades) async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_kGrades, jsonEncode(grades));
  }

  // ===== Nieobecności (placeholder – lokalny JSON) =====
  static Future<List<Map<String, dynamic>>> loadAbsences() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString(_kAbsences);
    if (raw == null || raw.isEmpty) return const [];
    try {
      final List<dynamic> arr = jsonDecode(raw);
      return arr.map((e) => Map<String, dynamic>.from(e as Map)).toList();
    } catch (_) {
      return const [];
    }
  }

  static Future<void> saveAbsences(List<Map<String, dynamic>> absences) async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_kAbsences, jsonEncode(absences));
  }

  // ===== Helpers =====
  /// Generowanie UUID v4 bez zewnętrznych zależności
  static String _uuidV4() {
    final rnd = Random.secure();
    final b = List<int>.generate(16, (_) => rnd.nextInt(256));
    b[6] = (b[6] & 0x0f) | 0x40; // wersja 4
    b[8] = (b[8] & 0x3f) | 0x80; // wariant
    String hex(int i) => b[i].toRadixString(16).padLeft(2, '0');
    return '${hex(0)}${hex(1)}${hex(2)}${hex(3)}-${hex(4)}${hex(5)}-${hex(6)}${hex(7)}-${hex(8)}${hex(9)}-${hex(10)}${hex(11)}${hex(12)}${hex(13)}${hex(14)}${hex(15)}';
  }
}

