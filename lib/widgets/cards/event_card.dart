import 'package:flutter/material.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/screens/home/details/event_details.dart';
import 'package:my_uz/models/event_model.dart';

/// EventCard – karta “Wydarzenia”
class EventCard extends StatelessWidget {
  static const double _kHeightEvent = 84;

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
    return LayoutBuilder(builder: (context, constraints) {
      final availH = constraints.maxHeight.isFinite ? constraints.maxHeight : double.infinity;
      // adaptive values
      final bool compact = availH.isFinite && availH < 64;
      final double vPad = compact ? 6 : 8;
      final double gap = compact ? 6 : 8;
      final int titleLines = compact ? 1 : (hugHeight ? 2 : 1);
      final int descLines = compact ? 2 : (hugHeight ? 3 : 2);

      final inner = Container(
        padding: EdgeInsets.symmetric(vertical: vPad, horizontal: 12),
        decoration: ShapeDecoration(
          color: backgroundColor ?? const Color(0xFFDAF5D7),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Teksty (jak TaskCard) – bez avatara
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Flexible(
                    fit: FlexFit.loose,
                    child: Text(
                      title,
                      maxLines: titleLines,
                      overflow: TextOverflow.ellipsis,
                      style: AppTextStyle.myUZLabelLarge.copyWith(
                        color: const Color(0xFF1D192B),
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  SizedBox(height: gap),
                  Flexible(
                    fit: FlexFit.loose,
                    child: Text(
                      description,
                      maxLines: descLines,
                      overflow: TextOverflow.ellipsis,
                      style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F)),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 0),
          ],
        ),
      );

      return GestureDetector(
        onTap: () => EventDetailsSheet.open(context, eventModel),
        child: inner,
      );
    });
  }
}
