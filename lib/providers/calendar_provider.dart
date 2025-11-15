// Plik: lib/providers/calendar_provider.dart
import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/providers/user_plan_provider.dart';

// POPRAWKA: Usunięto importy holiday
// import 'package:my_uz/models/holiday_model.dart';
// import 'package:my_uz/services/holiday_repository.dart';

/// Prostszy provider tygodniowy: mapuje monday -> lista zajęć.
class CalendarProvider extends ChangeNotifier {
  final Map<DateTime, List<ClassModel>> weekCache = {};
  final Set<DateTime> loadingWeeks = {};
  bool get loading => loadingWeeks.isNotEmpty;
  bool error = false;
  String? lastError;

  static final CalendarProvider instance = CalendarProvider._internal();

  final Future<List<ClassModel>> Function(DateTime, {String? groupCode, List<String> subgroups, String? groupId}) _fetchWeek;
  final Future<(String?, List<String>, String?)> Function() _loadGroupContext;

  CalendarProvider({
    Listenable? planListenable,
    Future<List<ClassModel>> Function(DateTime, {String? groupCode, List<String> subgroups, String? groupId})? fetchWeek,
    Future<(String?, List<String>, String?)> Function()? loadGroupContext,
  })  : _fetchWeek = fetchWeek ?? ClassesRepository.fetchWeek,
        _loadGroupContext = loadGroupContext ?? ClassesRepository.loadGroupContext {
    (planListenable ?? UserPlanProvider.instance).addListener(_onPlanChanged);
  }

  CalendarProvider._internal()
      : _fetchWeek = ClassesRepository.fetchWeek,
        _loadGroupContext = ClassesRepository.loadGroupContext {
    UserPlanProvider.instance.addListener(_onPlanChanged);
  }

  void _onPlanChanged() {
    clearCache();
    ensureWeekLoaded(DateTime.now());
  }

  void clearCache() {
    weekCache.clear();
    loadingWeeks.clear();
    error = false;
    lastError = null;
    notifyListeners();
  }

  DateTime _mondayOfWeek(DateTime d) {
    final wd = d.weekday;
    return DateTime(d.year, d.month, d.day - (wd - 1));
  }

  DateTime _stripTime(DateTime d) => DateTime(d.year, d.month, d.day);

  Future<void> ensureWeekLoaded(DateTime anyDay, {String? overrideGroupCode, String? overrideGroupId, List<String>? overrideSubgroups}) async {
    final monday = _mondayOfWeek(anyDay);
    final key = _stripTime(monday);
    if (weekCache.containsKey(key)) return;

    if (loadingWeeks.contains(key)) {
      while (loadingWeeks.contains(key)) {
        await Future.delayed(const Duration(milliseconds: 50));
      }
      return;
    }

    loadingWeeks.add(key);
    error = false;
    lastError = null;
    notifyListeners();

    try {
      String? groupCode;
      List<String> subgroups = const [];
      String? groupId;
      if (overrideGroupCode != null || overrideGroupId != null) {
        groupCode = overrideGroupCode;
        groupId = overrideGroupId;
        subgroups = overrideSubgroups ?? const [];
      } else {
        final ctx = await _loadGroupContext();
        groupCode = ctx.$1;
        subgroups = ctx.$2;
        groupId = ctx.$3;
      }

      // POPRAWKA: Usunięto HolidayRepository z Future.wait
      final list = await _fetchWeek(key, groupCode: groupCode, subgroups: subgroups, groupId: groupId).timeout(const Duration(seconds: 12));

      weekCache[key] = list;
      unawaited(_prefetchNeighbor(key.subtract(const Duration(days: 7)), groupCode, groupId, subgroups));
      unawaited(_prefetchNeighbor(key.add(const Duration(days: 7)), groupCode, groupId, subgroups));
    } on TimeoutException {
      error = true;
      lastError = 'Timeout podczas pobierania danych z serwera';
    } catch (e) {
      error = true;
      lastError = e.toString();
    } finally {
      loadingWeeks.remove(key);
      notifyListeners();
    }
  }

  Future<void> _prefetchNeighbor(DateTime mondayKey, String? groupCode, String? groupId, List<String> subgroups) async {
    final k = _stripTime(mondayKey);
    if (weekCache.containsKey(k) || loadingWeeks.contains(k)) return;
    loadingWeeks.add(k);
    notifyListeners();
    try {
      final list = await _fetchWeek(k, groupCode: groupCode, subgroups: subgroups, groupId: groupId).timeout(const Duration(seconds: 12));
      weekCache[k] = list;
    } catch (_) {
      // fail silently
    } finally {
      loadingWeeks.remove(k);
      notifyListeners();
    }
  }

  // POPRAWKA: Dodana metoda, której wymagał `home_screen.dart`
  // i która używa poprawnych nazw pól z `class_model.dart`
  ClassModel? findRelatedClass(ClassModel classModel) {
    for (final week in weekCache.values) {
      try {
        final found = week.firstWhere(
              (c) =>
          c.id != classModel.id &&
              // Użyj 'subject' i 'type' (zgodnie z class_model.dart)
              (c.subject == classModel.subject) &&
              (c.type == classModel.type) &&
              c.startTime.weekday == classModel.startTime.weekday &&
              c.startTime.hour == classModel.startTime.hour &&
              c.startTime.minute == classModel.startTime.minute,
        );
        return found;
      } catch (e) {
        // .firstWhere rzuca błąd, jeśli nie znajdzie; ignorujemy i szukamy dalej
      }
    }
    return null; // Nie znaleziono
  }
}