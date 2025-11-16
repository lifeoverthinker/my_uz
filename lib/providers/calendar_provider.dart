// Plik: lib/providers/calendar_provider.dart
import 'package:flutter/foundation.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/utils/date_utils.dart';

class CalendarProvider extends ChangeNotifier {
  bool loading = false;
  bool error = false;
  String? lastError;

  Map<DateTime, List<ClassModel>> weekCache = {};

  // +++ POPRAWKA: Dodano pole do sterowania stanem początkowym CalendarScreen +++
  String initialSection = 'calendar'; // 'calendar' lub 'schedule'
  // +++ KONIEC POPRAWKI +++

  static final CalendarProvider instance = CalendarProvider._internal();
  CalendarProvider._internal();

  Future<void> ensureWeekLoaded(
      DateTime anyDay, {
        String? overrideGroupCode,
        String? overrideGroupId,
        List<String>? overrideSubgroups,
      }) async {
    final monday = mondayOfWeek(anyDay);
    if (weekCache.containsKey(monday)) {
      return;
    }

    loading = true;
    error = false;
    lastError = null;
    notifyListeners();

    try {
      final (groupCode, subgroups, groupId) = await ClassesRepository.loadGroupContext();
      final from = monday;
      final to = monday.add(const Duration(days: 6));

      // +++ POPRAWKA: Użycie nazwanych argumentów 'from' i 'to' +++
      final list = await ClassesRepository.fetchRange(
        from: from,
        to: to,
        groupCode: overrideGroupCode ?? groupCode,
        subgroups: overrideSubgroups ?? subgroups,
        groupId: overrideGroupId ?? groupId,
      );
      // +++ KONIEC POPRAWKI +++

      final map = <DateTime, List<ClassModel>>{};
      for (final c in list) {
        final key = stripTime(c.startTime.toLocal());
        map.putIfAbsent(key, () => []).add(c);
      }

      weekCache[monday] = list;
    } catch (e) {
      error = true;
      lastError = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  void clearCache() {
    weekCache.clear();
    notifyListeners();
  }
}