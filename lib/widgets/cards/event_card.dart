// Plik: lib/widgets/cards/event_card.dart
import 'package:flutter/material.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/screens/home/details/event_details.dart';
import 'package:my_uz/models/event_model.dart';

/// EventCard – karta “Wydarzenia”
class EventCard extends StatelessWidget {
  // POPRAWKA: Ustawienie wysokości na 68px dla spójności z TaskCard/ClassCard
  static const double _kHeightEvent = 68;

  final String title;
  final String description;
  final Color? backgroundColor;
  final bool hugHeight;
  final EventModel eventModel;

  const EventCard({
    super.key,
    required this.title,
    required this.description,
    required this.eventModel,
    this.backgroundColor,
    this.hugHeight = false,
  });

  @override
  Widget build(BuildContext context) {
    final scale = MediaQuery.of(context).textScaleFactor;
    // W poziomej liście 'adaptive' jest false, więc opis będzie na 1 linii
    final adaptive = hugHeight || scale > 1.0;

    // Ustawienie maxLines: 1 dla stałej wysokości (list view)
    final descriptionMaxLines = adaptive ? 3 : 1;
    final titleMaxLines = adaptive ? 2 : 1; // Zmienione z 1 na 2 dla elastyczności, ale w liście i tak będzie 1 linia

    final inner = Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? const Color(0xFFDAF5D7),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center, // Wyśrodkowanie
        children: [
          Text(
            title,
            maxLines: titleMaxLines,
            overflow: TextOverflow.ellipsis,
            style: AppTextStyle.myUZLabelLarge.copyWith(
              color: const Color(0xFF1D192B),
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 4), // Zmniejszony odstęp (było 8), aby zmieścić się w 68px
          Text(
            description,
            maxLines: descriptionMaxLines,
            overflow: TextOverflow.ellipsis,
            style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F)),
          ),
        ],
      ),
    );

    // Gdy hugHeight jest false (w liście poziomej), wymuszamy minimalną wysokość 68px.
    if (!adaptive) {
      return GestureDetector(
        onTap: () => EventDetailsSheet.open(context, eventModel),
        child: ConstrainedBox(
          constraints: const BoxConstraints(minHeight: _kHeightEvent), // Używa teraz 68px
          child: inner,
        ),
      );
    }

    // W pozostałych przypadkach (np. tekst jest za duży lub hugHeight=true), pozwalamy mu być elastycznym.
    return GestureDetector(
      onTap: () => EventDetailsSheet.open(context, eventModel),
      child: inner,
    );
  }
}