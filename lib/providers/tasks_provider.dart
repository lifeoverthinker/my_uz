import 'package:flutter/foundation.dart';
import 'package:my_uz/services/i_user_tasks_repository.dart';
import 'package:my_uz/services/sqlite_user_store.dart';
import 'package:my_uz/providers/user_plan_provider.dart';

enum TaskFilter { all, today, upcoming, completed }

/// Provider odpowiedzialny za listę zadań użytkownika.
class TasksProvider extends ChangeNotifier {
  bool loading = false;
  List<TaskWithDescription> items = const [];
  TaskFilter filter = TaskFilter.all;

  static final TasksProvider instance = TasksProvider._internal();

  final Listenable _planListenable;
  final IUserTasksRepository _repo;

  TasksProvider._internal()
      : _planListenable = UserPlanProvider.instance,
        _repo = DefaultUserTasksRepository() {
    _planListenable.addListener(_onPlanChanged);
  }

  TasksProvider({Listenable? planListenable, IUserTasksRepository? repository})
      : _planListenable = planListenable ?? UserPlanProvider.instance,
        _repo = repository ?? DefaultUserTasksRepository() {
    _planListenable.addListener(_onPlanChanged);
  }

  void _onPlanChanged() {
    // When plan changes, clear repository cache and refresh
    clearCache();
    refresh();
  }

  Future<void> refresh({bool forceRefresh = false}) async {
    loading = true;
    notifyListeners();
    try {
      final tasks = await _repo.fetchUserTasks(forceRefresh: forceRefresh);
      final futures = tasks.map((t) async {
        final desc = await _repo.getTaskDescription(t.id);
        return TaskWithDescription(t, desc);
      }).toList();
      final list = await Future.wait(futures);
      list.sort((a, b) {
        final ad = a.model.deadline;
        final bd = b.model.deadline;
        final cmp = ad.compareTo(bd);
        if (cmp != 0) return cmp;
        if (a.model.completed != b.model.completed) return a.model.completed ? 1 : -1;
        return a.model.title.toLowerCase().compareTo(b.model.title.toLowerCase());
      });
      items = list;
    } catch (e) {
      // on error, keep existing items
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> addTask(TaskWithDescription entry) async {
    await _repo.upsertTask(entry.model, description: entry.description);
    await refresh();
  }

  Future<void> editTask(TaskWithDescription entry) async {
    await _repo.upsertTask(entry.model, description: entry.description);
    await refresh();
  }

  Future<void> deleteTask(String id) async {
    await _repo.deleteTask(id);
    await refresh();
  }

  Future<void> setTaskCompleted(String id, bool completed) async {
    await _repo.setTaskCompleted(id, completed);
    await refresh();
  }

  void clearCache() {
    try { _repo.clearCache(); } catch (_) {}
    items = const [];
    notifyListeners();
  }
}