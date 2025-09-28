/// Model danych zajęć (przedmiot, sala, prowadzący, czas).
/// Minimalny, bez zbędnych zależności.
/// Czas reprezentowany przez startTime / endTime (ISO8601).
class ClassModel {
  final String id;            // UUID / id rekordu w Supabase
  final String subject;       // Nazwa przedmiotu (np. "Analiza matematyczna II")
  final String room;          // Sala (np. "A-29, s. 305")
  final String lecturer;      // Prowadzący (np. "dr inż. Kowalski")
  final DateTime startTime;   // Czas rozpoczęcia
  final DateTime endTime;     // Czas zakończenia
  // --- Nowe opcjonalne pola (nie psują dotychczasowego użycia) ---
  final String? uid;          // UID z ICS / źródła (kolumna uid w zajecia_nauczyciela)
  final String? type;         // Typ zajęć (np. WYK / ĆW / LAB) – kolumna rz/typ
  final String? groupCode;    // Kod grupy (jeśli pochodzi z widoku plan_zajec)
  final String? subgroup;     // Podgrupa (A/B lub null / empty)

  const ClassModel({
    required this.id,
    required this.subject,
    required this.room,
    required this.lecturer,
    required this.startTime,
    required this.endTime,
    this.uid,
    this.type,
    this.groupCode,
    this.subgroup,
  });

  /// Fabryka z mapy (obsługuje różne nazwy kolumn – elastyczność przy widoku / tabeli).
  factory ClassModel.fromMap(Map<String, dynamic> map) {
    String readString(String keyPrimary, [String? fallback]) {
      final v = map[keyPrimary] ?? (fallback != null ? map[fallback] : null);
      if (v == null) return '';
      return v as String; // zakładamy poprawny typ – jeśli nie, rzuci
    }

    return ClassModel(
      id: readString('id'),
      subject: readString('subject', 'przedmiot'),
      room: readString('room', 'miejsce'),
      lecturer: readString('lecturer', 'nazwa_nauczyciela'),
      startTime: DateTime.parse(map['start_time'] ?? map['od'] as String),
      endTime: DateTime.parse(map['end_time'] ?? map['do_'] as String),
      uid: map['uid'] as String?,
      type: map['type'] as String? ?? map['typ'] as String? ?? map['rz'] as String?,
      groupCode: map['kod_grupy'] as String? ?? map['group_code'] as String?,
      subgroup: map['podgrupa'] as String? ?? map['subgroup'] as String?,
    );
  }

  /// Konwersja do mapy (np. insert / update w Supabase) – tylko podstawowe atrybuty.
  Map<String, dynamic> toMap() => {
    'id': id,
    'subject': subject,
    'room': room,
    'lecturer': lecturer,
    'start_time': startTime.toIso8601String(),
    'end_time': endTime.toIso8601String(),
    if (uid != null) 'uid': uid,
    if (type != null) 'type': type,
    if (groupCode != null) 'kod_grupy': groupCode,
    if (subgroup != null) 'podgrupa': subgroup,
  };

  ClassModel copyWith({
    String? id,
    String? subject,
    String? room,
    String? lecturer,
    DateTime? startTime,
    DateTime? endTime,
    String? uid,
    String? type,
    String? groupCode,
    String? subgroup,
  }) {
    return ClassModel(
      id: id ?? this.id,
      subject: subject ?? this.subject,
      room: room ?? this.room,
      lecturer: lecturer ?? this.lecturer,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      uid: uid ?? this.uid,
      type: type ?? this.type,
      groupCode: groupCode ?? this.groupCode,
      subgroup: subgroup ?? this.subgroup,
    );
  }

  @override
  String toString() => 'ClassModel(id=$id, subject=$subject, room=$room, lecturer=$lecturer, startTime=$startTime, endTime=$endTime, uid=$uid, type=$type, groupCode=$groupCode, subgroup=$subgroup)';

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other is ClassModel &&
            other.id == id &&
            other.subject == subject &&
            other.room == room &&
            other.lecturer == lecturer &&
            other.startTime == startTime &&
            other.endTime == endTime &&
            other.uid == uid &&
            other.type == type &&
            other.groupCode == groupCode &&
            other.subgroup == subgroup);
  }

  @override
  int get hashCode => Object.hash(id, subject, room, lecturer, startTime, endTime, uid, type, groupCode, subgroup);
}