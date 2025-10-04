import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/screens/home/details/event_details.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/event_card.dart';

/// Sekcja: Wydarzenia
/// Card height (Figma): 84
/// Slider height = 84
class EventsSection extends StatelessWidget {
  final List<EventModel>? events;
  final void Function(EventModel)? onTap;
  const EventsSection({super.key, this.events, this.onTap});

  static const double _kCardWidth = 264;
  static const double _kListHeight = 84; // = EventCard

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final list = events ?? _mockEvents(cs);
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 0, 0, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _SectionHeader(
            icon: MyUz.users_01,
            label: 'Wydarzenia',
            color: cs.onSurface,
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: _kListHeight,
            child: list.isEmpty
                ? const Center(child: Text('Brak wydarzeń'))
                : _buildListFrom(context, list),
          ),
        ],
      ),
    );
  }

  List<EventModel> _mockEvents(ColorScheme cs) {
    return [
      EventModel(
        id: 'e1',
        title: 'Dzień Otwarty UZ',
        description: 'Wstęp wolny, prezentacje kierunków.',
        date: 'Sobota, 12 paź 2025',
        time: '10:00–14:00',
        location: 'Campus A, Aula Główna',
        freeEntry: true,
        backgroundColor: const Color(0xFFDAF5D7),
        colorVariant: 0,
      ),
      EventModel(
        id: 'e2',
        title: 'Hackathon UZ',
        description: '48h kodowania, nagrody dla zwycięzców.',
        date: 'Piątek, 24 paź 2025',
        time: '18:00–18:00',
        location: 'Biblioteka, sala 101',
        freeEntry: false,
        backgroundColor: const Color(0xFFE2F6DE),
        colorVariant: 1,
      ),
      EventModel(
        id: 'e3',
        title: 'Spotkanie koła naukowego AI',
        description: 'Nowości w ML i demo projektów.',
        date: 'Środa, 29 paź 2025',
        time: '17:00–19:00',
        location: 'B-3, s. 12',
        freeEntry: true,
        backgroundColor: const Color(0xFFDAF5D7),
        colorVariant: 0,
      ),
    ];
  }

  Widget _buildListFrom(BuildContext context, List<EventModel> events) {
    return ListView.builder(
      padding: EdgeInsets.zero,
      scrollDirection: Axis.horizontal,
      physics: const ClampingScrollPhysics(),
      itemCount: events.length + 1,
      itemBuilder: (context, i) {
        if (i == events.length) return const SizedBox(width: 16); // trailing padding
        final e = events[i];
        final bg = _bg(e.colorVariant);
        return Padding(
          padding: EdgeInsets.only(right: i == events.length - 1 ? 0 : 8),
          child: SizedBox(
            width: _kCardWidth,
            child: EventCard(
              title: e.title,
              description: e.description,
              backgroundColor: bg,
              eventModel: e,
            ),
          ),
        );
      },
    );
  }

  Color _bg(int v) {
    // Kolory z makiety (#DAF4D6 bazowy, #E2F6DE jaśniejszy)
    switch (v % 2) {
      case 1:
        return const Color(0xFFE2F6DE);
      case 0:
      default:
        return const Color(0xFFDAF4D6);
    }
  }
}

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  const _SectionHeader({required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 20, color: color),
        const SizedBox(width: 8),
        Text(
          label,
          style: AppTextStyle.myUZTitleMedium.copyWith(
            fontSize: 18,
            height: 1.33,
            fontWeight: FontWeight.w500,
            color: color,
          ),
        ),
      ],
    );
  }
}
