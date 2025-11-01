import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/screens/home/details/event_details.dart';

/// Sekcja: Wydarzenia — ujednolicona z ClassSection
/// - padding 16
/// - card width 264
/// - list height 84 (kompakt)
class EventsSection extends StatelessWidget {
  final List<EventModel> events;
  final ValueChanged<EventModel> onTap;

  const EventsSection({super.key, required this.events, required this.onTap});

  static const double _kCardWidth = 264;
  static const double _kListHeight = 84;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        _SectionHeader(icon: MyUz.marker_pin_04, label: 'Wydarzenia', color: cs.onSurface),
        const SizedBox(height: 12),
        SizedBox(
          height: _kListHeight,
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
                hugHeight: true, // zachowanie jak ClassSection (dopasowanie do list height)
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

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  const _SectionHeader({required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Icon(icon, size: 20, color: color),
      const SizedBox(width: 8),
      Text(label, style: AppTextStyle.myUZTitleMedium.copyWith(fontSize: 18, height: 1.33, fontWeight: FontWeight.w500, color: color)),
    ]);
  }
}