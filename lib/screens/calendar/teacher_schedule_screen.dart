import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/services/classes_repository.dart';

/// Placeholder ekranu planu nauczyciela.
/// Zwraca Map {'apply': true, 'id': id, 'name': name} jeśli użytkownik zatwierdzi.
class TeacherScheduleScreen extends StatelessWidget {
  final String teacherId;
  final String teacherName;
  const TeacherScheduleScreen({super.key, required this.teacherId, required this.teacherName});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(MyUz.chevron_left, size: 24),
          onPressed: () => Navigator.of(context).pop(null),
        ),
        title: Text(teacherName.isNotEmpty ? teacherName : 'Nauczyciel'),
        backgroundColor: Theme.of(context).colorScheme.surface,
        foregroundColor: Theme.of(context).colorScheme.onSurface,
        elevation: 0,
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(null), child: const Text('Anuluj')),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            const Divider(height: 1, thickness: 1),
            Expanded(
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24.0),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text('Tu będzie ekran planu nauczyciela', style: Theme.of(context).textTheme.bodyMedium),
                      const SizedBox(height: 8),
                      Text(teacherName, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 24),
                      Text('Widok w przygotowaniu', style: Theme.of(context).textTheme.bodySmall),
                    ],
                  ),
                ),
              ),
            ),
            const Divider(height: 1, thickness: 1),
            Padding(
              padding: const EdgeInsets.all(12.0),
              child: Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => Navigator.of(context).pop(null),
                      child: const Text('Anuluj'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () async {
                        // Zapis preferencji nauczyciela
                        try {
                          await ClassesRepository.setTeacherPrefsById(teacherId);
                        } catch (_) {}
                        Navigator.of(context).pop({'apply': true, 'id': teacherId, 'name': teacherName});
                      },
                      child: const Text('Ustaw jako mój nauczyciel'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
