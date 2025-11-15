import 'package:flutter/foundation.dart';

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
    DateTime? parseDateTimeFromDynamic(dynamic v) {
      if (v == null) return null;
      DateTime? parsed;
      if (v is DateTime) {
        parsed = v;
      } else if (v is String) {
        try {
          parsed = DateTime.parse(v);
        } catch (_) {
          parsed = null;
        }
      } else if (v is int) {
        parsed = DateTime.fromMillisecondsSinceEpoch(v);
      } else {
        try {
          parsed = DateTime.parse(v.toString());
        } catch (_) {
          parsed = null;
        }
      }
      if (parsed == null) return null;
      return parsed.isUtc ? parsed.toLocal() : parsed;
    }

    String readString(String keyPrimary, [String? fallback]) {
      final dynamic v = map[keyPrimary] ?? (fallback != null ? map[fallback] : null);
      if (v == null) return '';
      return v.toString();
    }

    final start = parseDateTimeFromDynamic(map['start_time'] ?? map['od']);
    final end = parseDateTimeFromDynamic(map['end_time'] ?? map['do_'] ?? map['do']);
    if (start == null || end == null) {
      if (kDebugMode) {
        // log only in debug to avoid polluting production logs
        debugPrint('[ClassModel][PARSE-WARN] invalid times start=${map['start_time'] ?? map['od']} end=${map['end_time'] ?? map['do_'] ?? map['do']} id=${map['id']}');
      }
    }
    return ClassModel(
      id: readString('id'),
      subject: readString('subject', 'przedmiot'),
      room: readString('room', 'miejsce'),
      lecturer: readString('lecturer', 'nauczyciel'),
      startTime: start ?? DateTime.now(),
      endTime: end ?? (start ?? DateTime.now()).add(const Duration(hours: 1)),
      uid: map['uid']?.toString(),
      type: map['type']?.toString() ?? map['typ']?.toString() ?? map['rz']?.toString(),
      groupCode: map['kod_grupy']?.toString() ?? map['group_code']?.toString(),
      subgroup: map['podgrupa']?.toString() ?? map['subgroup']?.toString(),
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
  String toString() =>
      'ClassModel(id=$id, subject=$subject, room=$room, lecturer=$lecturer, startTime=$startTime, endTime=$endTime, uid=$uid, type=$type, groupCode=$groupCode, subgroup=$subgroup)';

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