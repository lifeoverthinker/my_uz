// event_model.dart
import 'package:flutter/material.dart';

class EventModel {
  final String id;              // Dodane pole id
  final String title;           // Tytuł wydarzenia
  final String description;     // Krótki opis lub szczegóły
  final String date;            // Data, np. "Piątek, 4 wrz 2025"
  final String time;            // Godzina, np. "18:00 - 19:00"
  final String location;        // Lokalizacja (szczegóły sali, adres)
  final bool freeEntry;         // Czy wydarzenie jest darmowe
  final Color? backgroundColor; // Tło karty, opcjonalnie
  final int colorVariant;       // Dodane pole

  EventModel({
    required this.id,           // Dodane pole id
    required this.title,
    required this.description,
    required this.date,
    required this.time,
    required this.location,
    required this.freeEntry,
    this.backgroundColor,
    this.colorVariant = 0,      // Domyślna wartość
  });
}
