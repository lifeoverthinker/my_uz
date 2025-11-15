/// Model danych dla nieobecności.
/// Reprezentuje pojedynczą nieobecność na zajęciach.
class AbsenceModel {
  final String id;              // UUID / id rekordu
  final String subject;         // Nazwa przedmiotu
  final DateTime date;          // Data nieobecności
  final String? type;           // Typ nieobecności (np. "nieusprawiedliwiona", "usprawiedliwiona", "spóźnienie")
  final String? reason;         // Powód nieobecności
  final bool excused;           // Czy nieobecność jest usprawiedliwiona
  final String? lecturer;       // Prowadzący zajęcia
  final int? duration;          // Czas trwania w minutach (dla spóźnień)
  final String? notes;          // Dodatkowe notatki
  final DateTime createdAt;     // Data utworzenia rekordu
  final DateTime? updatedAt;    // Data ostatniej aktualizacji

  const AbsenceModel({
    required this.id,
    required this.subject,
    required this.date,
    this.type,
    this.reason,
    this.excused = false,
    this.lecturer,
    this.duration,
    this.notes,
    required this.createdAt,
    this.updatedAt,
  });

  /// Fabryka z mapy (obsługuje deserializację z bazy danych).
  factory AbsenceModel.fromMap(Map<String, dynamic> map) {
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

    return AbsenceModel(
      id: map['id'] as String? ?? '',
      subject: map['subject'] as String? ?? '',
      date: parseDateTimeFromDynamic(map['date']) ?? DateTime.now(),
      type: map['type'] as String?,
      reason: map['reason'] as String?,
      excused: map['excused'] == 1 || map['excused'] == true,
      lecturer: map['lecturer'] as String?,
      duration: map['duration'] as int?,
      notes: map['notes'] as String?,
      createdAt: parseDateTimeFromDynamic(map['created_at']) ?? DateTime.now(),
      updatedAt: parseDateTimeFromDynamic(map['updated_at']),
    );
  }

  /// Konwersja do mapy (dla zapisu do bazy danych).
  Map<String, dynamic> toMap() => {
    'id': id,
    'subject': subject,
    'date': date.toIso8601String(),
    'type': type,
    'reason': reason,
    'excused': excused ? 1 : 0,
    'lecturer': lecturer,
    'duration': duration,
    'notes': notes,
    'created_at': createdAt.toIso8601String(),
    'updated_at': updatedAt?.toIso8601String(),
  };

  AbsenceModel copyWith({
    String? id,
    String? subject,
    DateTime? date,
    String? type,
    String? reason,
    bool? excused,
    String? lecturer,
    int? duration,
    String? notes,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return AbsenceModel(
      id: id ?? this.id,
      subject: subject ?? this.subject,
      date: date ?? this.date,
      type: type ?? this.type,
      reason: reason ?? this.reason,
      excused: excused ?? this.excused,
      lecturer: lecturer ?? this.lecturer,
      duration: duration ?? this.duration,
      notes: notes ?? this.notes,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  String toString() => 'AbsenceModel(id=$id, subject=$subject, date=$date, '
      'type=$type, excused=$excused, lecturer=$lecturer)';

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other is AbsenceModel &&
            other.id == id &&
            other.subject == subject &&
            other.date == date &&
            other.type == type &&
            other.reason == reason &&
            other.excused == excused &&
            other.lecturer == lecturer &&
            other.duration == duration &&
            other.notes == notes &&
            other.createdAt == createdAt &&
            other.updatedAt == updatedAt);
  }

  @override
  int get hashCode => Object.hash(
    id, subject, date, type, reason, excused,
    lecturer, duration, notes, createdAt, updatedAt,
  );
}
