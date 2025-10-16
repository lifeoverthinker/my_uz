import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:my_uz/services/database_service.dart';
import 'package:my_uz/repositories/grades_repository.dart';
import 'package:my_uz/repositories/absences_repository.dart';
import 'package:my_uz/repositories/settings_repository.dart';
import 'package:my_uz/models/grade_model.dart';
import 'package:my_uz/models/absence_model.dart';

void main() {
  // Inicjalizacja sqflite_ffi dla testów
  setUpAll(() {
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  });

  group('DatabaseService', () {
    setUp(() async {
      // Reset bazy przed każdym testem
      await DatabaseService.reset();
    });

    tearDown(() async {
      // Zamknij bazę po każdym teście
      await DatabaseService.close();
    });

    test('Database inicjalizuje się poprawnie', () async {
      final db = await DatabaseService.database;
      expect(db, isNotNull);
      expect(db.isOpen, isTrue);
    });

    test('Tabele są tworzone poprawnie', () async {
      final db = await DatabaseService.database;
      final tables = await db.rawQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
      );
      final tableNames = tables.map((t) => t['name'] as String).toSet();
      expect(tableNames.contains('grades'), isTrue);
      expect(tableNames.contains('absences'), isTrue);
      expect(tableNames.contains('settings'), isTrue);
    });
  });

  group('GradesRepository', () {
    setUp(() async {
      await DatabaseService.reset();
    });

    tearDown(() async {
      await DatabaseService.close();
    });

    test('Dodawanie i pobieranie ocen', () async {
      final grade = GradeModel(
        id: '1',
        subject: 'Matematyka',
        grade: '5.0',
        numericValue: 5.0,
        weight: 1.0,
        category: 'Egzamin',
        date: DateTime(2024, 1, 15),
        semester: '2023/2024 Zimowy',
        createdAt: DateTime.now(),
      );

      final result = await GradesRepository.insert(grade);
      expect(result, isTrue);

      final grades = await GradesRepository.getAll();
      expect(grades.length, equals(1));
      expect(grades.first.subject, equals('Matematyka'));
      expect(grades.first.grade, equals('5.0'));
    });

    test('Pobieranie ocen po przedmiocie', () async {
      await GradesRepository.insert(GradeModel(
        id: '1',
        subject: 'Matematyka',
        grade: '5.0',
        numericValue: 5.0,
        createdAt: DateTime.now(),
      ));
      await GradesRepository.insert(GradeModel(
        id: '2',
        subject: 'Fizyka',
        grade: '4.0',
        numericValue: 4.0,
        createdAt: DateTime.now(),
      ));

      final mathGrades = await GradesRepository.getBySubject('Matematyka');
      expect(mathGrades.length, equals(1));
      expect(mathGrades.first.subject, equals('Matematyka'));
    });

    test('Aktualizowanie oceny', () async {
      final grade = GradeModel(
        id: '1',
        subject: 'Matematyka',
        grade: '4.0',
        numericValue: 4.0,
        createdAt: DateTime.now(),
      );

      await GradesRepository.insert(grade);

      final updatedGrade = grade.copyWith(grade: '5.0', numericValue: 5.0);
      final result = await GradesRepository.update(updatedGrade);
      expect(result, isTrue);

      final retrieved = await GradesRepository.getById('1');
      expect(retrieved?.grade, equals('5.0'));
    });

    test('Usuwanie oceny', () async {
      await GradesRepository.insert(GradeModel(
        id: '1',
        subject: 'Matematyka',
        grade: '5.0',
        createdAt: DateTime.now(),
      ));

      final result = await GradesRepository.delete('1');
      expect(result, isTrue);

      final grades = await GradesRepository.getAll();
      expect(grades.isEmpty, isTrue);
    });

    test('Obliczanie średniej ważonej', () async {
      await GradesRepository.insert(GradeModel(
        id: '1',
        subject: 'Matematyka',
        numericValue: 5.0,
        weight: 2.0,
        createdAt: DateTime.now(),
      ));
      await GradesRepository.insert(GradeModel(
        id: '2',
        subject: 'Fizyka',
        numericValue: 4.0,
        weight: 1.0,
        createdAt: DateTime.now(),
      ));

      final average = await GradesRepository.calculateWeightedAverage();
      expect(average, isNotNull);
      // (5.0*2.0 + 4.0*1.0) / (2.0 + 1.0) = 14.0 / 3.0 = 4.666...
      expect(average!.toStringAsFixed(2), equals('4.67'));
    });
  });

  group('AbsencesRepository', () {
    setUp(() async {
      await DatabaseService.reset();
    });

    tearDown(() async {
      await DatabaseService.close();
    });

    test('Dodawanie i pobieranie nieobecności', () async {
      final absence = AbsenceModel(
        id: '1',
        subject: 'Matematyka',
        date: DateTime(2024, 1, 15),
        type: 'nieusprawiedliwiona',
        excused: false,
        lecturer: 'dr Jan Kowalski',
        createdAt: DateTime.now(),
      );

      final result = await AbsencesRepository.insert(absence);
      expect(result, isTrue);

      final absences = await AbsencesRepository.getAll();
      expect(absences.length, equals(1));
      expect(absences.first.subject, equals('Matematyka'));
      expect(absences.first.excused, isFalse);
    });

    test('Pobieranie nieobecności według statusu usprawiedliwienia', () async {
      await AbsencesRepository.insert(AbsenceModel(
        id: '1',
        subject: 'Matematyka',
        date: DateTime.now(),
        excused: false,
        createdAt: DateTime.now(),
      ));
      await AbsencesRepository.insert(AbsenceModel(
        id: '2',
        subject: 'Fizyka',
        date: DateTime.now(),
        excused: true,
        createdAt: DateTime.now(),
      ));

      final unexcused = await AbsencesRepository.getByExcusedStatus(excused: false);
      expect(unexcused.length, equals(1));
      expect(unexcused.first.subject, equals('Matematyka'));

      final excused = await AbsencesRepository.getByExcusedStatus(excused: true);
      expect(excused.length, equals(1));
      expect(excused.first.subject, equals('Fizyka'));
    });

    test('Oznaczanie nieobecności jako usprawiedliwionej', () async {
      await AbsencesRepository.insert(AbsenceModel(
        id: '1',
        subject: 'Matematyka',
        date: DateTime.now(),
        excused: false,
        createdAt: DateTime.now(),
      ));

      final result = await AbsencesRepository.markAsExcused('1');
      expect(result, isTrue);

      final absence = await AbsencesRepository.getById('1');
      expect(absence?.excused, isTrue);
    });

    test('Zliczanie nieobecności według przedmiotu', () async {
      await AbsencesRepository.insert(AbsenceModel(
        id: '1',
        subject: 'Matematyka',
        date: DateTime.now(),
        createdAt: DateTime.now(),
      ));
      await AbsencesRepository.insert(AbsenceModel(
        id: '2',
        subject: 'Matematyka',
        date: DateTime.now(),
        createdAt: DateTime.now(),
      ));
      await AbsencesRepository.insert(AbsenceModel(
        id: '3',
        subject: 'Fizyka',
        date: DateTime.now(),
        createdAt: DateTime.now(),
      ));

      final counts = await AbsencesRepository.countBySubject();
      expect(counts['Matematyka'], equals(2));
      expect(counts['Fizyka'], equals(1));
    });
  });

  group('SettingsRepository', () {
    setUp(() async {
      await DatabaseService.reset();
    });

    tearDown() async {
      await DatabaseService.close();
    });

    test('Zapisywanie i odczytywanie String', () async {
      await SettingsRepository.setString('test_key', 'test_value');
      final value = await SettingsRepository.getString('test_key');
      expect(value, equals('test_value'));
    });

    test('Zapisywanie i odczytywanie int', () async {
      await SettingsRepository.setInt('test_int', 42);
      final value = await SettingsRepository.getInt('test_int');
      expect(value, equals(42));
    });

    test('Zapisywanie i odczytywanie bool', () async {
      await SettingsRepository.setBool('test_bool', true);
      final value = await SettingsRepository.getBool('test_bool');
      expect(value, isTrue);
    });

    test('Zapisywanie i odczytywanie JSON', () async {
      final json = {'name': 'Test', 'value': 123};
      await SettingsRepository.setJson('test_json', json);
      final value = await SettingsRepository.getJson('test_json');
      expect(value, equals(json));
    });

    test('Sprawdzanie czy ustawienie istnieje', () async {
      await SettingsRepository.setString('existing_key', 'value');
      final exists = await SettingsRepository.exists('existing_key');
      final notExists = await SettingsRepository.exists('non_existing_key');
      expect(exists, isTrue);
      expect(notExists, isFalse);
    });

    test('Usuwanie ustawienia', () async {
      await SettingsRepository.setString('to_remove', 'value');
      await SettingsRepository.remove('to_remove');
      final value = await SettingsRepository.getString('to_remove');
      expect(value, isNull);
    });

    test('Pobieranie wszystkich kluczy', () async {
      await SettingsRepository.setString('key1', 'value1');
      await SettingsRepository.setString('key2', 'value2');
      final keys = await SettingsRepository.getKeys();
      expect(keys.length, equals(2));
      expect(keys.contains('key1'), isTrue);
      expect(keys.contains('key2'), isTrue);
    });

    test('Czyszczenie wszystkich ustawień', () async {
      await SettingsRepository.setString('key1', 'value1');
      await SettingsRepository.setString('key2', 'value2');
      await SettingsRepository.clear();
      final count = await SettingsRepository.count();
      expect(count, equals(0));
    });
  });
}
