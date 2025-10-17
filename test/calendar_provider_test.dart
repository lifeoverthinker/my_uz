import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:my_uz/providers/calendar_provider.dart';
import 'package:my_uz/models/class_model.dart';

// Fake fetchWeek that records calls
class FakeFetchWeek {
  bool called = false;
  final List<ClassModel> result;
  FakeFetchWeek([this.result = const []]);

  Future<List<ClassModel>> call(DateTime mondayKey, {String? groupCode, List<String>? subgroups, String? groupId}) async {
    called = true;
    return result;
  }
}

// Fake loadGroupContext
class FakeLoadGroupContext {
  bool called = false;
  Future<(String?, List<String>, String?)> call() async {
    called = true;
    return (null, <String>[], null);
  }
}

// Fake plan provider
class FakePlanProvider extends ChangeNotifier {}

void main() {
  test('CalendarProvider reacts to plan change by clearing cache and prefetching weeks', () async {
    final fakeFetch = FakeFetchWeek();
    final fakeLoadCtx = FakeLoadGroupContext();
    final fakePlan = FakePlanProvider();

    final provider = CalendarProvider(planListenable: fakePlan, fetchWeek: fakeFetch.call, loadGroupContext: fakeLoadCtx.call);

    expect(fakeFetch.called, isFalse);
    expect(fakeLoadCtx.called, isFalse);

    // Trigger plan change
    fakePlan.notifyListeners();

    // allow async prefetch to run
    await Future.delayed(Duration(milliseconds: 50));

    expect(fakeLoadCtx.called, isTrue, reason: 'loadGroupContext should be invoked to determine group/subgroups');
    expect(fakeFetch.called, isTrue, reason: 'fetchWeek should be invoked to prefetch current week');
  });
}

