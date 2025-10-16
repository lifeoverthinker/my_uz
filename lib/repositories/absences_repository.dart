import 'package:flutter/foundation.dart';
import 'package:my_uz/services/database_service.dart';
import 'package:my_uz/models/absence_model.dart';

/// Repozytorium do zarządzania nieobecnościami w lokalnej bazie danych.
/// Obsługuje operacje CRUD na nieobecnościach.
class AbsencesRepository {
  static const String _tableName = 'absences';

  /// Pobiera wszystkie nieobecności z bazy.
  static Future<List<AbsenceModel>> getAll() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getAll] Error: $e');
      return [];
    }
  }

  /// Pobiera nieobecności dla konkretnego przedmiotu.
  static Future<List<AbsenceModel>> getBySubject(String subject) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'subject = ?',
        whereArgs: [subject],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getBySubject] Error: $e');
      return [];
    }
  }

  /// Pobiera nieobecność po ID.
  static Future<AbsenceModel?> getById(String id) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'id = ?',
        whereArgs: [id],
        limit: 1,
      );
      if (maps.isEmpty) return null;
      return AbsenceModel.fromMap(maps.first);
    } catch (e) {
      debugPrint('[AbsencesRepository][getById] Error: $e');
      return null;
    }
  }

  /// Pobiera nieobecności z określonego zakresu dat.
  static Future<List<AbsenceModel>> getByDateRange({
    required DateTime from,
    required DateTime to,
  }) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'date >= ? AND date <= ?',
        whereArgs: [from.toIso8601String(), to.toIso8601String()],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getByDateRange] Error: $e');
      return [];
    }
  }

  /// Pobiera nieobecności według statusu usprawiedliwienia.
  static Future<List<AbsenceModel>> getByExcusedStatus({required bool excused}) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'excused = ?',
        whereArgs: [excused ? 1 : 0],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getByExcusedStatus] Error: $e');
      return [];
    }
  }

  /// Pobiera nieobecności według typu.
  static Future<List<AbsenceModel>> getByType(String type) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'type = ?',
        whereArgs: [type],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getByType] Error: $e');
      return [];
    }
  }

  /// Pobiera nieobecności dla konkretnego prowadzącego.
  static Future<List<AbsenceModel>> getByLecturer(String lecturer) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'lecturer = ?',
        whereArgs: [lecturer],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getByLecturer] Error: $e');
      return [];
    }
  }

  /// Dodaje nową nieobecność do bazy.
  static Future<bool> insert(AbsenceModel absence) async {
    try {
      await DatabaseService.insert(_tableName, absence.toMap());
      debugPrint('[AbsencesRepository][insert] Absence added: ${absence.id}');
      return true;
    } catch (e) {
      debugPrint('[AbsencesRepository][insert] Error: $e');
      return false;
    }
  }

  /// Aktualizuje istniejącą nieobecność.
  static Future<bool> update(AbsenceModel absence) async {
    try {
      final updatedAbsence = absence.copyWith(updatedAt: DateTime.now());
      final count = await DatabaseService.update(
        _tableName,
        updatedAbsence.toMap(),
        where: 'id = ?',
        whereArgs: [absence.id],
      );
      if (count > 0) {
        debugPrint('[AbsencesRepository][update] Absence updated: ${absence.id}');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[AbsencesRepository][update] Error: $e');
      return false;
    }
  }

  /// Usuwa nieobecność z bazy.
  static Future<bool> delete(String id) async {
    try {
      final count = await DatabaseService.delete(
        _tableName,
        where: 'id = ?',
        whereArgs: [id],
      );
      if (count > 0) {
        debugPrint('[AbsencesRepository][delete] Absence deleted: $id');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[AbsencesRepository][delete] Error: $e');
      return false;
    }
  }

  /// Usuwa wszystkie nieobecności z bazy.
  static Future<bool> deleteAll() async {
    try {
      await DatabaseService.delete(_tableName);
      debugPrint('[AbsencesRepository][deleteAll] All absences deleted');
      return true;
    } catch (e) {
      debugPrint('[AbsencesRepository][deleteAll] Error: $e');
      return false;
    }
  }

  /// Pobiera listę wszystkich unikalnych przedmiotów.
  static Future<List<String>> getSubjects() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.rawQuery(
        'SELECT DISTINCT subject FROM $_tableName WHERE subject IS NOT NULL AND subject != "" ORDER BY subject',
      );
      return maps.map((map) => map['subject'] as String).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getSubjects] Error: $e');
      return [];
    }
  }

  /// Pobiera listę wszystkich unikalnych typów nieobecności.
  static Future<List<String>> getTypes() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.rawQuery(
        'SELECT DISTINCT type FROM $_tableName WHERE type IS NOT NULL AND type != "" ORDER BY type',
      );
      return maps.map((map) => map['type'] as String).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getTypes] Error: $e');
      return [];
    }
  }

  /// Pobiera listę wszystkich unikalnych prowadzących.
  static Future<List<String>> getLecturers() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.rawQuery(
        'SELECT DISTINCT lecturer FROM $_tableName WHERE lecturer IS NOT NULL AND lecturer != "" ORDER BY lecturer',
      );
      return maps.map((map) => map['lecturer'] as String).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getLecturers] Error: $e');
      return [];
    }
  }

  /// Zlicza nieobecności według przedmiotu.
  static Future<Map<String, int>> countBySubject() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT subject, COUNT(*) as count FROM $_tableName '
        'WHERE subject IS NOT NULL AND subject != "" '
        'GROUP BY subject ORDER BY count DESC',
      );
      final Map<String, int> counts = {};
      for (final row in result) {
        counts[row['subject'] as String] = row['count'] as int;
      }
      return counts;
    } catch (e) {
      debugPrint('[AbsencesRepository][countBySubject] Error: $e');
      return {};
    }
  }

  /// Zlicza nieobecności według typu.
  static Future<Map<String, int>> countByType() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT type, COUNT(*) as count FROM $_tableName '
        'WHERE type IS NOT NULL AND type != "" '
        'GROUP BY type ORDER BY count DESC',
      );
      final Map<String, int> counts = {};
      for (final row in result) {
        counts[row['type'] as String] = row['count'] as int;
      }
      return counts;
    } catch (e) {
      debugPrint('[AbsencesRepository][countByType] Error: $e');
      return {};
    }
  }

  /// Pobiera liczbę wszystkich nieobecności.
  static Future<int> count() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT COUNT(*) as count FROM $_tableName',
      );
      return result.first['count'] as int;
    } catch (e) {
      debugPrint('[AbsencesRepository][count] Error: $e');
      return 0;
    }
  }

  /// Pobiera liczbę nieusprawiedliwionych nieobecności.
  static Future<int> countUnexcused() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT COUNT(*) as count FROM $_tableName WHERE excused = 0',
      );
      return result.first['count'] as int;
    } catch (e) {
      debugPrint('[AbsencesRepository][countUnexcused] Error: $e');
      return 0;
    }
  }

  /// Pobiera liczbę usprawiedliwionych nieobecności.
  static Future<int> countExcused() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT COUNT(*) as count FROM $_tableName WHERE excused = 1',
      );
      return result.first['count'] as int;
    } catch (e) {
      debugPrint('[AbsencesRepository][countExcused] Error: $e');
      return 0;
    }
  }

  /// Pobiera najnowsze nieobecności (limit).
  static Future<List<AbsenceModel>> getRecent({int limit = 10}) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        orderBy: 'created_at DESC',
        limit: limit,
      );
      return maps.map((map) => AbsenceModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[AbsencesRepository][getRecent] Error: $e');
      return [];
    }
  }

  /// Oznacza nieobecność jako usprawiedliwioną.
  static Future<bool> markAsExcused(String id) async {
    try {
      final count = await DatabaseService.update(
        _tableName,
        {
          'excused': 1,
          'updated_at': DateTime.now().toIso8601String(),
        },
        where: 'id = ?',
        whereArgs: [id],
      );
      if (count > 0) {
        debugPrint('[AbsencesRepository][markAsExcused] Absence marked as excused: $id');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[AbsencesRepository][markAsExcused] Error: $e');
      return false;
    }
  }

  /// Oznacza nieobecność jako nieusprawiedliwioną.
  static Future<bool> markAsUnexcused(String id) async {
    try {
      final count = await DatabaseService.update(
        _tableName,
        {
          'excused': 0,
          'updated_at': DateTime.now().toIso8601String(),
        },
        where: 'id = ?',
        whereArgs: [id],
      );
      if (count > 0) {
        debugPrint('[AbsencesRepository][markAsUnexcused] Absence marked as unexcused: $id');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[AbsencesRepository][markAsUnexcused] Error: $e');
      return false;
    }
  }
}
