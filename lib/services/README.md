# Database Service Documentation

## Przegląd

Ten katalog zawiera implementację lokalnej bazy danych SQLite dla aplikacji MyUZ, która przechowuje oceny, nieobecności i ustawienia użytkownika.

## Struktura

### DatabaseService (`database_service.dart`)

Główny serwis zarządzający bazą danych SQLite. Obsługuje:
- Inicjalizację bazy danych
- Tworzenie tabel
- Migracje bazy danych
- Podstawowe operacje CRUD

### Modele

#### GradeModel (`lib/models/grade_model.dart`)
Model reprezentujący ocenę studenta:
- `id` - unikalny identyfikator
- `subject` - nazwa przedmiotu
- `grade` - ocena (jako String)
- `numericValue` - wartość numeryczna (dla obliczeń)
- `weight` - waga oceny
- `category` - kategoria (np. "Egzamin", "Kolokwium")
- `date` - data wystawienia
- `semester` - semestr akademicki

#### AbsenceModel (`lib/models/absence_model.dart`)
Model reprezentujący nieobecność:
- `id` - unikalny identyfikator
- `subject` - nazwa przedmiotu
- `date` - data nieobecności
- `type` - typ nieobecności
- `excused` - czy usprawiedliwiona
- `lecturer` - prowadzący
- `duration` - czas trwania w minutach

### Repozytoria

#### GradesRepository (`lib/repositories/grades_repository.dart`)
Zarządza ocenami w bazie danych:
- `getAll()` - pobiera wszystkie oceny
- `getBySubject(subject)` - oceny dla przedmiotu
- `getBySemester(semester)` - oceny dla semestru
- `insert(grade)` - dodaje nową ocenę
- `update(grade)` - aktualizuje ocenę
- `delete(id)` - usuwa ocenę
- `calculateWeightedAverage()` - oblicza średnią ważoną
- `calculateSimpleAverage()` - oblicza średnią arytmetyczną

#### AbsencesRepository (`lib/repositories/absences_repository.dart`)
Zarządza nieobecnościami w bazie danych:
- `getAll()` - pobiera wszystkie nieobecności
- `getBySubject(subject)` - nieobecności dla przedmiotu
- `getByExcusedStatus(excused)` - filtruje po statusie
- `insert(absence)` - dodaje nieobecność
- `update(absence)` - aktualizuje nieobecność
- `delete(id)` - usuwa nieobecność
- `markAsExcused(id)` - oznacza jako usprawiedliwioną
- `countUnexcused()` - liczy nieusprawiedliwione

#### SettingsRepository (`lib/repositories/settings_repository.dart`)
Zarządza ustawieniami użytkownika (klucz-wartość):
- `getString(key)` - pobiera wartość String
- `getInt(key)` - pobiera wartość int
- `getBool(key)` - pobiera wartość bool
- `getJson(key)` - pobiera obiekt JSON
- `setString(key, value)` - zapisuje String
- `setInt(key, value)` - zapisuje int
- `setBool(key, value)` - zapisuje bool
- `setJson(key, value)` - zapisuje JSON
- `remove(key)` - usuwa ustawienie
- `clear()` - czyści wszystkie ustawienia

## Przykłady użycia

### Inicjalizacja bazy danych

```dart
import 'package:my_uz/services/database_service.dart';

// Baza inicjalizuje się automatycznie przy pierwszym użyciu
final db = await DatabaseService.database;
```

### Praca z ocenami

```dart
import 'package:my_uz/repositories/grades_repository.dart';
import 'package:my_uz/models/grade_model.dart';

// Dodawanie oceny
final grade = GradeModel(
  id: uuid.v4(),
  subject: 'Matematyka',
  grade: '5.0',
  numericValue: 5.0,
  weight: 2.0,
  category: 'Egzamin',
  date: DateTime.now(),
  semester: '2023/2024 Zimowy',
  createdAt: DateTime.now(),
);

await GradesRepository.insert(grade);

// Pobieranie ocen
final allGrades = await GradesRepository.getAll();
final mathGrades = await GradesRepository.getBySubject('Matematyka');

// Obliczanie średniej
final average = await GradesRepository.calculateWeightedAverage();
print('Średnia ważona: ${average?.toStringAsFixed(2)}');

// Aktualizacja oceny
final updatedGrade = grade.copyWith(grade: '5.5', numericValue: 5.5);
await GradesRepository.update(updatedGrade);

// Usunięcie oceny
await GradesRepository.delete(grade.id);
```

### Praca z nieobecnościami

```dart
import 'package:my_uz/repositories/absences_repository.dart';
import 'package:my_uz/models/absence_model.dart';

// Dodawanie nieobecności
final absence = AbsenceModel(
  id: uuid.v4(),
  subject: 'Fizyka',
  date: DateTime.now(),
  type: 'nieusprawiedliwiona',
  excused: false,
  lecturer: 'dr Jan Kowalski',
  createdAt: DateTime.now(),
);

await AbsencesRepository.insert(absence);

// Pobieranie nieobecności
final allAbsences = await AbsencesRepository.getAll();
final unexcused = await AbsencesRepository.getByExcusedStatus(excused: false);

// Usprawiedliwianie nieobecności
await AbsencesRepository.markAsExcused(absence.id);

// Statystyki
final unexcusedCount = await AbsencesRepository.countUnexcused();
final bySubject = await AbsencesRepository.countBySubject();
```

### Praca z ustawieniami

```dart
import 'package:my_uz/repositories/settings_repository.dart';

// Zapisywanie ustawień
await SettingsRepository.setThemeMode('dark');
await SettingsRepository.setNotificationsEnabled(true);
await SettingsRepository.setInt('sync_interval', 30);

// Odczytywanie ustawień
final theme = await SettingsRepository.getThemeMode();
final notificationsEnabled = await SettingsRepository.getNotificationsEnabled();
final syncInterval = await SettingsRepository.getInt('sync_interval');

// Zapisywanie niestandardowych ustawień
await SettingsRepository.setJson('user_preferences', {
  'language': 'pl',
  'fontSize': 14,
  'darkMode': true,
});

final preferences = await SettingsRepository.getJson('user_preferences');

// Czyszczenie ustawień
await SettingsRepository.remove('sync_interval');
await SettingsRepository.clear(); // Usuwa wszystkie ustawienia
```

## Testowanie

Testy znajdują się w pliku `test/database_test.dart`. Aby uruchomić testy:

```bash
flutter test test/database_test.dart
```

## Migracje

Przy aktualizacji schematu bazy danych:

1. Zwiększ wersję bazy w `DatabaseService._dbVersion`
2. Dodaj logikę migracji w `DatabaseService._onUpgrade`

Przykład:

```dart
static Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
  if (oldVersion < 2) {
    await db.execute('ALTER TABLE grades ADD COLUMN new_field TEXT');
  }
  if (oldVersion < 3) {
    // Kolejna migracja
  }
}
```

## Uwagi

- Wszystkie operacje są asynchroniczne
- Błędy są logowane przez `debugPrint`
- Baza używa SQLite poprzez pakiet `sqflite`
- Indeksy są tworzone automatycznie dla często używanych kolumn
- Ustawienia wspierają różne typy danych (String, int, double, bool, JSON)
