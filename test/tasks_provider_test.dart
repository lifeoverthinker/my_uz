import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:my_uz/providers/tasks_provider.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/i_user_tasks_repository.dart';

// Fake repository that records calls
class FakeTasksRepo implements IUserTasksRepository {
  bool clearCalled = false;
  bool fetchCalled = false;
  bool getDescCalled = false;

  @override
  void clearCache() {
    clearCalled = true;
  }

  @override
  Future<void> deleteTask(String id) async {}

  @override
  Future<String?> getTaskDescription(String id) async {
    getDescCalled = true;
    return null;
  }

  @override
  Future<List<TaskModel>> fetchUserTasks({bool forceRefresh = false}) async {
    fetchCalled = true;
    // return empty list
    return [];
  }

  @override
  Future<TaskModel> upsertTask(TaskModel task, {String? description}) async {
    return task;
  }

  @override
  Future<void> setTaskCompleted(String id, bool completed) async {}
}

// A simple fake ChangeNotifier to act as plan provider
class FakePlanProvider extends ChangeNotifier {}

void main() {
  test('TasksProvider reacts to plan change by clearing cache and refreshing', () async {
    final fakeRepo = FakeTasksRepo();
    final fakePlan = FakePlanProvider();

    final provider = TasksProvider(planListenable: fakePlan, repository: fakeRepo);

    // At start, no calls
    expect(fakeRepo.clearCalled, isFalse);
    expect(fakeRepo.fetchCalled, isFalse);

    // Trigger plan change
    fakePlan.notifyListeners();

    // allow event loop to process async refresh
    await Future.delayed(Duration(milliseconds: 50));

    // After plan change, TasksProvider should have called clearCache() on repo and started refresh (fetchUserTasks)
    expect(fakeRepo.clearCalled, isTrue);
    expect(fakeRepo.fetchCalled, isTrue);
  });
}

