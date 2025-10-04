import 'package:flutter/material.dart';

/// Placeholder ekranu: Plan grupy (do implementacji w osobnym branchu)
/// Tutaj pojawi się szczegółowy widok planu grupy (dzienny/tygodniowy),
/// na razie pokazujemy tylko prosty placeholder zgodny z resztą aplikacji.
class GroupScheduleScreen extends StatelessWidget {
  final String? groupCode;
  final String? groupId; // nowy: opcjonalne id grupy
  const GroupScheduleScreen({super.key, this.groupCode, this.groupId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(groupCode == null ? 'Plan grupy' : 'Plan: $groupCode'),
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF1D192B),
        elevation: 0,
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Górny pasek akcji (placeholder)
              Row(
                children: [
                  Expanded(child: Text(groupCode ?? 'Brak wybranej grupy', style: Theme.of(context).textTheme.titleLarge)),
                  IconButton(onPressed: () {}, icon: const Icon(Icons.share)),
                ],
              ),
              const SizedBox(height: 12),
              // Miejsce na przyciski widoku (Dzień / Tydzień)
              Row(
                children: [
                  OutlinedButton(onPressed: () {}, child: const Text('Dzień')),
                  const SizedBox(width: 8),
                  OutlinedButton(onPressed: () {}, child: const Text('Tydzień')),
                  const Spacer(),
                  // Jeśli mamy informacje o grupie, pokaż przycisk pozwalający ustawić ją jako główną (zwraca potwierdzenie)
                  if (groupCode != null)
                    ElevatedButton(
                      onPressed: () {
                        // Nie zapisujemy prefs tutaj — zwracamy wynik do poprzedniego ekranu,
                        // który może zapisać preferencję jeśli użytkownik tego chce.
                        Navigator.pop(context, {'apply': true, 'id': groupId, 'code': groupCode});
                      },
                      child: const Text('Ustaw jako moja grupa'),
                    ),
                ],
              ),
              const SizedBox(height: 24),
              // Placeholder content
              Expanded(
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: const [
                      Icon(Icons.calendar_month, size: 64, color: Color(0xFF9E9E9E)),
                      SizedBox(height: 12),
                      Text('Widok planu grupy jest w przygotowaniu', style: TextStyle(color: Color(0xFF6E6E6E))),
                      SizedBox(height: 8),
                      Text('Implementacja: osobny branch / task', style: TextStyle(color: Color(0xFFBDBDBD))),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
