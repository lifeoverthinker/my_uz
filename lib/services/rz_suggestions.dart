import 'package:my_uz/services/classes_repository.dart';

class RzSuggestions {
  RzSuggestions._();

  /// Attempt to return distinct `rz` for given subject.
  /// 1) Try ClassesRepository.getTypesForSubjectInDefaultGroup(subject) if available.
  /// 2) Otherwise fallback to heuristics/common list.
  static Future<List<String>> fetchForSubject(String subject) async {
    final s = subject.trim();
    if (s.isEmpty) return const [];
    try {
      // try repository first (preferred — uses your existing DB logic if present)
      try {
        final types = await ClassesRepository.getTypesForSubjectInDefaultGroup(s);
        if (types.isNotEmpty) {
          // normalize and return unique
          final uniq = types.map((e) => e.trim()).where((e) => e.isNotEmpty).toSet().toList();
          uniq.sort((a, b) => a.toLowerCase().compareTo(b.toLowerCase()));
          return uniq;
        }
      } catch (_) {
        // ignore repo-specific errors and fall through to heuristics
      }

      // heuristics fallback
      final common = <String>['WYK', 'ĆW', 'LAB', 'PROJ', 'SEM'];
      if (s.toLowerCase().contains('labor') || s.toLowerCase().contains('lab')) return ['LAB', 'ĆW', 'PROJ'];
      if (s.toLowerCase().contains('wyk')) return ['WYK', 'ĆW'];
      return common;
    } catch (_) {
      return const [];
    }
  }
}