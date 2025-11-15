/// Słownik skrótów rodzajów zajęć (RZ) – tłumaczenia UZ
abstract class RzDictionary {
  /// Mapa skrót → pełny opis
  static const Map<String, String> _abbreviations = {
    'R': 'Rezerwacja',
    'BHP': 'Szkolenie BHP',
    'C': 'Ćwiczenia',
    'Cz': 'Ćwiczenia / Zdalne',
    'Ć': 'Ćwiczenia',
    'ĆL': 'Ćwiczenia i laboratorium',
    'E': 'Egzamin',
    'E/Z': 'Egzamin/zdalne',
    'I': 'Inne',
    'K': 'Konwersatorium',
    'L': 'Laboratorium',
    'P': 'Projekt',
    'Pra': 'Praktyka',
    'Pro': 'Proseminarium',
    'PrZ': 'Praktyka zawodowa',
    'P/Z': 'Projekt / Zdalne',
    'S': 'Seminarium',
    'Sk': 'Samokształcenie',
    'T': 'Terenowe',
    'W': 'Wykład',
    'war': 'Warsztaty',
    'W+C': 'Wykład i ćwiczenia',
    'WĆL': 'Wykład + ćwiczenia + laboratorium',
    'W+K': 'Wykłady + Konwersatoria',
    'W+L': 'Wykład i laboratorium',
    'W+P': 'Wykład + projekt',
    'WW': 'Wykład i warsztaty',
    'W/Z': 'Wykład/Zdalne',
    'Z': 'Zdalne',
    'ZK': 'Zajęcia kliniczne',
    'Zp': 'Zajęcia praktyczne',
  };

  /// Pełny opis
  static String getDescription(String? abbreviation) {
    final trimmed = (abbreviation ?? '').trim();
    return _abbreviations[trimmed] ?? trimmed;
  }

  /// Lista skrótów (sort)
  static List<String> get allAbbreviations {
    final list = _abbreviations.keys.toList();
    list.sort();
    return list;
  }

  /// Pełna mapa
  static Map<String, String> get allWithDescriptions => Map.unmodifiable(_abbreviations);

  /// Czy znany skrót
  static bool isKnownAbbreviation(String? text) {
    return _abbreviations.containsKey((text ?? '').trim());
  }
}
