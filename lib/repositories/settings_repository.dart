import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:my_uz/services/database_service.dart';

/// Repozytorium do zarządzania ustawieniami użytkownika w lokalnej bazie danych.
/// Obsługuje przechowywanie par klucz-wartość z automatyczną serializacją.
class SettingsRepository {
  static const String _tableName = 'settings';

  /// Typ danych dla ustawień.
  enum SettingType {
    string,
    int,
    double,
    bool,
    json,
  }

  /// Pobiera wartość ustawienia jako String.
  static Future<String?> getString(String key) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        where: 'key = ?',
        whereArgs: [key],
        limit: 1,
      );
      if (maps.isEmpty) return null;
      return maps.first['value'] as String?;
    } catch (e) {
      debugPrint('[SettingsRepository][getString] Error: $e');
      return null;
    }
  }

  /// Pobiera wartość ustawienia jako int.
  static Future<int?> getInt(String key) async {
    try {
      final value = await getString(key);
      if (value == null) return null;
      return int.tryParse(value);
    } catch (e) {
      debugPrint('[SettingsRepository][getInt] Error: $e');
      return null;
    }
  }

  /// Pobiera wartość ustawienia jako double.
  static Future<double?> getDouble(String key) async {
    try {
      final value = await getString(key);
      if (value == null) return null;
      return double.tryParse(value);
    } catch (e) {
      debugPrint('[SettingsRepository][getDouble] Error: $e');
      return null;
    }
  }

  /// Pobiera wartość ustawienia jako bool.
  static Future<bool?> getBool(String key) async {
    try {
      final value = await getString(key);
      if (value == null) return null;
      return value.toLowerCase() == 'true' || value == '1';
    } catch (e) {
      debugPrint('[SettingsRepository][getBool] Error: $e');
      return null;
    }
  }

  /// Pobiera wartość ustawienia jako obiekt JSON.
  static Future<Map<String, dynamic>?> getJson(String key) async {
    try {
      final value = await getString(key);
      if (value == null || value.isEmpty) return null;
      return jsonDecode(value) as Map<String, dynamic>;
    } catch (e) {
      debugPrint('[SettingsRepository][getJson] Error: $e');
      return null;
    }
  }

  /// Pobiera wartość ustawienia jako listę JSON.
  static Future<List<dynamic>?> getJsonList(String key) async {
    try {
      final value = await getString(key);
      if (value == null || value.isEmpty) return null;
      return jsonDecode(value) as List<dynamic>;
    } catch (e) {
      debugPrint('[SettingsRepository][getJsonList] Error: $e');
      return null;
    }
  }

  /// Zapisuje wartość ustawienia jako String.
  static Future<bool> setString(String key, String value) async {
    try {
      await _upsert(key, value, SettingType.string);
      debugPrint('[SettingsRepository][setString] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setString] Error: $e');
      return false;
    }
  }

  /// Zapisuje wartość ustawienia jako int.
  static Future<bool> setInt(String key, int value) async {
    try {
      await _upsert(key, value.toString(), SettingType.int);
      debugPrint('[SettingsRepository][setInt] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setInt] Error: $e');
      return false;
    }
  }

  /// Zapisuje wartość ustawienia jako double.
  static Future<bool> setDouble(String key, double value) async {
    try {
      await _upsert(key, value.toString(), SettingType.double);
      debugPrint('[SettingsRepository][setDouble] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setDouble] Error: $e');
      return false;
    }
  }

  /// Zapisuje wartość ustawienia jako bool.
  static Future<bool> setBool(String key, bool value) async {
    try {
      await _upsert(key, value.toString(), SettingType.bool);
      debugPrint('[SettingsRepository][setBool] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setBool] Error: $e');
      return false;
    }
  }

  /// Zapisuje wartość ustawienia jako obiekt JSON.
  static Future<bool> setJson(String key, Map<String, dynamic> value) async {
    try {
      final jsonString = jsonEncode(value);
      await _upsert(key, jsonString, SettingType.json);
      debugPrint('[SettingsRepository][setJson] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setJson] Error: $e');
      return false;
    }
  }

  /// Zapisuje wartość ustawienia jako listę JSON.
  static Future<bool> setJsonList(String key, List<dynamic> value) async {
    try {
      final jsonString = jsonEncode(value);
      await _upsert(key, jsonString, SettingType.json);
      debugPrint('[SettingsRepository][setJsonList] Setting saved: $key');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][setJsonList] Error: $e');
      return false;
    }
  }

  /// Usuwa ustawienie.
  static Future<bool> remove(String key) async {
    try {
      final count = await DatabaseService.delete(
        _tableName,
        where: 'key = ?',
        whereArgs: [key],
      );
      if (count > 0) {
        debugPrint('[SettingsRepository][remove] Setting removed: $key');
        return true;
      }
      return false;
    } catch (e) {
      debugPrint('[SettingsRepository][remove] Error: $e');
      return false;
    }
  }

  /// Usuwa wszystkie ustawienia.
  static Future<bool> clear() async {
    try {
      await DatabaseService.delete(_tableName);
      debugPrint('[SettingsRepository][clear] All settings cleared');
      return true;
    } catch (e) {
      debugPrint('[SettingsRepository][clear] Error: $e');
      return false;
    }
  }

  /// Sprawdza, czy ustawienie istnieje.
  static Future<bool> exists(String key) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        columns: ['key'],
        where: 'key = ?',
        whereArgs: [key],
        limit: 1,
      );
      return maps.isNotEmpty;
    } catch (e) {
      debugPrint('[SettingsRepository][exists] Error: $e');
      return false;
    }
  }

  /// Pobiera wszystkie klucze ustawień.
  static Future<List<String>> getKeys() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        columns: ['key'],
        orderBy: 'key',
      );
      return maps.map((map) => map['key'] as String).toList();
    } catch (e) {
      debugPrint('[SettingsRepository][getKeys] Error: $e');
      return [];
    }
  }

  /// Pobiera wszystkie ustawienia jako mapę.
  static Future<Map<String, String>> getAll() async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        orderBy: 'key',
      );
      final Map<String, String> settings = {};
      for (final map in maps) {
        final key = map['key'] as String;
        final value = map['value'] as String?;
        if (value != null) {
          settings[key] = value;
        }
      }
      return settings;
    } catch (e) {
      debugPrint('[SettingsRepository][getAll] Error: $e');
      return {};
    }
  }

  /// Pobiera typ ustawienia.
  static Future<SettingType?> getType(String key) async {
    try {
      final List<Map<String, dynamic>> maps = await DatabaseService.query(
        _tableName,
        columns: ['type'],
        where: 'key = ?',
        whereArgs: [key],
        limit: 1,
      );
      if (maps.isEmpty) return null;
      final typeString = maps.first['type'] as String?;
      if (typeString == null) return null;
      return SettingType.values.firstWhere(
        (e) => e.toString().split('.').last == typeString,
        orElse: () => SettingType.string,
      );
    } catch (e) {
      debugPrint('[SettingsRepository][getType] Error: $e');
      return null;
    }
  }

  /// Pobiera liczbę ustawień.
  static Future<int> count() async {
    try {
      final List<Map<String, dynamic>> result = await DatabaseService.rawQuery(
        'SELECT COUNT(*) as count FROM $_tableName',
      );
      return result.first['count'] as int;
    } catch (e) {
      debugPrint('[SettingsRepository][count] Error: $e');
      return 0;
    }
  }

  /// Wewnętrzna metoda do wstawiania lub aktualizowania ustawienia.
  static Future<void> _upsert(String key, String value, SettingType type) async {
    final data = {
      'key': key,
      'value': value,
      'type': type.toString().split('.').last,
      'updated_at': DateTime.now().toIso8601String(),
    };
    await DatabaseService.insert(_tableName, data);
  }

  /// Pomocnicze metody dla często używanych ustawień.
  
  /// Ustawienia motywu.
  static Future<String?> getThemeMode() => getString('theme_mode');
  static Future<bool> setThemeMode(String mode) => setString('theme_mode', mode);

  /// Ustawienia języka.
  static Future<String?> getLanguage() => getString('language');
  static Future<bool> setLanguage(String language) => setString('language', language);

  /// Ustawienia powiadomień.
  static Future<bool?> getNotificationsEnabled() => getBool('notifications_enabled');
  static Future<bool> setNotificationsEnabled(bool enabled) => setBool('notifications_enabled', enabled);

  /// Ustawienia synchronizacji.
  static Future<bool?> getAutoSync() => getBool('auto_sync');
  static Future<bool> setAutoSync(bool enabled) => setBool('auto_sync', enabled);

  /// Ostatnia synchronizacja.
  static Future<String?> getLastSyncTime() => getString('last_sync_time');
  static Future<bool> setLastSyncTime(String time) => setString('last_sync_time', time);

  /// Ustawienia wyświetlania kalendarza.
  static Future<String?> getCalendarView() => getString('calendar_view');
  static Future<bool> setCalendarView(String view) => setString('calendar_view', view);

  /// Domyślny widok startowy.
  static Future<String?> getDefaultView() => getString('default_view');
  static Future<bool> setDefaultView(String view) => setString('default_view', view);
}
