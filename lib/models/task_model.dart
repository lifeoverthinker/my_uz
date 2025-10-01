/// Model danych zadania (nazwa, deadline, przedmiot).
/// Można powiązać z zajęciami przez `classId` (opcjonalne).
class TaskModel {
  final String id;             // UUID / id rekordu
  final String title;          // Nazwa zadania (np. "Projekt zaliczeniowy")
  final DateTime deadline;     // Termin (deadline)
  final String subject;        // Przedmiot (np. "PPO", "Analiza II")
  final String? classId;       // Opcjonalne powiązanie z ClassModel.id
  final bool completed;        // Status ukończenia (lokalnie / zdalnie)
  final String? type;         // Typ zaliczenia (np. Laboratorium, Egzamin)

  const TaskModel({
    required this.id,
    required this.title,
    required this.deadline,
    required this.subject,
    this.classId,
    this.completed = false,
    this.type,
  });

  factory TaskModel.fromMap(Map<String, dynamic> map) {
    return TaskModel(
      id: map['id'] as String,
      title: map['title'] as String,
      deadline: DateTime.parse(map['deadline'] as String),
      subject: map['subject'] as String,
      classId: map['class_id'] as String?,
      completed: (map['completed'] as bool?) ?? false,
      type: map['type'] as String?,
    );
  }

  Map<String, dynamic> toMap() => {
    'id': id,
    'title': title,
    'deadline': deadline.toIso8601String(),
    'subject': subject,
    'class_id': classId,
    'completed': completed,
    'type': type,
  };

  TaskModel copyWith({
    String? id,
    String? title,
    DateTime? deadline,
    String? subject,
    String? classId,
    bool? completed,
    String? type,
  }) {
    return TaskModel(
      id: id ?? this.id,
      title: title ?? this.title,
      deadline: deadline ?? this.deadline,
      subject: subject ?? this.subject,
      classId: classId ?? this.classId,
      completed: completed ?? this.completed,
      type: type ?? this.type,
    );
  }

  @override
  String toString() =>
      'TaskModel(id=$id, title=$title, deadline=$deadline, subject=$subject, classId=$classId, completed=$completed, type=$type)';

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other is TaskModel &&
            other.id == id &&
            other.title == title &&
            other.deadline == deadline &&
            other.subject == subject &&
            other.classId == classId &&
            other.completed == completed &&
            other.type == type);
  }

  @override
  int get hashCode =>
      Object.hash(id, title, deadline, subject, classId, completed, type);
}