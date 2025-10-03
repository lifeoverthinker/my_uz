import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/screens/home/details/event_details.dart';

/// Sekcja: Wydarzenia
/// Card height (Figma): 84
/// Slider height = 84
class EventsSection extends StatelessWidget {
  final List<EventModel> events;
  final ValueChanged<EventModel> onTap;

  const EventsSection({
    super.key,
    required this.events,
    required this.onTap,
  });

  static const double _kCardWidth = 264;
  static const double _kListHeight = 84; // = EventCard

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
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
            child: ListView.builder(
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
                    child: InkWell(
                      borderRadius: BorderRadius.circular(8),
                      splashColor: Colors.transparent,
                      highlightColor: Colors.transparent,
                      onTap: () {
                        EventDetailsSheet.open(context, e);
                        // Możesz też wywołać onTap(e) jeśli potrzebujesz
                        // onTap(e);
                      },
                      child: EventCard(
                        title: e.title,
                        description: e.description,
                        backgroundColor: bg,
                        eventModel: e,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
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
