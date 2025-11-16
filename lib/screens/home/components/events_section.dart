// Plik: lib/screens/home/components/events_section.dart
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/screens/home/details/event_details.dart';
// USUNIĘTO: import 'package:my_uz/theme/app_colors.dart'; // Usunięty nieużywany import

/// Sekcja: Wydarzenia — ujednolicona z ClassSection
class EventsSection extends StatelessWidget {
  final List<EventModel> events;
  final ValueChanged<EventModel> onTap;
  final VoidCallback? onGoToEvents;

  const EventsSection({
    super.key,
    required this.events,
    required this.onTap,
    this.onGoToEvents,
  });

  static const double _kCardWidth = 264;
  static const double _kListHeight = 68; // Wysokość ujednolicona

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        // Ujednolicony nagłówek (Icon, Title, Spacer, Action)
        Row(children: [
          Icon(MyUz.marker_pin_04, size: 20, color: cs.onSurface),
          const SizedBox(width: 8),
          Text('Wydarzenia', style: AppTextStyle.myUZTitleMedium.copyWith(fontSize: 18, height: 1.33, fontWeight: FontWeight.w500, color: cs.onSurface)),
          const Spacer(),
          if (onGoToEvents != null)
            TextButton(
              onPressed: onGoToEvents,
              child: const Text('Więcej'),
            )
        ]),
        const SizedBox(height: 12),
        SizedBox(
          height: _kListHeight, // Używa teraz 68
          child: events.isEmpty ? _buildEmpty(cs) : _buildList(context),
        ),
      ]),
    );
  }

  Widget _buildEmpty(ColorScheme cs) {
    return Align(
      alignment: Alignment.centerLeft,
      child: Text('Brak nadchodzących wydarzeń', style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant)),
    );
  }

  Widget _buildList(BuildContext context) {
    return ListView.builder(
      padding: EdgeInsets.zero,
      scrollDirection: Axis.horizontal,
      physics: const ClampingScrollPhysics(),
      itemCount: events.length + 1,
      itemBuilder: (context, i) {
        if (i == events.length) return const SizedBox(width: 16);
        final e = events[i];
        final bg = _bg(e.colorVariant);
        return Padding(
          padding: EdgeInsets.only(right: i == events.length - 1 ? 0 : 8),
          child: SizedBox(
            width: _kCardWidth,
            child: InkWell(
              borderRadius: BorderRadius.circular(8),
              splashColor: Colors.transparent,
              highlightColor: Colors.transparent,
              onTap: () {
                EventDetailsSheet.open(context, e);
                onTap(e);
              },
              child: EventCard(
                title: e.title,
                description: e.description,
                eventModel: e,
                backgroundColor: bg,
                hugHeight: false,
              ),
            ),
          ),
        );
      },
    );
  }

  Color _bg(int v) {
    switch (v % 2) {
      case 1:
        return const Color(0xFFE2F6DE);
      case 0:
      default:
        return const Color(0xFFDAF4D6);
    }
  }
}