// filepath: lib/services/i_user_tasks_repository.dart
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/services/user_tasks_repository.dart';

abstract class IUserTasksRepository {
  Future<List<TaskModel>> fetchUserTasks({bool forceRefresh});
  Future<String?> getTaskDescription(String id);
  Future<TaskModel> upsertTask(TaskModel task, {String? description});
  Future<void> deleteTask(String id);
  Future<void> setTaskCompleted(String id, bool completed);
  void clearCache();
}

/// Default adapter that delegates to the concrete UserTasksRepository.instance.
class DefaultUserTasksRepository implements IUserTasksRepository {
  @override
  Future<List<TaskModel>> fetchUserTasks({bool forceRefresh = false}) => UserTasksRepository.instance.fetchUserTasks(forceRefresh: forceRefresh);

  @override
  Future<String?> getTaskDescription(String id) => UserTasksRepository.instance.getTaskDescription(id);

  @override
  Future<TaskModel> upsertTask(TaskModel task, {String? description}) => UserTasksRepository.instance.upsertTask(task, description: description);

  @override
  Future<void> deleteTask(String id) => UserTasksRepository.instance.deleteTask(id);

  @override
  Future<void> setTaskCompleted(String id, bool completed) => UserTasksRepository.instance.setTaskCompleted(id, completed);

  @override
  void clearCache() => UserTasksRepository.instance.clearCache();
}
