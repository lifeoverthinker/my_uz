import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:flutter/foundation.dart';

/// Serwis zarządzania lokalną bazą danych SQLite.
/// Obsługuje inicjalizację bazy, migracje i dostęp do instancji bazy.
class DatabaseService {
  static Database? _database;
  static const String _dbName = 'my_uz.db';
  static const int _dbVersion = 1;

  /// Prywatny konstruktor dla singletona.
  DatabaseService._();

  /// Pobiera instancję bazy danych. Inicjalizuje bazę przy pierwszym użyciu.
  static Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  /// Inicjalizacja bazy danych.
  static Future<Database> _initDatabase() async {
    try {
      final dbPath = await getDatabasesPath();
      final path = join(dbPath, _dbName);

      debugPrint('[DatabaseService] Initializing database at: $path');

      return await openDatabase(
        path,
        version: _dbVersion,
        onCreate: _onCreate,
        onUpgrade: _onUpgrade,
      );
    } catch (e) {
      debugPrint('[DatabaseService] Error initializing database: $e');
      rethrow;
    }
  }

  /// Tworzenie tabel przy pierwszym uruchomieniu.
  static Future<void> _onCreate(Database db, int version) async {
    debugPrint('[DatabaseService] Creating database tables (version: $version)');

    // Tabela ocen
    await db.execute('''
      CREATE TABLE grades (
        id TEXT PRIMARY KEY,
        subject TEXT NOT NULL,
        grade TEXT,
        numeric_value REAL,
        weight REAL,
        category TEXT,
        description TEXT,
        date TEXT,
        semester TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT
      )
    ''');

    // Indeksy dla tabeli ocen
    await db.execute('CREATE INDEX idx_grades_subject ON grades(subject)');
    await db.execute('CREATE INDEX idx_grades_semester ON grades(semester)');
    await db.execute('CREATE INDEX idx_grades_date ON grades(date)');

    // Tabela nieobecności
    await db.execute('''
      CREATE TABLE absences (
        id TEXT PRIMARY KEY,
        subject TEXT NOT NULL,
        date TEXT NOT NULL,
        type TEXT,
        reason TEXT,
        excused INTEGER NOT NULL DEFAULT 0,
        lecturer TEXT,
        duration INTEGER,
        notes TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT
      )
    ''');

    // Indeksy dla tabeli nieobecności
    await db.execute('CREATE INDEX idx_absences_subject ON absences(subject)');
    await db.execute('CREATE INDEX idx_absences_date ON absences(date)');
    await db.execute('CREATE INDEX idx_absences_excused ON absences(excused)');

    // Tabela ustawień (klucz-wartość)
    await db.execute('''
      CREATE TABLE settings (
        key TEXT PRIMARY KEY,
        value TEXT,
        type TEXT,
        updated_at TEXT NOT NULL
      )
    ''');

    debugPrint('[DatabaseService] Database tables created successfully');
  }

  /// Migracje bazy danych przy aktualizacji wersji.
  static Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    debugPrint('[DatabaseService] Upgrading database from v$oldVersion to v$newVersion');

    // Miejsce na przyszłe migracje
    // if (oldVersion < 2) {
    //   await db.execute('ALTER TABLE grades ADD COLUMN new_field TEXT');
    // }
  }

  /// Zamknięcie bazy danych.
  static Future<void> close() async {
    if (_database != null) {
      await _database!.close();
      _database = null;
      debugPrint('[DatabaseService] Database closed');
    }
  }

  /// Usuwa bazę danych (przydatne do testów).
  static Future<void> deleteDatabase() async {
    try {
      final dbPath = await getDatabasesPath();
      final path = join(dbPath, _dbName);
      await databaseFactory.deleteDatabase(path);
      _database = null;
      debugPrint('[DatabaseService] Database deleted');
    } catch (e) {
      debugPrint('[DatabaseService] Error deleting database: $e');
      rethrow;
    }
  }

  /// Resetuje bazę danych (usuwa i tworzy na nowo).
  static Future<void> reset() async {
    await close();
    await deleteDatabase();
    _database = await _initDatabase();
    debugPrint('[DatabaseService] Database reset');
  }

  /// Wykonuje transakcję.
  static Future<T> transaction<T>(Future<T> Function(Transaction txn) action) async {
    final db = await database;
    return await db.transaction(action);
  }

  /// Wykonuje zapytanie SELECT.
  static Future<List<Map<String, dynamic>>> query(
    String table, {
    bool? distinct,
    List<String>? columns,
    String? where,
    List<Object?>? whereArgs,
    String? groupBy,
    String? having,
    String? orderBy,
    int? limit,
    int? offset,
  }) async {
    final db = await database;
    return await db.query(
      table,
      distinct: distinct,
      columns: columns,
      where: where,
      whereArgs: whereArgs,
      groupBy: groupBy,
      having: having,
      orderBy: orderBy,
      limit: limit,
      offset: offset,
    );
  }

  /// Wstawia rekord do bazy.
  static Future<int> insert(
    String table,
    Map<String, dynamic> values, {
    ConflictAlgorithm? conflictAlgorithm,
  }) async {
    final db = await database;
    return await db.insert(
      table,
      values,
      conflictAlgorithm: conflictAlgorithm ?? ConflictAlgorithm.replace,
    );
  }

  /// Aktualizuje rekordy w bazie.
  static Future<int> update(
    String table,
    Map<String, dynamic> values, {
    String? where,
    List<Object?>? whereArgs,
  }) async {
    final db = await database;
    return await db.update(
      table,
      values,
      where: where,
      whereArgs: whereArgs,
    );
  }

  /// Usuwa rekordy z bazy.
  static Future<int> delete(
    String table, {
    String? where,
    List<Object?>? whereArgs,
  }) async {
    final db = await database;
    return await db.delete(
      table,
      where: where,
      whereArgs: whereArgs,
    );
  }

  /// Wykonuje surowe zapytanie SQL.
  static Future<List<Map<String, dynamic>>> rawQuery(
    String sql, [
    List<Object?>? arguments,
  ]) async {
    final db = await database;
    return await db.rawQuery(sql, arguments);
  }

  /// Wykonuje surowe polecenie SQL (INSERT, UPDATE, DELETE).
  static Future<int> rawInsert(
    String sql, [
    List<Object?>? arguments,
  ]) async {
    final db = await database;
    return await db.rawInsert(sql, arguments);
  }

  /// Wykonuje surowe polecenie SQL (UPDATE, DELETE).
  static Future<int> rawUpdate(
    String sql, [
    List<Object?>? arguments,
  ]) async {
    final db = await database;
    return await db.rawUpdate(sql, arguments);
  }

  /// Wykonuje surowe polecenie SQL (DELETE).
  static Future<int> rawDelete(
    String sql, [
    List<Object?>? arguments,
  ]) async {
    final db = await database;
    return await db.rawDelete(sql, arguments);
  }
}
