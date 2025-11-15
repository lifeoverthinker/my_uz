import 'package:flutter/material.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';

/// EventDetails — teraz używa reużywalnego SheetScaffold dla spójnego wyglądu.
abstract class EventDetailsSheet {
  static Future<void> open(BuildContext context, EventModel event) {
    return SheetScaffold.showAsModal<void>(
      context,
      child: _EventDetailsContent(eventModel: event),
      barrierColor: Colors.black54,
    );
  }
}

class _EventDetailsContent extends StatelessWidget {
  final EventModel eventModel;
  const _EventDetailsContent({required this.eventModel});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    const headerBottomGap = 20.0;
    const rowGap = 12.0;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            AdaptiveIconSlot(
              child: const Icon(MyUz.x_close, size: 20),
              onTap: () => Navigator.of(context).maybePop(),
              semanticsLabel: 'Zamknij szczegóły wydarzenia',
              isButton: true,
              iconSize: 20,
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                eventModel.title,
                style: AppTextStyle.myUZTitleLarge.copyWith(fontWeight: FontWeight.w600),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            const SizedBox(width: 4),
          ],
        ),
        const SizedBox(height: 12),

        Text(
          _dateLine(eventModel.date, eventModel.time),
          style: AppTextStyle.myUZBodyMedium.copyWith(color: cs.onSurfaceVariant),
        ),
        const SizedBox(height: headerBottomGap),

        _DetailRow(icon: MyUz.marker_pin_04, label: 'Lokalizacja', value: eventModel.location.isNotEmpty ? eventModel.location : 'Lokalizacja -'),
        const SizedBox(height: rowGap),
        _DetailRow(icon: MyUz.menu_03, label: 'Opis', value: eventModel.description.isNotEmpty ? eventModel.description : 'Brak opisu'),
        const SizedBox(height: rowGap),
        _DetailRow(icon: MyUz.trophy_01, label: 'Wstęp', value: eventModel.freeEntry ? 'Wstęp wolny' : 'Wstęp płatny'),
        const SizedBox(height: 20),
      ],
    );
  }

  static String _dateLine(String date, String time) => '$date • $time';
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _DetailRow({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        AdaptiveIconSlot(child: Icon(icon, size: 20), iconSize: 20),
        const SizedBox(width: 12),
        Expanded(
          child: Align(
            alignment: Alignment.centerLeft,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onSurfaceVariant)),
                const SizedBox(height: 6),
                Text(value, style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface)),
              ],
            ),
          ),
        ),
      ],
    );
  }
}