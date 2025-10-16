/// Model danych dla ocen.
/// Reprezentuje pojedynczą ocenę z przedmiotu.
class GradeModel {
  final String id;              // UUID / id rekordu
  final String subject;         // Nazwa przedmiotu
  final String? grade;          // Ocena (np. "5.0", "4.5", "zaliczony")
  final double? numericValue;   // Wartość numeryczna oceny (dla obliczeń średniej)
  final double? weight;         // Waga oceny
  final String? category;       // Kategoria (np. "Egzamin", "Kolokwium", "Projekt")
  final String? description;    // Opis oceny
  final DateTime? date;         // Data wystawienia oceny
  final String? semester;       // Semestr (np. "2023/2024 Letni")
  final DateTime createdAt;     // Data utworzenia rekordu
  final DateTime? updatedAt;    // Data ostatniej aktualizacji

  const GradeModel({
    required this.id,
    required this.subject,
    this.grade,
    this.numericValue,
    this.weight,
    this.category,
    this.description,
    this.date,
    this.semester,
    required this.createdAt,
    this.updatedAt,
  });

  /// Fabryka z mapy (obsługuje deserializację z bazy danych).
  factory GradeModel.fromMap(Map<String, dynamic> map) {
    DateTime? parseDateTimeFromDynamic(dynamic v) {
      if (v == null) return null;
      if (v is DateTime) return v;
      if (v is String) {
        try {
          return DateTime.parse(v);
        } catch (_) {
          return null;
        }
      }
      if (v is int) return DateTime.fromMillisecondsSinceEpoch(v);
      return null;
    }

    return GradeModel(
      id: map['id'] as String? ?? '',
      subject: map['subject'] as String? ?? '',
      grade: map['grade'] as String?,
      numericValue: map['numeric_value'] != null 
          ? (map['numeric_value'] as num).toDouble() 
          : null,
      weight: map['weight'] != null 
          ? (map['weight'] as num).toDouble() 
          : null,
      category: map['category'] as String?,
      description: map['description'] as String?,
      date: parseDateTimeFromDynamic(map['date']),
      semester: map['semester'] as String?,
      createdAt: parseDateTimeFromDynamic(map['created_at']) ?? DateTime.now(),
      updatedAt: parseDateTimeFromDynamic(map['updated_at']),
    );
  }

  /// Konwersja do mapy (dla zapisu do bazy danych).
  Map<String, dynamic> toMap() => {
    'id': id,
    'subject': subject,
    'grade': grade,
    'numeric_value': numericValue,
    'weight': weight,
    'category': category,
    'description': description,
    'date': date?.toIso8601String(),
    'semester': semester,
    'created_at': createdAt.toIso8601String(),
    'updated_at': updatedAt?.toIso8601String(),
  };

  GradeModel copyWith({
    String? id,
    String? subject,
    String? grade,
    double? numericValue,
    double? weight,
    String? category,
    String? description,
    DateTime? date,
    String? semester,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return GradeModel(
      id: id ?? this.id,
      subject: subject ?? this.subject,
      grade: grade ?? this.grade,
      numericValue: numericValue ?? this.numericValue,
      weight: weight ?? this.weight,
      category: category ?? this.category,
      description: description ?? this.description,
      date: date ?? this.date,
      semester: semester ?? this.semester,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  String toString() => 'GradeModel(id=$id, subject=$subject, grade=$grade, '
      'numericValue=$numericValue, weight=$weight, category=$category, '
      'date=$date, semester=$semester)';

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other is GradeModel &&
            other.id == id &&
            other.subject == subject &&
            other.grade == grade &&
            other.numericValue == numericValue &&
            other.weight == weight &&
            other.category == category &&
            other.description == description &&
            other.date == date &&
            other.semester == semester &&
            other.createdAt == createdAt &&
            other.updatedAt == updatedAt);
  }

  @override
  int get hashCode => Object.hash(
    id, subject, grade, numericValue, weight, category,
    description, date, semester, createdAt, updatedAt,
  );
}
