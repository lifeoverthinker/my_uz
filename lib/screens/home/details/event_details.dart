// event_details.dart
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/theme/text_style.dart';

abstract class EventDetailsSheet {
  static Future<void> open(BuildContext context, EventModel event) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (_) => _EventDetailsDraggable(eventModel: event),
    );
  }
}

const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
const double _kTopRadius = 24;
const double _kHitArea = 48;
const double _kCircleSmall = 32;
const double _kCircleLarge = 40;
const double _kIconToTextGap = 12;

class _EventDetailsDraggable extends StatelessWidget {
  final EventModel eventModel;
  const _EventDetailsDraggable({required this.eventModel});

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _EventSheetScaffold(
          scrollController: scrollController,
          child: _EventDetailsContent(eventModel: eventModel),
        );
      },
    );
  }
}

class _EventSheetScaffold extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  const _EventSheetScaffold({required this.child, required this.scrollController});

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    const handleTopGap = 8.0;
    const handleToXGap = 8.0;
    const xToHeaderGap = 12.0;
    return Container(
      margin: EdgeInsets.zero,
      padding: EdgeInsets.only(top: topPadding + handleTopGap),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(_kTopRadius)),
        boxShadow: [
          BoxShadow(color: Color(0x4C000000), blurRadius: 3, offset: Offset(0, 1)),
          BoxShadow(color: Color(0x26000000), blurRadius: 8, offset: Offset(0, 4), spreadRadius: 3),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(horizontal, 0, horizontal, 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const _Grip(),
            const SizedBox(height: handleToXGap),
            Row(
              children: [
                _AdaptiveIconSlot(
                  iconSize: 24,
                  semanticsLabel: 'Zamknij szczegóły wydarzenia',
                  isButton: true,
                  onTap: () => Navigator.of(context).maybePop(),
                  child: const Icon(MyUz.x_close, size: 24, color: Color(0xFF1D192B)),
                ),
                const Spacer(),
              ],
            ),
            const SizedBox(height: xToHeaderGap),
            Expanded(
              child: SingleChildScrollView(
                controller: scrollController,
                child: child,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _EventDetailsContent extends StatelessWidget {
  final EventModel eventModel;
  const _EventDetailsContent({required this.eventModel});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    const headerBottomGap = 28.0;
    const rowVerticalGap = 12.0;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _AdaptiveIconSlot(
              iconSize: 16,
              child: _TypeColorMarker(
                color: eventModel.backgroundColor ?? const Color(0xFFDAF5D7), // taki sam jak w EventCard
              ),
            ),
            const SizedBox(width: _kIconToTextGap),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    eventModel.title,
                    style: AppTextStyle.myUZTitleLarge.copyWith(
                      fontWeight: FontWeight.w500,
                      color: const Color(0xFF1D192B),
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    _dateLine(eventModel.date, eventModel.time),
                    style: AppTextStyle.myUZBodyMedium.copyWith(
                      color: cs.onSurfaceVariant,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: headerBottomGap),
        _DetailRow(icon: MyUz.marker_pin_04, label: eventModel.location.isNotEmpty ? eventModel.location : 'Lokalizacja -'),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(icon: MyUz.menu_03, label: eventModel.description.isNotEmpty ? eventModel.description : 'Opis -'),
        const SizedBox(height: rowVerticalGap),
        _DetailRow(
          icon: MyUz.trophy_01, // ZAMIANA z MyUz.ticket na istniejącą ikonę
          label: eventModel.freeEntry ? 'Wstęp wolny' : 'Wstęp płatny',
        ),
      ],
    );
  }

  static String _dateLine(String date, String time) {
    return '$date • $time';
  }
}

class _Grip extends StatelessWidget {
  const _Grip();
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 40,
      height: 4,
      decoration: BoxDecoration(
        color: Colors.black26,
        borderRadius: BorderRadius.circular(2),
      ),
    );
  }
}

class _AdaptiveIconSlot extends StatelessWidget {
  final double iconSize;
  final Widget child;
  final VoidCallback? onTap;
  final String? semanticsLabel;
  final bool isButton;
  const _AdaptiveIconSlot({
    required this.iconSize,
    required this.child,
    this.onTap,
    this.semanticsLabel,
    this.isButton = false,
  });

  @override
  Widget build(BuildContext context) {
    final double circle = iconSize >= 24 ? _kCircleLarge : _kCircleSmall;
    Widget inner = SizedBox(
      width: _kHitArea,
      height: _kHitArea,
      child: Center(
        child: Container(
          width: circle,
          height: circle,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(circle / 2),
          ),
          alignment: Alignment.center,
          child: SizedBox(
            width: iconSize,
            height: iconSize,
            child: FittedBox(fit: BoxFit.contain, child: child),
          ),
        ),
      ),
    );

    if (onTap != null) {
      inner = Material(
        type: MaterialType.transparency,
        child: InkWell(
          borderRadius: BorderRadius.circular(circle / 2),
          splashColor: Colors.black12,
          onTap: onTap,
          child: inner,
        ),
      );
    }
    if (semanticsLabel != null) {
      inner = Semantics(
        button: isButton,
        label: semanticsLabel,
        child: inner,
      );
    }
    return inner;
  }
}

class _TypeColorMarker extends StatelessWidget {
  final Color color;
  const _TypeColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 16,
      height: 16,
      decoration: ShapeDecoration(
        color: color,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
    );
  }
}

/// Wiersz szczegółu (ikona + tekst)
class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  const _DetailRow({required this.icon, required this.label});
  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        _AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: cs.onSurface),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Text(
            label,
            style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface),
          ),
        ),
      ],
    );
  }
}
