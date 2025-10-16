# Repositories Documentation

## Przegląd

Ten katalog zawiera repozytoria zarządzające danymi w lokalnej bazie danych SQLite. Repozytoria dostarczają wysokopoziomowy interfejs do operacji CRUD na różnych typach danych.

## Dostępne repozytoria

### GradesRepository

Zarządza ocenami studenta. Obsługuje pełny cykl życia ocen od dodawania, przez aktualizację, po usuwanie.

**Główne funkcje:**
- Pełny CRUD (Create, Read, Update, Delete)
- Filtrowanie po przedmiocie, semestrze, dacie
- Obliczanie średnich (ważonej i arytmetycznej)
- Statystyki (zliczanie, grupowanie)

### AbsencesRepository

Zarządza nieobecnościami studenta na zajęciach.

**Główne funkcje:**
- Pełny CRUD
- Filtrowanie po przedmiocie, dacie, statusie usprawiedliwienia
- Oznaczanie jako usprawiedliwione/nieusprawiedliwione
- Statystyki nieobecności

### SettingsRepository

Zarządza ustawieniami użytkownika w formacie klucz-wartość.

**Główne funkcje:**
- Obsługa różnych typów danych (String, int, double, bool, JSON)
- Predefiniowane metody dla popularnych ustawień
- Serializacja/deserializacja JSON

## Wzorce użycia

### Podstawowe operacje CRUD

```dart
// CREATE
final grade = GradeModel(...);
await GradesRepository.insert(grade);

// READ
final allGrades = await GradesRepository.getAll();
final grade = await GradesRepository.getById('id');

// UPDATE
final updated = grade.copyWith(grade: '5.0');
await GradesRepository.update(updated);

// DELETE
await GradesRepository.delete('id');
```

### Filtrowanie danych

```dart
// Po przedmiocie
final mathGrades = await GradesRepository.getBySubject('Matematyka');

// Po semestrze
final winterGrades = await GradesRepository.getBySemester('2023/2024 Zimowy');

// Po zakresie dat
final recentGrades = await GradesRepository.getByDateRange(
  from: DateTime(2024, 1, 1),
  to: DateTime(2024, 6, 30),
);

// Po statusie
final unexcused = await AbsencesRepository.getByExcusedStatus(excused: false);
```

### Statystyki i agregacje

```dart
// Obliczanie średniej ważonej
final weightedAvg = await GradesRepository.calculateWeightedAverage();

// Zliczanie według przedmiotu
final countsBySubject = await AbsencesRepository.countBySubject();

// Zliczanie nieusprawiedliwionych
final unexcusedCount = await AbsencesRepository.countUnexcused();
```

### Obsługa błędów

Wszystkie metody repozytoriów logują błędy przez `debugPrint` i zwracają bezpieczne wartości domyślne:
- Metody zwracające listy: pusta lista `[]`
- Metody zwracające pojedynczy obiekt: `null`
- Metody operacji: `false` w przypadku błędu

```dart
// Bezpieczne użycie
final grades = await GradesRepository.getAll(); // Nigdy null, zawsze lista
if (grades.isEmpty) {
  print('Brak ocen');
}

final grade = await GradesRepository.getById('id');
if (grade == null) {
  print('Ocena nie znaleziona');
}

final success = await GradesRepository.insert(newGrade);
if (!success) {
  print('Błąd podczas dodawania oceny');
}
```

## Najlepsze praktyki

### 1. Używaj transakcji dla wielu operacji

```dart
await DatabaseService.transaction((txn) async {
  await GradesRepository.insert(grade1);
  await GradesRepository.insert(grade2);
  // Obie operacje albo się udadzą, albo obie zostaną wycofane
});
```

### 2. Korzystaj z metod pomocniczych

```dart
// Zamiast bezpośredniego dostępu do ustawień
final theme = await SettingsRepository.getThemeMode();

// Jest bardziej czytelne niż
final theme = await SettingsRepository.getString('theme_mode');
```

### 3. Zawsze sprawdzaj wynik operacji

```dart
final success = await GradesRepository.delete(gradeId);
if (success) {
  showSuccessMessage();
} else {
  showErrorMessage();
}
```

### 4. Używaj copyWith dla aktualizacji

```dart
final updatedGrade = existingGrade.copyWith(
  grade: '5.0',
  numericValue: 5.0,
);
await GradesRepository.update(updatedGrade);
```

### 5. Pamietaj o ID

Przy tworzeniu nowych obiektów zawsze generuj unikalne ID:

```dart
import 'package:uuid/uuid.dart';

final uuid = Uuid();
final grade = GradeModel(
  id: uuid.v4(),
  // ... pozostałe pola
);
```

## Migracja z SharedPreferences

Jeśli przechowujesz dane w SharedPreferences, możesz je migrować do bazy:

```dart
Future<void> migrateFromPrefs() async {
  final prefs = await SharedPreferences.getInstance();
  
  // Migracja ustawień
  final theme = prefs.getString('theme_mode');
  if (theme != null) {
    await SettingsRepository.setThemeMode(theme);
  }
  
  // Wyczyść stare dane
  await prefs.remove('theme_mode');
}
```

## Debugowanie

Włącz szczegółowe logi:

```dart
// Wszystkie repozytoria logują operacje przez debugPrint
// W production można wyłączyć logi ustawiając:
import 'package:flutter/foundation.dart';

// Dodaj w main.dart:
if (kReleaseMode) {
  debugPrint = (String? message, {int? wrapWidth}) {};
}
```

## Wydajność

### Indeksy

Repozytoria automatycznie tworzą indeksy na często używanych kolumnach:
- `subject` - dla szybkich zapytań po przedmiocie
- `date` - dla zapytań czasowych
- `semester` - dla filtrowania po semestrze

### Cache

Repozytoria nie implementują własnego cache. Jeśli potrzebujesz cache, użyj:

```dart
class GradesCache {
  static List<GradeModel>? _cache;
  static DateTime? _cacheTime;
  
  static Future<List<GradeModel>> getAll() async {
    if (_cache != null && 
        _cacheTime != null && 
        DateTime.now().difference(_cacheTime!) < Duration(minutes: 5)) {
      return _cache!;
    }
    
    _cache = await GradesRepository.getAll();
    _cacheTime = DateTime.now();
    return _cache!;
  }
  
  static void invalidate() {
    _cache = null;
    _cacheTime = null;
  }
}
```

## Backup i Eksport

```dart
Future<Map<String, dynamic>> exportData() async {
  return {
    'grades': (await GradesRepository.getAll())
        .map((g) => g.toMap()).toList(),
    'absences': (await AbsencesRepository.getAll())
        .map((a) => a.toMap()).toList(),
    'settings': await SettingsRepository.getAll(),
  };
}

Future<void> importData(Map<String, dynamic> data) async {
  // Wyczyść istniejące dane
  await GradesRepository.deleteAll();
  await AbsencesRepository.deleteAll();
  await SettingsRepository.clear();
  
  // Importuj nowe
  for (final gradeMap in data['grades']) {
    await GradesRepository.insert(GradeModel.fromMap(gradeMap));
  }
  for (final absenceMap in data['absences']) {
    await AbsencesRepository.insert(AbsenceModel.fromMap(absenceMap));
  }
  // ... settings
}
```
