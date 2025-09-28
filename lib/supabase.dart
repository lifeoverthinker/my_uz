import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// PROSTA KONFIGURACJA SUPABASE (KISS)
/// - Jeden plik w /lib
/// - Źródła kluczy:
///   1. --dart-define (SUPABASE_URL, SUPABASE_ANON_KEY)
///   2. .env (fallback) – dodaj do assets w pubspec.yaml
/// - Nie używaj service_role w aplikacji klienckiej.
/// - RLS musi chronić dane (polityki w panelu Supabase).
class Supa {
  static bool _initialized = false;

  static const String _urlDefine = String.fromEnvironment('SUPABASE_URL');
  static const String _keyDefine = String.fromEnvironment('SUPABASE_ANON_KEY');

  /// Inicjalizacja – wywołaj raz na starcie (main()).
  static Future<void> init() async {
    if (_initialized) return;

    // Ładujemy .env tylko jeśli potrzeba (fallback).
    await dotenv.load(fileName: '.env');

    final url = (_urlDefine.isNotEmpty ? _urlDefine : dotenv.env['SUPABASE_URL'])?.trim();
    final anonKey = (_keyDefine.isNotEmpty ? _keyDefine : dotenv.env['SUPABASE_ANON_KEY'])?.trim();

    if (url == null || url.isEmpty || anonKey == null || anonKey.isEmpty) {
      throw Exception(
        'Brak konfiguracji Supabase. Dodaj .env lub użyj --dart-define (SUPABASE_URL / SUPABASE_ANON_KEY).',
      );
    }

    await Supabase.initialize(
      url: url,
      anonKey: anonKey,
    );

    _initialized = true;
    if (kDebugMode) {
      debugPrint('[Supa] Inicjalizacja OK (url=$url)');
    }
  }

  /// Skrót do klienta Supabase (upewnij się, że init() już wywołane).
  static SupabaseClient get client {
    if (!_initialized) {
      throw StateError('Supabase nie został zainicjalizowany – wywołaj Supa.init() przed użyciem.');
    }
    return Supabase.instance.client;
  }
}