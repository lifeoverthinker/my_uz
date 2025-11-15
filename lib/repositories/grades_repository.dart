import 'package:flutter/foundation.dart';
import 'package:my_uz/services/database_service.dart';
import 'package:my_uz/models/grade_model.dart';

/// Repozytorium do zarządzania ocenami w lokalnej bazie danych.
/// Obsługuje operacje CRUD na ocenach.
class GradesRepository {
  static const String _tableName = 'grades';

  /// Pobiera wszystkie oceny z bazy.
  static Future<List<GradeModel>> getAll() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => GradeModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getAll] Error: $e');
      return [];
    }
  }

  /// Pobiera oceny dla konkretnego przedmiotu.
  static Future<List<GradeModel>> getBySubject(String subject) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'subject = ?',
        whereArgs: [subject],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => GradeModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getBySubject] Error: $e');
      return [];
    }
  }

  /// Pobiera oceny dla konkretnego semestru.
  static Future<List<GradeModel>> getBySemester(String semester) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'semester = ?',
        whereArgs: [semester],
        orderBy: 'date DESC, created_at DESC',
      );
      return maps.map((map) => GradeModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getBySemester] Error: $e');
      return [];
    }
  }

  /// Pobiera ocenę po ID.
  static Future<GradeModel?> getById(String id) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'id = ?',
        whereArgs: [id],
        limit: 1,
      );
      if (maps.isEmpty) return null;
      return GradeModel.fromMap(maps.first);
    } catch (e) {
      debugPrint('[GradesRepository][getById] Error: $e');
      return null;
    }
  }

  /// Pobiera oceny z określonego zakresu dat.
  static Future<List<GradeModel>> getByDateRange({
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
      return maps.map((map) => GradeModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getByDateRange] Error: $e');
      return [];
    }
  }

  /// Dodaje nową ocenę do bazy.
  static Future<bool> insert(GradeModel grade) async {
    try {
      await DatabaseService.insert(_tableName, grade.toMap());
      debugPrint('[GradesRepository][insert] Grade added: ${grade.id}');
      return true;
    } catch (e) {
      debugPrint('[GradesRepository][insert] Error: $e');
      return false;
    }
  }

  /// Aktualizuje istniejącą ocenę.
  static Future<bool> update(GradeModel grade) async {
    try {
      final updatedGrade = grade.copyWith(updatedAt: DateTime.now());
      final count = await DatabaseService.update(
        _tableName,
        updatedGrade.toMap(),
        where: 'id = ?',
        whereArgs: [grade.id],
      );
      if (count > 0) {
        debugPrint('[GradesRepository][update] Grade updated: ${grade.id}');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[GradesRepository][update] Error: $e');
      return false;
    }
  }

  /// Usuwa ocenę z bazy.
  static Future<bool> delete(String id) async {
    try {
      final count = await DatabaseService.delete(
        _tableName,
        where: 'id = ?',
        whereArgs: [id],
      );
      if (count > 0) {
        debugPrint('[GradesRepository][delete] Grade deleted: $id');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[GradesRepository][delete] Error: $e');
      return false;
    }
  }

  /// Usuwa wszystkie oceny z bazy.
  static Future<bool> deleteAll() async {
    try {
      await DatabaseService.delete(_tableName);
      debugPrint('[GradesRepository][deleteAll] All grades deleted');
      return true;
    } catch (e) {
      debugPrint('[GradesRepository][deleteAll] Error: $e');
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
      debugPrint('[GradesRepository][getSubjects] Error: $e');
      return [];
    }
  }

  /// Pobiera listę wszystkich unikalnych semestrów.
  static Future<List<String>> getSemesters() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.rawQuery(
        'SELECT DISTINCT semester FROM $_tableName WHERE semester IS NOT NULL AND semester != "" ORDER BY semester DESC',
      );
      return maps.map((map) => map['semester'] as String).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getSemesters] Error: $e');
      return [];
    }
  }

  /// Oblicza średnią ważoną dla wszystkich ocen.
  static Future<double?> calculateWeightedAverage() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT SUM(numeric_value * weight) as weighted_sum, SUM(weight) as total_weight '
        'FROM $_tableName WHERE numeric_value IS NOT NULL AND weight IS NOT NULL',
      );
      if (result.isEmpty) return null;
      final weightedSum = result.first['weighted_sum'];
      final totalWeight = result.first['total_weight'];
      if (weightedSum == null || totalWeight == null || totalWeight == 0) {
        return null;
      }
      return (weightedSum as num) / (totalWeight as num);
    } catch (e) {
      debugPrint('[GradesRepository][calculateWeightedAverage] Error: $e');
      return null;
    }
  }

  /// Oblicza średnią ważoną dla konkretnego przedmiotu.
  static Future<double?> calculateWeightedAverageBySubject(String subject) async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT SUM(numeric_value * weight) as weighted_sum, SUM(weight) as total_weight '
        'FROM $_tableName WHERE subject = ? AND numeric_value IS NOT NULL AND weight IS NOT NULL',
        [subject],
      );
      if (result.isEmpty) return null;
      final weightedSum = result.first['weighted_sum'];
      final totalWeight = result.first['total_weight'];
      if (weightedSum == null || totalWeight == null || totalWeight == 0) {
        return null;
      }
      return (weightedSum as num) / (totalWeight as num);
    } catch (e) {
      debugPrint('[GradesRepository][calculateWeightedAverageBySubject] Error: $e');
      return null;
    }
  }

  /// Oblicza średnią arytmetyczną dla wszystkich ocen.
  static Future<double?> calculateSimpleAverage() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT AVG(numeric_value) as average FROM $_tableName WHERE numeric_value IS NOT NULL',
      );
      if (result.isEmpty || result.first['average'] == null) return null;
      return (result.first['average'] as num).toDouble();
    } catch (e) {
      debugPrint('[GradesRepository][calculateSimpleAverage] Error: $e');
      return null;
    }
  }

  /// Zlicza oceny według kategorii.
  static Future<Map<String, int>> countByCategory() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT category, COUNT(*) as count FROM $_tableName '
        'WHERE category IS NOT NULL AND category != "" '
        'GROUP BY category ORDER BY count DESC',
      );
      final Map<String, int> counts = {};
      for (final row in result) {
        counts[row['category'] as String] = row['count'] as int;
      }
      return counts;
    } catch (e) {
      debugPrint('[GradesRepository][countByCategory] Error: $e');
      return {};
    }
  }

  /// Pobiera liczbę wszystkich ocen.
  static Future<int> count() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT COUNT(*) as count FROM $_tableName',
      );
      return result.first['count'] as int;
    } catch (e) {
      debugPrint('[GradesRepository][count] Error: $e');
      return 0;
    }
  }

  /// Pobiera najnowsze oceny (limit).
  static Future<List<GradeModel>> getRecent({int limit = 10}) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        orderBy: 'created_at DESC',
        limit: limit,
      );
      return maps.map((map) => GradeModel.fromMap(map)).toList();
    } catch (e) {
      debugPrint('[GradesRepository][getRecent] Error: $e');
      return [];
    }
  }
}
