// Plik: lib/providers/tasks_provider.dart
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:my_uz/services/i_user_tasks_repository.dart';
import 'package:my_uz/providers/user_plan_provider.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/widgets/tasks/task_details.dart';

enum TaskFilter { all, today, upcoming, completed }

class TaskWithDescription {
  final TaskModel model;
  final String? description;
  const TaskWithDescription(this.model, this.description);
}

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

  String? getTaskDescription(String id) {
    try {
      final entry = items.firstWhere((it) => it.model.id == id);
      return entry.description;
    } catch (e) {
      return null;
    }
  }

  /// Otwiera istniejące zadanie
  Future<void> openTaskDetails(BuildContext context, TaskModel task) async {
    final desc = getTaskDescription(task.id);
    if (!context.mounted) return;

    await TaskDetailsSheet.show(
      context,
      task,
      description: desc,
      startInEditMode: false, // Otwórz w trybie detali
      onDelete: () async {
        await deleteTask(task.id);
        if (context.mounted) Navigator.of(context).pop();
      },
      onToggleCompleted: (completed) async {
        await setTaskCompleted(task.id, completed);
      },
      onSaveEdit: (updatedTask, newDescription) async {
        await upsertTask(updatedTask, description: newDescription);
      },
    );
  }

  /// Otwiera nowe, puste zadanie w trybie edycji
  Future<void> showAddTaskSheet(BuildContext context) async {
    if (!context.mounted) return;

    // Utwórz tymczasowy model dla nowego zadania
    final newTask = TaskModel(
      id: DateTime.now().millisecondsSinceEpoch.toString(), // Tymczasowe ID
      title: '',
      deadline: DateTime(DateTime.now().year, DateTime.now().month, DateTime.now().day),
      subject: '',
      completed: false,
    );

    await TaskDetailsSheet.show(
      context,
      newTask,
      description: '',
      startInEditMode: true, // Otwórz od razu w trybie edycji
      onDelete: () {
        // "Usuń" po prostu zamyka modal, bo zadanie nie jest zapisane
        if (context.mounted) Navigator.of(context).pop();
      },
      onToggleCompleted: (completed) {
        // Ignoruj, zadanie nie jest jeszcze zapisane
      },
      onSaveEdit: (updatedTask, newDescription) async {
        // Zapisz nowe zadanie
        await upsertTask(updatedTask, description: newDescription);
        // Po zapisie, provider sam odświeży listę,
        // a formularz przełączy się w tryb detali (dzięki _handleSave)
      },
    );
  }

  Future<void> upsertTask(TaskModel task, {String? description}) async {
    await _repo.upsertTask(task, description: description);
    await refresh();
  }

  Future<void> addTask(TaskModel task, String? description) async {
    await upsertTask(task, description: description);
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