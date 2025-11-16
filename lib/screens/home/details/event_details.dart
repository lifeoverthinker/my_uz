// Plik: lib/screens/home/details/event_details.dart
import 'package:flutter/material.dart';
import 'package:my_uz/models/event_model.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/utils/constants.dart'; // Import dla kIconToTextGap
import 'package:my_uz/theme/app_colors.dart'; // Dodany dla kolorów markera

/// EventDetails — teraz używa DraggableScrollableSheet dla spójnego wyglądu.
abstract class EventDetailsSheet {
  static Future<void> open(BuildContext context, EventModel event) {
    // --- POPRAWKA: Zmieniono showAsModal na showModalBottomSheet ---
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (_) => _EventDetailsDraggable(eventModel: event),
    );
    // --- KONIEC POPRAWKI ---
  }
}

// --- POCZĄTEK DODANEGO KODU (WZOROWANE NA ClassDetails) ---
const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;

class _EventDetailsDraggable extends StatelessWidget {
  final EventModel eventModel;
  const _EventDetailsDraggable({required this.eventModel});

  @override
  Widget build(BuildContext context) {
    // Używamy DraggableScrollableSheet dla spójnego feelingu
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _SheetScaffoldBody(
          scrollController: scrollController,
          child: _EventDetailsContent(eventModel: eventModel),
        );
      },
    );
  }
}

/// Skopiowane z ClassDetails dla spójnego wyglądu (GripHandle, X, Padding)
class _SheetScaffoldBody extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  const _SheetScaffoldBody({required this.child, required this.scrollController});

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
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
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
            const GripHandle(), // Uchwyt do przeciągania
            const SizedBox(height: handleToXGap),
            Row(
              children: [
                AdaptiveIconSlot(
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
// --- KONIEC DODANEGO KODU ---

// --- DODANY WIDŻET MARKERA ---
class _EventColorMarker extends StatelessWidget {
  final Color color;
  const _EventColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 20, // Rozmiar 20x20 jak w ClassDetails
      height: 20, // Rozmiar 20x20
      decoration: ShapeDecoration(
        color: color,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
    );
  }
}
// --- KONIEC DODANEGO WIDŻETU ---

class _EventDetailsContent extends StatelessWidget {
  final EventModel eventModel;
  const _EventDetailsContent({required this.eventModel});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    const headerBottomGap = 28.0; // Ujednolicona wartość na 28.0
    const rowGap = 12.0;

    final markerColor = eventModel.colorVariant == 1 ? const Color(0xFFE2F6DE) : const Color(0xFFDAF4D6);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // --- UJEDNOLICONY NAGŁÓWEK Z MARKEREM (jak w ClassDetails) ---
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            AdaptiveIconSlot(
              iconSize: 20,
              child: _EventColorMarker(color: markerColor),
            ),
            const SizedBox(width: kIconToTextGap), // 12.0
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    eventModel.title,
                    style: AppTextStyle.myUZTitleLarge.copyWith(
                      fontWeight: FontWeight.w500, // w500 jak w ClassDetails
                      color: const Color(0xFF1D192B),
                    ),
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6), // Spacja 6px
                  Text(
                    _dateLine(eventModel.date, eventModel.time),
                    style: AppTextStyle.myUZBodyMedium.copyWith(color: cs.onSurfaceVariant),
                  ),
                ],
              ),
            ),
          ],
        ),
        // --- KONIEC UJEDNOLICONEGO NAGŁÓWKA ---

        const SizedBox(height: headerBottomGap), // Spacja 28.0

        // --- POPRAWKA: Usunięto etykiety 'label' ---
        _DetailRow(icon: MyUz.marker_pin_04, value: eventModel.location.isNotEmpty ? eventModel.location : 'Lokalizacja -'),
        const SizedBox(height: rowGap), // 12.0
        _DetailRow(icon: MyUz.menu_03, value: eventModel.description.isNotEmpty ? eventModel.description : 'Brak opisu'),
        const SizedBox(height: rowGap), // 12.0
        _DetailRow(icon: MyUz.trophy_01, value: eventModel.freeEntry ? 'Wstęp wolny' : 'Wstęp płatny'),
        // --- KONIEC POPRAWKI ---
        const SizedBox(height: 20),
      ],
    );
  }

  static String _dateLine(String date, String time) => '$date • $time';
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  // USUNIĘTO: final String label;
  final String value;
  // ZMODYFIKOWANO KONSTRUKTOR
  const _DetailRow({required this.icon, required this.value});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: const Color(0xFF1D192B)), // Ciemny kolor ikony, rozmiar 20
        ),
        const SizedBox(width: kIconToTextGap), // Wcięcia 12px
        // POPRAWKA: Wyświetlamy tylko wartość (value)
        Expanded(
          child: Text(
            value,
            style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface), // Utrzymujemy styl głównego tekstu
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}