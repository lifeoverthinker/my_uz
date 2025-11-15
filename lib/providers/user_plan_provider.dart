// filepath: c:\Users\Martyna\Documents\GitHub\my_uz\lib\providers\user_plan_provider.dart
import 'package:flutter/foundation.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/services/user_tasks_repository.dart';

/// Central provider that stores the selected group/plan and coordinates cache invalidation.
class UserPlanProvider extends ChangeNotifier {
  String? _currentGroupId;
  String? _currentPlanId; // optional: if you have separate plan id

  /// Singleton default instance for gradual migration / simple wiring.
  static final UserPlanProvider instance = UserPlanProvider._internal();

  UserPlanProvider._internal() {
    // lazy load preferences
    _init();
  }

  String? get currentGroupId => _currentGroupId;
  String? get currentPlanId => _currentPlanId;

  bool get hasGroup => _currentGroupId != null && _currentGroupId!.isNotEmpty;

  Future<void> _init() async {
    final ctx = await ClassesRepository.loadGroupContext();
    _currentGroupId = ctx.$3; // groupId
    // planId mapping not implemented – keep null unless extended
    notifyListeners();
  }

  /// Load current selection from preferences (explicit call)
  Future<void> loadFromPrefs() async {
    final ctx = await ClassesRepository.loadGroupContext();
    _currentGroupId = ctx.$3;
    notifyListeners();
  }

  /// Set default group by ID+code (preferred). This method:
  ///  1) persists preferences via ClassesRepository.setGroupPrefsById
  ///  2) clears caches in repositories
  ///  3) updates in-memory state and notifies listeners
  Future<void> setDefaultGroupById({required String groupId, required String groupCode, List<String> subgroups = const []}) async {
    // persist
    await ClassesRepository.setGroupPrefsById(groupId: groupId, groupCode: groupCode, subgroups: subgroups);

    // update in-memory
    _currentGroupId = groupId;

    // clear caches in repositories (best-effort)
    try { ClassesRepository.clearAllCaches(); } catch (_) {}
    try { UserTasksRepository.instance.clearCache(); } catch (_) {}
    // other repositories should also expose clearCache() and be invoked here

    // notify listeners AFTER caches cleared and persisted
    notifyListeners();
  }

  /// Alternate: set by code (without known id). This will persist code and attempt to resolve id in background as ClassesRepository already does.
  Future<void> setDefaultGroupByCode(String groupCode, List<String> subgroups) async {
    await ClassesRepository.setGroupPrefs(groupCode, subgroups);
    // Do not set _currentGroupId here (resolution may happen in background in ClassesRepository), but clear caches and notify
    try { ClassesRepository.clearAllCaches(); } catch (_) {}
    try { UserTasksRepository.instance.clearCache(); } catch (_) {}
    notifyListeners();
  }
}
