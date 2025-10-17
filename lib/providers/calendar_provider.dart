// filepath: c:\Users\Martyna\Documents\GitHub\my_uz\lib\providers\calendar_provider.dart
import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/providers/user_plan_provider.dart';

/// Provider centralizujący pobieranie tygodni/zakresów dla kalendarza i cache.
class CalendarProvider extends ChangeNotifier {
  final Map<DateTime, List<ClassModel>> weekCache = {};
  final Set<DateTime> loadingWeeks = {};
  bool loading = false;
  bool error = false;
  String? errorMsg;

  static final CalendarProvider instance = CalendarProvider._internal();

  // Injectable hooks for testability
  final Future<List<ClassModel>> Function(DateTime, {String? groupCode, List<String> subgroups, String? groupId}) _fetchWeek;
  final Future<(String?, List<String>, String?)> Function() _loadGroupContext;

  /// Public constructor for tests / alternative wiring. Pass a custom [planListenable]
  /// (e.g., a fake ChangeNotifier) and optional hooks to override network/prefs access.
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
    // When plan changes, clear cache and proactively prefetch current week.
    clearCache();
    // try to prefetch the week for today (best-effort)
    ensureWeekLoaded(DateTime.now());
    notifyListeners();
  }

  void clearCache() {
    weekCache.clear();
    loadingWeeks.clear();
    loading = false;
    error = false;
    errorMsg = null;
  }

  DateTime _mondayOfWeek(DateTime d) {
    final wd = d.weekday;
    return DateTime(d.year, d.month, d.day - (wd - 1));
  }

  DateTime _stripTime(DateTime d) => DateTime(d.year, d.month, d.day);

  /// Ensure the given week is loaded (returns immediately if cached).
  Future<void> ensureWeekLoaded(DateTime anyDay, {String? overrideGroupCode, String? overrideGroupId, List<String>? overrideSubgroups}) async {
    final monday = _mondayOfWeek(anyDay);
    final mondayKey = _stripTime(monday);
    if (weekCache.containsKey(mondayKey)) return;
    unawaited(_fetchWeekData(mondayKey, overrideGroupCode: overrideGroupCode, overrideGroupId: overrideGroupId, overrideSubgroups: overrideSubgroups));
  }

  Future<void> _fetchWeekData(DateTime mondayKey, {String? overrideGroupCode, String? overrideGroupId, List<String>? overrideSubgroups}) async {
    if (loadingWeeks.contains(mondayKey)) return;
    loadingWeeks.add(mondayKey);
    loading = true;
    error = false;
    errorMsg = null;
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

      final list = await _fetchWeek(mondayKey, groupCode: groupCode, subgroups: subgroups, groupId: groupId);
      weekCache[mondayKey] = list;

      // prefetch neighbors
      unawaited(_fetchWeekData(mondayKey.subtract(const Duration(days: 7)), overrideGroupCode: overrideGroupCode, overrideGroupId: overrideGroupId, overrideSubgroups: overrideSubgroups));
      unawaited(_fetchWeekData(mondayKey.add(const Duration(days: 7)), overrideGroupCode: overrideGroupCode, overrideGroupId: overrideGroupId, overrideSubgroups: overrideSubgroups));
    } catch (e) {
      error = true;
      errorMsg = e.toString();
    } finally {
      loadingWeeks.remove(mondayKey);
      loading = loadingWeeks.isNotEmpty;
      notifyListeners();
    }
  }
}
